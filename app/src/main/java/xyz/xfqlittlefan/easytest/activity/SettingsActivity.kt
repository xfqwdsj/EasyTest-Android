package xyz.xfqlittlefan.easytest.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.activity.base.BaseActivity
import xyz.xfqlittlefan.easytest.util.UtilClass.getResString
import xyz.xfqlittlefan.easytest.widget.MaterialContainer
import xyz.xfqlittlefan.easytest.widget.PreferenceContainer

class SettingsActivity : BaseActivity() {

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MaterialContainer(
                title = stringResource(id = R.string.settings),
                navigationIcon = {
                    IconButton(onClick = { super.onBackPressed() }) {
                        Icon(Icons.Filled.ArrowBack, stringResource(R.string.back))
                    }
                }
            ) {
                PreferenceContainer(modifier = Modifier.fillMaxSize(), context = this, contentPadding = it) {
                    category(title = getResString(R.string.general)) {
                        editPreference(
                            key = "custom_source",
                            title = getResString(R.string.custom_source),
                            summary = getResString(R.string.one_per_line)
                        )
                    }
                    category(title = getResString(R.string.theme)) {
                        colorPickerPreference(
                            key = "theme_color",
                            title = getResString(R.string.theme_color),
                            summary = getResString(R.string.theme_color_summary)
                        )

                    }
                }
            }
        }
    }
}