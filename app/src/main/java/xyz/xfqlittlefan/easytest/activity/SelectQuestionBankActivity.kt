package xyz.xfqlittlefan.easytest.activity

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.xfq.bottomdialog.BottomDialog
import xyz.xfqlittlefan.easytest.QuestionBank
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.activity.base.BaseActivity
import xyz.xfqlittlefan.easytest.databinding.ActivitySelectQuestionBankBinding
import xyz.xfqlittlefan.easytest.util.ActivityMap
import xyz.xfqlittlefan.easytest.util.QuestionBankAdapter
import okhttp3.*
import rikka.recyclerview.fixEdgeEffect
import xyz.xfqlittlefan.easytest.widget.BlurBehindDialogBuilder
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
        intent.getStringArrayListExtra("urlList")?.let {
            get(it, intent.getIntegerArrayListExtra("index") ?: ArrayList())
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
                            BlurBehindDialogBuilder(this@SelectQuestionBankActivity)
                                .setTitle(R.string.failed)
                                .setMessage(resources.getString(R.string.error, e))
                                .setCancelable(false)
                                .setPositiveButton(android.R.string.ok) { dialogInterface: DialogInterface, i: Int ->
                                    finish()
                                }
                                .show()
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
                                            QuestionBankAdapter(questionList, this@SelectQuestionBankActivity, index) { item: QuestionBank ->
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
        if (item.url != "" && item.url != null) {
            Intent(this, TestActivity::class.java).apply {
                putExtra("url", item.url)
                putExtra("random", item.random)
                startActivity(this)
            }
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.refresh -> {
                if (intent.getStringArrayListExtra("urlList") != null) {
                    get(
                        intent.getStringArrayListExtra("urlList")!!,
                        intent.getIntegerArrayListExtra("index") ?: ArrayList()
                    )
                }
                true
            }
            else -> false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.refresh_menu, menu)
        return true
    }
}
