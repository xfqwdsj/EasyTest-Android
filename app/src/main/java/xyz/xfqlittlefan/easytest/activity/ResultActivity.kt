package xyz.xfqlittlefan.easytest.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import xyz.xfqlittlefan.easytest.activity.ui.theme.Green700
import xyz.xfqlittlefan.easytest.activity.ui.theme.Red700
import xyz.xfqlittlefan.easytest.data.Question
import xyz.xfqlittlefan.easytest.data.Result
import xyz.xfqlittlefan.easytest.util.UtilClass.getCorrectnessColor
import xyz.xfqlittlefan.easytest.util.UtilClass.getGson
import xyz.xfqlittlefan.easytest.util.UtilClass.getSelectionColor
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
                                backgroundColor = MaterialTheme.colors.background.copy(alpha = 0.95f),
                                contentPadding = rememberInsetsPaddingValues(insets = LocalWindowInsets.current.statusBars),
                                elevation = 0.dp
                            )
                        },
                        bottomBar = {
                            Spacer(Modifier.navigationBarsHeight())
                        }
                    ) { contentPadding ->
                        LazyColumn(contentPadding = contentPadding) {
                            @Composable
                            fun AnswerItem(type: Int, color: Color? = null, content: @Composable () -> Unit) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Spacer(Modifier.width(15.dp))
                                    Box(Modifier.padding(vertical = 15.dp)) {
                                        when (type) {
                                            1 -> {
                                                Checkbox(
                                                    checked = (color != null),
                                                    onCheckedChange = null,
                                                    colors = CheckboxDefaults.colors(checkedColor = color ?: MaterialTheme.colors.secondary)
                                                )
                                            }
                                            2 -> {
                                                RadioButton(
                                                    selected = (color != null),
                                                    onClick = null,
                                                    colors = RadioButtonDefaults.colors(selectedColor = color ?: MaterialTheme.colors.secondary)
                                                )
                                            }
                                            3 -> {
                                                Box(
                                                    Modifier
                                                        .padding(2.dp)
                                                        .size(20.dp)
                                                        .background(color = color ?: MaterialTheme.colors.onSurface.copy(alpha = 0.6f), shape = RoundedCornerShape(2.dp))
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.width(15.dp))
                                    content()
                                }
                            }

                            @Composable
                            fun AnswerItem(type: Int, text: String, color: Color? = null) {
                                AnswerItem(type, color) {
                                    Spacer(Modifier.width(5.dp))
                                    MarkdownText(markdown = text, modifier = Modifier.padding(vertical = 15.dp))
                                    Spacer(Modifier.width(5.dp))
                                }
                            }

                            fun initItems(result: Result) {
                                if (result.isCorrect) {
                                    val questionList = getGson().fromJson<List<Question>>(result.question, object : TypeToken<List<Question>>() {}.type)
                                    val stateMap = getGson().fromJson<Map<Int, Map<Int, Map<Int, Float>>>>(result.state, object : TypeToken<Map<Int, Map<Int, Map<Int, Float>>>>() {}.type)
                                    // TODO: val url = result.url
                                    // TODO: val setId = result.setId
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
                                                            Spacer(Modifier.height(10.dp))
                                                            MarkdownText(
                                                                modifier = Modifier.padding(horizontal = 10.dp),
                                                                markdown = parseQuestion(question).first,
                                                                style = MaterialTheme.typography.subtitle1
                                                            )
                                                            Spacer(Modifier.height(10.dp))
                                                            Card(
                                                                modifier = Modifier
                                                                    .padding(horizontal = 10.dp)
                                                                    .fillMaxWidth(),
                                                                backgroundColor = MaterialTheme.colors.onBackground.copy(alpha = 0.05f),
                                                                shape = RoundedCornerShape(10.dp),
                                                                elevation = 0.dp
                                                            ) {
                                                                Column(modifier = Modifier.fillMaxWidth()) {
                                                                    val list = when (question.type) {
                                                                        1 -> question.options
                                                                        2 -> question.userAnswer
                                                                        else -> listOf<String>()
                                                                    }
                                                                    val type = if (question.type == 1) {
                                                                        if (question.maxSelecting != null) 1 else 2
                                                                    } else if (question.type == 2) 3 else 114514

                                                                    list.forEachIndexed { i, _ ->
                                                                        val color = if (question.type == 1) {
                                                                            if (question.userAnswer[i] == "1") getSelectionColor(question, i) else null
                                                                        } else if (question.type == 2) getCorrectnessColor(stateMap, index, i) else Red700
                                                                        AnswerItem(type = type, text = if (question.type == 1) question.options[i].text else question.userAnswer[i], color = color)
                                                                    }
                                                                }
                                                            }
                                                            Spacer(Modifier.height(10.dp))
                                                            var expanded by remember { mutableStateOf(false) }
                                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                                TextButton(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    onClick = { expanded = !expanded },
                                                                    shape = RoundedCornerShape(0.dp)
                                                                ) {
                                                                    Text(stringResource(if (expanded) R.string.collapse_details else R.string.expand_details))
                                                                }
                                                                AnimatedVisibility(visible = expanded) {
                                                                    Column(modifier = Modifier.fillMaxWidth()) {
                                                                        Spacer(Modifier.height(10.dp))
                                                                        Card(
                                                                            modifier = Modifier
                                                                                .padding(horizontal = 10.dp)
                                                                                .fillMaxWidth(),
                                                                            backgroundColor = MaterialTheme.colors.onBackground.copy(alpha = 0.05f),
                                                                            shape = RoundedCornerShape(10.dp),
                                                                            elevation = 0.dp
                                                                        ) {
                                                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                                                val list = when (question.type) {
                                                                                    1 -> question.options
                                                                                    2 -> question.userAnswer
                                                                                    else -> listOf<String>()
                                                                                }
                                                                                val type = if (question.type == 1) {
                                                                                    if (question.maxSelecting != null) 1 else 2
                                                                                } else if (question.type == 2) 3 else 114514

                                                                                list.forEachIndexed { i, _ ->
                                                                                    if (question.type == 1 && question.options[i].isCorrect) {
                                                                                        val color = if (question.userAnswer[i] == "1") Green700 else null
                                                                                        AnswerItem(type = type, text = if (question.type == 1) question.options[i].text else question.userAnswer[i], color = color)
                                                                                    } else if (question.type == 2) {
                                                                                        AnswerItem(type = type, color = Green700) {
                                                                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                                                                question.answers[i].text.forEach {
                                                                                                    Row(modifier = Modifier.fillMaxWidth()) {
                                                                                                        Spacer(Modifier.width(5.dp))
                                                                                                        MarkdownText(
                                                                                                            markdown = it,
                                                                                                            modifier = Modifier
                                                                                                                .padding(vertical = 15.dp)
                                                                                                        )
                                                                                                        Spacer(Modifier.width(5.dp))
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                        Spacer(Modifier.height(10.dp))
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
                                        initItems(Result().apply {
                                            question = it["question"] as String?
                                            state = it["state"] as String?
                                            setId = it["idMap"] as String?
                                            url = it["url"] as String?
                                        })
                                    }.dispose()
                                } else {
                                    val result = LitePal.find<Result>(id.toLongOrDefault(-1))
                                    if (result != null) {
                                        initItems(result)
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