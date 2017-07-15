package me.juhezi.utils.view

import android.util.Log

/**
 * Log 扩展
 * Created by Juhezi[juhezix@163.com] on 2017/7/15.
 */

fun Any.li(message: String) = Log.i(javaClass.simpleName, message)

fun Any.ld(message: String) = Log.d(javaClass.simpleName, message)

fun Any.lv(message: String) = Log.v(javaClass.simpleName, message)

fun Any.lw(message: String) = Log.w(javaClass.simpleName, message)

fun Any.le(message: String) = Log.e(javaClass.simpleName, message)