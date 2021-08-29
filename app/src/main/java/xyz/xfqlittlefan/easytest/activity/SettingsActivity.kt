package xyz.xfqlittlefan.easytest.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.activity.compose.setContent
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.res.stringResource
import androidx.recyclerview.widget.RecyclerView
import com.takisoft.preferencex.PreferenceFragmentCompat
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.activity.base.BaseActivity
import xyz.xfqlittlefan.easytest.databinding.ActivitySettingsBinding
import xyz.xfqlittlefan.easytest.util.UtilClass
import xyz.xfqlittlefan.easytest.util.ThemeColorPreference
import xyz.xfqlittlefan.easytest.util.ThemeUtil
import rikka.material.app.DayNightDelegate
import rikka.preference.SimpleMenuPreference
import rikka.recyclerview.fixEdgeEffect
import rikka.widget.borderview.BorderRecyclerView
import xyz.xfqlittlefan.easytest.widget.MaterialContainer
import xyz.xfqlittlefan.easytest.widget.PreferenceContainer


class SettingsActivity : BaseActivity() {

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialContainer(
                title = stringResource(id = R.string.settings),
                navigationIcon = {
                    IconButton(onClick = { super.onBackPressed() }) {
                        Icon(Icons.Filled.ArrowBack, stringResource(R.string.back))
                    }
                }
            ) {
                PreferenceContainer {
                    category(title = stringResource(id = R.string.general)) {
                        editPreference(key = "", title = "", defaultValue = "")
                    }
                }
            }
        }
    }
}