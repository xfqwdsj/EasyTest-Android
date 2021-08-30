package xyz.xfqlittlefan.easytest.widget

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import xyz.xfqlittlefan.easytest.theme.EasyTestTheme

@Composable
fun MaterialContainer(
    themeKey: String = "Blue",
    darkTheme: Boolean = isSystemInDarkTheme(),
    topBar: @Composable () -> Unit = { },
    bottomBar: @Composable () -> Unit = { },
    content: @Composable (PaddingValues) -> Unit
) {
    EasyTestTheme(themeKey = themeKey, darkTheme = darkTheme) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = topBar,
            bottomBar = bottomBar,
            content = content
        )
    }
}

@Composable
fun MaterialContainer(
    themeKey: String = "Blue",
    darkTheme: Boolean = isSystemInDarkTheme(),
    title: String,
    subtitle: String? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = { },
    content: @Composable (PaddingValues) -> Unit
) {
    ProvideWindowInsets {
        MaterialContainer(
            themeKey = themeKey,
            darkTheme = darkTheme,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(text = title)
                            if (subtitle != null) {
                                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                    Text(text = subtitle, style = MaterialTheme.typography.subtitle2)
                                }
                            }
                        }
                    },
                    contentPadding = rememberInsetsPaddingValues(insets = LocalWindowInsets.current.statusBars),
                    navigationIcon = navigationIcon,
                    actions = actions,
                    backgroundColor = MaterialTheme.colors.background.copy(alpha = 0.95f),
                    elevation = 0.dp
                )
            },
            bottomBar = { Spacer(Modifier.navigationBarsHeight()) },
            content = content
        )
    }
}