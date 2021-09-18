package xyz.xfqlittlefan.easytest.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import cn.leancloud.LCUser
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.activity.base.ComposeBaseActivity
import xyz.xfqlittlefan.easytest.activity.viewmodel.MainActivityViewModel
import xyz.xfqlittlefan.easytest.theme.*
import xyz.xfqlittlefan.easytest.util.UtilClass
import xyz.xfqlittlefan.easytest.widget.HorizontalSpacer
import xyz.xfqlittlefan.easytest.widget.MaterialContainer
import xyz.xfqlittlefan.easytest.widget.VerticalSpacer

class MainActivity : ComposeBaseActivity() {
    private val viewModel by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialContainer(
                themeKey = UtilClass.theme,
                darkTheme = UtilClass.getDark(),
                title = stringResource(R.string.home)
            ) { contentPadding ->
                LazyColumn(
                    state = rememberLazyListState(),
                    contentPadding = PaddingValues(
                        start = 10.dp,
                        top = contentPadding.calculateTopPadding(),
                        end = 10.dp,
                        bottom = contentPadding.calculateBottomPadding()
                    )
                ) {
                    item {
                        VerticalSpacer(size = 10.dp)
                    }
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
                        VerticalSpacer(size = 10.dp)
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
                        VerticalSpacer(size = 10.dp)
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
                        VerticalSpacer(size = 10.dp)
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

    override fun onResume() {
        super.onResume()
        viewModel.update()
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun ObviousCard(icon: ImageVector, title: String, subtitle: String? = null, onClick: () -> Unit) {
        ActionCard(
            color = if (MaterialTheme.colors.isLight) Green900 else Green300,
            contentColor = if (MaterialTheme.colors.isLight) White else Black,
            icon = icon,
            onClick = onClick
        ) {
            AnimatedContent(
                targetState = title,
                transitionSpec = { fadeIn() with fadeOut() }
            ) {
                Text(text = it, style = MaterialTheme.typography.subtitle1)
            }
            if (subtitle != null) {
                VerticalSpacer(size = 4.dp)
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    AnimatedContent(
                        targetState = subtitle,
                        transitionSpec = { fadeIn() with fadeOut() }
                    ) {
                        Text(text = it, style = MaterialTheme.typography.subtitle2)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun MainCard(icon: ImageVector, title: String, subtitle: String? = null, onClick: () -> Unit) {
        ActionCard(
            icon = icon,
            onClick = onClick
        ) {
            AnimatedContent(
                targetState = title,
                transitionSpec = { fadeIn() with fadeOut() }
            ) {
                Text(text = it, style = MaterialTheme.typography.subtitle1)
            }
            if (subtitle != null) {
                VerticalSpacer(size = 4.dp)
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    AnimatedContent(
                        targetState = subtitle,
                        transitionSpec = { fadeIn() with fadeOut() }
                    ) {
                        Text(text = it, style = MaterialTheme.typography.subtitle2)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun SecondaryCard(icon: ImageVector, title: String, subtitle: String? = null, onClick: () -> Unit) {
        ActionCard(
            icon = icon,
            onClick = onClick,
            color = Transparent
        ) {
            AnimatedContent(
                targetState = title,
                transitionSpec = { fadeIn() with fadeOut() }
            ) {
                Text(text = it, style = MaterialTheme.typography.subtitle1)
            }
            if (subtitle != null) {
                VerticalSpacer(size = 4.dp)
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    AnimatedContent(
                        targetState = subtitle,
                        transitionSpec = { fadeIn() with fadeOut() }
                    ) {
                        Text(text = it, style = MaterialTheme.typography.subtitle2)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun ActionCard(
        color: Color = MaterialTheme.colors.surface,
        contentColor: Color = contentColorFor(color),
        icon: ImageVector,
        onClick: () -> Unit,
        content: @Composable ColumnScope.() -> Unit
    ) {
        Card(
            onClick = onClick,
            shape = RoundedCornerShape(10.dp),
            backgroundColor = color,
            contentColor = contentColor,
            elevation = 0.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalSpacer(size = 20.dp)
                Icon(
                    imageVector = icon,
                    contentDescription = stringResource(R.string.icon),
                    modifier = Modifier
                        .padding(vertical = 20.dp)
                        .size(32.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    content = content
                )
            }
        }
    }
}