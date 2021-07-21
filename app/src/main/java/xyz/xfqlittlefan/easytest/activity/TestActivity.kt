package xyz.xfqlittlefan.easytest.activity

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.*
import android.webkit.WebView
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children
import androidx.transition.TransitionManager
import androidx.viewpager.widget.ViewPager
import com.alibaba.fastjson.JSON
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.transition.MaterialFadeThrough
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import okhttp3.*
import rikka.core.util.ClipboardUtils
import rikka.widget.borderview.BorderNestedScrollView
import xyz.xfqlittlefan.easytest.data.Question
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.data.Result
import xyz.xfqlittlefan.easytest.activity.base.BaseActivity
import xyz.xfqlittlefan.easytest.databinding.ActivityTestBinding
import xyz.xfqlittlefan.easytest.databinding.LayoutNestedScrollViewBinding
import xyz.xfqlittlefan.easytest.util.ActivityMap
import xyz.xfqlittlefan.easytest.util.MyClass.dip2PxF
import xyz.xfqlittlefan.easytest.util.MyClass.dip2PxI
import xyz.xfqlittlefan.easytest.util.MyClass.getResColor
import xyz.xfqlittlefan.easytest.util.MyClass.getResString
import xyz.xfqlittlefan.easytest.util.MyClass.setMarginTop
import xyz.xfqlittlefan.easytest.adapter.TestPagerAdapter
import xyz.xfqlittlefan.easytest.widget.BlurBehindDialogBuilder
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class TestActivity : BaseActivity() {
    private lateinit var binding: ActivityTestBinding
    private lateinit var url: String
    private lateinit var userAnswerList: List<Question>
    private lateinit var adapter: TestPagerAdapter
    private lateinit var markwon: Markwon
    private val positionList: MutableList<Int> = ArrayList()
    private val viewList: MutableList<View> = ArrayList()

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
                    BlurBehindDialogBuilder(this@TestActivity)
                        .setTitle(R.string.failed)
                        .setMessage(resources.getString(R.string.error, e))
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->
                            finish()
                        }
                        .show()
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
            val view: View?
            when (question.type) {
                1 -> {
                    view = layoutInflater.inflate(R.layout.layout_test, LayoutNestedScrollViewBinding.inflate(layoutInflater).root)
                    markwon.setMarkdown(view.findViewById(R.id.question), question.question)
                    val buttonList: MutableList<CheckBox> = ArrayList()
                    for (i in question.options.indices) {
                        val layoutId = if (question.maxSelecting == null) R.layout.layout_test_single_choose_checkbox else R.layout.layout_test_multiple_choose_checkbox
                        val button = (LayoutInflater.from(this).inflate(layoutId, LinearLayout(this), false) as CheckBox).apply {
                            markwon.setMarkdown(this, question.options[i].text)
                            isChecked = when (userAnswerList[position].userAnswer[i]) {
                                "1" -> true
                                else -> false
                            }
                            visibility = View.VISIBLE
                            setOnCheckedChangeListener { _, isChecked ->
                                userAnswerList[position].userAnswer[i] = when (isChecked) {
                                    true -> "1"
                                    false -> "2"
                                }
                                if (question.maxSelecting == null) {
                                    if (isChecked) {
                                        for (otherButton in buttonList) {
                                            if (!(otherButton === this)) {
                                                otherButton.isChecked = false
                                            }
                                        }
                                    }
                                } else {
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
                        }
                        buttonList.add(button)
                        view.findViewById<LinearLayout>(R.id.container)
                            .addView(MaterialCardView(this).apply {
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                ).apply {
                                    if (i != 0) {
                                        setMarginTop(dip2PxI(5F))
                                    }
                                }
                                id = i
                                cardElevation = 0F
                                radius = dip2PxF(10F)
                                setContentPadding(
                                    dip2PxI(5F),
                                    dip2PxI(5F),
                                    dip2PxI(5F),
                                    dip2PxI(5F)
                                )
                                addView(button)
                            })
                    }
                }
                2 -> {  //填空题
                    view = layoutInflater.inflate(R.layout.layout_test, LayoutNestedScrollViewBinding.inflate(layoutInflater).root)
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
                    markwon.setMarkdown(view.findViewById(R.id.question), questionText)
                    if (bank.isEmpty()) {
                        view.findViewById<LinearLayout>(R.id.container).addView(MaterialCardView(this).apply {
                            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                            id = 0
                            cardElevation = 0F
                            radius = dip2PxF(10F)
                            addView(TextInputEditText(this@TestActivity).apply {
                                setHint(R.string.your_answer)
                                gravity = Gravity.TOP or Gravity.START
                                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                                setText(userAnswerList[position].userAnswer[0])
                                addTextChangedListener(object : TextWatcher {
                                    override fun afterTextChanged(s: Editable?) {
                                        userAnswerList[position].userAnswer[0] = s.toString()
                                    }

                                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                                })
                            })
                        })
                    } else {
                        for (i in bank.indices) {  //遍历“空”
                            view.findViewById<LinearLayout>(R.id.container).addView(MaterialCardView(this).apply {
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                ).apply {
                                    if (i != 0) {
                                        setMarginTop(dip2PxI(5F))
                                    }
                                }
                                id = i
                                cardElevation = 0F
                                radius = dip2PxF(10F)
                                addView(TextInputEditText(this@TestActivity).apply {
                                    hint = bank[i]
                                    gravity = Gravity.CENTER
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    )
                                    inputType = InputType.TYPE_CLASS_TEXT
                                    setText(userAnswerList[position].userAnswer[i])
                                    addTextChangedListener(object : TextWatcher {
                                        override fun afterTextChanged(s: Editable?) {
                                            userAnswerList[position].userAnswer[i] = s.toString()
                                        }

                                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                                    })
                                })
                            })
                        }
                    }
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
        fun operateControls(position: Int) {
            when (position) {
                0 -> {
                    binding.toolbar.menu.findItem(R.id.previous).isEnabled = false
                    binding.toolbar.menu.findItem(R.id.next).isEnabled = true
                }
                viewList.size - 1 -> {
                    binding.toolbar.menu.findItem(R.id.previous).isEnabled = true
                    binding.toolbar.menu.findItem(R.id.next).isEnabled = false
                }
                else -> {
                    binding.toolbar.menu.findItem(R.id.previous).isEnabled = true
                    binding.toolbar.menu.findItem(R.id.next).isEnabled = true
                }
            }

        }
        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            private var currentPosition: Int? = null

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (currentPosition == null) currentPosition = position
                val newPosition: Int = if (position < currentPosition!!) position else if (positionOffsetPixels == 0) position else position + 1
                (viewList[currentPosition!!] as BorderNestedScrollView).borderVisibilityChangedListener = null
                (viewList[newPosition] as BorderNestedScrollView).setBorderVisibilityChangedListener { top, _, _, _ ->
                    binding.appbar.isRaised = !top
                }
                binding.appbar.isRaised = !(viewList[newPosition] as BorderNestedScrollView).isShowingTopBorder || !(viewList[currentPosition!!] as BorderNestedScrollView).isShowingTopBorder
            }

            override fun onPageSelected(position: Int) {
                currentPosition = position
                binding.appbar.isRaised = !(viewList[position] as BorderNestedScrollView).isShowingTopBorder
                operateControls(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        binding.viewPager.adapter = adapter
        operateControls(binding.viewPager.currentItem)
        binding.start.setOnClickListener {
            val transform = MaterialFadeThrough()
            TransitionManager.beginDelayedTransition(binding.root, transform)
            binding.start.visibility = View.GONE
            binding.viewPager.visibility = View.VISIBLE
            binding.toolbar.menu.findItem(R.id.previous).isVisible = true
            binding.toolbar.menu.findItem(R.id.next).isVisible = true
            binding.toolbar.menu.findItem(R.id.submit).isVisible = true
        }
        binding.start.isEnabled = true
    }

    private fun onSubmitClick() {
        val scoreList: MutableList<Float> = ArrayList()
        val correctnessList: MutableList<String> = ArrayList()
        /*
         * 1 -> 正确
         * 2 -> 错误
         * 3 -> 半对
         * 4 -> 待定
         */
        var questionList: MutableList<Question> = ArrayList()
        val request = Request.Builder()
            .url(url)
            .removeHeader("User-Agent")
            .addHeader("User-Agent", WebView(this).settings.userAgentString)
            .build()
        val view = layoutInflater.inflate(R.layout.layout_test_result, LayoutNestedScrollViewBinding.inflate(layoutInflater).root)
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    BlurBehindDialogBuilder(this@TestActivity)
                        .setTitle(R.string.failed)
                        .setMessage(resources.getString(R.string.error, e))
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->
                            finish()
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
                            originalCorrectness.contains('3') -> "3"
                            originalCorrectness.contains('1') -> "1"
                            else -> "2"
                        }
                    }
                    total += scoreList[index]
                    scoreText += scoreList[index]
                    scoreText += "("
                    scoreText += getResString(
                        when (correctness) {
                            "1" -> R.string.correct
                            "2" -> R.string.wrong
                            "3" -> R.string.half_correct
                            "4" -> R.string.no_points
                            else -> 114514
                        }
                    )
                    scoreText += ")"
                    scoreText += if (index != scoreList.size - 1) " + " else " = $total"
                }
                view.findViewById<TextView>(R.id.resultScoreText).text = scoreText
                correctness = correctnessList[questionIndex][bankIndex].toString()
                view.findViewById<ChipGroup>(R.id.resultNoPointsGroup).removeView(button)
                if (view.findViewById<ChipGroup>(R.id.resultNoPointsGroup).childCount == 0) {
                    view.findViewById<CardView>(R.id.resultNoPoints).visibility = View.GONE
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
                if (correctness == "2" || correctness == "3") {
                    view.findViewById<CardView>(R.id.resultWrong).visibility = View.VISIBLE
                    view.findViewById<ChipGroup>(R.id.resultWrongGroup).addView(button)
                    sort(view.findViewById(R.id.resultWrongGroup))
                }
            }

            fun sort(view: ChipGroup) {
                val children = view.children.toMutableList()
                view.removeAllViews()
                children.sortBy { it.id }
                children.forEach { view.addView(it) }
            }

            @SuppressLint("ClickableViewAccessibility")
            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    try {
                        questionList = JSON.parseArray(response.body?.string(), Question::class.java)
                        runOnUiThread {
                            recoveryList()
                            binding.toolbar.menu.findItem(R.id.result).isVisible = true
                            binding.toolbar.menu.findItem(R.id.submit).isVisible = false
                            var scoreText = ""
                            var total = 0F
                            for (questionIndex in questionList.indices) {
                                questionList[questionIndex].userAnswer = userAnswerList[questionIndex].userAnswer
                                var score = 0F
                                val layout = layoutInflater.inflate(R.layout.layout_test_answer, LinearLayout(this@TestActivity), false)
                                val online = questionList[questionIndex]
                                val questionButton = Chip(this@TestActivity).apply {
                                    setOnClickListener {
                                        binding.viewPager.currentItem = questionIndex
                                    }
                                    text = resources.getString(R.string.question_number, questionIndex + 1)
                                    tag = questionIndex
                                }
                                when (online.type) {
                                    1 -> {
                                        var hasScore = true
                                        var shouldCorrect = 0
                                        var actuallyCorrect = 0
                                        var answer = ""
                                        for (j in online.options.indices) {
                                            val cardView = viewList[questionIndex].findViewById<CardView>(j)
                                            cardView.getChildAt(0).isEnabled = false
                                            if (online.maxSelecting == null) {
                                                if (userAnswerList[questionIndex].userAnswer[j] == "1") {
                                                    score += online.options[j].score
                                                }
                                                if (userAnswerList[questionIndex].userAnswer[j] == "1" && online.options[j].isCorrect == true) {
                                                    actuallyCorrect++
                                                } else if (userAnswerList[questionIndex].userAnswer[j] == "1" && online.options[j].isCorrect == false) {
                                                    cardView.setCardBackgroundColor(getResColor(R.color.colorTestWrong))
                                                }
                                            } else {
                                                when (online.scoreType) {
                                                    1 -> {
                                                        if (userAnswerList[questionIndex].userAnswer[j] == "1") {
                                                            score += online.options[j].score
                                                        }
                                                    }
                                                    2 -> {
                                                        when {
                                                            online.options[j].isCorrect == true && userAnswerList[questionIndex].userAnswer[j] == "1" -> {
                                                                score += online.options[j].score
                                                            }
                                                            online.options[j].isCorrect == false && userAnswerList[questionIndex].userAnswer[j] == "1" -> {
                                                                hasScore = false
                                                            }
                                                        }
                                                    }
                                                }
                                                if (online.options[j].isCorrect == true) {
                                                    shouldCorrect++
                                                }
                                                if (userAnswerList[questionIndex].userAnswer[j] == "1" && online.options[j].isCorrect == true) {
                                                    actuallyCorrect++
                                                } else if (userAnswerList[questionIndex].userAnswer[j] == "1" && online.options[j].isCorrect == false) {
                                                    cardView.setCardBackgroundColor(getResColor(R.color.colorTestWrong))
                                                }
                                            }
                                            if (online.options[j].isCorrect == true) {
                                                cardView.setCardBackgroundColor(getResColor(R.color.colorTestCorrect))
                                                answer += "${online.options[j].text}; "
                                            }
                                        }
                                        layout.findViewById<TextView>(R.id.answer).text = answer.substring(0, answer.length - 2)
                                        viewList[questionIndex].findViewById<ConstraintLayout>(R.id.constraint).addView(layout)
                                        ConstraintSet().apply {
                                            clone(viewList[questionIndex].findViewById<ConstraintLayout>(R.id.constraint))
                                            connect(R.id.answerLayout, ConstraintSet.TOP, R.id.container, ConstraintSet.BOTTOM)
                                            connect(R.id.answerLayout, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dip2PxI(16F))
                                            connect(R.id.answerLayout, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dip2PxI(16F))
                                            connect(R.id.answerLayout, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, dip2PxI(16F))
                                            applyTo(viewList[questionIndex].findViewById(R.id.constraint))
                                        }
                                        if (!hasScore) {
                                            score = 0F
                                        }
                                        val correctness = if (online.maxSelecting == null) {
                                            when (actuallyCorrect) {
                                                1 -> "1"
                                                else -> "2"
                                            }
                                        } else {
                                            when {
                                                !hasScore -> "2"
                                                shouldCorrect == actuallyCorrect -> "1"
                                                actuallyCorrect == 0 -> "2"
                                                else -> "3"
                                            }
                                        }
                                        correctnessList.add(correctness)
                                    }
                                    2 -> {
                                        var correctness = ""
                                        var answer = ""
                                        for (answerIndex in online.answers.indices) {
                                            val bankButton = Chip(this@TestActivity).apply {
                                                setOnClickListener {
                                                    binding.viewPager.currentItem = questionIndex
                                                }
                                                text = resources.getString(R.string.question_number, questionIndex + 1)
                                                tag = questionIndex
                                            }
                                            val correctAnswers = online.answers[answerIndex].text
                                            answer += "$correctAnswers; "
                                            val cardView = viewList[questionIndex].findViewById<CardView>(answerIndex)
                                            cardView.getChildAt(0).isEnabled = false
                                            cardView.getChildAt(0).isClickable = false
                                            cardView.getChildAt(0).isLongClickable = false
                                            var correct = -1
                                            var maxScore = 0F
                                            for ((index, correctAnswer) in correctAnswers.withIndex()) {
                                                if (userAnswerList[questionIndex].userAnswer[answerIndex] == correctAnswer) {
                                                    correct = index
                                                }
                                                if (online.answers[answerIndex].score[index] > maxScore) maxScore = online.answers[answerIndex].score[index]
                                            }
                                            when {
                                                correct != -1 -> {
                                                    score += online.answers[answerIndex].score[correct]
                                                    cardView.setCardBackgroundColor(getResColor(R.color.colorTestCorrect))
                                                    correctness += "1"
                                                }
                                                online.answers[answerIndex].exactMatch -> {
                                                    cardView.setCardBackgroundColor(getResColor(R.color.colorTestWrong))
                                                    correctness += "2"
                                                    view.findViewById<ChipGroup>(R.id.resultWrongGroup).addView(bankButton)
                                                }
                                                else -> {
                                                    cardView.setCardBackgroundColor(getResColor(R.color.colorTestHalf))
                                                    cardView.setOnClickListener {
                                                        val inputLayout = layoutInflater.inflate(R.layout.layout_dialog_input, LinearLayout(this@TestActivity), false) as LinearLayout
                                                        val input = inputLayout.getChildAt(0) as TextInputEditText
                                                        input.hint = getString(R.string.your_score, maxScore.toString())
                                                        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

                                                        input.addTextChangedListener(object : TextWatcher {
                                                            private var text = ""
                                                            private var selection = 0
                                                            private var record = true

                                                            fun set() {
                                                                input.setText(text)
                                                                input.setSelection(selection)
                                                            }

                                                            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                                                                text = p0.toString()
                                                                if (record) selection = input.selectionStart else record = true
                                                            }

                                                            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                                                                if (p0.toString() == "") {
                                                                    return
                                                                }
                                                                if (p0.toString().toFloatOrNull() == null) {
                                                                    record = false
                                                                    set()
                                                                    return
                                                                }
                                                                if (p0.toString().toFloat() < 0 || p0.toString().toFloat() > maxScore) {
                                                                    record = false
                                                                    set()
                                                                }
                                                            }

                                                            override fun afterTextChanged(p0: Editable?) {}
                                                        })
                                                        BlurBehindDialogBuilder(this@TestActivity)     //赋分窗口
                                                            .setTitle(R.string.scoring)
                                                            .setView(inputLayout)
                                                            .setPositiveButton(R.string.correct) { _: DialogInterface, _: Int ->
                                                                val userScore = (input.text.toString().toFloatOrNull() ?: 0).toFloat()
                                                                scoreList[questionIndex] += userScore
                                                                val finalCorrectness = if (userScore == maxScore) "1" else if (userScore == 0F) "2" else if (userScore < maxScore) "3" else "5"
                                                                val questionCorrectnessList = correctnessList[questionIndex].split("")   //当前题目的正确情况
                                                                var questionCorrectnessString = ""
                                                                for (correctnessIndex in 1..(questionCorrectnessList.size - 2)) {   //因为用""分割出来的列表头尾都是空的
                                                                    questionCorrectnessString += (if (correctnessIndex - 1 == answerIndex) finalCorrectness else questionCorrectnessList[correctnessIndex])
                                                                }
                                                                correctnessList[questionIndex] = questionCorrectnessString
                                                                viewList[questionIndex].findViewById<CardView>(answerIndex).setCardBackgroundColor(getResColor(when (finalCorrectness) {
                                                                    "1" -> R.color.colorTestCorrect
                                                                    "2" -> R.color.colorTestWrong
                                                                    "3" -> R.color.colorTestHalf
                                                                    else -> 114514
                                                                }))
                                                                refreshView(questionIndex, answerIndex, bankButton)
                                                                it.setOnClickListener(null)
                                                                it.isClickable = false
                                                            }
                                                            .setNegativeButton(R.string.wrong) { _: DialogInterface, _: Int ->
                                                                val questionCorrectnessList = correctnessList[questionIndex].split("")
                                                                var questionCorrectnessString = ""
                                                                for (correctnessIndex in 1..(questionCorrectnessList.size - 2)) {
                                                                    questionCorrectnessString += if (correctnessIndex - 1 == answerIndex) "2" else questionCorrectnessList[correctnessIndex]
                                                                }
                                                                correctnessList[questionIndex] = questionCorrectnessString
                                                                viewList[questionIndex].findViewById<CardView>(answerIndex).setCardBackgroundColor(getResColor(R.color.colorTestWrong))
                                                                refreshView(questionIndex, answerIndex, bankButton)
                                                                it.setOnClickListener(null)
                                                                it.isClickable = false
                                                            }
                                                            .setNeutralButton(android.R.string.cancel) { _: DialogInterface, _: Int -> }
                                                            .show()
                                                    }
                                                    view.findViewById<ChipGroup>(R.id.resultNoPointsGroup).addView(bankButton)
                                                    correctness += "4"
                                                }
                                            }
                                        }
                                        layout.findViewById<TextView>(R.id.answer).text = answer.substring(0, answer.length - 2)
                                        viewList[questionIndex].findViewById<ConstraintLayout>(R.id.constraint).addView(layout)
                                        ConstraintSet().apply {
                                            clone(viewList[questionIndex].findViewById<ConstraintLayout>(R.id.constraint))
                                            connect(R.id.answerLayout, ConstraintSet.TOP, R.id.container, ConstraintSet.BOTTOM)
                                            connect(R.id.answerLayout, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dip2PxI(16F))
                                            connect(R.id.answerLayout, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dip2PxI(16F))
                                            connect(R.id.answerLayout, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, dip2PxI(16F))
                                            applyTo(viewList[questionIndex].findViewById(R.id.constraint))
                                        }
                                        correctnessList.add(correctness)
                                    }
                                }
                                scoreList.add(score)
                                var correctness = correctnessList[questionIndex]
                                val originalCorrectness = correctness
                                if (originalCorrectness.length > 1) {
                                    correctness = when {
                                        originalCorrectness.contains('4') -> "4"
                                        originalCorrectness.contains('1') && originalCorrectness.contains('2') -> "3"
                                        originalCorrectness.contains('3') -> "3"
                                        originalCorrectness.contains('1') -> "1"
                                        else -> "2"
                                    }
                                }
                                total += scoreList[questionIndex]
                                scoreText += scoreList[questionIndex]
                                scoreText += "("
                                scoreText += getResString(
                                    when (correctness) {
                                        "1" -> R.string.correct
                                        "2" -> R.string.wrong
                                        "3" -> R.string.half_correct
                                        "4" -> R.string.no_points
                                        else -> 114514
                                    }
                                )
                                scoreText += ")"
                                scoreText += if (questionIndex != questionList.size - 1) " + " else " = $total"
                                if ((originalCorrectness == "2" || originalCorrectness == "3") && online.type == 1) {
                                    view.findViewById<ChipGroup>(R.id.resultWrongGroup).addView(questionButton)
                                }
                            }
                            view.findViewById<TextView>(R.id.resultScoreText).text = scoreText
                            if (view.findViewById<ChipGroup>(R.id.resultWrongGroup).childCount > 0) {
                                view.findViewById<CardView>(R.id.resultWrong).visibility = View.VISIBLE
                            }
                            if (view.findViewById<ChipGroup>(R.id.resultNoPointsGroup).childCount > 0) {
                                view.findViewById<CardView>(R.id.resultNoPoints).visibility = View.VISIBLE
                            } else {
                                view.findViewById<Button>(R.id.submit).isEnabled = true
                                view.findViewById<Button>(R.id.submit).setOnClickListener {
                                    val result = Result()
                                    result.question = JSON.toJSONString(questionList)
                                    result.correctnessList = correctnessList
                                    if (result.save()) {
                                        Snackbar.make(binding.root, resources.getString(R.string.upload_success, result.id), Snackbar.LENGTH_LONG)
                                            .setAction(android.R.string.copy) {
                                                ClipboardUtils.put(this@TestActivity, result.id.toString())
                                            }
                                            .show()
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
        return when (item.itemId) {
            android.R.id.home -> {
                exit()
                true
            }
            R.id.previous -> {
                binding.viewPager.setCurrentItem(binding.viewPager.currentItem - 1, true)
                true
            }
            R.id.next -> {
                binding.viewPager.setCurrentItem(binding.viewPager.currentItem + 1, true)
                true
            }
            R.id.result -> {
                binding.viewPager.setCurrentItem(viewList.size - 1, true)
                true
            }
            R.id.submit -> {
                onSubmitClick()
                true
            }
            else -> false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.test_menu, menu)
        return true
    }

    override fun onBackPressed() {
        exit()
    }

    private fun exit() {
        BlurBehindDialogBuilder(this)
            .setTitle(R.string.exit)
            .setMessage(R.string.ask_exit)
            .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->
                finish()
            }
            .setNegativeButton(android.R.string.cancel) { _: DialogInterface, _: Int -> }
            .show()
    }
}