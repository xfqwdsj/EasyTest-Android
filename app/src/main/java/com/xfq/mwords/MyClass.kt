package com.xfq.mwords

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat

object MyClass {
    val INSERT_TOP: Int = 0
    val INSERT_BOTTOM: Int = 1
    val INSERT_LEFT: Int = 2
    val INSERT_RIGHT: Int = 3
    private var context: Context? = null

    fun getResString(id: Int): String {
        return context!!.resources!!.getString(id)
    }

    fun getResStringArray(id: Int): Array<String> {
        return context!!.resources!!.getStringArray(id)
    }

    fun getResColor(id: Int): Int {
        return ContextCompat.getColor(context!!, id)
    }

    fun setInsert(type: Int, view: View) {
        when (type) {
            INSERT_TOP -> {
                view.setOnApplyWindowInsetsListener { myView, windowInsets ->
                    myView.setPadding(myView.paddingLeft, windowInsets.systemWindowInsetTop, myView.paddingRight, myView.paddingBottom)
                    windowInsets
                }
            }
            INSERT_BOTTOM -> {
                view.setOnApplyWindowInsetsListener { myView, windowInsets ->
                    myView.setPadding(myView.paddingLeft, myView.paddingTop, myView.paddingRight, windowInsets.systemWindowInsetBottom)
                    windowInsets
                }
            }
            INSERT_LEFT -> {
                view.setOnApplyWindowInsetsListener { myView, windowInsets ->
                    myView.setPadding(windowInsets.systemWindowInsetLeft, myView.paddingTop, myView.paddingRight, myView.paddingBottom)
                    windowInsets
                }
            }
            INSERT_RIGHT -> {
                view.setOnApplyWindowInsetsListener { myView, windowInsets ->
                    myView.setPadding(myView.paddingLeft, myView.paddingTop, windowInsets.systemWindowInsetRight, myView.paddingBottom)
                    windowInsets
                }
            }
        }
    }

    fun dip2Px(dpValue: Float): Int {
        val scale = context!!.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun px2Dip(pxValue: Float): Int {
        val scale = context!!.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    fun init(context: Context) {
        this.context = context
    }
}