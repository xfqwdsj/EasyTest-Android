package xyz.xfqlittlefan.easytest.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Blue700,
    primaryVariant = Blue900,
    secondary = Blue700,
    background = Black
)

private val LightColorPalette = lightColors(
    primary = Blue500,
    primaryVariant = Blue700,
    secondary = Blue500,
    background = Gray50
)

fun colors(key: String, dark: Boolean = false): Colors {
    return if (dark) {
        darkColors(
            primary = getColor700(key),
            primaryVariant = getColor900(key),
            secondary = getColor700(key),
            background = Black
        )
    } else {
        lightColors(
            primary = getColor500(key),
            primaryVariant = getColor700(key),
            secondary = getColor500(key),
            background = Gray50
        )
    }
}

@Composable
fun EasyTestTheme(themeKey: String = "Blue", darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colors = colors(themeKey, darkTheme),
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}