package xyz.xfqlittlefan.easytest.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.gson.reflect.TypeToken
import dev.jeziellago.compose.markdowntext.MarkdownText
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.activity.base.ComposeBaseActivity
import xyz.xfqlittlefan.easytest.activity.viewmodel.ResultActivityViewModel
import xyz.xfqlittlefan.easytest.util.UtilClass.getGson
import xyz.xfqlittlefan.easytest.util.UtilClass.getResultItemColor
import xyz.xfqlittlefan.easytest.util.UtilClass.getResultTitleBackGroundColor
import xyz.xfqlittlefan.easytest.widget.MaterialContainer
import xyz.xfqlittlefan.easytest.widget.TextDialog
import xyz.xfqlittlefan.easytest.widget.VerticalSpacer

class ResultActivity : ComposeBaseActivity() {
    private val viewModel by viewModels<ResultActivityViewModel>()

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.init(intent.getStringExtra("id"), intent.getBooleanExtra("uploaded", false))
        setContent {
            MaterialContainer(
                title = R.string.result,
                onBack = { super.onBackPressed() }
            ) { contentPadding ->
                LazyColumn(contentPadding = contentPadding) {
                    itemsIndexed(viewModel.display) { index, displayData ->
                        Card(
                            modifier = Modifier
                                .padding(
                                    start = 10.dp,
                                    top = 10.dp,
                                    end = 10.dp,
                                    bottom = if (index + 1 == viewModel.display.size) 10.dp else 0.dp
                                )
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            elevation = 0.dp
                        ) {
                            Column {
                                Surface(color = getResultTitleBackGroundColor(displayData.correctness, MaterialTheme.colors.isLight)) {
                                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                        Text(
                                            text = displayData.title,
                                            modifier = Modifier
                                                .padding(5.dp)
                                                .fillMaxWidth(),
                                            style = MaterialTheme.typography.body2
                                        )
                                    }
                                }
                                Column {
                                    var expanded by remember { mutableStateOf(false) }
                                    VerticalSpacer(size = 10.dp)
                                    MarkdownText(
                                        markdown = displayData.question,
                                        modifier = Modifier.padding(horizontal = 10.dp),
                                        style = MaterialTheme.typography.subtitle1
                                    )
                                    VerticalSpacer(size = 10.dp)
                                    Surface(
                                        modifier = Modifier
                                            .padding(horizontal = 10.dp)
                                            .fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colors.onBackground.copy(alpha = 0.05f)
                                    ) {
                                        Column {
                                            displayData.items.forEach {
                                                val number = it.first
                                                val string = it.second
                                                if (displayData.type == 1 || displayData.type == 2) {
                                                    val correctness = if (number shr 1 and 1 == 1) 1 else 2
                                                    val userSelected = number shr 0 and 1 == 1
                                                    AnswerItem(
                                                        type = displayData.type,
                                                        text = string,
                                                        color = if (userSelected) getResultItemColor(correctness) else null
                                                    )
                                                } else if (displayData.type == 3) {
                                                    AnswerItem(
                                                        type = displayData.type,
                                                        text = string,
                                                        color = getResultItemColor(number)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    VerticalSpacer(size = 10.dp)
                                    TextButton(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = { expanded = !expanded },
                                        shape = RoundedCornerShape(0.dp)
                                    ) {
                                        AnimatedContent(
                                            targetState = expanded,
                                            transitionSpec = {
                                                if (targetState) {
                                                    slideInVertically({ -it }) + fadeIn() with
                                                            slideOutVertically({ it }) + fadeOut()
                                                } else {
                                                    slideInVertically({ it }) + fadeIn() with
                                                            slideOutVertically({ -it }) + fadeOut()
                                                }
                                            }
                                        ) {
                                            Text(stringResource(if (it) R.string.collapse_details else R.string.expand_details))
                                        }
                                    }
                                    AnimatedVisibility(visible = expanded) {
                                        Column {
                                            VerticalSpacer(size = 10.dp)
                                            Surface(
                                                modifier = Modifier
                                                    .padding(horizontal = 10.dp)
                                                    .fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colors.onBackground.copy(alpha = 0.05f)
                                            ) {
                                                Column {
                                                    displayData.detailsItems.forEach {
                                                        val number = it.first
                                                        val string = it.second
                                                        if (displayData.type == 1 || displayData.type == 2) {
                                                            AnswerItem(
                                                                type = displayData.type,
                                                                text = string,
                                                                color = if (number == 1) getResultItemColor(1) else null
                                                            )
                                                        } else if (displayData.type == 3) {
                                                            AnswerItem(
                                                                type = displayData.type,
                                                                color = getResultItemColor(1)
                                                            ) {
                                                                Column {
                                                                    val list = getGson().fromJson<List<String>>(string, object : TypeToken<List<String>>() {}.type)
                                                                    list.forEach {
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .padding(horizontal = 5.dp)
                                                                                .fillMaxWidth()
                                                                        ) {
                                                                            MarkdownText(
                                                                                markdown = it,
                                                                                modifier = Modifier.padding(vertical = 15.dp)
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            VerticalSpacer(size = 10.dp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (viewModel.dialog) {
                    TextDialog(
                        title = stringResource(id = R.string.failed),
                        message = stringResource(id = R.string.error, formatArgs = arrayOf(viewModel.message)),
                        onConfirm = {
                            viewModel.dialog = false
                            finish()
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.compositeDisposable.dispose()
    }

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
}