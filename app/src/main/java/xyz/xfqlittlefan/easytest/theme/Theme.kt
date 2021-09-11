package xyz.xfqlittlefan.easytest.theme

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

fun getLightColors(key: String): Colors {
    return lightColors(
        primary = getColor500(key),
        primaryVariant = getColor700(key),
        secondary = getColor500(key),
        secondaryVariant = getColor500(key),
        background = Gray50
    )
}

fun getDarkColors(key: String): Colors {
    return darkColors(
        primary = getColor300(key),
        primaryVariant = getColor500(key),
        secondary = getColor300(key),
        secondaryVariant = getColor300(key),
        background = Black
    )
}

fun colors(key: String, dark: Boolean? = false, system: Boolean = false): Colors {
    return if (dark == true) {
        getDarkColors(key)
    } else if (dark == false) {
        getLightColors(key)
    } else {
        if (system) {
            getDarkColors(key)
        } else {
            getLightColors(key)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EasyTestTheme(themeKey: String = "Blue", darkTheme: Boolean? = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    Box {
        Spacer(modifier = Modifier.matchParentSize().background(color = colors(themeKey, darkTheme, isSystemInDarkTheme()).background))
        Crossfade(targetState = Pair(themeKey, darkTheme)) {
            MaterialTheme(
                colors = colors(it.first, it.second, isSystemInDarkTheme()),
                typography = Typography,
                shapes = Shapes,
                content = content
            )
        }
    }
}