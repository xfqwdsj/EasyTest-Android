package com.xfq.easytest

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.xfq.bottomdialog.BottomDialog
import com.xfq.easytest.MyClass.INSET_TOP
import com.xfq.easytest.MyClass.setInset
import com.xfq.easytest.databinding.ActivitySelectQuestionBankBinding
import okhttp3.*
import java.io.IOException

class SelectQuestionBankActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectQuestionBankBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectQuestionBankBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setInset(INSET_TOP, binding.toolbar)

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
                            binding.recyclerView.layoutManager = LinearLayoutManager(this@SelectQuestionBankActivity)
                            binding.recyclerView.adapter = QuestionBankAdapter(json) { item: QuestionBank, isLast: Boolean -> onItemClicked(item, isLast) }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    private fun onItemClicked(item: QuestionBank, isLast: Boolean) {
        if (isLast) {
            Intent(this, TestActivity::class.java).apply {
                putExtra("url", item.url)
                startActivity(this)
            }
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
