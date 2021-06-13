package com.xfq.easytest.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.*
import android.webkit.WebView
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.transition.TransitionManager
import androidx.viewbinding.ViewBinding
import com.alibaba.fastjson.JSON
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.xfq.bottomdialog.BottomDialog
import com.xfq.easytest.Question
import com.xfq.easytest.R
import com.xfq.easytest.Result
import com.xfq.easytest.activity.base.BaseActivity
import com.xfq.easytest.databinding.*
import com.xfq.easytest.util.ActivityMap
import com.xfq.easytest.util.MyClass.dip2PxI
import com.xfq.easytest.util.MyClass.getResColor
import com.xfq.easytest.util.MyClass.getResString
import com.xfq.easytest.util.TestPagerAdapter
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

class TestActivity : BaseActivity() {
    private lateinit var binding: ActivityTestBinding
    private lateinit var url: String
    private lateinit var userAnswerList: List<Question>
    private lateinit var adapter: TestPagerAdapter
    private lateinit var markwon: Markwon
    private val positionList: MutableList<Int> = ArrayList()
    private val bindingList: MutableList<ViewBinding> = ArrayList()

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setAppBar(binding.appbar, binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        ActivityMap.clear()
        url = intent.getStringExtra("url").toString()  //获取题目url
        markwon = Markwon.builder(this).apply {
            usePlugin(StrikethroughPlugin.create())
            usePlugin(TablePlugin.create(this@TestActivity))
            usePlugin(TaskListPlugin.create(this@TestActivity))
            usePlugin(HtmlPlugin.create())
            usePlugin(ImagesPlugin.create())
            usePlugin(LinkifyPlugin.create())
        }.build()  //初始化Markwon
        val random = this.intent.getBooleanExtra("random", false)  //获取是否随机排序
        var questionList: MutableList<Question>  //预留一个题目列表 稍后进行解析

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
                        questionList = JSON.parseArray(
                            response.body?.string().toString(),
                            Question::class.java
                        ).toMutableList()  //把获取到的json字符串数组解析出来
                        userAnswerList = List(questionList.size) { Question() }
                        for (i in questionList.indices) {
                            userAnswerList[i].userAnswer = questionList[i].userAnswer
                        }
                        runOnUiThread {
                            setAdapter(random, questionList)  //给ViewPager和TabLayout设置适配器
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
            var questionBinding: ViewBinding?
            when (question.type) {
                1 -> {  //填空题
                    questionBinding = LayoutTestFillBinding.inflate(layoutInflater)
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
                    markwon.setMarkdown(questionBinding.question, questionText)
                    for (i in bank.indices) {  //遍历“空”
                        questionBinding.editGroup
                            .addView(MaterialCardView(this).apply {
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                ).apply {
                                    setMargins(dip2PxI(5F), dip2PxI(5F), dip2PxI(5F), dip2PxI(5F))
                                }
                                id = i
                                cardElevation = 0F
                                background = null
                                addView(TextInputEditText(this@TestActivity).apply input@{
                                    hint = bank[i]
                                    minEms = 5
                                    gravity = Gravity.CENTER
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.WRAP_CONTENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    )
                                    setText(userAnswerList[position].userAnswer[i])
                                    addTextChangedListener(object : TextWatcher {
                                        override fun beforeTextChanged(
                                            s: CharSequence?,
                                            start: Int,
                                            count: Int,
                                            after: Int
                                        ) {
                                        }

                                        override fun onTextChanged(
                                            s: CharSequence?,
                                            start: Int,
                                            before: Int,
                                            count: Int
                                        ) {
                                        }

                                        override fun afterTextChanged(s: Editable?) {
                                            userAnswerList[position].userAnswer[i] = s.toString()
                                        }
                                    })
                                })
                            })
                    }
                }
                2 -> {
                    questionBinding = LayoutTestChooseBinding.inflate(layoutInflater)
                    markwon.setMarkdown(questionBinding.question, question.question)
                    val buttonList: MutableList<CheckBox> = ArrayList()
                    for (i in question.children.indices) {
                        val button = (LayoutInflater.from(this).inflate(
                            R.layout.layout_for_test_single_choose,
                            LinearLayout(this),
                            false
                        ) as CheckBox).apply {
                            markwon.setMarkdown(this, question.children[i].text)
                            isChecked = when (userAnswerList[position].userAnswer[i]) {
                                "1" -> true
                                "2" -> false
                                else -> false
                            }
                            visibility = View.VISIBLE
                            setOnCheckedChangeListener { _, isChecked ->
                                userAnswerList[position].userAnswer[i] = when (isChecked) {
                                    true -> "1"
                                    false -> "2"
                                }
                                if (isChecked) {
                                    for (otherButton in buttonList) {
                                        if (!(otherButton === this)) {
                                            otherButton.isChecked = false
                                        }
                                    }
                                }
                            }
                        }
                        buttonList.add(button)
                        questionBinding.chooseGroup
                            .addView(MaterialCardView(this).apply {
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                ).apply {
                                    setMargins(dip2PxI(5F), dip2PxI(5F), dip2PxI(5F), dip2PxI(5F))
                                }
                                id = i
                                cardElevation = 0F
                                background = null
                                addView(button)
                            })
                    }
                }
                3 -> {
                    questionBinding = LayoutTestChooseBinding.inflate(layoutInflater)
                    markwon.setMarkdown(questionBinding.question, question.question)
                    val buttonList: MutableList<CheckBox> = ArrayList()
                    for (i in question.children.indices) {
                        val button = MaterialCheckBox(this).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(dip2PxI(5F), 0, dip2PxI(5F), 0)
                            }
                            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
                            markwon.setMarkdown(this, question.children[i].text)
                            isChecked = when (userAnswerList[position].userAnswer[i]) {
                                "1" -> true
                                "2" -> false
                                else -> false
                            }
                            setOnCheckedChangeListener { _, isChecked ->
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
                        }
                        buttonList.add(button)
                        questionBinding.chooseGroup
                            .addView(MaterialCardView(this).apply {
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                ).apply {
                                    setMargins(dip2PxI(5F), dip2PxI(5F), dip2PxI(5F), dip2PxI(5F))
                                }
                                id = i
                                cardElevation = 0F
                                background = null
                                addView(button)
                            })
                    }
                }
                4 -> {
                    questionBinding = LayoutTestGeneralBinding.inflate(layoutInflater)
                    markwon.setMarkdown(questionBinding.question, question.question)
                    questionBinding.edit.apply {
                        setText(userAnswerList[position].userAnswer[0])
                        addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {
                            }

                            override fun afterTextChanged(s: Editable?) {
                                userAnswerList[position].userAnswer[0] = s.toString()
                            }
                        })
                    }
                }
                else -> {
                    finish()
                    return
                }
            }
            //view.setInset(INSET_BOTTOM)
            bindingList.add(questionBinding)
        }
        if (random) {
            randomSort()
        } else {
            for (i in bindingList.indices) {
                positionList.add(i)
            }
        }
        adapter = TestPagerAdapter(bindingList, positionList, this)
        binding.viewPager.adapter = adapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
        binding.start.setOnClickListener {
            val transform = MaterialContainerTransform().apply {
                setPathMotion(MaterialArcMotion())
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

    private fun onSubmitClick() {
        val scoreList: MutableList<Float> = ArrayList()
        val correctnessList: MutableList<String> = ArrayList()
        /*
        1 -> 正确
        2 -> 错误
        3 -> 半对
        4 -> 待定
         */
        var questionList: MutableList<Question> = ArrayList()
        val request = Request.Builder()
            .url(url)
            .removeHeader("User-Agent")
            .addHeader("User-Agent", WebView(this).settings.userAgentString)
            .build()
        val resultBinding = LayoutTestResultBinding.inflate(layoutInflater)
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

            fun refreshView(questionIndex: Int, bankIndex: Int, button: Chip) {
                var scoreText = ""
                var total = 0F
                var correctness: String
                for (index in questionList.indices) {
                    correctness = correctnessList[index]
                    val originalCorrectness = correctness
                    if (originalCorrectness.length > 1) {
                        correctness = when {
                            originalCorrectness.contains('4') -> "4"
                            originalCorrectness.contains('1') && originalCorrectness.contains('2') -> "3"
                            originalCorrectness.contains('1') -> "1"
                            else -> "2"
                        }
                    }
                    total += scoreList[index]
                    scoreText += "${scoreList[index]}(${
                        getResString(
                            when (correctness) {
                                "1" -> R.string.correct
                                "2" -> R.string.wrong
                                "3" -> R.string.half_correct
                                "4" -> R.string.no_points
                                else -> R.string.unknown
                            }
                        )
                    })${if (index != scoreList.size - 1) " + " else " = $total"}"
                }
                resultBinding.resultScoreText.text = scoreText
                correctness = correctnessList[questionIndex][bankIndex].toString()
                resultBinding.resultNoPointsGroup.removeView(button)
                if (resultBinding.resultNoPointsGroup.childCount == 0) {
                    resultBinding.resultNoPoints.visibility = View.GONE
                    resultBinding.submit.isEnabled = true
                    resultBinding.submit.setOnClickListener {
                        val result = Result()
                        result.question = JSON.toJSONString(questionList)
                        result.correctnessList = correctnessList
                        if (result.save()) {
                            Snackbar.make(
                                binding.root,
                                resources.getString(R.string.upload_success, result.id),
                                Snackbar.LENGTH_LONG
                            ).show()
                            it.visibility = View.GONE
                        }
                    }
                }
                if (correctness == "2" || correctness == "3") {
                    resultBinding.resultWrong.visibility = View.VISIBLE
                    resultBinding.resultWrongGroup.addView(button)
                }
            }

            fun refreshView(questionIndex: Int, button: Chip) {
                refreshView(questionIndex, 0, button)
            }

            @SuppressLint("ClickableViewAccessibility")
            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    try {
                        questionList =
                            JSON.parseArray(response.body?.string(), Question::class.java)
                        runOnUiThread {
                            recoveryList()
                            binding.toolbar.menu.findItem(R.id.submit).isVisible = false
                            var scoreText = ""
                            for (questionIndex in questionList.indices) {
                                questionList[questionIndex].userAnswer =
                                    userAnswerList[questionIndex].userAnswer
                                var score = 0F
                                val layoutAnswerBinding =
                                    LayoutAnswerBinding.inflate(layoutInflater)
                                val online = questionList[questionIndex]
                                val questionButton = Chip(this@TestActivity).apply {
                                    setOnClickListener {
                                        binding.viewPager.currentItem = questionIndex
                                    }
                                    text = resources.getString(
                                        R.string.question_number,
                                        questionIndex + 1
                                    )
                                }
                                when (online.type) {
                                    1 -> {
                                        var correctness = ""
                                        var answer = ""
                                        for (childrenIndex in online.children.indices) {
                                            val bankButton = Chip(this@TestActivity).apply {
                                                setOnClickListener {
                                                    binding.viewPager.currentItem = questionIndex
                                                }
                                                text = resources.getString(
                                                    R.string.question_number,
                                                    questionIndex + 1
                                                )
                                            }
                                            val correctAnswer = online.children[childrenIndex].text
                                            answer += "$correctAnswer; "
                                            val cardView =
                                                bindingList[questionIndex].root.findViewById<CardView>(
                                                    childrenIndex
                                                )
                                            cardView.getChildAt(0).isEnabled = false
                                            (cardView.getChildAt(0) as EditText).isClickable = false
                                            (cardView.getChildAt(0) as EditText).isLongClickable =
                                                false
                                            when {
                                                userAnswerList[questionIndex].userAnswer[childrenIndex] == correctAnswer -> {
                                                    score += online.children[childrenIndex].score
                                                    cardView.setCardBackgroundColor(getResColor(R.color.colorTestCorrect))
                                                    correctness += "1"
                                                }
                                                online.children[childrenIndex].exactMatch || online.children[childrenIndex].exactMatch == null -> {
                                                    cardView.setCardBackgroundColor(getResColor(R.color.colorTestWrong))
                                                    correctness += "2"
                                                    resultBinding.resultWrongGroup
                                                        .addView(bankButton)
                                                }
                                                else -> {
                                                    cardView.setCardBackgroundColor(getResColor(R.color.colorTestHalf))
                                                    cardView.setOnClickListener {
                                                        BottomDialog().create(this@TestActivity)
                                                            .apply {
                                                                setTitle(R.string.scoring)
                                                                setContent(R.string.ask_answer_correct)
                                                                setButton1(R.string.correct) {
                                                                    scoreList[questionIndex] += questionList[questionIndex].children[childrenIndex].score
                                                                    val questionCorrectnessList =
                                                                        correctnessList[questionIndex].split(
                                                                            ""
                                                                        )
                                                                    var questionCorrectnessString =
                                                                        ""
                                                                    for (correctnessIndex in 1..(questionCorrectnessList.size - 2)) {
                                                                        questionCorrectnessString += if (correctnessIndex - 1 == childrenIndex) {
                                                                            "1"
                                                                        } else {
                                                                            questionCorrectnessList[correctnessIndex]
                                                                        }
                                                                    }
                                                                    correctnessList[questionIndex] =
                                                                        questionCorrectnessString
                                                                    bindingList[questionIndex].root.findViewById<CardView>(
                                                                        childrenIndex
                                                                    ).setCardBackgroundColor(
                                                                        getResColor(R.color.colorTestCorrect)
                                                                    )
                                                                    refreshView(
                                                                        questionIndex,
                                                                        childrenIndex,
                                                                        bankButton
                                                                    )
                                                                    it.setOnClickListener(null)
                                                                    it.isClickable = false
                                                                    close()
                                                                }
                                                                setButton2(R.string.wrong) {
                                                                    val questionCorrectnessList =
                                                                        correctnessList[questionIndex].split(
                                                                            ""
                                                                        )
                                                                    var questionCorrectnessString =
                                                                        ""
                                                                    for (correctnessIndex in 1..(questionCorrectnessList.size - 2)) {
                                                                        questionCorrectnessString += if (correctnessIndex - 1 == childrenIndex) {
                                                                            "2"
                                                                        } else {
                                                                            questionCorrectnessList[correctnessIndex]
                                                                        }
                                                                    }
                                                                    correctnessList[questionIndex] =
                                                                        questionCorrectnessString
                                                                    bindingList[questionIndex].root.findViewById<CardView>(
                                                                        childrenIndex
                                                                    ).setCardBackgroundColor(
                                                                        getResColor(R.color.colorTestWrong)
                                                                    )
                                                                    refreshView(
                                                                        questionIndex,
                                                                        childrenIndex,
                                                                        bankButton
                                                                    )
                                                                    it.setOnClickListener(null)
                                                                    it.isClickable = false
                                                                    close()
                                                                }
                                                                setButton3(android.R.string.cancel) {
                                                                    close()
                                                                }
                                                                show()
                                                            }
                                                    }
                                                    resultBinding.resultNoPointsGroup
                                                        .addView(bankButton)
                                                    correctness += "4"
                                                }
                                            }
                                        }
                                        layoutAnswerBinding.answer.text =
                                            answer.substring(0, answer.length - 2)
                                        (bindingList[questionIndex] as? LayoutTestFillBinding)?.constraint?.addView(
                                            layoutAnswerBinding.root
                                        )
                                        ConstraintSet().apply {
                                            clone((bindingList[questionIndex] as? LayoutTestFillBinding)?.constraint)
                                            connect(
                                                R.id.answerLayout,
                                                ConstraintSet.TOP,
                                                R.id.editGroup,
                                                ConstraintSet.BOTTOM,
                                                dip2PxI(8F)
                                            )
                                            applyTo((bindingList[questionIndex] as? LayoutTestFillBinding)?.constraint)
                                        }
                                        correctnessList.add(correctness)
                                    }
                                    2 -> {
                                        var actuallyCorrect = 0
                                        for (j in online.children.indices) {
                                            val cardView =
                                                bindingList[questionIndex].root.findViewById<CardView>(
                                                    j
                                                )
                                            cardView.getChildAt(0).isEnabled = false
                                            if (userAnswerList[questionIndex].userAnswer[j] == "1") {
                                                score += online.children[j].score
                                            }
                                            if (userAnswerList[questionIndex].userAnswer[j] == "1" && online.children[j].isCorrect == true) {
                                                actuallyCorrect++
                                            } else if (userAnswerList[questionIndex].userAnswer[j] == "1" && online.children[j].isCorrect == false) {
                                                cardView.setCardBackgroundColor(getResColor(R.color.colorTestWrong))
                                            }
                                            if (online.children[j].isCorrect == true) {
                                                cardView.setCardBackgroundColor(getResColor(R.color.colorTestCorrect))
                                                layoutAnswerBinding.answer.text =
                                                    online.children[j].text
                                                (bindingList[questionIndex] as? LayoutTestChooseBinding)?.constraint?.addView(
                                                    layoutAnswerBinding.root
                                                )
                                                ConstraintSet().apply {
                                                    clone((bindingList[questionIndex] as? LayoutTestChooseBinding)?.constraint)
                                                    connect(
                                                        R.id.answerLayout,
                                                        ConstraintSet.TOP,
                                                        R.id.chooseGroup,
                                                        ConstraintSet.BOTTOM,
                                                        dip2PxI(8F)
                                                    )
                                                    applyTo((bindingList[questionIndex] as? LayoutTestChooseBinding)?.constraint)
                                                }
                                            }
                                        }
                                        val correctness = when (actuallyCorrect) {
                                            1 -> 1
                                            else -> 2
                                        }
                                        correctnessList.add(correctness.toString())
                                    }
                                    3 -> {
                                        var hasScore = true
                                        var shouldCorrect = 0
                                        var actuallyCorrect = 0
                                        var answer = ""
                                        for (j in online.children.indices) {
                                            val cardView =
                                                bindingList[questionIndex].root.findViewById<CardView>(
                                                    j
                                                )
                                            cardView.getChildAt(0).isEnabled = false
                                            when (online.scoreType) {
                                                1 -> {
                                                    if (userAnswerList[questionIndex].userAnswer[j] == "1") {
                                                        score += online.children[j].score
                                                    }
                                                }
                                                2 -> {
                                                    when {
                                                        online.children[j].isCorrect == true && userAnswerList[questionIndex].userAnswer[j] == "1" -> {
                                                            score += online.children[j].score
                                                        }
                                                        online.children[j].isCorrect == false && userAnswerList[questionIndex].userAnswer[j] == "1" -> {
                                                            hasScore = false
                                                        }
                                                    }
                                                }
                                            }
                                            if (online.children[j].isCorrect == true) {
                                                shouldCorrect++
                                            }
                                            if (userAnswerList[questionIndex].userAnswer[j] == "1" && online.children[j].isCorrect == true) {
                                                actuallyCorrect++
                                            } else if (userAnswerList[questionIndex].userAnswer[j] == "1" && online.children[j].isCorrect == false) {
                                                cardView.setCardBackgroundColor(getResColor(R.color.colorTestWrong))
                                            }
                                            if (online.children[j].isCorrect == true) {
                                                cardView.setCardBackgroundColor(getResColor(R.color.colorTestCorrect))
                                                answer += "${online.children[j].text}; "
                                            }
                                        }
                                        layoutAnswerBinding.answer.text =
                                            answer.substring(0, answer.length - 2)
                                        (bindingList[questionIndex] as? LayoutTestChooseBinding)?.constraint?.addView(
                                            layoutAnswerBinding.root
                                        )
                                        ConstraintSet().apply {
                                            clone((bindingList[questionIndex] as? LayoutTestChooseBinding)?.constraint)
                                            connect(
                                                R.id.answerLayout,
                                                ConstraintSet.TOP,
                                                R.id.chooseGroup,
                                                ConstraintSet.BOTTOM,
                                                dip2PxI(8F)
                                            )
                                            applyTo((bindingList[questionIndex] as? LayoutTestChooseBinding)?.constraint)
                                        }
                                        if (!hasScore) {
                                            score = 0F
                                        }
                                        correctnessList.add(
                                            when {
                                                !hasScore -> "2"
                                                shouldCorrect == actuallyCorrect -> "1"
                                                actuallyCorrect == 0 -> "2"
                                                else -> "3"
                                            }
                                        )
                                    }
                                    4 -> {
                                        val cardView =
                                            (bindingList[questionIndex] as? LayoutTestGeneralBinding)?.cardView
                                        cardView?.getChildAt(0)?.isEnabled = false
                                        cardView?.getChildAt(0)?.isClickable = false
                                        cardView?.getChildAt(0)?.isLongClickable = false
                                        var correctness = 2
                                        for (child in online.children) {
                                            val correctAnswer = child.text
                                            if (correctness != 1) {
                                                when {
                                                    userAnswerList[questionIndex].userAnswer[0] == correctAnswer -> {
                                                        correctness = 1
                                                        score += online.children[0].score
                                                        cardView?.setCardBackgroundColor(
                                                            getResColor(
                                                                R.color.colorTestCorrect
                                                            )
                                                        )
                                                    }
                                                    online.children[0].exactMatch == true -> {
                                                        correctness = 2
                                                        cardView?.setCardBackgroundColor(
                                                            getResColor(
                                                                R.color.colorTestWrong
                                                            )
                                                        )
                                                    }
                                                    else -> {
                                                        correctness = 4
                                                        cardView?.setCardBackgroundColor(
                                                            getResColor(
                                                                R.color.colorTestHalf
                                                            )
                                                        )
                                                        cardView?.setOnClickListener {
                                                            BottomDialog().create(this@TestActivity)
                                                                .apply {
                                                                    setTitle(R.string.scoring)
                                                                    setContent(R.string.ask_answer_correct)
                                                                    setButton1(R.string.correct) {
                                                                        scoreList[questionIndex] += questionList[questionIndex].children[0].score
                                                                        correctnessList[questionIndex] =
                                                                            "1"
                                                                        cardView.setCardBackgroundColor(
                                                                            getResColor(R.color.colorTestCorrect)
                                                                        )
                                                                        refreshView(
                                                                            questionIndex,
                                                                            questionButton
                                                                        )
                                                                        it.setOnClickListener(null)
                                                                        it.isClickable = false
                                                                        close()
                                                                    }
                                                                    setButton2(R.string.wrong) {
                                                                        correctnessList[questionIndex] =
                                                                            "2"
                                                                        cardView.setCardBackgroundColor(
                                                                            getResColor(R.color.colorTestWrong)
                                                                        )
                                                                        refreshView(
                                                                            questionIndex,
                                                                            questionButton
                                                                        )
                                                                        it.setOnClickListener(null)
                                                                        it.isClickable = false
                                                                        close()
                                                                    }
                                                                    setButton3(android.R.string.cancel) {
                                                                        close()
                                                                    }
                                                                    show()
                                                                }
                                                        }
                                                        resultBinding.resultNoPointsGroup
                                                            .addView(questionButton)
                                                    }
                                                }
                                            }
                                        }
                                        layoutAnswerBinding.answer.text =
                                            online.children[online.children.indices.random()].text
                                        layoutAnswerBinding.change.visibility = View.VISIBLE
                                        layoutAnswerBinding.change.setOnClickListener {
                                            layoutAnswerBinding.answer.text =
                                                online.children[online.children.indices.random()].text
                                        }
                                        (bindingList[questionIndex] as? LayoutTestGeneralBinding)?.constraint?.addView(
                                            layoutAnswerBinding.root
                                        )
                                        ConstraintSet().apply {
                                            clone((bindingList[questionIndex] as? LayoutTestGeneralBinding)?.constraint)
                                            connect(
                                                R.id.answerLayout,
                                                ConstraintSet.TOP,
                                                R.id.cardView,
                                                ConstraintSet.BOTTOM,
                                                dip2PxI(8F)
                                            )
                                            applyTo((bindingList[questionIndex] as? LayoutTestGeneralBinding)?.constraint)
                                        }
                                        correctnessList.add(correctness.toString())
                                    }
                                }
                                scoreList.add(score)
                                var correctness = correctnessList[questionIndex]
                                val originalCorrectness = correctness
                                var total = 0F
                                if (originalCorrectness.length > 1) {
                                    correctness = when {
                                        originalCorrectness.contains('4') -> "4"
                                        originalCorrectness.contains('1') && originalCorrectness.contains(
                                            '2'
                                        ) -> "3"
                                        originalCorrectness.contains('1') -> "1"
                                        else -> "2"
                                    }
                                }
                                total += scoreList[questionIndex]
                                scoreText += "${scoreList[questionIndex]}(${
                                    getResString(
                                        when (correctness) {
                                            "1" -> R.string.correct
                                            "2" -> R.string.wrong
                                            "3" -> R.string.half_correct
                                            "4" -> R.string.no_points
                                            else -> R.string.unknown
                                        }
                                    )
                                })${if (questionIndex != scoreList.size - 1) " + " else " = $total"}"
                                if (originalCorrectness == "2" || originalCorrectness == "3") {
                                    resultBinding.resultWrongGroup
                                        .addView(questionButton)
                                }
                            }
                            resultBinding.resultScoreText.text = scoreText
                            if (resultBinding.resultWrongGroup.childCount > 0) {
                                resultBinding.resultWrong.visibility = View.VISIBLE
                            }
                            if (resultBinding.resultNoPointsGroup.childCount > 0) {
                                resultBinding.resultNoPoints.visibility = View.VISIBLE
                            } else {
                                resultBinding.submit.isEnabled = true
                                resultBinding.submit.setOnClickListener {
                                    val result = Result()
                                    result.question = JSON.toJSONString(questionList)
                                    result.correctnessList = correctnessList
                                    if (result.save()) {
                                        Snackbar.make(
                                            binding.root,
                                            resources.getString(R.string.upload_success, result.id),
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                        it.visibility = View.GONE
                                    }
                                }
                            }
                            bindingList.add(resultBinding)
                            adapter.submitted = true
                            adapter.notifyDataSetChanged()
                            binding.viewPager.currentItem = bindingList.size - 1
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    private fun randomSort() {
        val originalList = bindingList.toMutableList()
        for (i in bindingList.indices) {
            while (true) {
                val random = (0 until bindingList.size).random()
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
            bindingList[i] = originalList[positionList[i]]
        }
    }

    private fun recoveryList() {
        if (bindingList.size == positionList.size) {
            val originalList = bindingList.toMutableList()
            val originalPositionList = positionList.toMutableList()
            for (i in bindingList.indices) {
                positionList[i] = i
                bindingList[originalPositionList[i]] = originalList[i]
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
                onSubmitClick()
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