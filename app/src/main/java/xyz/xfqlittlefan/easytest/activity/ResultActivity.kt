package xyz.xfqlittlefan.easytest.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import xyz.xfqlittlefan.easytest.data.Question
import xyz.xfqlittlefan.easytest.data.Result
import xyz.xfqlittlefan.easytest.util.UtilClass.getSelectionColor
import xyz.xfqlittlefan.easytest.util.UtilClass.getGson
import xyz.xfqlittlefan.easytest.util.UtilClass.getStateContent
import xyz.xfqlittlefan.easytest.util.UtilClass.parseQuestion

class ResultActivity : BaseActivity() {
    @OptIn(ExperimentalAnimationApi::class)
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
                                                                .fillMaxWidth()
                                                        ) {
                                                            MarkdownText(
                                                                modifier = Modifier.padding(10.dp),
                                                                markdown = parseQuestion(question).first,
                                                                style = MaterialTheme.typography.subtitle1
                                                            )
                                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                                if (question.type == 1) {
                                                                    question.options.forEachIndexed { i, option ->
                                                                        Row(
                                                                            modifier = Modifier
                                                                                .fillMaxWidth()
                                                                                .clickable { }
                                                                                .padding(15.dp)
                                                                        ) {
                                                                            val color = getSelectionColor(question, i)
                                                                            if (question.maxSelecting != null) {
                                                                                Checkbox(
                                                                                    checked = (question.userAnswer[i] == "1"),
                                                                                    onCheckedChange = null,
                                                                                    colors = CheckboxDefaults.colors(
                                                                                        checkedColor = color.first ?: MaterialTheme.colors.secondary,
                                                                                        uncheckedColor = color.second ?: MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                                                                    )
                                                                                )
                                                                            } else {
                                                                                RadioButton(
                                                                                    selected = (question.userAnswer[i] == "1"),
                                                                                    onClick = null,
                                                                                    colors = RadioButtonDefaults.colors(
                                                                                        selectedColor = color.first ?: MaterialTheme.colors.secondary,
                                                                                        unselectedColor = color.second ?: MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                                                                    )
                                                                                )
                                                                            }
                                                                            MarkdownText(
                                                                                modifier = Modifier.padding(start = 5.dp),
                                                                                markdown = option.text
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            Spacer(Modifier.height(10.dp))
                                                            var expanded by remember { mutableStateOf(false) }
                                                            Column {
                                                                TextButton(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    onClick = { expanded = !expanded },
                                                                    shape = RoundedCornerShape(0.dp)
                                                                ) {
                                                                    Text(stringResource(if (expanded) R.string.collapse_details else R.string.expand_details))
                                                                }
                                                                Column {
                                                                    AnimatedVisibility(visible = expanded) {
                                                                        Text("哈哈哈")
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