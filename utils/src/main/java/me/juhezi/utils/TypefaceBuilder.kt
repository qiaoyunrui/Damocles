package me.juhezi.utils

import android.content.res.AssetManager
import android.graphics.Typeface
import android.text.TextUtils

/**
 * Typeface Builder
 * Created by Juhezi[juhezix@163.com] on 2017/7/17.
 */
class Wrapper(var path: String = "",
              var assetManager: AssetManager? = null) {
    fun build(): Typeface? {
        if (!TextUtils.isEmpty(path) && assetManager != null) {
            return Typeface.createFromAsset(assetManager, path)
        }
        return null
    }
}

fun buildTypeface(buildAction: Wrapper.() -> Unit) =
        Wrapper().apply(buildAction).build()

