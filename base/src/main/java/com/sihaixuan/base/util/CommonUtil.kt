package com.sihaixuan.base.util

import android.app.Activity
import android.view.View

/**
 *
 * 项目名称：ComponentDemo
 * 类描述：
 * 创建人：toney
 * 创建时间：2019/5/9 16:24
 * 邮箱：xiyangfeisa@foxmail.com
 * 备注：
 * @version   1.0
 *
 */
fun <V : View> Activity.bindView(id: Int): Lazy<V> = lazy {
    viewFinder(id) as V
}

private val Activity.viewFinder: Activity.(Int) -> View?
    get() = { findViewById(it) }