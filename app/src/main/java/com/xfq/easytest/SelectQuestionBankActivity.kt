package com.xfq.easytest

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.preference.PreferenceManager
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


        val urlList = PreferenceManager.getDefaultSharedPreferences(this).getString("custom_source", "")!!.split("\n").toMutableList()
        urlList.add("https://xfqwdsj.gitee.io/easy-test/question-bank-index.json")
        binding.refresh.post {
            binding.refresh.isRefreshing = true
        }
        binding.refresh.setOnRefreshListener {
            get(urlList)
        }
        get(urlList)
    }

    fun get(urlList: List<String>) {
        val json: MutableList<QuestionBank> = ArrayList()
        for (i in urlList.indices) {
            val url = urlList[i]
            if (url != "") {
                val request = Request.Builder()
                        .url(url)
                        .removeHeader("User-Agent")
                        .addHeader("User-Agent", WebView(this).settings.userAgentString)
                        .build()
                OkHttpClient().newCall(request).enqueue(object : Callback {
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
                                for (`object` in JSON.parseArray(response.body?.string(), QuestionBank::class.java)) {
                                    json.add(`object`)
                                }
                                if (i == urlList.size - 1) {
                                    runOnUiThread {
                                        binding.recyclerView.layoutManager = LinearLayoutManager(this@SelectQuestionBankActivity)
                                        binding.recyclerView.adapter = QuestionBankAdapter(json) { item: QuestionBank -> onItemClicked(item) }
                                        binding.refresh.post {
                                            binding.refresh.isRefreshing = false
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                })
            }
        }
    }

    private fun onItemClicked(item: QuestionBank) {
        if (item.url != "") {
            Intent(this, TestActivity::class.java).apply {
                putExtra("url", item.url)
                putExtra("random", item.random)
                startActivity(this)
            }
            finish()
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
