package xyz.xfqlittlefan.easytest.widget

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import xyz.xfqlittlefan.easytest.theme.EasyTestTheme
import xyz.xfqlittlefan.easytest.theme.Transparent

@JvmName("BasicMaterialContainer")
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

@SuppressLint("ModifierParameter")
@JvmName("MaterialContainer")
@Composable
fun MaterialContainer(
    themeKey: String = "Blue",
    darkTheme: Boolean = isSystemInDarkTheme(),
    title: String,
    subtitle: String? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = { },
    scrollValue: Float,
    content: @Composable (PaddingValues) -> Unit
) {
    MaterialContainer(
        themeKey = themeKey,
        darkTheme = darkTheme,
        topBar = {
            Box {
                val elevation by animateFloatAsState(targetValue = if (scrollValue > 0) AppBarDefaults.TopAppBarElevation.value else 0f)
                val alpha by animateFloatAsState(targetValue = if (scrollValue > 0) 0.95f else 1f)
                Spacer(
                    modifier = Modifier
                        .matchParentSize()
                        .background(color = MaterialTheme.colors.background)
                        .shadow(elevation = elevation.dp)
                        .alpha(alpha)
                )
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
                    backgroundColor = Transparent,
                    elevation = 0.dp
                )
            }
        },
        bottomBar = { Spacer(Modifier.navigationBarsHeight()) },
        content = content
    )
}