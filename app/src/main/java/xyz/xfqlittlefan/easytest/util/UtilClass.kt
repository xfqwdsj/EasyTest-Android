package xyz.xfqlittlefan.easytest.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

@SuppressLint("StaticFieldLeak")
object UtilClass {
    private lateinit var context: Context
    private val gson = Gson()

    const val CORRECTNESS = 0
    const val SCORE = 1

    const val QUESTION_SET_ID = 0
    const val QUESTION_BANK_ID = 1

    fun ViewGroup.MarginLayoutParams.setMarginTop(size: Int) {
        setMargins(leftMargin, size, rightMargin, bottomMargin)
    }

    fun ViewGroup.MarginLayoutParams.setMarginBottom(size: Int) {
        setMargins(leftMargin, topMargin, rightMargin, size)
    }

    fun getResString(id: Int): String {
        return context.resources!!.getString(id)
    }

    fun getResStringArray(id: Int): Array<String> {
        return context.resources!!.getStringArray(id)
    }

    fun getResColor(id: Int): Int {
        return ContextCompat.getColor(context, id)
    }

    fun dip2PxI(dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun px2DipI(pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    fun dip2PxF(dpValue: Float): Float {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f)
    }

    fun px2DipF(pxValue: Float): Float {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f)
    }

    fun getPreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun getGson(): Gson {
        return gson
    }

    fun init(context: Context) {
        this.context = context
    }

    fun RecyclerView.smoothScroll(position: Int, mode: Int = LinearSmoothScroller.SNAP_TO_START) {
        val smoothScroller = object : LinearSmoothScroller(this.context) {
            override fun getVerticalSnapPreference(): Int = mode
            override fun getHorizontalSnapPreference(): Int = mode
        }
        smoothScroller.targetPosition = position
        layoutManager?.startSmoothScroll(smoothScroller)
    }

    fun getQuestionStateMap(map: Map<Int, Map<Int, Map<Int, Float>>>): Map<Int, Map<Int, Float>> {
        val returnMap = mutableMapOf<Int, Map<Int, Float>>()
        map.forEach { (i, valueI) ->
            val questionMap = mutableMapOf(SCORE to 0F, CORRECTNESS to 2F)
            var count1 = 0
            var count2 = 0
            var count3 = 0
            var count4 = 0
            valueI.forEach { (_, valueJ) ->
                when (valueJ[CORRECTNESS]?.toInt()) {
                    1 -> count1++
                    2 -> count2++
                    3 -> count3++
                    4 -> count4++
                    else -> {
                        count1 = 114514
                        count2 = 114514
                        count3 = 114514
                        count4 = 114514
                    }
                }
                questionMap[SCORE] = questionMap[SCORE]?.plus(valueJ[SCORE] ?: 0F) ?: -114514F
            }
            if (!(count1 < 0 && count2 < 0 && count3 < 0 && count4 < 0)) {
                when {
                    count4 > 0 -> questionMap[CORRECTNESS] = 4F
                    count1 > 0 && count2 > 0 || count3 > 0 -> questionMap[CORRECTNESS] = 3F
                    count1 > 0 -> questionMap[CORRECTNESS] = 1F
                    else -> questionMap[CORRECTNESS] = 2F
                }
            }
            returnMap[i] = questionMap
        }
        return returnMap
    }
}