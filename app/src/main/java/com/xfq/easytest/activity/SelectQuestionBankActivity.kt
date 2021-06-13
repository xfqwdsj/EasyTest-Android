package com.xfq.easytest.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.xfq.bottomdialog.BottomDialog
import com.xfq.easytest.QuestionBank
import com.xfq.easytest.R
import com.xfq.easytest.activity.base.BaseActivity
import com.xfq.easytest.databinding.ActivitySelectQuestionBankBinding
import com.xfq.easytest.util.ActivityMap
import com.xfq.easytest.util.QuestionBankAdapter
import okhttp3.*
import rikka.recyclerview.fixEdgeEffect
import java.io.IOException

class SelectQuestionBankActivity : BaseActivity() {
    private lateinit var binding: ActivitySelectQuestionBankBinding

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectQuestionBankBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setAppBar(binding.appbar, binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.recyclerView.fixEdgeEffect(false)
        binding.recyclerView.borderViewDelegate.setBorderVisibilityChangedListener { top, _, _, _ ->
            binding.appbar.isRaised = !top
        }
        binding.progress.setVisibilityAfterHide(View.GONE)
        ActivityMap.addActivity(this)
        if (intent.getStringArrayListExtra("urlList") != null) {
            get(
                intent.getStringArrayListExtra("urlList")!!,
                if (intent.getIntegerArrayListExtra("index") != null) intent.getIntegerArrayListExtra(
                    "index"
                )!! else ArrayList()
            )
        }
    }

    fun get(urlList: List<String>, index: ArrayList<Int>) {
        runOnUiThread {
            binding.progress.show()
        }
        val questionList: MutableList<QuestionBank> = ArrayList()
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
                                var cacheList: MutableList<QuestionBank> = ArrayList()
                                for (`object` in JSON.parseArray(
                                    response.body?.string(),
                                    QuestionBank::class.java
                                )) {
                                    cacheList.add(`object`)
                                }
                                for (currentIndex in index) {
                                    cacheList = cacheList[currentIndex].children.toMutableList()
                                }
                                questionList.addAll(cacheList)
                                if (i == urlList.size - 1) {
                                    runOnUiThread {
                                        binding.progress.hide()
                                        binding.recyclerView.layoutManager =
                                            LinearLayoutManager(this@SelectQuestionBankActivity)
                                        binding.recyclerView.adapter =
                                            QuestionBankAdapter(
                                                questionList,
                                                this@SelectQuestionBankActivity,
                                                index
                                            ) { item: QuestionBank ->
                                                onItemClicked(item)
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
        } else if (item.itemId == R.id.refresh) {
            if (intent.getStringArrayListExtra("urlList") != null) {
                get(
                    intent.getStringArrayListExtra("urlList")!!,
                    if (intent.getIntegerArrayListExtra("index") != null) intent.getIntegerArrayListExtra(
                        "index"
                    )!! else ArrayList()
                )
            }
        }
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.refresh_menu, menu)
        return true
    }
}
