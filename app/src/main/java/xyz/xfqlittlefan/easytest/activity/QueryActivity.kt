package xyz.xfqlittlefan.easytest.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.activity.base.BaseActivity
import xyz.xfqlittlefan.easytest.theme.EasyTestTheme
import xyz.xfqlittlefan.easytest.util.UtilClass
import xyz.xfqlittlefan.easytest.widget.BackIcon
import xyz.xfqlittlefan.easytest.widget.MaterialContainer

class QueryActivity : BaseActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MaterialContainer(
                themeKey = UtilClass.theme,
                darkTheme = UtilClass.getDark(),
                title = stringResource(R.string.query),
                navigationIcon = { BackIcon { super.onBackPressed() } }
            ) { contentPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(contentPadding)
                ) {
                    var text by remember { mutableStateOf("") }
                    var expanded by remember { mutableStateOf(false) }

                    var uploaded by remember { mutableStateOf(false) }

                    TextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier
                            .padding(start = 10.dp, end = 10.dp, top = 10.dp)
                            .fillMaxWidth(),
                        label = {
                            Text(stringResource(id = R.string.query))
                        },
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
                                onClick = { expanded = !expanded },
                                shape = RoundedCornerShape(0.dp)
                            ) {
                                Text(stringResource(if (expanded) R.string.collapse_options else R.string.expand_options))
                            }
                            Column {
                                AnimatedVisibility(visible = expanded) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .selectable(selected = uploaded) {
                                                uploaded = !uploaded
                                            }
                                            .padding(15.dp)
                                    ) {
                                        Checkbox(checked = uploaded, onCheckedChange = null)
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
                                putExtra("id", text)
                                putExtra("uploaded", uploaded)
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
