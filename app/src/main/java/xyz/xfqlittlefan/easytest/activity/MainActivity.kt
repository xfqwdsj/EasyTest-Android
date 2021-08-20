package xyz.xfqlittlefan.easytest.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import cn.leancloud.LCUser
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import com.google.android.material.snackbar.Snackbar
import org.litepal.LitePal
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.activity.base.BaseActivity
import xyz.xfqlittlefan.easytest.activity.viewmodel.MainActivityViewModel
import xyz.xfqlittlefan.easytest.theme.*
import xyz.xfqlittlefan.easytest.databinding.ActivityMainBinding
import xyz.xfqlittlefan.easytest.widget.MaterialContainer

class MainActivity : BaseActivity() {
    private val viewModel by viewModels<MainActivityViewModel>()

    @OptIn(ExperimentalMaterialApi::class)
    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scrollState = rememberLazyListState()
            MaterialContainer(
                title = stringResource(R.string.home),
                scrollValue = scrollState.firstVisibleItemScrollOffset.toFloat()
            ) { contentPadding ->
                LazyColumn(state = scrollState, contentPadding = contentPadding) {
                    item {
                        ObviousCard(
                            icon = Icons.Filled.School,
                            title = stringResource(R.string.start_test),
                            subtitle = stringResource(R.string.start_test_summary)
                        ) {
                            startActivity(Intent(this@MainActivity, SelectQuestionBankActivity::class.java).apply {
                                val urlList = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                                    .getString("custom_source", "")!!
                                    .split("\n").toMutableList()
                                urlList.add(0, "https://gitee.com/xfqwdsj/easy-test/raw/master/question-bank-index.json")
                                putStringArrayListExtra("urlList", ArrayList(urlList))
                            })
                        }
                    }
                    item {
                        MainCard(
                            icon = Icons.Filled.PlaylistAddCheck,
                            title = stringResource(R.string.result),
                            subtitle = stringResource(R.string.result_query_summary)
                        ) {
                            startActivity(Intent(this@MainActivity, QueryActivity::class.java))
                        }
                    }
                    item {
                        MainCard(
                            icon = Icons.Filled.Person,
                            title = stringResource(R.string.account),
                            subtitle = stringResource(if (viewModel.logged) R.string.account_summary else R.string.account_summary_no_logged_in)
                        ) {
                            if (viewModel.logged) {
                                LCUser.logOut()
                                viewModel.update()
                            } else {
                                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                            }
                        }
                    }
                    item {
                        SecondaryCard(
                            icon = Icons.Filled.Settings,
                            title = stringResource(R.string.settings)
                        ) {
                            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onResume() {
        super.onResume()
        viewModel.update()
    }

    @ExperimentalMaterialApi
    @Composable
    fun ObviousCard(icon: ImageVector, title: String, subtitle: String? = null, onClick: () -> Unit) {
        ActionCard(
            color = if (MaterialTheme.colors.isLight) Green900 else Green300,
            contentColor = if (MaterialTheme.colors.isLight) White else Black,
            icon = icon,
            onClick = onClick,
            elevation = 4.dp
        ) {
            Column(Modifier.fillMaxSize()) {
                Text(text = title, style = MaterialTheme.typography.h6)
                if (subtitle != null) {
                    Spacer(Modifier.height(4.dp))
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(text = subtitle, style = MaterialTheme.typography.subtitle1)
                    }
                }
            }
        }
    }

    @ExperimentalMaterialApi
    @Composable
    fun MainCard(icon: ImageVector, title: String, subtitle: String? = null, onClick: () -> Unit) {
        ActionCard(
            icon = icon,
            onClick = onClick,
            elevation = 4.dp
        ) {
            Column(Modifier.fillMaxSize()) {
                Text(text = title, style = MaterialTheme.typography.h6)
                if (subtitle != null) {
                    Spacer(Modifier.height(4.dp))
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(text = subtitle, style = MaterialTheme.typography.subtitle1)
                    }
                }
            }
        }
    }

    @ExperimentalMaterialApi
    @Composable
    fun SecondaryCard(icon: ImageVector, title: String, subtitle: String? = null, onClick: () -> Unit) {
        ActionCard(
            icon = icon,
            onClick = onClick,
            elevation = 4.dp,
            color = Transparent
        ) {
            Column(Modifier.fillMaxSize()) {
                Text(text = title, style = MaterialTheme.typography.h6)
                if (subtitle != null) {
                    Spacer(Modifier.height(4.dp))
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(text = subtitle, style = MaterialTheme.typography.subtitle1)
                    }
                }
            }
        }
    }

    @ExperimentalMaterialApi
    @Composable
    fun ActionCard(
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colors.surface,
        contentColor: Color = contentColorFor(color),
        icon: ImageVector,
        elevation: Dp = 0.dp,
        onClick: () -> Unit,
        content: @Composable () -> Unit
    ) {
        Card(
            modifier = modifier,
            backgroundColor = color,
            contentColor = contentColor,
            shape = RoundedCornerShape(10.dp),
            elevation = elevation,
            onClick = onClick
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.width(20.dp))
                Icon(
                    imageVector = icon, contentDescription = stringResource(R.string.icon), modifier = Modifier
                        .padding(vertical = 20.dp)
                        .size(32.dp)
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    content()
                }
            }
        }
    }
}