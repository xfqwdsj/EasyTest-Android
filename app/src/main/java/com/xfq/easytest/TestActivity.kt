package com.xfq.easytest

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.*
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.WindowCompat
import androidx.transition.TransitionManager
import com.alibaba.fastjson.JSON
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.transition.MaterialContainerTransform
import com.xfq.bottomdialog.BottomDialog
import com.xfq.easytest.MyClass.INSET_TOP
import com.xfq.easytest.MyClass.dip2PxI
import com.xfq.easytest.MyClass.getResColor
import com.xfq.easytest.MyClass.getResString
import com.xfq.easytest.MyClass.setInset
import com.xfq.easytest.databinding.ActivityTestBinding
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import okhttp3.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class TestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTestBinding
    private lateinit var url: String
    private lateinit var userAnswerList: List<Question>
    private lateinit var adapter: TestPagerAdapter
    private lateinit var markwon: Markwon
    private val positionList: MutableList<Int> = ArrayList()
    private val viewList: MutableList<View> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setInset(INSET_TOP, binding.toolbar)
        url = this.intent.getStringExtra("url").toString()  //获取题目url
        markwon = Markwon.builder(this).apply {
            usePlugin(StrikethroughPlugin.create())
            usePlugin(TablePlugin.create(this@TestActivity))
            usePlugin(TaskListPlugin.create(this@TestActivity))
            usePlugin(HtmlPlugin.create())
            usePlugin(ImagesPlugin.create())
            usePlugin(LinkifyPlugin.create())
        }.build()  //初始化Markwon
        val random = this.intent.getBooleanExtra("random", false)  //获取是否随机排序
        var questions: MutableList<Question>  //预留一个题目列表 稍后进行解析

        val request = Request.Builder()
                .url(url)
                .removeHeader("User-Agent")
                .addHeader("User-Agent", WebView(this).settings.userAgentString)  //加UA以免gitee屏蔽
                .build()
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    BottomDialog().create(this@TestActivity).apply {
                        setTitle(R.string.failed)
                        setContent(resources.getString(R.string.error, e))
                        setCancelAble(false)
                        setButton1(android.R.string.ok) {
                            close()
                            finish()
                        }
                    }
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    try {
                        questions = JSON.parseArray(response.body?.string().toString(), Question::class.java).toMutableList()  //把获取到的json字符串数组解析出来
                        userAnswerList = List(questions.size) { Question() }
                        for (i in questions.indices) {
                            userAnswerList[i].userAnswer = questions[i].userAnswer
                        }
                        runOnUiThread {
                            setAdapter(random, questions)  //给ViewPager和TabLayout设置适配器
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    private fun setAdapter(random: Boolean, questionList: List<Question>) {
        for (position in questionList.indices) {
            val question = questionList[position]  //当前的question
            val view = layoutInflater.inflate(R.layout.layout_test, LinearLayout(this), true)
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
                    for (i in bank.indices) {  //遍历“空”
                        val cardView = MaterialCardView(this)
                        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        params.setMargins(dip2PxI(5F), dip2PxI(5F), dip2PxI(5F), dip2PxI(5F))
                        cardView.layoutParams = params
                        cardView.id = i
                        cardView.cardElevation = 0F
                        val editText = TextInputEditText(this)
                        val watcher = object : TextWatcher {
                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                            override fun afterTextChanged(s: Editable?) {
                                userAnswerList[position].userAnswer[i] = s.toString()
                            }
                        }
                        editText.hint = bank[i]
                        editText.minEms = 5
                        editText.gravity = Gravity.CENTER
                        editText.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        editText.setText(userAnswerList[position].userAnswer[i])
                        editText.addTextChangedListener(watcher)
                        editText.tag = watcher
                        cardView.addView(editText)
                        view.findViewById<FlexboxLayout>(R.id.fillBankEdit).addView(cardView)
                    }
                }
                2 -> {
                    view.findViewById<ScrollView>(R.id.chooseQuestionLayout).visibility = View.VISIBLE
                    markwon.setMarkdown(view.findViewById(R.id.chooseQuestion), question.question)
                    val buttonList: MutableList<CheckBox> = ArrayList()
                    for (i in question.children.indices) {
                        val cardView = MaterialCardView(this)
                        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        params.setMargins(dip2PxI(5F), dip2PxI(5F), dip2PxI(5F), dip2PxI(5F))
                        cardView.layoutParams = params
                        cardView.id = i
                        cardView.cardElevation = 0F
                        val button = LayoutInflater.from(this).inflate(R.layout.layout_for_test_single_choose, LinearLayout(this), false) as CheckBox
                        markwon.setMarkdown(button, question.children[i].text)
                        button.isChecked = when (userAnswerList[position].userAnswer[i]) {
                            "1" -> true
                            "2" -> false
                            else -> false
                        }
                        button.visibility = View.VISIBLE
                        button.setOnCheckedChangeListener { _, isChecked ->
                            userAnswerList[position].userAnswer[i] = when (isChecked) {
                                true -> "1"
                                false -> "2"
                            }
                            if (isChecked) {
                                for (otherButton in buttonList) {
                                    if (!(otherButton === button)) {
                                        otherButton.isChecked = false
                                    }
                                }
                            }
                        }
                        buttonList.add(button)
                        cardView.addView(button)
                        view.findViewById<LinearLayout>(R.id.chooseGroup).addView(cardView)
                    }
                }
                3 -> {
                    view.findViewById<ScrollView>(R.id.chooseQuestionLayout).visibility = View.VISIBLE
                    markwon.setMarkdown(view.findViewById(R.id.chooseQuestion), question.question)
                    val buttonList: MutableList<CheckBox> = ArrayList()
                    for (i in question.children.indices) {
                        val cardView = MaterialCardView(this)
                        val cardParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        cardParams.setMargins(dip2PxI(5F), dip2PxI(5F), dip2PxI(5F), dip2PxI(5F))
                        cardView.layoutParams = cardParams
                        cardView.id = i
                        cardView.cardElevation = 0F
                        val button = MaterialCheckBox(this)
                        val buttonParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        buttonParams.setMargins(dip2PxI(5F), 0, dip2PxI(5F), 0)
                        button.layoutParams = buttonParams
                        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
                        markwon.setMarkdown(button, question.children[i].text)
                        button.isChecked = when (userAnswerList[position].userAnswer[i]) {
                            "1" -> true
                            "2" -> false
                            else -> false
                        }
                        button.setOnCheckedChangeListener { _, isChecked ->
                            userAnswerList[position].userAnswer[i] = when (isChecked) {
                                true -> "1"
                                false -> "2"
                            }
                            val uncheckedButtonList: MutableList<CheckBox> = ArrayList()
                            var checked = 0
                            for (other in buttonList) {
                                if (other.isChecked) {
                                    checked++
                                } else {
                                    uncheckedButtonList.add(other)
                                }
                            }
                            for (uncheckedButton in uncheckedButtonList) {
                                uncheckedButton.isEnabled = checked != question.maxSelecting
                            }
                        }
                        buttonList.add(button)
                        cardView.addView(button)
                        view.findViewById<LinearLayout>(R.id.chooseGroup).addView(cardView)
                    }
                }
                4 -> {
                    view.findViewById<ScrollView>(R.id.generalQuestionLayout).visibility = View.VISIBLE
                    markwon.setMarkdown(view.findViewById(R.id.generalQuestion), question.question)
                    view.findViewById<EditText>(R.id.generalEdit).setText(userAnswerList[position].userAnswer[0])
                    val watcher = object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                        override fun afterTextChanged(s: Editable?) {
                            userAnswerList[position].userAnswer[0] = s.toString()
                        }
                    }
                    view.findViewById<EditText>(R.id.generalEdit).addTextChangedListener(watcher)
                    view.findViewById<EditText>(R.id.generalEdit).tag = watcher
                }
                else -> {
                    finish()
                    return
                }
            }
            viewList.add(view)
        }
        if (random) {
            randomSort()
        } else {
            for (i in viewList.indices) {
                positionList.add(i)
            }
        }
        adapter = TestPagerAdapter(viewList, positionList, this)
        binding.viewPager.adapter = adapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
        binding.start.setOnClickListener {
            val transform = MaterialContainerTransform()
            transform.apply {
                scrimColor = Color.TRANSPARENT
                startView = binding.start
                endView = binding.testContainer
                addTarget(binding.testContainer)
            }
            TransitionManager.beginDelayedTransition(binding.root, transform)
            binding.testContainer.visibility = View.VISIBLE
            binding.start.visibility = View.GONE
            binding.root.removeView(binding.start)
            binding.toolbar.menu.findItem(R.id.submit).isVisible = true
        }
        binding.start.isEnabled = true
    }

    private fun submit() {
        val scoreList: MutableList<Float> = ArrayList()
        val correctnessList: MutableList<Int> = ArrayList()
        /*
        1 -> 正确
        2 -> 错误
        3 -> 半对
        4 -> 待定
         */
        var questionList: MutableList<Question>
        val request = Request.Builder()
                .url(url)
                .removeHeader("User-Agent")
                .addHeader("User-Agent", WebView(this).settings.userAgentString)
                .build()
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    BottomDialog().create(this@TestActivity).apply {
                        setTitle(R.string.failed)
                        setContent(resources.getString(R.string.error, e))
                        setCancelAble(false)
                        setButton1(android.R.string.ok) {
                            close()
                            finish()
                        }
                    }
                }
            }

            @SuppressLint("ClickableViewAccessibility")
            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    try {
                        questionList = JSON.parseArray(response.body?.string(), Question::class.java)
                        runOnUiThread {
                            recoveryList()
                            var fillScoringFunction: (Int, Int) -> Unit = { _, _ -> }
                            var generalScoringFunction: (Int) -> Unit = {}
                            for (i in questionList.indices) {
                                questionList[i].userAnswer = userAnswerList[i].userAnswer
                                var score = 0F
                                val layout = layoutInflater.inflate(R.layout.layout_answer, LinearLayout(this@TestActivity), false)
                                val online = questionList[i]
                                when (online.type) {
                                    1 -> {
                                        var correctness = 0
                                        var answer = ""
                                        for (j in online.children.indices) {
                                            val correctAnswer = online.children[j].text
                                            answer += "$correctAnswer; "
                                            val cardView = viewList[i].findViewById<CardView>(j)
                                            cardView.getChildAt(0).isEnabled = false
                                            (cardView.getChildAt(0) as EditText).removeTextChangedListener(((cardView.getChildAt(0) as EditText).tag) as TextWatcher)
                                            (cardView.getChildAt(0) as EditText).isClickable = false
                                            (cardView.getChildAt(0) as EditText).isLongClickable = false
                                            when {
                                                userAnswerList[i].userAnswer[j] == correctAnswer -> {
                                                    score += online.children[j].score
                                                    cardView.setCardBackgroundColor(getResColor(R.color.colorTestRight))
                                                    if (correctness != 3 && correctness != 4) {
                                                        correctness = 1
                                                    } else if (correctness == 2) {
                                                        correctness = 3
                                                    }
                                                }
                                                online.children[j].exactMatch == true || online.children[j].exactMatch == null -> {
                                                    cardView.setCardBackgroundColor(getResColor(R.color.colorTestWrong))
                                                    if (correctness != 3 && correctness != 4) {
                                                        correctness = 2
                                                    } else if (correctness == 1) {
                                                        correctness = 3
                                                    }
                                                }
                                                else -> {
                                                    correctness = 4
                                                    cardView.setCardBackgroundColor(getResColor(R.color.colorTestHalf))
                                                    cardView.setOnClickListener {
                                                        fillScoringFunction(i, j)
                                                    }
                                                }
                                            }
                                        }
                                        layout.findViewById<TextView>(R.id.answer).text = answer.substring(0, answer.length - 2)
                                        viewList[i].findViewById<ConstraintLayout>(R.id.fillBankQuestionConstraint).addView(layout)
                                        val constraintSet = ConstraintSet()
                                        constraintSet.clone(viewList[i].findViewById<ConstraintLayout>(R.id.fillBankQuestionConstraint))
                                        constraintSet.connect(R.id.answerLayout, ConstraintSet.TOP, R.id.fillBankEdit, ConstraintSet.BOTTOM, dip2PxI(8F))
                                        constraintSet.applyTo(viewList[i].findViewById(R.id.fillBankQuestionConstraint))
                                        correctnessList.add(correctness)
                                    }
                                    2 -> {
                                        var actuallyCorrect = 0
                                        for (j in online.children.indices) {
                                            val cardView = viewList[i].findViewById<CardView>(j)
                                            cardView.getChildAt(0).isEnabled = false
                                            if (userAnswerList[i].userAnswer[j] == "1") {
                                                score += online.children[j].score
                                            }
                                            if (userAnswerList[i].userAnswer[j] == "1" && online.children[j].isCorrect == true) {
                                                actuallyCorrect++
                                            } else if (userAnswerList[i].userAnswer[j] == "1" && online.children[j].isCorrect == false) {
                                                cardView.setCardBackgroundColor(getResColor(R.color.colorTestWrong))
                                            }
                                            if (online.children[j].isCorrect == true) {
                                                cardView.setCardBackgroundColor(getResColor(R.color.colorTestRight))
                                                layout.findViewById<TextView>(R.id.answer).text = online.children[j].text
                                                viewList[i].findViewById<ConstraintLayout>(R.id.chooseQuestionConstraint).addView(layout)
                                                val constraintSet = ConstraintSet()
                                                constraintSet.clone(viewList[i].findViewById<ConstraintLayout>(R.id.chooseQuestionConstraint))
                                                constraintSet.connect(R.id.answerLayout, ConstraintSet.TOP, R.id.chooseGroup, ConstraintSet.BOTTOM, dip2PxI(8F))
                                                constraintSet.applyTo(viewList[i].findViewById(R.id.chooseQuestionConstraint))
                                            }
                                        }
                                        val correctness = when (actuallyCorrect) {
                                            1 -> 1
                                            else -> 2
                                        }
                                        correctnessList.add(correctness)
                                    }
                                    3 -> {
                                        var hasScore = true
                                        var shouldCorrect = 0
                                        var actuallyCorrect = 0
                                        var answer = ""
                                        for (j in online.children.indices) {
                                            val cardView = viewList[i].findViewById<CardView>(j)
                                            cardView.getChildAt(0).isEnabled = false
                                            when (online.scoreType) {
                                                1 -> {
                                                    if (userAnswerList[i].userAnswer[j] == "1") {
                                                        score += online.children[j].score
                                                    }
                                                }
                                                2 -> {
                                                    when {
                                                        online.children[j].isCorrect == true && userAnswerList[i].userAnswer[j] == "1" -> {
                                                            score += online.children[j].score
                                                        }
                                                        online.children[j].isCorrect == false && userAnswerList[i].userAnswer[j] == "1" -> {
                                                            hasScore = false
                                                        }
                                                    }
                                                }
                                            }
                                            if (online.children[j].isCorrect == true) {
                                                shouldCorrect++
                                            }
                                            if (userAnswerList[i].userAnswer[j] == "1" && online.children[j].isCorrect == true) {
                                                actuallyCorrect++
                                            } else if (userAnswerList[i].userAnswer[j] == "1" && online.children[j].isCorrect == false) {
                                                cardView.setCardBackgroundColor(getResColor(R.color.colorTestWrong))
                                            }
                                            if (online.children[j].isCorrect == true) {
                                                cardView.setCardBackgroundColor(getResColor(R.color.colorTestRight))
                                                answer += "${online.children[j].text}; "
                                            }
                                        }
                                        layout.findViewById<TextView>(R.id.answer).text = answer.substring(0, answer.length - 2)
                                        viewList[i].findViewById<ConstraintLayout>(R.id.chooseQuestionConstraint).addView(layout)
                                        val constraintSet = ConstraintSet()
                                        constraintSet.clone(viewList[i].findViewById<ConstraintLayout>(R.id.chooseQuestionConstraint))
                                        constraintSet.connect(R.id.answerLayout, ConstraintSet.TOP, R.id.chooseGroup, ConstraintSet.BOTTOM, dip2PxI(8F))
                                        constraintSet.applyTo(viewList[i].findViewById(R.id.chooseQuestionConstraint))
                                        if (!hasScore) {
                                            score = 0F
                                        }
                                        correctnessList.add(when {
                                            !hasScore -> 2
                                            shouldCorrect == actuallyCorrect -> 1
                                            actuallyCorrect == 0 -> 2
                                            else -> 3
                                        })
                                    }
                                    4 -> {
                                        val cardView = viewList[i].findViewById<CardView>(R.id.cardView)
                                        cardView.getChildAt(0).isEnabled = false
                                        (cardView.getChildAt(0) as EditText).removeTextChangedListener(((cardView.getChildAt(0) as EditText).tag) as TextWatcher)
                                        (cardView.getChildAt(0) as EditText).isClickable = false
                                        (cardView.getChildAt(0) as EditText).isLongClickable = false
                                        var correctness = 2
                                        for (j in online.children.indices) {
                                            val correctAnswer = online.children[j].text
                                            if (correctness != 1) {
                                                when {
                                                    userAnswerList[i].userAnswer[0] == correctAnswer -> {
                                                        correctness = 1
                                                        score += online.children[j].score
                                                        cardView.setCardBackgroundColor(getResColor(R.color.colorTestRight))
                                                    }
                                                    online.children[0].exactMatch == true -> {
                                                        correctness = 2
                                                        cardView.setCardBackgroundColor(getResColor(R.color.colorTestWrong))
                                                    }
                                                    else -> {
                                                        correctness = 4
                                                        cardView.setCardBackgroundColor(getResColor(R.color.colorTestHalf))
                                                        cardView.setOnClickListener {
                                                            generalScoringFunction(i)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        layout.findViewById<TextView>(R.id.answer).text = online.children[online.children.indices.random()].text
                                        layout.findViewById<Button>(R.id.change).visibility = View.VISIBLE
                                        layout.findViewById<Button>(R.id.change).setOnClickListener {
                                            layout.findViewById<TextView>(R.id.answer).text = online.children[online.children.indices.random()].text
                                        }
                                        viewList[i].findViewById<ConstraintLayout>(R.id.generalQuestionConstraint).addView(layout)
                                        val constraintSet = ConstraintSet()
                                        constraintSet.clone(viewList[i].findViewById<ConstraintLayout>(R.id.generalQuestionConstraint))
                                        constraintSet.connect(R.id.answerLayout, ConstraintSet.TOP, R.id.cardView, ConstraintSet.BOTTOM, dip2PxI(8F))
                                        constraintSet.applyTo(viewList[i].findViewById(R.id.generalQuestionConstraint))
                                        correctnessList.add(correctness)
                                    }
                                }
                                scoreList.add(score)
                            }
                            binding.toolbar.menu.findItem(R.id.submit).isVisible = false
                            val view = layoutInflater.inflate(R.layout.layout_test, LinearLayout(this@TestActivity), true)
                            view.findViewById<ScrollView>(R.id.resultLayout).visibility = View.VISIBLE
                            val wrongList: MutableList<Int> = ArrayList()
                            var scoreText = ""
                            var total = 0F
                            var hasNoPoints = false
                            for (i in scoreList.indices) {
                                total += scoreList[i]
                                scoreText += "${scoreList[i]}(${
                                    getResString(when (correctnessList[i]) {
                                        1 -> R.string.correct
                                        2 -> R.string.wrong
                                        3 -> R.string.half_correct
                                        4 -> R.string.no_points
                                        else -> R.string.unknown
                                    })
                                })${if (i != scoreList.size - 1) " + " else " = $total"}"
                                val button = Chip(this@TestActivity)
                                button.setOnClickListener {
                                    binding.viewPager.currentItem = i
                                }
                                button.text = resources.getString(R.string.question_number, i + 1)
                                if (correctnessList[i] != 1 && correctnessList[i] != 4) {
                                    wrongList.add(i)
                                    view.findViewById<ChipGroup>(R.id.resultWrongGroup).addView(button)
                                } else if (correctnessList[i] == 4) {
                                    hasNoPoints = true
                                    fillScoringFunction = { questionPosition, bankPosition ->
                                        if (correctnessList[questionPosition] == 4) {
                                            fun refreshView() {
                                                scoreText = ""
                                                total = 0F
                                                for (j in scoreList.indices) {
                                                    total += scoreList[j]
                                                    scoreText += "${scoreList[j]}(${
                                                        getResString(when (correctnessList[j]) {
                                                            1 -> R.string.correct
                                                            2 -> R.string.wrong
                                                            3 -> R.string.half_correct
                                                            4 -> R.string.no_points
                                                            else -> R.string.unknown
                                                        })
                                                    })${if (j != scoreList.size - 1) " + " else " = $total"}"
                                                }
                                                view.findViewById<TextView>(R.id.resultScoreText).text = scoreText
                                                when (correctnessList[questionPosition]) {
                                                    1 -> {
                                                        view.findViewById<ChipGroup>(R.id.resultNoPointsGroup).removeView(button)
                                                        if (view.findViewById<ChipGroup>(R.id.resultNoPointsGroup).childCount == 0) {
                                                            view.findViewById<LinearLayout>(R.id.resultNoPoints).visibility = View.GONE
                                                            view.findViewById<Button>(R.id.submit).isEnabled = true
                                                            view.findViewById<Button>(R.id.submit).setOnClickListener {
                                                                val result = Result()
                                                                result.question = JSON.toJSONString(questionList)
                                                                result.correctnessList = correctnessList
                                                                if (result.save()) {
                                                                    Snackbar.make(binding.root, resources.getString(R.string.upload_success, result.id), Snackbar.LENGTH_LONG).show()
                                                                    it.visibility = View.GONE
                                                                }
                                                            }
                                                        }
                                                    }
                                                    2 -> {
                                                        val newButton = Chip(this@TestActivity)
                                                        newButton.setOnClickListener {
                                                            binding.viewPager.currentItem = questionPosition
                                                        }
                                                        newButton.text = resources.getString(R.string.question_number, questionPosition + 1)
                                                        view.findViewById<LinearLayout>(R.id.resultWrong).visibility = View.VISIBLE
                                                        view.findViewById<ChipGroup>(R.id.resultWrongGroup).addView(newButton)
                                                    }
                                                }
                                            }

                                            BottomDialog().create(this@TestActivity).apply {
                                                setTitle(R.string.scoring)
                                                setContent(R.string.ask_answer_correct)
                                                setButton1(R.string.correct) {
                                                    correctnessList[questionPosition] = 1
                                                    viewList[questionPosition].findViewById<CardView>(questionPosition).setCardBackgroundColor(getResColor(R.color.colorTestRight))
                                                    scoreList[questionPosition] += questionList[questionPosition].children[bankPosition].score
                                                    var correctness = 0
                                                    val question = questionList[questionPosition]
                                                    for (j in question.children.indices) {
                                                        val correctAnswer = question.children[j].text
                                                        when {
                                                            userAnswerList[i].userAnswer[j] == correctAnswer -> {
                                                                if (correctness != 3 && correctness != 4) {
                                                                    correctness = 1
                                                                } else if (correctness == 2) {
                                                                    correctness = 3
                                                                }
                                                            }
                                                            question.children[j].exactMatch == true || question.children[j].exactMatch == null -> {
                                                                if (correctness != 3 && correctness != 4) {
                                                                    correctness = 2
                                                                } else if (correctness == 1) {
                                                                    correctness = 3
                                                                }
                                                            }
                                                            else -> {
                                                                correctness = 4
                                                            }
                                                        }
                                                    }
                                                    correctnessList[questionPosition] = correctness
                                                    refreshView()
                                                    close()
                                                }
                                                setButton2(R.string.wrong) {
                                                    correctnessList[questionPosition] = 2
                                                    viewList[questionPosition].findViewById<CardView>(R.id.cardView).setCardBackgroundColor(getResColor(R.color.colorTestWrong))
                                                    refreshView()
                                                    close()
                                                }
                                                setButton3(android.R.string.cancel) {
                                                    close()
                                                }
                                                show()
                                            }
                                        }
                                    }
                                    generalScoringFunction = { position ->
                                        if (correctnessList[position] == 4) {
                                            fun refreshView() {
                                                scoreText = ""
                                                total = 0F
                                                for (j in scoreList.indices) {
                                                    total += scoreList[j]
                                                    scoreText += "${scoreList[j]}(${
                                                        getResString(when (correctnessList[j]) {
                                                            1 -> R.string.correct
                                                            2 -> R.string.wrong
                                                            3 -> R.string.half_correct
                                                            4 -> R.string.no_points
                                                            else -> R.string.unknown
                                                        })
                                                    })${if (j != scoreList.size - 1) " + " else " = $total"}"
                                                }
                                                view.findViewById<TextView>(R.id.resultScoreText).text = scoreText
                                                view.findViewById<ChipGroup>(R.id.resultNoPointsGroup).removeView(button)
                                                if (view.findViewById<ChipGroup>(R.id.resultNoPointsGroup).childCount == 0) {
                                                    view.findViewById<LinearLayout>(R.id.resultNoPoints).visibility = View.GONE
                                                    view.findViewById<Button>(R.id.submit).isEnabled = true
                                                    view.findViewById<Button>(R.id.submit).setOnClickListener {
                                                        val result = Result()
                                                        result.question = JSON.toJSONString(questionList)
                                                        result.correctnessList = correctnessList
                                                        if (result.save()) {
                                                            Snackbar.make(binding.root, resources.getString(R.string.upload_success, result.id), Snackbar.LENGTH_LONG).show()
                                                            it.visibility = View.GONE
                                                        }
                                                    }
                                                }
                                                if (correctnessList[position] == 2) {
                                                    val newButton = Chip(this@TestActivity)
                                                    newButton.setOnClickListener {
                                                        binding.viewPager.currentItem = position
                                                    }
                                                    newButton.text = resources.getString(R.string.question_number, position + 1)
                                                    view.findViewById<LinearLayout>(R.id.resultWrong).visibility = View.VISIBLE
                                                    view.findViewById<ChipGroup>(R.id.resultWrongGroup).addView(newButton)
                                                }
                                            }

                                            BottomDialog().create(this@TestActivity).apply {
                                                setTitle(R.string.scoring)
                                                setContent(R.string.ask_answer_correct)
                                                setButton1(R.string.correct) {
                                                    correctnessList[position] = 1
                                                    viewList[position].findViewById<CardView>(R.id.cardView).setCardBackgroundColor(getResColor(R.color.colorTestRight))
                                                    scoreList[position] = questionList[position].children[0].score
                                                    refreshView()
                                                    close()
                                                }
                                                setButton2(R.string.wrong) {
                                                    correctnessList[position] = 2
                                                    viewList[position].findViewById<CardView>(R.id.cardView).setCardBackgroundColor(getResColor(R.color.colorTestWrong))
                                                    refreshView()
                                                    close()
                                                }
                                                setButton3(android.R.string.cancel) {
                                                    close()
                                                }
                                                show()
                                            }
                                        }
                                    }
                                    view.findViewById<ChipGroup>(R.id.resultNoPointsGroup).addView(button)
                                }
                            }
                            view.findViewById<TextView>(R.id.resultScoreText).text = scoreText
                            if (wrongList.isNotEmpty()) {
                                view.findViewById<LinearLayout>(R.id.resultWrong).visibility = View.VISIBLE
                            }
                            if (hasNoPoints) {
                                view.findViewById<LinearLayout>(R.id.resultNoPoints).visibility = View.VISIBLE
                            } else {
                                view.findViewById<Button>(R.id.submit).isEnabled = true
                                view.findViewById<Button>(R.id.submit).setOnClickListener {
                                    val result = Result()
                                    result.question = JSON.toJSONString(questionList)
                                    result.correctnessList = correctnessList
                                    if (result.save()) {
                                        Snackbar.make(binding.root, resources.getString(R.string.upload_success, result.id), Snackbar.LENGTH_LONG).show()
                                        it.visibility = View.GONE
                                    }
                                }
                            }
                            viewList.add(view)
                            adapter.submitted = true
                            adapter.notifyDataSetChanged()
                            binding.viewPager.currentItem = viewList.size - 1
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    private fun randomSort() {
        val originalList = viewList.toMutableList()
        for (i in viewList.indices) {
            while (true) {
                val random = (0 until viewList.size).random()
                var isOnly = true
                for (number in positionList) {
                    if (number == random) {
                        isOnly = false
                    }
                }
                if (isOnly) {
                    positionList.add(random)
                    break
                }
            }
        }
        for (i in positionList.indices) {
            viewList[i] = originalList[positionList[i]]
        }
    }

    private fun recoveryList() {
        if (viewList.size == positionList.size) {
            val originalList = viewList.toMutableList()
            val originalPositionList = positionList.toMutableList()
            for (i in viewList.indices) {
                positionList[i] = i
                viewList[originalPositionList[i]] = originalList[i]
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.submit -> {
                submit()
                return true
            }
        }
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.test_menu, menu)
        binding.toolbar.menu.findItem(R.id.submit).isVisible = false
        return true
    }
}