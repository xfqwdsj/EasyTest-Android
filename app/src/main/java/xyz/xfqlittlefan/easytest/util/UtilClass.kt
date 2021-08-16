package xyz.xfqlittlefan.easytest.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.view.ViewGroup
import androidx.compose.material.CheckboxColors
import androidx.compose.material.CheckboxDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.activity.ui.theme.*
import xyz.xfqlittlefan.easytest.data.Question
import java.lang.reflect.Type
import kotlin.math.min

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

    fun getQuestionScore(question: Question): Float {
        var questionScore = 0F
        when (question.type) {
            1 -> {
                when (question.scoreType) {
                    1 -> {
                        if (question.maxSelecting == null) {
                            question.options.forEach {
                                if (it.score > questionScore) questionScore = it.score
                            }
                        } else {
                            val list = question.options.sortedByDescending { it.score }
                            for (i in 0 until min(question.maxSelecting, list.size)) {
                                questionScore += list[i].score
                            }

                        }
                    }
                    2 -> {
                        if (question.maxSelecting != null) {
                            val list = question.options.filter { it.isCorrect }.sortedByDescending { it.score }
                            for (i in 0 until min(question.maxSelecting, list.size)) {
                                questionScore += list[i].score
                            }
                        }
                    }
                }
            }
            2 -> {
                for (i in question.answers.indices) {
                    questionScore += getMaxScore(question, i)
                }
            }
            else -> questionScore = 114514F
        }
        return questionScore
    }

    fun getMaxScore(question: Question, answerIndex: Int): Float {
        var maxScore = 0F
        question.answers[answerIndex].score.forEach {
            if (it > maxScore) maxScore = it
        }
        return maxScore
    }

    fun getStateContent(question: Question, map: Map<Int, Map<Int, Map<Int, Float>>>, index: Int): Pair<Color, String> {
        val questionMap = getQuestionStateMap(map)
        val questionNumber = index + 1
        val userScore = questionMap[index]?.get(SCORE)
        return when (questionMap[index]?.get(CORRECTNESS)) {
            1F -> Pair(Green200, context.resources.getString(R.string.result_question_state, questionNumber, getResString(R.string.correct), userScore, getQuestionScore(question)))
            3F -> Pair(Yellow200, context.resources.getString(R.string.result_question_state, questionNumber, getResString(R.string.half_correct), userScore, getQuestionScore(question)))
            4F -> Pair(Blue200, context.resources.getString(R.string.result_question_state, questionNumber, getResString(R.string.no_points), userScore, getQuestionScore(question)))
            else -> Pair(Red200, context.resources.getString(R.string.result_question_state, questionNumber, getResString(R.string.wrong), userScore, getQuestionScore(question)))
        }
    }

    fun getSelectionColor(question: Question, index: Int): Pair<Color?, Color?> {
        val userSelected = question.userAnswer[index] == "1"
        return if (question.options[index].isCorrect == true) {
            Pair(if (userSelected) Green700 else null, if (!userSelected) Green700 else null)
        } else {
            Pair(if (userSelected) Red700 else null, null)
        }
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

    fun parseQuestion(question: Question): Pair<String, List<String>?> {
        if (question.type == 1) {
            return Pair(question.question, null)
        }
        var questionText = ""  //题目文本
        var escaped = false  //转义模式
        var isBank = false  //“空”模式
        var bankText = ""  //当前“空”提示文本
        val bank: MutableList<String> = mutableListOf()  //“空”提示文本列表
        for (char in question.question) {
            when (char) {
                '&' -> {  //遇到'&'
                    if (escaped) {
                        escaped = false  //取消转义状态
                        questionText += char  //给题目文本加上
                        if (isBank) {
                            bankText += char  //如果在“空”里还要给该空文本加上
                        }
                    } else {
                        escaped = true
                    }
                }
                '{' -> {
                    if (escaped) {
                        escaped = false
                        questionText += char
                        if (isBank) {
                            bankText += char
                        }
                    } else if (!isBank) {
                        isBank = true
                        questionText += " <u>**[  "
                    } else {  //  !escaped || isBank
                        questionText += char
                        if (isBank) {
                            bankText += char
                        }
                    }
                }
                '}' -> {
                    if (escaped) {
                        questionText += char
                        escaped = false
                        if (isBank) {
                            bankText += char
                        }
                    } else if (isBank) {
                        isBank = false
                        questionText += "  ]**</u> "
                        bank.add(bankText)
                        bankText = ""
                    } else {  //  !escaped || !isBank
                        questionText += char
                        if (!isBank) {
                            bankText += char
                        }
                    }
                }
                else -> {  //如果是其他字符就老实加入
                    questionText += char
                    if (isBank) {
                        bankText += char
                    }
                }
            }
        }
        return Pair(questionText, bank)
    }
}