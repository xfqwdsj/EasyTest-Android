package com.xfq.easytest

import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.WindowCompat
import com.alibaba.fastjson.JSON
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.xfq.easytest.MyClass.getResColor
import com.xfq.easytest.databinding.ActivityResultBinding
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        MyClass.setInset(MyClass.INSET_TOP, binding.toolbar)

        val result = intent.getParcelableExtra<Result>("result")
        if (result != null) {
            val questionList = JSON.parseArray(result.question, Question::class.java).toMutableList()
            val markwon = Markwon.builder(this).apply {
                usePlugin(StrikethroughPlugin.create())
                usePlugin(TablePlugin.create(this@ResultActivity))
                usePlugin(TaskListPlugin.create(this@ResultActivity))
                usePlugin(HtmlPlugin.create())
                usePlugin(ImagesPlugin.create())
                usePlugin(LinkifyPlugin.create())
            }.build()  //初始化Markwon
            val viewList: MutableList<View> = ArrayList()
            val positionList: MutableList<Int> = ArrayList()
            val scoreList: MutableList<Float> = ArrayList()
            for ((index, question) in questionList.withIndex()) {
                val view = layoutInflater.inflate(R.layout.layout_test, LinearLayout(this), true)
                val answerView = layoutInflater.inflate(R.layout.layout_answer, LinearLayout(this), false)
                when (question.type) {
                    1 -> {  //填空题
                        view.findViewById<ScrollView>(R.id.fillBankQuestionLayout).visibility = View.VISIBLE
                        var questionText = ""  //预留即将显示的题目文本
                        var escaped = false  //转义模式
                        var isBank = false  //“空”模式
                        var bankText = ""  //当前“空”提示文本
                        val bank: MutableList<String> = ArrayList()  //“空”提示文本列表
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
                        markwon.setMarkdown(view.findViewById(R.id.fillBankQuestion), questionText)
                        var score = 0F
                        var answer = ""
                        for (i in bank.indices) {  //遍历“空”
                            answer += question.children[i].text
                            view.findViewById<FlexboxLayout>(R.id.fillBankEdit).addView(MaterialCardView(this).apply {
                                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                                    setMargins(MyClass.dip2PxI(5F), MyClass.dip2PxI(5F), MyClass.dip2PxI(5F), MyClass.dip2PxI(5F))
                                }
                                id = i
                                cardElevation = 0F
                                addView(TextInputEditText(this@ResultActivity).apply input@{
                                    hint = bank[i]
                                    minEms = 5
                                    gravity = Gravity.CENTER
                                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                                    setText(question.userAnswer[i])
                                    isEnabled = false
                                })
                                setCardBackgroundColor(when (result.correctnessList[index][i]) {
                                    '1' -> getResColor(R.color.colorTestCorrect)
                                    else -> getResColor(R.color.colorTestWrong)
                                })
                            })
                            if (result.correctnessList[index][i] == '1') score += question.children[i].score
                        }
                        scoreList.add(score)
                        answerView.findViewById<TextView>(R.id.answer).text = answer.substring(0, answer.length - 2)
                        viewList[index].findViewById<ConstraintLayout>(R.id.fillBankQuestionConstraint).addView(answerView)
                        ConstraintSet().apply {
                            clone(viewList[index].findViewById<ConstraintLayout>(R.id.fillBankQuestionConstraint))
                            connect(R.id.answerLayout, ConstraintSet.TOP, R.id.fillBankEdit, ConstraintSet.BOTTOM, MyClass.dip2PxI(8F))
                            applyTo(viewList[index].findViewById(R.id.fillBankQuestionConstraint))
                        }
                    }
                    2 -> {
                        view.findViewById<ScrollView>(R.id.chooseQuestionLayout).visibility = View.VISIBLE
                        markwon.setMarkdown(view.findViewById(R.id.chooseQuestion), question.question)
                        var score = 0F
                        val buttonList: MutableList<CheckBox> = ArrayList()
                        for (i in question.children.indices) {
                            val button = (LayoutInflater.from(this).inflate(R.layout.layout_for_test_single_choose, LinearLayout(this), false) as CheckBox).apply {
                                markwon.setMarkdown(this, question.children[i].text)
                                isChecked = when (question.userAnswer[i]) {
                                    "1" -> true
                                    "2" -> false
                                    else -> false
                                }
                                visibility = View.VISIBLE
                                isEnabled = false
                            }
                            buttonList.add(button)
                            view.findViewById<LinearLayout>(R.id.chooseGroup).addView(MaterialCardView(this).apply {
                                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                                    setMargins(MyClass.dip2PxI(5F), MyClass.dip2PxI(5F), MyClass.dip2PxI(5F), MyClass.dip2PxI(5F))
                                }
                                id = i
                                cardElevation = 0F
                                addView(button)
                                if (question.children[i].isCorrect && question.userAnswer[i] == "1") {
                                    setCardBackgroundColor(getResColor(R.color.colorTestCorrect))
                                } else if (!question.children[i].isCorrect && question.userAnswer[i] == "1") {
                                    setCardBackgroundColor(getResColor(R.color.colorTestWrong))
                                }
                                if (question.children[i].isCorrect) {
                                    answerView.findViewById<TextView>(R.id.answer).text = question.children[i].text
                                    viewList[index].findViewById<ConstraintLayout>(R.id.fillBankQuestionConstraint).addView(answerView)
                                    ConstraintSet().apply {
                                        clone(viewList[index].findViewById<ConstraintLayout>(R.id.fillBankQuestionConstraint))
                                        connect(R.id.answerLayout, ConstraintSet.TOP, R.id.fillBankEdit, ConstraintSet.BOTTOM, MyClass.dip2PxI(8F))
                                        applyTo(viewList[index].findViewById(R.id.fillBankQuestionConstraint))
                                    }
                                }
                            })
                            if (question.userAnswer[i] == "1") score += question.children[i].score
                        }
                        scoreList.add(score)
                    }
                    3 -> {
                        view.findViewById<ScrollView>(R.id.chooseQuestionLayout).visibility = View.VISIBLE
                        markwon.setMarkdown(view.findViewById(R.id.chooseQuestion), question.question)
                        var score = 0F
                        var hasScore = true
                        var answer = ""
                        for (i in question.children.indices) {
                            val button = MaterialCheckBox(this).apply {
                                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                                    setMargins(MyClass.dip2PxI(5F), 0, MyClass.dip2PxI(5F), 0)
                                }
                                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
                                markwon.setMarkdown(this, question.children[i].text)
                                isChecked = when (question.userAnswer[i]) {
                                    "1" -> true
                                    "2" -> false
                                    else -> false
                                }
                                isEnabled = false
                            }
                            view.findViewById<LinearLayout>(R.id.chooseGroup).addView(MaterialCardView(this).apply {
                                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                                    setMargins(MyClass.dip2PxI(5F), MyClass.dip2PxI(5F), MyClass.dip2PxI(5F), MyClass.dip2PxI(5F))
                                }
                                id = i
                                cardElevation = 0F
                                addView(button)
                                if (question.children[i].isCorrect && question.userAnswer[i] == "1") {
                                    setCardBackgroundColor(getResColor(R.color.colorTestCorrect))
                                } else if (!question.children[i].isCorrect && question.userAnswer[i] == "1") {
                                    setCardBackgroundColor(getResColor(R.color.colorTestWrong))
                                }
                            })
                            when (question.scoreType) {
                                1 -> {
                                    if (question.userAnswer[i] == "1") {
                                        score += question.children[i].score
                                    }
                                }
                                2 -> {
                                    when {
                                        question.children[i].isCorrect && question.userAnswer[i] == "1" -> {
                                            score += question.children[i].score
                                        }
                                        !question.children[i].isCorrect && question.userAnswer[i] == "1" -> {
                                            hasScore = false
                                        }
                                    }
                                }
                            }
                            if (question.children[i].isCorrect) {
                                answer += "${question.children[i].text}; "
                            }
                        }
                        scoreList.add(if (hasScore) score else 0F)
                        answerView.findViewById<TextView>(R.id.answer).text = answer.substring(0, answer.length - 2)
                        viewList[index].findViewById<ConstraintLayout>(R.id.fillBankQuestionConstraint).addView(answerView)
                        ConstraintSet().apply {
                            clone(viewList[index].findViewById<ConstraintLayout>(R.id.fillBankQuestionConstraint))
                            connect(R.id.answerLayout, ConstraintSet.TOP, R.id.fillBankEdit, ConstraintSet.BOTTOM, MyClass.dip2PxI(8F))
                            applyTo(viewList[index].findViewById(R.id.fillBankQuestionConstraint))
                        }
                    }
                    4 -> {
                        view.findViewById<ScrollView>(R.id.generalQuestionLayout).visibility = View.VISIBLE
                        markwon.setMarkdown(view.findViewById(R.id.generalQuestion), question.question)
                        view.findViewById<EditText>(R.id.generalEdit).apply {
                            setText(question.userAnswer[0])
                            isEnabled = false
                        }
                        view.findViewById<CardView>(R.id.cardView).setCardBackgroundColor(when (result.correctnessList[index]) {
                            "1" -> getResColor(R.color.colorTestCorrect)
                            else -> getResColor(R.color.colorTestWrong)
                        })
                        scoreList.add(if (result.correctnessList[index] == "1") question.children[0].score else 0F)
                        answerView.findViewById<TextView>(R.id.answer).text = question.children[question.children.indices.random()].text
                        answerView.findViewById<Button>(R.id.change).visibility = View.VISIBLE
                        answerView.findViewById<Button>(R.id.change).setOnClickListener {
                            answerView.findViewById<TextView>(R.id.answer).text = question.children[question.children.indices.random()].text
                        }
                        viewList[index].findViewById<ConstraintLayout>(R.id.fillBankQuestionConstraint).addView(answerView)
                        ConstraintSet().apply {
                            clone(viewList[index].findViewById<ConstraintLayout>(R.id.fillBankQuestionConstraint))
                            connect(R.id.answerLayout, ConstraintSet.TOP, R.id.fillBankEdit, ConstraintSet.BOTTOM, MyClass.dip2PxI(8F))
                            applyTo(viewList[index].findViewById(R.id.fillBankQuestionConstraint))
                        }
                    }
                    else -> {
                        finish()
                        return
                    }
                }
                viewList.add(view)
                positionList.add(index)
            }
            val view = layoutInflater.inflate(R.layout.layout_test, LinearLayout(this), true).apply {
                findViewById<ScrollView>(R.id.resultLayout).visibility = View.VISIBLE
            }
            var scoreText = ""
            var total = 0F
            for (i in questionList.indices) {
                var correctness = result.correctnessList[i]
                val originalCorrectness = correctness
                if (originalCorrectness.length > 1) {
                    correctness = when {
                        originalCorrectness.contains('4') -> "4"
                        originalCorrectness.contains('1') && originalCorrectness.contains('2') -> "3"
                        originalCorrectness.contains('1') -> "1"
                        else -> "2"
                    }
                }
                total += scoreList[i]
                scoreText += "${scoreList[i]}(${
                    MyClass.getResString(when (correctness) {
                        "1" -> R.string.correct
                        "2" -> R.string.wrong
                        "3" -> R.string.half_correct
                        "4" -> R.string.no_points
                        else -> R.string.unknown
                    })
                })${if (i != scoreList.size - 1) " + " else " = $total"}"
                val button = Chip(this).apply {
                    setOnClickListener {
                        binding.viewPager.currentItem = i
                    }
                    text = resources.getString(R.string.question_number, i + 1)
                }
                if (originalCorrectness == "2" || originalCorrectness == "3") {
                    view.findViewById<ChipGroup>(R.id.resultWrongGroup).addView(button)
                } else if (originalCorrectness == "4") {
                    view.findViewById<ChipGroup>(R.id.resultNoPointsGroup).addView(button)
                } else if (originalCorrectness.length > 1) {
                    for (char in originalCorrectness) {
                        val bankButton = Chip(this).apply {
                            setOnClickListener {
                                binding.viewPager.currentItem = i
                            }
                            text = resources.getString(R.string.question_number, i + 1)
                        }
                        if (char == '2') {
                            view.findViewById<ChipGroup>(R.id.resultWrongGroup).addView(bankButton)
                        } else if (char == '4') {
                            view.findViewById<ChipGroup>(R.id.resultNoPointsGroup).addView(bankButton)
                        }
                    }
                }
            }
            view.findViewById<TextView>(R.id.resultScoreText).text = scoreText
            if (view.findViewById<ChipGroup>(R.id.resultWrongGroup).childCount > 0) {
                view.findViewById<LinearLayout>(R.id.resultWrong).visibility = View.VISIBLE
            }
            if (view.findViewById<ChipGroup>(R.id.resultNoPointsGroup).childCount > 0) {
                view.findViewById<LinearLayout>(R.id.resultNoPoints).visibility = View.VISIBLE
            }
            view.findViewById<Button>(R.id.submit).visibility = View.GONE
            viewList.add(view)
            binding.viewPager.adapter = TestPagerAdapter(viewList, positionList, this).apply {
                submitted = true
            }
            binding.tabLayout.setupWithViewPager(binding.viewPager)
        } else {
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return false
    }
}