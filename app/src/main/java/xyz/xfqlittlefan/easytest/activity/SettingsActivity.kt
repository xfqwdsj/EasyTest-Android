package xyz.xfqlittlefan.easytest.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.activity.base.BaseActivity
import xyz.xfqlittlefan.easytest.util.UtilClass
import xyz.xfqlittlefan.easytest.util.UtilClass.dark
import xyz.xfqlittlefan.easytest.util.UtilClass.getDark
import xyz.xfqlittlefan.easytest.util.UtilClass.getResString
import xyz.xfqlittlefan.easytest.util.UtilClass.theme
import xyz.xfqlittlefan.easytest.widget.BackIcon
import xyz.xfqlittlefan.easytest.widget.MaterialContainer
import xyz.xfqlittlefan.easytest.widget.PreferenceContainer

class SettingsActivity : BaseActivity() {

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MaterialContainer(
                themeKey = UtilClass.theme,
                darkTheme = getDark(),
                title = stringResource(id = R.string.settings),
                navigationIcon = { BackIcon { super.onBackPressed() } }
            ) { contentPadding ->
                PreferenceContainer(modifier = Modifier.fillMaxSize(), context = this, contentPadding = contentPadding) {
                    category(title = getResString(R.string.general)) {
                        edit(
                            key = "custom_source",
                            title = getResString(R.string.custom_source),
                            summary = getResString(R.string.one_per_line)
                        )
                    }
                    category(title = getResString(R.string.theme)) {
                        colorPicker(
                            key = "theme_color",
                            title = getResString(R.string.theme_color),
                            summary = getResString(R.string.theme_color_summary)
                        ) {
                            UtilClass.theme = it
                        }
                        menu(
                            key = "dark_theme",
                            title = getResString(R.string.dark_theme),
                            items = listOf(
                                getResString(R.string.dark_theme_follow_system),
                                getResString(R.string.dark_theme_on),
                                getResString(R.string.dark_theme_off)
                            )
                        ) {
                            dark = it
                        }
                    }
                }
            }
        }
    }
}