package xyz.xfqlittlefan.easytest.activity

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import okhttp3.*
import rikka.recyclerview.fixEdgeEffect
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.activity.base.BaseActivity
import xyz.xfqlittlefan.easytest.adapter.QuestionBankAdapter
import xyz.xfqlittlefan.easytest.data.QuestionSet
import xyz.xfqlittlefan.easytest.databinding.ActivitySelectQuestionBankBinding
import xyz.xfqlittlefan.easytest.util.ActivityMap
import xyz.xfqlittlefan.easytest.util.MyClass
import xyz.xfqlittlefan.easytest.util.MyClass.smoothScroll
import xyz.xfqlittlefan.easytest.widget.BlurBehindDialogBuilder
import java.io.IOException

class SelectQuestionBankActivity : BaseActivity() {
    private lateinit var binding: ActivitySelectQuestionBankBinding

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectQuestionBankBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setAppBar(binding.appBar, binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.recyclerView.fixEdgeEffect(false)
        binding.recyclerView.borderViewDelegate.setBorderVisibilityChangedListener { top, _, _, _ ->
            binding.appBar.isRaised = !top
        }
        binding.progress.setVisibilityAfterHide(View.GONE)
        ActivityMap.addActivity(this)
        val index = intent.getIntegerArrayListExtra("index") ?: ArrayList()
        val questionList: MutableList<QuestionSet.Set> = ArrayList()
        val adapter = QuestionBankAdapter(questionList, this@SelectQuestionBankActivity, index) { item: QuestionSet.Set ->
            onItemClicked(item)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this@SelectQuestionBankActivity)
        binding.recyclerView.adapter = adapter
        intent.getStringArrayListExtra("urlList")?.let {
            get(it, index)
        }
    }

    fun get(urlList: List<String>, index: ArrayList<Int>) {
        binding.progress.show()
        var completedCount = 0
        val size = mutableListOf<Int>()
        urlList.forEach { _ ->
            size.add(0)
        }
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
                        completedCount ++
                        runOnUiThread {
                            binding.progress.hide()
                            BlurBehindDialogBuilder(this@SelectQuestionBankActivity)
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
                                val set = Gson().fromJson(response.body?.string(), QuestionSet::class.java)
                                set.init()
                                set.setUrl(url)
                                var cacheList: MutableList<QuestionSet.Set> = ArrayList()
                                cacheList.addAll(set.set)
                                for (currentIndex in index) {
                                    cacheList = cacheList[currentIndex].children.toMutableList()
                                }
                                completedCount ++
                                size[i] = cacheList.size
                                var position = 0
                                for (j in 0 until i) {
                                    position += size[j]
                                }
                                runOnUiThread {
                                    (binding.recyclerView.adapter as QuestionBankAdapter).add(position, cacheList)
                                    binding.recyclerView.smoothScroll(0)
                                }
                                if (completedCount == urlList.size) {
                                    runOnUiThread {
                                        binding.progress.hide()
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                })
            } else {
                completedCount ++
            }
        }
    }

    private fun onItemClicked(item: QuestionSet.Set) {
        if (item.url != "" && item.url != null) {
            requestedOrientation = if (MyClass.getPreferences().getBoolean("enable_landscape", false)) {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
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
                super.onBackPressed()
                true
            }
            R.id.refresh -> {
                if (intent.getStringArrayListExtra("urlList") != null) {
                    (binding.recyclerView.adapter as QuestionBankAdapter).reset()
                    get(intent.getStringArrayListExtra("urlList")!!, intent.getIntegerArrayListExtra("index") ?: ArrayList())
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
