package com.xfq.easytest

import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.alibaba.fastjson.JSON
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.xfq.easytest.MyClass.INSET_TOP
import com.xfq.easytest.MyClass.setInset
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
        binding.appbar.setInset(INSET_TOP)

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

            for ((position, question) in questionList.withIndex()) {
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
                                })
                            })
                        }
                    }
                    2 -> {
                        view.findViewById<ScrollView>(R.id.chooseQuestionLayout).visibility = View.VISIBLE
                        markwon.setMarkdown(view.findViewById(R.id.chooseQuestion), question.question)
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
                            }
                            buttonList.add(button)
                            view.findViewById<LinearLayout>(R.id.chooseGroup).addView(MaterialCardView(this).apply {
                                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                                    setMargins(MyClass.dip2PxI(5F), MyClass.dip2PxI(5F), MyClass.dip2PxI(5F), MyClass.dip2PxI(5F))
                                }
                                id = i
                                cardElevation = 0F
                                addView(button)
                            })
                        }
                    }
                    3 -> {
                        view.findViewById<ScrollView>(R.id.chooseQuestionLayout).visibility = View.VISIBLE
                        markwon.setMarkdown(view.findViewById(R.id.chooseQuestion), question.question)
                        val buttonList: MutableList<CheckBox> = ArrayList()
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
                            }
                            buttonList.add(button)
                            view.findViewById<LinearLayout>(R.id.chooseGroup).addView(MaterialCardView(this).apply {
                                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                                    setMargins(MyClass.dip2PxI(5F), MyClass.dip2PxI(5F), MyClass.dip2PxI(5F), MyClass.dip2PxI(5F))
                                }
                                id = i
                                cardElevation = 0F
                                addView(button)
                            })
                        }
                    }
                    4 -> {
                        view.findViewById<ScrollView>(R.id.generalQuestionLayout).visibility = View.VISIBLE
                        markwon.setMarkdown(view.findViewById(R.id.generalQuestion), question.question)
                        view.findViewById<EditText>(R.id.generalEdit).apply {
                            setText(question.userAnswer[0])
                        }
                    }
                    else -> {
                        finish()
                        return
                    }
                }
                view.setInset(MyClass.INSET_BOTTOM)
                viewList.add(view)
            }
            for (i in viewList.indices) {
                positionList.add(i)
            }

            val view =
                layoutInflater.inflate(R.layout.layout_test, LinearLayout(this), true).apply {
                    findViewById<ScrollView>(R.id.resultLayout).visibility = View.VISIBLE
                }

            //viewList.add(view)
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