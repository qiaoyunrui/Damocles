package me.juhezi.utils.view

import android.animation.Animator
import android.animation.ValueAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.OrientationHelper
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

/**
 * RecyclerView 扩展类
 * 主要作用是 Recycler 改造成 View Pager
 * Created by Juhezi[juhezix@163.com] on 2017/7/9.
 */
private const val TAG = "RecyclerViewExtension"

class Property {

    private val DEFAULT_SCALE_PERCENT = 0.9f  //默认的缩小百分比

    private val DEFAULT_SCROLL_SPEED_LIMIT = 5    //滑动速度限制

    var scalePercent: Float = DEFAULT_SCALE_PERCENT
        set(value) {
            field = if (value > 0 && value <= 1) value
            else DEFAULT_SCALE_PERCENT
            centerScalePercent = (1 + field) / 2
        }

    var centerScalePercent: Float = (1 + scalePercent) / 2

    var scrollSpeedLimit: Int = DEFAULT_SCROLL_SPEED_LIMIT
        set(value) {
            field = if (value > 0) value else DEFAULT_SCROLL_SPEED_LIMIT
        }

    var orientation = OrientationHelper.HORIZONTAL  //滑动方向

    var anchor: Int = -1
    var position: Int = -1

    var isFirst = true

    var isResetting = false
    var isResetted = false

    var animDuration: Long = 200

    var action: ((Int) -> Unit)? = null    //监听事件

    var layoutManager: LinearLayoutManager? = null
    override fun toString(): String {
        return "Property(anchor=$anchor, position=$position)"
    }
}

fun RecyclerView.transform(property: Property) = with(property) {
    if (getLayoutManager() == null &&
            getLayoutManager() !is LinearLayoutManager)  //必须是线性布局
        return
    layoutManager = getLayoutManager() as LinearLayoutManager
    orientation = layoutManager!!.orientation   //获取滑动的方向
    addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
        if (isFirst) {
            getAnchor(property)
            getCenterItemPosition(property)
            resetItemPosition(property, false)
            isFirst = false
        }
    }
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE &&
                    !isResetted &&
                    !isResetting) {
                resetItemPosition(property, true)
            }
        }

        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            isResetted = false
            getAnchor(property)
            getCenterItemPosition(property)
            val max = layoutManager!!
                    .findViewCoordinateByPosition(position, {
                        width
                    }, {
                        height
                    }) / 2
            val distance = layoutManager!!
                    .findViewCoordinateByPosition(position) - anchor
            val absDistance = Math.abs(distance)
            val p = (max - absDistance) / max.toFloat()
            val currentPercent = p * (1 - centerScalePercent) + centerScalePercent
            val otherPercent = (centerScalePercent - scalePercent) * (1 - p) + scalePercent
            layoutManager!!.findViewByPosition(position).noName(orientation == OrientationHelper.VERTICAL,
                    { scaleY = currentPercent },
                    { scaleX = currentPercent })
            layoutManager!!.findViewByPosition(position - 1).noName(orientation == OrientationHelper.VERTICAL,
                    { scaleY = otherPercent },
                    { scaleX = otherPercent })
            layoutManager!!.findViewByPosition(position + 1).noName(orientation == OrientationHelper.VERTICAL,
                    { scaleY = otherPercent },
                    { scaleX = otherPercent })
            if (Math.abs(if (orientation == OrientationHelper.HORIZONTAL) dx else dy) <= scrollSpeedLimit &&
                    scrollState == RecyclerView.SCROLL_STATE_SETTLING &&
                    !isResetted &&
                    !isResetting) {
                resetItemPosition(property, true)
            }
            action?.invoke(property.position)   //执行监听事件
        }

    })
}

/**
 * 获取锚点
 */
private fun RecyclerView.getAnchor(property: Property) = with(property) {
    var parentSize = if (orientation == OrientationHelper.HORIZONTAL) measuredWidth
    else measuredHeight
    if (childCount <= 0) {
        anchor = -1
        return
    }
    var position = layoutManager!!.findFirstVisibleItemPosition()
    var itemSize = if (orientation == OrientationHelper.HORIZONTAL)
        layoutManager!!.findViewByPosition(position).measuredWidth
    else layoutManager!!.findViewByPosition(position).measuredHeight
    anchor = (parentSize - itemSize) / 2
}

private fun getCenterItemPosition(property: Property) = with(property) {
    if (anchor < 0) return
    var startPosition = layoutManager!!.findFirstVisibleItemPosition()
    var endPosition = layoutManager!!.findLastVisibleItemPosition()
    if (startPosition == 0) startPosition++
    if (endPosition == layoutManager!!.itemCount - 1) endPosition--
    var distance = Int.MAX_VALUE
    for (i in startPosition..endPosition) {
        val tempCoordinate = layoutManager!!.findViewCoordinateByPosition(i)
        val tempDistance = Math.abs(tempCoordinate - anchor)
        if (tempDistance < distance) {
            distance = tempDistance
            position = i
        }
    }
}

fun RecyclerView.resetItemPosition(property: Property, hasAnim: Boolean) = with(property) {
    if (isResetting) return
    if (position <= 0) return
    val coordinate = layoutManager!!.findViewCoordinateByPosition(position)
    val distance = (coordinate - anchor)
    if (hasAnim) {
        val valueAnimator = ValueAnimator.ofInt(0, distance)
        valueAnimator.interpolator = AccelerateDecelerateInterpolator()
        valueAnimator.duration = animDuration
        var lastDistance = 0
        valueAnimator.addUpdateListener {
            isResetting = true
            val tempDistance = (it.animatedValue as Int) - lastDistance
            if (orientation == OrientationHelper.HORIZONTAL) {
                scrollBy(tempDistance, 0)
            } else {
                scrollBy(0, tempDistance)
            }
            lastDistance = it.animatedValue as Int
        }
        valueAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                isResetting = false
                isResetted = true
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
//                    isResetting = true
            }

        })
        valueAnimator.start()
    } else {
        if (orientation == OrientationHelper.HORIZONTAL) {
            scrollBy(distance, 0)
        } else {
            scrollBy(0, distance)
        }
    }
}


fun LinearLayoutManager.findViewCoordinateByPosition(position: Int,
                                                     horizontalAction: (View.() -> Int) = { x.toInt() },
                                                     verticalAction: (View.() -> Int) = { y.toInt() }) =
        if (orientation == OrientationHelper.HORIZONTAL) findViewByPosition(position).horizontalAction()
        else findViewByPosition(position).verticalAction()

fun LinearLayoutManager.findNullableViewCoordinateByPosition(position: Int, horizontalAction: View?.() -> Unit,
                                                             verticalAction: View?.() -> Unit) {
    if (orientation == OrientationHelper.HORIZONTAL) findViewByPosition(position).horizontalAction()
    else findViewByPosition(position).verticalAction()
}

fun View.noName(vertical: Boolean, horizontalAction: View.() -> Unit, verticalAction: View.() -> Unit) {
    if (vertical) {
        verticalAction()
    } else {
        horizontalAction()
    }
}
