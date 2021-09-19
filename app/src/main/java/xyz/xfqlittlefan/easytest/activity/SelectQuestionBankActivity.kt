package xyz.xfqlittlefan.easytest.activity

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.activity.base.ComposeBaseActivity
import xyz.xfqlittlefan.easytest.activity.viewmodel.SelectQuestionBankActivityViewModel
import xyz.xfqlittlefan.easytest.util.UtilClass
import xyz.xfqlittlefan.easytest.widget.HorizontalSpacer
import xyz.xfqlittlefan.easytest.widget.MaterialContainer
import xyz.xfqlittlefan.easytest.widget.VerticalSpacer

class SelectQuestionBankActivity : ComposeBaseActivity() {
    private val viewModel by viewModels<SelectQuestionBankActivityViewModel>()

    @OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.init(
            intent.getIntegerArrayListExtra("indexList") ?: ArrayList(),
            intent.getStringArrayListExtra("urlList") ?: ArrayList(),
            this
        )
        setContent {
            MaterialContainer(
                title = R.string.select_question_bank,
                onBack = { super.onBackPressed() },
                actions = {
                    IconButton(onClick = {
                        viewModel.init(
                            intent.getIntegerArrayListExtra("indexList") ?: ArrayList(),
                            intent.getStringArrayListExtra("urlList") ?: ArrayList(),
                            this@SelectQuestionBankActivity
                        )
                    }) {
                        Icon(imageVector = Icons.Filled.Refresh, contentDescription = stringResource(id = R.string.refresh))
                    }
                }
            ) { contentPadding ->
                Box {
                    AnimatedVisibility(
                        modifier = Modifier.padding(top = contentPadding.calculateTopPadding()),
                        visible = viewModel.progressing
                    ) { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
                    LazyColumn(contentPadding = contentPadding) {
                        itemsIndexed(viewModel.items) { index, item ->
                            Card(
                                onClick = {
                                    if (item.children.isNullOrEmpty()) {
                                        if (item.url != "" && item.url != null) {
                                            requestedOrientation = if (UtilClass.getPreferences().getBoolean("enable_landscape", false)) {
                                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                            } else {
                                                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                            }
                                            startActivity(Intent(this@SelectQuestionBankActivity, TestActivity::class.java).apply {
                                                putExtra("url", item.url)
                                                putExtra("random", item.random)
                                                putExtra("id", item.id)
                                                putExtra("questionSetUrl", item.questionSetUrl)
                                            })
                                            finish()
                                        }
                                    } else {
                                        val indexList = ArrayList(viewModel.indexList)
                                        val urlList = arrayListOf(item.questionSetUrl)
                                        indexList.add(item.index)
                                        startActivity(Intent(this@SelectQuestionBankActivity, SelectQuestionBankActivity::class.java).apply {
                                            putIntegerArrayListExtra("indexList", indexList)
                                            putStringArrayListExtra("urlList", urlList)
                                        })
                                    }
                                },
                                modifier = Modifier
                                    .padding(
                                        start = 10.dp,
                                        top = 10.dp,
                                        end = 10.dp,
                                        bottom = if (index + 1 == viewModel.items.size) 10.dp else 0.dp
                                    )
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                elevation = 0.dp
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    HorizontalSpacer(size = 10.dp)
                                    Icon(
                                        imageVector = if (item.children.isNullOrEmpty()) Icons.Filled.Book
                                        else Icons.Filled.Folder,
                                        contentDescription = stringResource(id = R.string.question_bank_icon),
                                        modifier = Modifier.padding(vertical = 10.dp).size(32.dp)
                                    )
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(text = item.name, style = MaterialTheme.typography.subtitle1)
                                        VerticalSpacer(size = 15.dp)
                                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                            Text(text = item.description, style = MaterialTheme.typography.subtitle2)
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
