package com.xfq.easytest

import kotlin.properties.Delegates

open class Question {
    lateinit var question: String

    class FillBankQuestion: Question() {
        lateinit var answer: List<FillBankQuestionAnswer>

        class FillBankQuestionAnswer {
            lateinit var answer: String
            var score by Delegates.notNull<Float>()
            lateinit var userAnswer: String
            var exactMatch by Delegates.notNull<Boolean>()
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

    class GeneralQuestion: Question() {
        var exactMatch by Delegates.notNull<Boolean>()
        lateinit var answer: List<String>
        var score by Delegates.notNull<Float>()
        lateinit var userAnswer: String
    }
}