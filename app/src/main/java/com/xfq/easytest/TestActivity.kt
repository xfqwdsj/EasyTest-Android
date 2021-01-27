package com.xfq.easytest

import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.widget.addTextChangedListener
import com.alibaba.fastjson.JSON
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.xfq.bottomdialog.BottomDialog
import com.xfq.easytest.MyClass.INSET_TOP
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
    private lateinit var questionList: List<Question>
    private lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setInset(INSET_TOP, binding.toolbar)
        url = this.intent.getStringExtra("url").toString()
        val random = this.intent.getBooleanExtra("random", false)
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
                        questionList = questions
                        runOnUiThread {
                            setAdapter(random)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    private fun setAdapter(random: Boolean) {
        val viewList: MutableList<View> = ArrayList()
        for (position in questionList.indices) {
            val question = questionList[position]
            val view = LayoutInflater.from(this@TestActivity).inflate(R.layout.layout_test, LinearLayout(this), true)
            val markwon = Markwon.builder(this@TestActivity).apply {
                usePlugin(StrikethroughPlugin.create())
                usePlugin(TablePlugin.create(this@TestActivity))
                usePlugin(TaskListPlugin.create(this@TestActivity))
                usePlugin(HtmlPlugin.create())
                usePlugin(ImagesPlugin.create())
                usePlugin(LinkifyPlugin.create())
            }.build()
            if (question is Question.FillBankQuestion) {
                if (question.question.replace("&%{", "").replace("&}", "").count { it == '{' } == question.question.replace("&%{", "").replace("&}", "").count { it == '}' } && question.question.replace("&%{", "").replace("&}", "").count { it == '{' } != 0) {
                    view.findViewById<ScrollView>(R.id.fillBankQuestionLayout).visibility = View.VISIBLE
                    view.findViewById<FlexboxLayout>(R.id.fillBankEdit).foregroundGravity
                    var questionText = ""
                    val textArray = question.question.split("}").toMutableList()
                    var bankNumber = 0
                    for (i in textArray.indices) {
                        if (textArray[i] != "") {
                            if (textArray[i].last() == '&') {
                                questionText += textArray[i].substring(0, textArray[i].length - 1).replace("&%{", "%{") + "}"
                            } else {
                                val number = bankNumber
                                val editText = TextInputEditText(this)
                                editText.hint = textArray[i].substring(textArray[i].indexOf("%{") + 2)
                                editText.minEms = 3
                                editText.gravity = Gravity.CENTER
                                editText.setText((questionList[position] as Question.FillBankQuestion).answer[number].userAnswer)
                                editText.addTextChangedListener {
                                    (questionList[position] as Question.FillBankQuestion).answer[number].userAnswer = it.toString()
                                }
                                questionText += textArray[i].replace("%{", " <u>**[  ") + "  ]**</u> "
                                view.findViewById<FlexboxLayout>(R.id.fillBankEdit).addView(editText)
                                bankNumber++
                            }
                        }
                    }
                    markwon.setMarkdown(view.findViewById(R.id.fillBankQuestion), questionText)
                }
            } else if (question is Question.SingleChooseQuestion) {
                view.findViewById<ScrollView>(R.id.singleChooseQuestionLayout).visibility = View.VISIBLE
                markwon.setMarkdown(view.findViewById(R.id.singleQuestion), question.question)
                for (i in question.options.indices) {
                    val button = RadioButton(this)
                    button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
                    markwon.setMarkdown(button, question.options[i].option)
                    button.isSelected = (questionList[position] as Question.SingleChooseQuestion).options[i].userSelected
                    button.setOnCheckedChangeListener { _, isChecked ->
                        (questionList[position] as Question.SingleChooseQuestion).options[i].userSelected = isChecked
                    }
                    button.setOnClickListener {
                        if (it.isSelected) {
                            it.isSelected = false
                        }
                    }
                    view.findViewById<RadioGroup>(R.id.singleGroup).addView(button)
                }
            } else if (question is Question.MultipleChooseQuestion) {
                view.findViewById<ScrollView>(R.id.multipleChooseQuestionLayout).visibility = View.VISIBLE
                markwon.setMarkdown(view.findViewById(R.id.multipleQuestion), question.question)
                for (i in question.options.indices) {
                    val button = CheckBox(this)
                    button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
                    markwon.setMarkdown(button, question.options[i].option)
                    button.isSelected = (questionList[position] as Question.MultipleChooseQuestion).options[i].userSelected
                    button.id = i
                    button.setOnCheckedChangeListener { _, isChecked ->
                        (questionList[position] as Question.MultipleChooseQuestion).options[i].userSelected = isChecked
                        var checked = 0
                        val buttonList: MutableList<CheckBox> = ArrayList()
                        for (j in question.options.indices) {
                            if (question.options[j].userSelected) {
                                checked++
                            } else {
                                buttonList.add(view.findViewById(j))
                            }
                        }
                        for (uncheckButton in buttonList) {
                            uncheckButton.isEnabled = !(checked == question.maxSelecting)
                        }
                    }
                    view.findViewById<LinearLayout>(R.id.multipleGroup).addView(button)
                }
            } else if (question is Question.GeneralQuestion) {
                view.findViewById<ScrollView>(R.id.generalQuestionLayout).visibility = View.VISIBLE
                markwon.setMarkdown(view.findViewById(R.id.generalQuestion), question.question)
                view.findViewById<EditText>(R.id.generalEdit).setText((questionList[position] as Question.GeneralQuestion).userAnswer)
                view.findViewById<EditText>(R.id.generalEdit).addTextChangedListener {
                    (questionList[position] as Question.GeneralQuestion).userAnswer = it.toString()
                }
            } else {
                finish()
                return
            }
            viewList.add(view)
        }
        if (random) {
            randomSort(viewList)
        }
        binding.viewPager.adapter = TestPagerAdaper(viewList, this)
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

    private fun submit() {
        var score = 0F
        var scoreAbolished = false
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
                        for (i in questionList.indices) {
                            var thisScore = 0F
                            val local = questionList[i]
                            val online = questions[i]
                            if (
                                    local is Question.FillBankQuestion &&
                                    online is Question.FillBankQuestion &&
                                    !scoreAbolished &&
                                    local.answer.size == online.answer.size
                            ) {
                                for (j in local.answer.indices) {
                                    if (
                                            local.answer[j].answer == online.answer[j].answer &&
                                            local.answer[j].answer == local.answer[j].userAnswer &&
                                            local.answer[j].score == online.answer[j].score &&
                                            local.answer[j].exactMatch == online.answer[j].exactMatch
                                    ) {
                                        thisScore += local.answer[j].score
                                    } else if (
                                            local.answer[j].answer != online.answer[j].answer ||
                                            local.answer[j].score != online.answer[j].score ||
                                            local.answer[j].exactMatch != online.answer[j].exactMatch
                                    ) {
                                        scoreAbolished = true
                                    }
                                }
                            } else if (
                                    local is Question.SingleChooseQuestion &&
                                    online is Question.SingleChooseQuestion &&
                                    !scoreAbolished &&
                                    local.options.size == online.options.size
                            ) {
                                for (j in local.options.indices) {
                                    if (
                                            local.options[j].isCorrect == online.options[j].isCorrect &&
                                            local.options[j].score == online.options[j].score &&
                                            local.options[j].userSelected
                                    ) {
                                        thisScore += local.options[j].score
                                    } else if (
                                            local.options[j].isCorrect != online.options[j].isCorrect ||
                                            local.options[j].score != online.options[j].score
                                    ) {
                                        scoreAbolished = true
                                    }
                                }
                            } else if (local is Question.MultipleChooseQuestion &&
                                    online is Question.MultipleChooseQuestion &&
                                    !scoreAbolished &&
                                    local.options.size == online.options.size
                            ) {
                                if (local.scoreType == online.scoreType) {
                                    var hasScore = true
                                    for (j in local.options.indices) {
                                        if (
                                                local.options[j].isCorrect == online.options[j].isCorrect &&
                                                local.options[j].score == online.options[j].score
                                        ) {
                                            when (local.scoreType) {
                                                1 -> {
                                                    if (local.options[j].userSelected) {
                                                        thisScore += local.options[j].score
                                                    }
                                                }
                                                2 -> {
                                                    if (local.options[j].isCorrect == local.options[j].userSelected && local.options[j].userSelected) {
                                                        thisScore += local.options[j].score
                                                    } else if (local.options[j].isCorrect != local.options[j].userSelected && local.options[j].userSelected) {
                                                        hasScore = false
                                                    }
                                                }
                                            }
                                        } else {
                                            scoreAbolished = true
                                        }
                                    }
                                    if (!hasScore) {
                                        thisScore = 0F
                                    }
                                } else {
                                    scoreAbolished = true
                                }
                            } else if (local is Question.GeneralQuestion && online is Question.GeneralQuestion && !scoreAbolished) {
                                if (local.exactMatch == online.exactMatch && local.score == online.score && local.answer == online.answer) {
                                    for (answer in local.answer) {
                                        if (local.userAnswer == answer && local.exactMatch) {
                                            thisScore += local.score
                                        }
                                    }
                                } else {
                                    scoreAbolished = true
                                }
                            } else {
                                finish()
                            }
                            score += thisScore
                        }
                        if (scoreAbolished) {
                            score = 0F
                        }
                        runOnUiThread {
                            Snackbar.make(binding.root, score.toString(), Snackbar.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    private fun <T> swap(list: MutableList<T>, i: Int, j: Int) {
        val temp = list[i]
        list[i] = list[j]
        list[j] = temp
    }

    private fun <T> randomSort(list: MutableList<T>) {
        val length = list.size
        for (i in length downTo 1) {
            val random = Random()
            val randomInt = random.nextInt(i)
            swap(list, randomInt, i - 1)
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
        return true
    }
}