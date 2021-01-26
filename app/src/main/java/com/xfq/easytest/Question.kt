package com.xfq.easytest

import kotlin.properties.Delegates

open class Question {
    lateinit var question: String

    class FillBankQuestion: Question() {
        lateinit var answer: FillBankQuestionAnswer

        class FillBankQuestionAnswer {
            lateinit var answer: String
            var score by Delegates.notNull<Float>()
        }
    }

    class SingleChooseQuestion: Question() {
        lateinit var options: List<SingleChooseQuestionOption>

        class SingleChooseQuestionOption {
            lateinit var option: String
            var isCorrect by Delegates.notNull<Boolean>()
            var score by Delegates.notNull<Float>()
        }
    }

    class MultipleChooseQuestion: Question() {
        var minSelecting by Delegates.notNull<Int>()
        var maxSelecting by Delegates.notNull<Int>()
        lateinit var options: List<MultipleChooseQuestionOption>

        class MultipleChooseQuestionOption {
            lateinit var option: String
            var isCorrect by Delegates.notNull<Boolean>()
            lateinit var selected: MultipleChooseQuestionOptionSelectAble
            lateinit var unselected: MultipleChooseQuestionOptionSelectAble

            class MultipleChooseQuestionOptionSelectAble {
                lateinit var selectedOption: List<Int>
                lateinit var unselectedOption: List<Int>
                var score by Delegates.notNull<Float>()
            }
        }
    }

    class GeneralQuestion: Question() {
        var exactMatch by Delegates.notNull<Boolean>()
        lateinit var answer: List<String>
        var score by Delegates.notNull<Float>()
    }
}