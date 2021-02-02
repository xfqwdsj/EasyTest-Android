package com.xfq.easytest

import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.WindowCompat
import androidx.core.widget.addTextChangedListener
import com.alibaba.fastjson.JSON
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.xfq.bottomdialog.BottomDialog
import com.xfq.easytest.MyClass.INSET_TOP
import com.xfq.easytest.MyClass.dip2PxI
import com.xfq.easytest.MyClass.getResColor
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
    private lateinit var userAnswer: List<Question>
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
        val random = this.intent.getBooleanExtra("random", false)  //获取是否随机排序
        val questions: MutableList<Question> = ArrayList()  //预留一个题目列表 稍后进行解析

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
                        val json = JSON.parseArray(response.body?.string())  //把获取到的json字符串数组解析出来
                        for (i in 0 until json.size) {
                            val questionObject = json.getJSONObject(i)  //当前循环到的object
                            val type = questionObject.getInteger("type")  //获取type
                            if (type < 1 || type > 4) {  //排除非法type
                                continue
                            }
                            questionObject.remove("type")  //从object中把type移除
                            questions.add(JSON.parseObject(questionObject.toJSONString(), when (type) {
                                1 -> Question.FillBankQuestion::class.java
                                2 -> Question.SingleChooseQuestion::class.java
                                3 -> Question.MultipleChooseQuestion::class.java
                                4 -> Question.GeneralQuestion::class.java
                                else -> Question::class.java
                            }))
                        }
                        cleanQuestions(questions)  //保存一个用于存放用户答案的list: userAnswer
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
            val view = LayoutInflater.from(this).inflate(R.layout.layout_test, LinearLayout(this), true)
            val markwon = Markwon.builder(this).apply {
                usePlugin(StrikethroughPlugin.create())
                usePlugin(TablePlugin.create(this@TestActivity))
                usePlugin(TaskListPlugin.create(this@TestActivity))
                usePlugin(HtmlPlugin.create())
                usePlugin(ImagesPlugin.create())
                usePlugin(LinkifyPlugin.create())
            }.build()  //初始化Markwon
            when (question) {
                is Question.FillBankQuestion -> {  //填空题
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
                        val editText = TextInputEditText(this)
                        editText.hint = bank[i]
                        editText.minEms = 5
                        editText.gravity = Gravity.CENTER
                        editText.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        editText.setText((userAnswer[position] as Question.FillBankQuestion).answer[i].userAnswer)
                        editText.addTextChangedListener {
                            (userAnswer[position] as Question.FillBankQuestion).answer[i].userAnswer = it.toString()
                        }
                        cardView.addView(editText)
                        view.findViewById<FlexboxLayout>(R.id.fillBankEdit).addView(cardView)
                    }
                }
                is Question.SingleChooseQuestion -> {
                    view.findViewById<ScrollView>(R.id.chooseQuestionLayout).visibility = View.VISIBLE
                    markwon.setMarkdown(view.findViewById(R.id.chooseQuestion), question.question)
                    val buttonList: MutableList<CheckBox> = ArrayList()
                    for (i in question.options.indices) {
                        val cardView = MaterialCardView(this)
                        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        params.setMargins(dip2PxI(5F), dip2PxI(5F), dip2PxI(5F), dip2PxI(5F))
                        cardView.layoutParams = params
                        cardView.id = i
                        val button = LayoutInflater.from(this).inflate(R.layout.layout_for_test_single_choose, LinearLayout(this), false) as CheckBox
                        markwon.setMarkdown(button, question.options[i].option)
                        button.isChecked = (userAnswer[position] as Question.SingleChooseQuestion).options[i].userSelected
                        button.visibility = View.VISIBLE
                        button.setOnCheckedChangeListener { _, isChecked ->
                            (userAnswer[position] as Question.SingleChooseQuestion).options[i].userSelected = isChecked
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
                is Question.MultipleChooseQuestion -> {
                    view.findViewById<ScrollView>(R.id.chooseQuestionLayout).visibility = View.VISIBLE
                    markwon.setMarkdown(view.findViewById(R.id.chooseQuestion), question.question)
                    val buttonList: MutableList<CheckBox> = ArrayList()
                    for (i in question.options.indices) {
                        val cardView = MaterialCardView(this)
                        val cardParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        cardParams.setMargins(dip2PxI(5F), dip2PxI(5F), dip2PxI(5F), dip2PxI(5F))
                        cardView.layoutParams = cardParams
                        cardView.id = i
                        val button = MaterialCheckBox(this)
                        val buttonParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        buttonParams.setMargins(dip2PxI(5F), 0, dip2PxI(5F), 0)
                        button.layoutParams = buttonParams
                        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
                        markwon.setMarkdown(button, question.options[i].option)
                        button.isChecked = (userAnswer[position] as Question.MultipleChooseQuestion).options[i].userSelected
                        button.setOnCheckedChangeListener { _, isChecked ->
                            (userAnswer[position] as Question.MultipleChooseQuestion).options[i].userSelected = isChecked
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
                is Question.GeneralQuestion -> {
                    view.findViewById<ScrollView>(R.id.generalQuestionLayout).visibility = View.VISIBLE
                    markwon.setMarkdown(view.findViewById(R.id.generalQuestion), question.question)
                    view.findViewById<EditText>(R.id.generalEdit).setText((userAnswer[position] as Question.GeneralQuestion).userAnswer)
                    view.findViewById<EditText>(R.id.generalEdit).addTextChangedListener {
                        (userAnswer[position] as Question.GeneralQuestion).userAnswer = it.toString()
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
            randomSort(viewList)
        } else {
            for (i in viewList.indices) {
                positionList.add(i)
            }
        }
        binding.start.setOnClickListener {
            binding.viewPager.adapter = TestPagerAdapter(viewList, this)
            binding.tabLayout.setupWithViewPager(binding.viewPager)
            binding.start.isEnabled = false
            binding.start.visibility = View.GONE
            binding.toolbar.menu.findItem(R.id.submit).isVisible = true
            binding.root.removeView(binding.start)
        }
        binding.start.isEnabled = true
    }

    private fun submit() {
        val score: MutableList<Float> = ArrayList()
        val correctness: MutableList<Int> = ArrayList()
        /*
        1 -> 正确
        2 -> 错误
        3 -> 半对
        4 -> 待定
         */
        val questions: MutableList<Question> = ArrayList()
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

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    try {
                        val json = JSON.parseArray(response.body?.string())
                        for (i in 0 until json.size) {
                            val questionObject = json.getJSONObject(i)
                            val type = questionObject.getInteger("type")
                            questionObject.remove("type")
                            when (type) {
                                1 -> {
                                    val question = JSON.parseObject(questionObject.toJSONString(), Question.FillBankQuestion::class.java)
                                    questions.add(question)
                                }
                                2 -> {
                                    val question = JSON.parseObject(questionObject.toJSONString(), Question.SingleChooseQuestion::class.java)
                                    questions.add(question)
                                }
                                3 -> {
                                    val question = JSON.parseObject(questionObject.toJSONString(), Question.MultipleChooseQuestion::class.java)
                                    questions.add(question)
                                }
                                4 -> {
                                    val question = JSON.parseObject(questionObject.toJSONString(), Question.GeneralQuestion::class.java)
                                    questions.add(question)
                                }
                            }
                        }
                        for (i in questions.indices) {
                            var thisScore = 0F
                            when (val online = questions[i]) {
                                is Question.FillBankQuestion -> {
                                    var shouldCorrect = 0
                                    var actuallyCorrect = 0
                                    for (j in online.answer.indices) {
                                        val cardView = viewList[positionList[i]].findViewById<CardView>(j)
                                        cardView.getChildAt(0).isEnabled = false
                                        cardView.setOnClickListener {
                                            cardView.setCardBackgroundColor(getResColor(R.color.colorTestRight))
                                            (cardView.getChildAt(0) as EditText).addTextChangedListener { }
                                            (cardView.getChildAt(0) as EditText).setText((questions[i] as Question.FillBankQuestion).answer[j].answer)
                                        }
                                        if (online.answer[j].answer == (userAnswer[i] as Question.FillBankQuestion).answer[j].userAnswer) {
                                            thisScore += online.answer[j].score
                                            shouldCorrect++
                                            actuallyCorrect++
                                            cardView.setCardBackgroundColor(getResColor(R.color.colorTestRight))
                                        } else {
                                            shouldCorrect++
                                            cardView.setCardBackgroundColor(getResColor(R.color.colorTestWrong))
                                        }
                                    }
                                    correctness.add(when (actuallyCorrect) {
                                        shouldCorrect -> 1
                                        0 -> 2
                                        else -> 3
                                    })
                                }
                                is Question.SingleChooseQuestion -> {
                                    var actuallyCorrect = 0
                                    for (j in online.options.indices) {
                                        val cardView = viewList[positionList[i]].findViewById<CardView>(j)
                                        cardView.getChildAt(0).isEnabled = false
                                        if ((userAnswer[i] as Question.SingleChooseQuestion).options[j].userSelected) {
                                            thisScore += online.options[j].score
                                        }
                                        if ((userAnswer[i] as Question.SingleChooseQuestion).options[j].userSelected && online.options[j].isCorrect == (userAnswer[i] as Question.SingleChooseQuestion).options[j].userSelected) {
                                            actuallyCorrect++
                                        } else if ((userAnswer[i] as Question.SingleChooseQuestion).options[j].userSelected && online.options[j].isCorrect != (userAnswer[i] as Question.SingleChooseQuestion).options[j].userSelected) {
                                            cardView.setCardBackgroundColor(getResColor(R.color.colorTestWrong))
                                        }
                                        if (online.options[j].isCorrect) {
                                            cardView.setCardBackgroundColor(getResColor(R.color.colorTestRight))
                                        }
                                    }
                                    correctness.add(when (actuallyCorrect) {
                                        1 -> 1
                                        else -> 2
                                    })
                                }
                                is Question.MultipleChooseQuestion -> {
                                    var hasScore = true
                                    var shouldCorrect = 0
                                    var actuallyCorrect = 0
                                    for (j in online.options.indices) {
                                        val cardView = viewList[positionList[i]].findViewById<CardView>(j)
                                        cardView.getChildAt(0).isEnabled = false
                                        when (online.scoreType) {
                                            1 -> {
                                                if ((userAnswer[i] as Question.MultipleChooseQuestion).options[j].userSelected) {
                                                    thisScore += online.options[j].score
                                                }
                                            }
                                            2 -> {
                                                when {
                                                    online.options[j].isCorrect == (userAnswer[i] as Question.MultipleChooseQuestion).options[j].userSelected &&
                                                            (userAnswer[i] as Question.MultipleChooseQuestion).options[j].userSelected -> {
                                                        thisScore += online.options[j].score
                                                    }
                                                    online.options[j].isCorrect != (userAnswer[i] as Question.MultipleChooseQuestion).options[j].userSelected &&
                                                            (userAnswer[i] as Question.MultipleChooseQuestion).options[j].userSelected -> {
                                                        hasScore = false
                                                    }
                                                }
                                            }
                                        }
                                        if (online.options[j].isCorrect) {
                                            shouldCorrect++
                                        }
                                        if ((userAnswer[i] as Question.MultipleChooseQuestion).options[j].userSelected && online.options[j].isCorrect == (userAnswer[i] as Question.MultipleChooseQuestion).options[j].userSelected) {
                                            actuallyCorrect++
                                        } else if ((userAnswer[i] as Question.MultipleChooseQuestion).options[j].userSelected && online.options[j].isCorrect != (userAnswer[i] as Question.MultipleChooseQuestion).options[j].userSelected) {
                                            cardView.setCardBackgroundColor(getResColor(R.color.colorTestWrong))
                                        }
                                        if (online.options[j].isCorrect) {
                                            cardView.setCardBackgroundColor(getResColor(R.color.colorTestRight))
                                        }
                                    }
                                    if (!hasScore) {
                                        thisScore = 0F
                                        correctness.add(2)
                                    } else if (shouldCorrect == actuallyCorrect) {
                                        correctness.add(1)
                                    } else {
                                        correctness.add(3)
                                    }
                                }
                                is Question.GeneralQuestion -> {
                                    val cardView = viewList[positionList[i]].findViewById<CardView>(R.id.cardView)
                                    cardView.getChildAt(0).isEnabled = false
                                    cardView.setOnClickListener {
                                        cardView.setCardBackgroundColor(getResColor(R.color.colorTestRight))
                                        (cardView.getChildAt(0) as EditText).addTextChangedListener { }
                                        (cardView.getChildAt(0) as EditText).setText((questions[i] as Question.GeneralQuestion).answer[(questions[i] as Question.GeneralQuestion).answer.indices.random()])
                                    }
                                    var correct = 2
                                    for (answer in online.answer) {
                                        when {
                                            (userAnswer[i] as Question.GeneralQuestion).userAnswer == answer -> {
                                                thisScore += online.score
                                                if (correct != 1) {
                                                    correct = 1
                                                    cardView.setCardBackgroundColor(getResColor(R.color.colorTestRight))
                                                }
                                            }
                                            online.exactMatch -> {
                                                if (correct != 1) {
                                                    correct = 2
                                                    cardView.setCardBackgroundColor(getResColor(R.color.colorTestWrong))
                                                }
                                            }
                                            else -> {
                                                if (correct != 1) {
                                                    correct = 4
                                                    cardView.setCardBackgroundColor(getResColor(R.color.colorTestHalf))
                                                }
                                            }
                                        }
                                    }
                                    correctness.add(correct)
                                }
                            }
                            score.add(thisScore)
                        }
                        binding.toolbar.menu.findItem(R.id.submit).isVisible = false
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    private fun cleanQuestions(list: List<Question>) {
        for (i in list.indices) {
            when (list[i]) {
                is Question.FillBankQuestion -> {
                    for (j in (list[i] as Question.FillBankQuestion).answer.indices) {
                        (list[i] as Question.FillBankQuestion).answer[j].answer = ""
                        (list[i] as Question.FillBankQuestion).answer[j].score = 0F
                    }
                }
                is Question.SingleChooseQuestion -> {
                    for (j in (list[i] as Question.SingleChooseQuestion).options.indices) {
                        (list[i] as Question.SingleChooseQuestion).options[j].isCorrect = false
                        (list[i] as Question.SingleChooseQuestion).options[j].score = 0F
                    }
                }
                is Question.MultipleChooseQuestion -> {
                    for (j in (list[i] as Question.MultipleChooseQuestion).options.indices) {
                        (list[i] as Question.MultipleChooseQuestion).options[j].isCorrect = false
                        (list[i] as Question.MultipleChooseQuestion).options[j].score = 0F
                    }
                }
                is Question.GeneralQuestion -> {
                    for (j in (list[i] as Question.GeneralQuestion).answer.indices) {
                        (list[i] as Question.GeneralQuestion).answer[j] = ""
                    }
                    (list[i] as Question.GeneralQuestion).score = 0F
                }
            }
        }
        userAnswer = list
    }

    private fun <T> randomSort(list: MutableList<T>) {
        val originalList = list.toMutableList()
        for (i in list.indices) {
            while (true) {
                val random = (0 until list.size).random()
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
            list[positionList[i]] = originalList[i]
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