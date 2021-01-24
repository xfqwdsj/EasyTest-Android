package com.xfq.easytest

import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.xfq.bottomdialog.BottomDialog
import com.xfq.easytest.MyClass.INSERT_TOP
import com.xfq.easytest.MyClass.setInset
import kotlinx.android.synthetic.main.activity_select_question_bank.*
import okhttp3.*
import java.io.IOException

class SelectQuestionBankActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_question_bank)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setInset(INSERT_TOP, toolbar)

        val url = "https://xfqwdsj.gitee.io/easy-test/question-bank-index.json"

        val client = OkHttpClient()
        val request = Request.Builder()
                .url(url)
                .removeHeader("User-Agent")
                .addHeader("User-Agent", WebView(this).settings.userAgentString)
                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    BottomDialog().create(this@SelectQuestionBankActivity).apply {
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
                        val json = JSON.parseArray(response.body?.string(), QuestionBank::class.java)
                        runOnUiThread {
                            recyclerView.layoutManager = LinearLayoutManager(this@SelectQuestionBankActivity)
                            recyclerView.adapter = QuestionBankAdapter(json, 0) { item: QuestionBank -> onItemClicked(item) }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    private fun onItemClicked(item: QuestionBank) {

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return false
    }
}
