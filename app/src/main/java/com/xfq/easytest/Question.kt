package com.xfq.easytest

import android.os.Parcel
import android.os.Parcelable
import kotlin.properties.Delegates

open class Question() : Parcelable {
    lateinit var question: String

    constructor(parcel: Parcel) : this() {
        question = parcel.readString().toString()
    }

    class FillBankQuestion : Question() {
        lateinit var answer: List<FillBankQuestionAnswer>

        class FillBankQuestionAnswer {
            lateinit var answer: String
            var score by Delegates.notNull<Float>()
            lateinit var userAnswer: String
        }
    }

    class Option {
        lateinit var option: String
        var isCorrect by Delegates.notNull<Boolean>()
        var score by Delegates.notNull<Float>()
        var userSelected by Delegates.notNull<Boolean>()
    }

    class SingleChooseQuestion : Question() {
        lateinit var options: List<Option>
    }

    class MultipleChooseQuestion : Question() {
        var scoreType by Delegates.notNull<Int>()

        /*
        1 -> 选择即得options[].score分
        2 -> 选对即得options[].score分 选错不得分
         */
        var maxSelecting by Delegates.notNull<Int>()
        lateinit var options: List<Option>
    }

    class GeneralQuestion : Question() {
        var exactMatch by Delegates.notNull<Boolean>()
        lateinit var answer: MutableList<String>
        var score by Delegates.notNull<Float>()
        lateinit var userAnswer: String
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(question)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Question> {
        override fun createFromParcel(parcel: Parcel): Question {
            return Question(parcel)
        }

        override fun newArray(size: Int): Array<Question?> {
            return arrayOfNulls(size)
        }
    }
}