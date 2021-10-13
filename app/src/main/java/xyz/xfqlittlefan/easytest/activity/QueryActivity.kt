package xyz.xfqlittlefan.easytest.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.activity.base.BaseActivity
import xyz.xfqlittlefan.easytest.activity.viewmodel.QueryActivityViewModel
import xyz.xfqlittlefan.easytest.theme.expand
import xyz.xfqlittlefan.easytest.widget.MaterialContainer
import xyz.xfqlittlefan.easytest.widget.getRaised

class QueryActivity : BaseActivity() {
    private val viewModel by viewModels<QueryActivityViewModel>()

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state = rememberScrollState()

            MaterialContainer(
                title = R.string.query,
                raised = getRaised(state),
                onBack = { super.onBackPressed() }
            ) { contentPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(state)
                        .padding(contentPadding)
                ) {
                    TextField(
                        value = viewModel.text,
                        onValueChange = { viewModel.text = it },
                        modifier = Modifier
                            .padding(start = 10.dp, end = 10.dp, top = 10.dp)
                            .fillMaxWidth(),
                        label = { Text(stringResource(id = R.string.query)) },
                        singleLine = true,
                        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                    )
                    Card(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp),
                        elevation = 0.dp
                    ) {
                        Column {
                            TextButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { viewModel.expanded = !viewModel.expanded },
                                shape = RectangleShape
                            ) {
                                AnimatedContent(
                                    targetState = viewModel.expanded,
                                    transitionSpec = { expand(targetState) }
                                ) {
                                    Text(stringResource(if (it) R.string.collapse_options else R.string.expand_options))
                                }
                            }
                            Column {
                                AnimatedVisibility(visible = viewModel.expanded) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .selectable(selected = viewModel.uploaded) {
                                                viewModel.uploaded = !viewModel.uploaded
                                            }
                                            .padding(15.dp)
                                    ) {
                                        Checkbox(checked = viewModel.uploaded, onCheckedChange = null)
                                        Text(
                                            modifier = Modifier.padding(start = 5.dp),
                                            text = stringResource(id = R.string.cloud_result)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Button(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        onClick = {
                            startActivity(Intent(this@QueryActivity, ResultActivity::class.java).apply {
                                putExtra("id", viewModel.text)
                                putExtra("uploaded", viewModel.uploaded)
                            })
                            finish()
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(stringResource(id = R.string.query))
                    }
                }
            }
        }
    }
}
