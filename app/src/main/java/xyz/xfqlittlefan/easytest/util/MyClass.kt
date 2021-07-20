package xyz.xfqlittlefan.easytest.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.MarginLayoutParamsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.preference.PreferenceManager

@SuppressLint("StaticFieldLeak")
object MyClass {
    private var context: Context? = null
    const val INSET_TOP: Int = 0
    const val INSET_BOTTOM: Int = 1
    const val INSET_LEFT: Int = 2
    const val INSET_RIGHT: Int = 3

    fun ViewGroup.MarginLayoutParams.setMarginTop(size: Int) {
        setMargins(leftMargin, size, rightMargin, bottomMargin)
    }

    fun ViewGroup.MarginLayoutParams.setMarginBottom(size: Int) {
        setMargins(leftMargin, topMargin, rightMargin, size)
    }

    fun getResString(id: Int): String {
        return context!!.resources!!.getString(id)
    }

    fun getResStringArray(id: Int): Array<String> {
        return context!!.resources!!.getStringArray(id)
    }

    fun getResColor(id: Int): Int {
        return ContextCompat.getColor(context!!, id)
    }

    /*
    fun View.setInset(type: Int) {
        when (type) {
            INSET_TOP -> {
                val padding = this.paddingTop
                ViewCompat.setOnApplyWindowInsetsListener(this) { myView, windowInsets ->
                    myView.updatePadding(top = padding + windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top)
                    windowInsets
                }
                this.requestApplyInsetsWhenAttached()
            }
            INSET_BOTTOM -> {
                val padding = this.paddingBottom
                ViewCompat.setOnApplyWindowInsetsListener(this) { myView, windowInsets ->
                    myView.updatePadding(
                        bottom = padding + windowInsets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                        ).bottom
                    )
                    windowInsets
                }
                this.requestApplyInsetsWhenAttached()
            }
        }
    }

    fun View.requestApplyInsetsWhenAttached() {
        if (isAttachedToWindow) {
            requestApplyInsets()
        } else {
            addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    v.removeOnAttachStateChangeListener(this)
                    v.requestApplyInsets()
                }

                override fun onViewDetachedFromWindow(v: View) = Unit
            })
        }
    }
     */

    fun dip2PxI(dpValue: Float): Int {
        val scale = context!!.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun px2DipI(pxValue: Float): Int {
        val scale = context!!.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    fun dip2PxF(dpValue: Float): Float {
        val scale = context!!.resources.displayMetrics.density
        return (dpValue * scale + 0.5f)
    }

    fun px2DipF(pxValue: Float): Float {
        val scale = context!!.resources.displayMetrics.density
        return (pxValue / scale + 0.5f)
    }

    fun getPreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun init(context: Context) {
        MyClass.context = context
    }
}