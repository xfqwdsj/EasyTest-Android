package xyz.xfqlittlefan.easytest.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import cn.leancloud.LCObject
import cn.leancloud.LCQuery
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import com.google.gson.reflect.TypeToken
import dev.jeziellago.compose.markdowntext.MarkdownText
import okhttp3.internal.toLongOrDefault
import org.litepal.LitePal
import org.litepal.extension.find
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.activity.base.BaseActivity
import xyz.xfqlittlefan.easytest.activity.ui.theme.EasyTestTheme
import xyz.xfqlittlefan.easytest.activity.ui.theme.Green200
import xyz.xfqlittlefan.easytest.data.Question
import xyz.xfqlittlefan.easytest.data.Result
import xyz.xfqlittlefan.easytest.util.UtilClass.getGson
import xyz.xfqlittlefan.easytest.util.UtilClass.getQuestionStateMap
import xyz.xfqlittlefan.easytest.util.UtilClass.getStateContent
import xyz.xfqlittlefan.easytest.util.UtilClass.parseQuestion

class ResultActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            EasyTestTheme {
                ProvideWindowInsets {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text(stringResource(R.string.result)) },
                                navigationIcon = {
                                    IconButton(onClick = { super.onBackPressed() }) {
                                        Icon(Icons.Filled.ArrowBack, stringResource(R.string.back))
                                    }
                                },
                                backgroundColor = MaterialTheme.colors.background,
                                contentPadding = rememberInsetsPaddingValues(insets = LocalWindowInsets.current.statusBars)
                            )
                        },
                        bottomBar = {
                            Spacer(Modifier.navigationBarsHeight())
                        }
                    ) { contentPadding ->
                        LazyColumn(contentPadding = contentPadding) {
                            fun initItem(result: Result) {
                                if (result.isCorrect) {
                                    val questionList = getGson().fromJson<List<Question>>(result.question, object : TypeToken<List<Question>>() {}.type)
                                    val stateMap = getGson().fromJson<Map<Int, Map<Int, Map<Int, Float>>>>(result.state, object : TypeToken<Map<Int, Map<Int, Map<Int, Float>>>>() {}.type)
                                    val url = result.url
                                    val setId = result.setId
                                    if (questionList.size == stateMap.size) {
                                        questionList.forEachIndexed { index, question ->
                                            item {
                                                Card(
                                                    modifier = Modifier
                                                        .padding(10.dp)
                                                        .fillMaxWidth(),
                                                    shape = RoundedCornerShape(10.dp),
                                                    elevation = 0.dp
                                                ) {
                                                    Column {
                                                        val stateContent = getStateContent(question, stateMap, index)
                                                        Surface(color = stateContent.first, modifier = Modifier.fillMaxWidth()) {
                                                            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                                                Text(
                                                                    text = stateContent.second, modifier = Modifier
                                                                        .padding(5.dp)
                                                                        .fillMaxWidth(),
                                                                    style = MaterialTheme.typography.body2
                                                                )
                                                            }
                                                        }
                                                        Column(
                                                            modifier = Modifier
                                                                .padding(10.dp)
                                                                .fillMaxWidth()
                                                        ) {
                                                            MarkdownText(
                                                                markdown = parseQuestion(question).first,
                                                                style = MaterialTheme.typography.subtitle1
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            val id = intent.getStringExtra("id")
                            if (id != null) {
                                if (intent.getBooleanExtra("uploaded", false)) {
                                    val query = LCQuery<LCObject>("EasyTestResult")
                                    query.getInBackground(id).subscribe {
                                        initItem(Result().apply {
                                            question = it["question"] as String?
                                            state = it["state"] as String?
                                            setId = it["idMap"] as String?
                                            url = it["url"] as String?
                                        })
                                    }.dispose()
                                } else {
                                    val result = LitePal.find<Result>(id.toLongOrDefault(-1))
                                    if (result != null) {
                                        initItem(result)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}