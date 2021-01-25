package com.xfq.easytest

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

object MyClass {
    const val INSET_TOP: Int = 0
    const val INSET_BOTTOM: Int = 1
    const val INSET_LEFT: Int = 2
    const val INSET_RIGHT: Int = 3
    const val RESULT_TYPE_UPLOADED = 0
    const val RESULT_TYPE_NO_UPLOADED_NO_SAVE = 1
    const val RESULT_TYPE_NO_UPLOADED_SAVED = 2
    const val RESULT_TYPE_LOCAL_UPLOADED = 3
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

    fun setInset(type: Int, view: View) {
        when (type) {
            INSET_TOP -> {
                ViewCompat.setOnApplyWindowInsetsListener(view) { myView, windowInsets ->
                    myView.setPadding(myView.paddingLeft, windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top, myView.paddingRight, myView.paddingBottom)
                    windowInsets
                }
            }
            INSET_BOTTOM -> {
                ViewCompat.setOnApplyWindowInsetsListener(view) { myView, windowInsets ->
                    myView.setPadding(myView.paddingLeft, myView.paddingTop, myView.paddingRight, windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).bottom)
                    windowInsets
                }
            }
            INSET_LEFT -> {
                ViewCompat.setOnApplyWindowInsetsListener(view) { myView, windowInsets ->
                    myView.setPadding(windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).left, myView.paddingTop, myView.paddingRight, myView.paddingBottom)
                    windowInsets
                }
            }
            INSET_RIGHT -> {
                ViewCompat.setOnApplyWindowInsetsListener(view) { myView, windowInsets ->
                    myView.setPadding(myView.paddingLeft, myView.paddingTop, windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).right, myView.paddingBottom)
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