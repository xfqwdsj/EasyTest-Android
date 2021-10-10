package xyz.xfqlittlefan.easytest.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.theme.Black
import xyz.xfqlittlefan.easytest.theme.EasyTestTheme
import xyz.xfqlittlefan.easytest.theme.Transparent
import xyz.xfqlittlefan.easytest.util.UtilClass

@Composable
fun MaterialContainer(
    themeKey: String = "Blue",
    darkTheme: Boolean? = isSystemInDarkTheme(),
    topBar: @Composable () -> Unit = { },
    bottomBar: @Composable () -> Unit = { },
    content: @Composable (PaddingValues) -> Unit
) {
    ProvideWindowInsets {
        EasyTestTheme(themeKey = themeKey, darkTheme = darkTheme) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = topBar,
                bottomBar = bottomBar,
                content = content
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MaterialContainer(
    themeKey: String = "Blue",
    darkTheme: Boolean? = isSystemInDarkTheme(),
    title: String,
    subtitle: String? = null,
    raised: Boolean = false,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = { },
    content: @Composable (PaddingValues) -> Unit
) {
    MaterialContainer(
        themeKey = themeKey,
        darkTheme = darkTheme,
        topBar = {
            val alpha = animateFloatAsState(targetValue = if (raised) 0.95f else 1f)
            
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
                backgroundColor = MaterialTheme.colors.background.copy(alpha = alpha.value),
                elevation = 0.dp
            )
        },
        bottomBar = { Spacer(Modifier.navigationBarsHeight(20.dp)) }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content(it)
            AnimatedVisibility(
                visible = raised,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Spacer(
                    modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = Brush.verticalGradient(
                        colors = listOf(
                            Black.copy(alpha = 0.5f),
                            Transparent
                        )
                    ))
                )
            }
        }
    }
}

@Composable
fun MaterialContainer(
    title: String,
    subtitle: String? = null,
    raised: Boolean = false,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = { },
    content: @Composable (PaddingValues) -> Unit
) {
    MaterialContainer(
        themeKey = UtilClass.theme,
        darkTheme = UtilClass.getDark(),
        title = title,
        subtitle = subtitle,
        raised = raised,
        navigationIcon = { BackIcon(onClick = onBack) },
        actions = actions,
        content = content
    )
}

@Composable
fun MaterialContainer(
    title: Int,
    subtitle: Int? = null,
    raised: Boolean = false,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = { },
    content: @Composable (PaddingValues) -> Unit
) {
    MaterialContainer(
        title = stringResource(id = title),
        subtitle = subtitle?.let { stringResource(id = it) },
        raised = raised,
        onBack = onBack,
        actions = actions,
        content = content
    )
}

@Composable
fun BackIcon(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(Icons.Filled.ArrowBack, stringResource(R.string.back))
    }
}

fun getRaised(lazyListState: LazyListState) = lazyListState.firstVisibleItemScrollOffset > 0

fun getRaised(scrollState: ScrollState) = scrollState.value > 0