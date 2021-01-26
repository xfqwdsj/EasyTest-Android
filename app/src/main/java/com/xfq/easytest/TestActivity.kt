package com.xfq.easytest

import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.alibaba.fastjson.JSON
import com.xfq.bottomdialog.BottomDialog
import com.xfq.easytest.MyClass.INSET_TOP
import com.xfq.easytest.MyClass.setInset
import com.xfq.easytest.databinding.ActivityTestBinding
import okhttp3.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class TestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setInset(INSET_TOP, binding.toolbar)
        val url = this.intent.getStringExtra("url")
        val random = this.intent.getBooleanExtra("random", false)
        val questions: MutableList<Question> = ArrayList()

        if (url != null) {
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
                            if (random) {
                                randomSort(questions)
                            }
                            runOnUiThread {
                                binding.viewPager.adapter = TestPagerAdapter(questions, this@TestActivity)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            })
        } else {
            finish()
        }
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
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return false
    }
}