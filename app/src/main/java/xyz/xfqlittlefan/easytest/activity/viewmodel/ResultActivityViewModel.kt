package xyz.xfqlittlefan.easytest.activity.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cn.leancloud.LCObject
import cn.leancloud.LCQuery
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable
import org.litepal.LitePal
import org.litepal.extension.find
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.data.Question
import xyz.xfqlittlefan.easytest.data.Result
import xyz.xfqlittlefan.easytest.util.UtilClass.getGson
import xyz.xfqlittlefan.easytest.util.UtilClass.getQuestionStateMap
import xyz.xfqlittlefan.easytest.util.UtilClass.getResString
import xyz.xfqlittlefan.easytest.util.UtilClass.getResultItemTitle
import xyz.xfqlittlefan.easytest.util.UtilClass.parseQuestion

const val CORRECTNESS = 0
const val TITLE = 1
const val QUESTION = 2
const val TYPE = 3
const val ITEMS = 4
const val DETAILS_ITEMS = 5

class ResultActivityViewModel : ViewModel() {
    var id by mutableStateOf(Pair(Any(), false))
        private set

    var display by mutableStateOf(listOf<Map<Int, Any>>())
        private set

    var dialog by mutableStateOf(false)
    var message by mutableStateOf("")
        private set

    val compositeDisposable = CompositeDisposable()

    fun init(id: String?, uploaded: Boolean) {
        if (uploaded && id != null) {
            val query = LCQuery<LCObject>("EasyTestResult")
            compositeDisposable.add(query.getInBackground(id).subscribe({
                initItems(Result().apply {
                    question = it["question"] as String?
                    state = it["state"] as String?
                    setId = it["idMap"] as String?
                    url = it["url"] as String?
                })
            }, {
                dialog = true
                message = it.localizedMessage ?: it.toString()
            }))
            this.id = Pair(id.toString(), uploaded)
        } else if (!uploaded && id?.toLongOrNull() != null) {
            val result = LitePal.find<Result>(id.toLong())
            if (result != null) {
                initItems(result)
            } else {
                dialog = true
                message = getResString(R.string.not_found)
            }
            this.id = Pair(id.toLong(), uploaded)
        } else {
            dialog = true
            message = getResString(R.string.unknown_error)
        }
    }

    private fun initItems(result: Result) {
        if (result.isCorrect) {
            val questionList = getGson().fromJson<List<Question>>(result.question, object : TypeToken<List<Question>>() {}.type)
            val stateMap = getGson().fromJson<Map<Int, Map<Int, Map<Int, Float>>>>(result.state, object : TypeToken<Map<Int, Map<Int, Map<Int, Float>>>>() {}.type)
            if (questionList.size == stateMap.size) {
                questionList.forEachIndexed { index, question ->
                    val list = display.toMutableList()
                    val map = mutableMapOf<Int, Any>()
                    map[CORRECTNESS] = getQuestionStateMap(stateMap)[index]?.get(CORRECTNESS)?.toInt() ?: 2
                    map[TITLE] = getResultItemTitle(question, stateMap, index)
                    map[QUESTION] = parseQuestion(question).first
                    map[TYPE] = if (question.type == 1) {
                        if (question.maxSelecting != null) 1 else 2
                    } else if (question.type == 2) 3 else 114514
                    val items = mutableListOf<Pair<Int, String>>()
                    val detailsItems = mutableListOf<Pair<Int, String>>()
                    when (question.type) {
                        1 -> {
                            question.options.forEachIndexed { i, option ->
                                var number = 0
                                if (option.isCorrect) number += 0b10
                                if (question.userAnswer[i] == "1") number += 0b1
                                items.add(Pair(number, option.text))
                                if (option.isCorrect) detailsItems.add(Pair(if (question.userAnswer[i] == "1") 1 else 0, option.text))
                            }
                        }
                        2 -> {
                            question.userAnswer.forEachIndexed { i, answer ->
                                items.add(Pair(stateMap[index]?.get(i)?.get(CORRECTNESS)?.toInt() ?: 2, answer))
                                detailsItems.add(Pair(1, getGson().toJson(question.answers[i].text)))
                            }
                        }
                    }
                    map[ITEMS] = items.toList()
                    map[DETAILS_ITEMS] = detailsItems.toList()
                    list.add(map.toMap())
                    display = list
                }
            }
        } else {
            dialog = true
            message = getResString(R.string.unknown_error)
        }
    }
}