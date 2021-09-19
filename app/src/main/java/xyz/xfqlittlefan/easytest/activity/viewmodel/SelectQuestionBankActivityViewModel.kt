package xyz.xfqlittlefan.easytest.activity.viewmodel

import android.content.Context
import android.webkit.WebView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import okhttp3.*
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.data.QuestionSet
import xyz.xfqlittlefan.easytest.util.UtilClass
import xyz.xfqlittlefan.easytest.util.UtilClass.getGson
import xyz.xfqlittlefan.easytest.util.UtilClass.getResString
import xyz.xfqlittlefan.easytest.util.UtilClass.getUA
import java.io.IOException

class SelectQuestionBankActivityViewModel : ViewModel() {
    var progressing by mutableStateOf(false)

    var items by mutableStateOf(listOf<QuestionSet.Set>())
        private set

    var indexList by mutableStateOf(listOf<Int>())
        private set

    var dialog by mutableStateOf(false)
    var message by mutableStateOf("")
        private set

    fun init(
        indexList: ArrayList<Int>,
        urlList: ArrayList<String>
    ) {
        items = listOf()
        progressing = true
        var startedCount = 0
        var completedCount = 0
        val size = mutableListOf<Int>()
        this.indexList = indexList
        urlList.forEach { _ -> size.add(0) }
        urlList.forEachIndexed { i, url ->
            if (url.isNotEmpty()) {
                startedCount++
                try {
                    OkHttpClient().newCall(
                        Request.Builder()
                            .url(url)
                            .removeHeader("User-Agent")
                            .addHeader("User-Agent", getUA())
                            .build()
                    ).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            completedCount++
                            progressing = false
                            message = getResString(R.string.error, e.localizedMessage ?: e.toString())
                            dialog = true
                        }

                        override fun onResponse(call: Call, response: Response) {
                            if (response.code == 200) {
                                val set = getGson().fromJson(response.body?.string(), QuestionSet::class.java)
                                val list = items.toMutableList()
                                var cacheList = mutableListOf<QuestionSet.Set>()
                                var position = 0
                                set.init()
                                set.url = url
                                cacheList.addAll(set.set)
                                indexList.forEach {
                                    cacheList = cacheList[it].children.toMutableList()
                                }
                                size[i] = cacheList.size
                                for (j in 0 until i) {
                                    position += size[j]
                                }
                                list.addAll(position, cacheList)
                                items = list
                                completedCount++
                                if (completedCount == startedCount) {
                                    progressing = false
                                }
                            }
                        }
                    })
                } catch (e: Throwable) {
                    e.printStackTrace()
                    message = getResString(R.string.error, e.localizedMessage ?: e.toString())
                    dialog = true
                }
            }
        }
    }
}