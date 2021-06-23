package xyz.xfqlittlefan.easytest.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.preference.SwitchPreference
import androidx.recyclerview.widget.RecyclerView
import com.takisoft.preferencex.PreferenceFragmentCompat
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.activity.base.BaseActivity
import xyz.xfqlittlefan.easytest.databinding.ActivitySettingsBinding
import xyz.xfqlittlefan.easytest.util.MyClass
import xyz.xfqlittlefan.easytest.util.ThemeColorPreference
import xyz.xfqlittlefan.easytest.util.ThemeUtil
import rikka.core.util.ResourceUtils
import rikka.material.app.DayNightDelegate
import rikka.preference.SimpleMenuPreference
import rikka.recyclerview.fixEdgeEffect
import rikka.widget.borderview.BorderRecyclerView


class SettingsActivity : BaseActivity() {
    private val prefixKey = SettingsActivity::class.java.name + '.'
    private val savedInstanceStateExtra = prefixKey + "SAVED_INSTANCE_STATE"
    private lateinit var binding: ActivitySettingsBinding

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setAppBar(binding.appbar, binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.beginTransaction().replace(R.id.settings, SettingsFragment(binding))
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return false
    }

    class SettingsFragment(val binding: ActivitySettingsBinding) : PreferenceFragmentCompat() {
        override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            findPreference<SimpleMenuPreference>("dark_theme")?.setOnPreferenceChangeListener { _, newValue ->
                if (!MyClass.getPreferences()
                        .getString("dark_theme", ThemeUtil.MODE_NIGHT_FOLLOW_SYSTEM)
                        ?.equals(newValue)!!
                ) {
                    DayNightDelegate.setDefaultNightMode(ThemeUtil.getDarkTheme(newValue as String))
                    val activity = activity as SettingsActivity
                    activity.restart()
                }
                true
            }

            findPreference<SwitchPreference>("black_dark_theme")?.setOnPreferenceChangeListener { _, _ ->
                val activity = activity as SettingsActivity
                if (ResourceUtils.isNightMode(resources.configuration)) {
                    activity.restart()
                }
                true
            }

            findPreference<ThemeColorPreference>("theme_color")?.setOnPreferenceChangeListener { _, _ ->
                val activity = activity as SettingsActivity
                activity.restart()
                true
            }
        }

        override fun onCreateRecyclerView(
            inflater: LayoutInflater?,
            parent: ViewGroup?,
            savedInstanceState: Bundle?
        ): RecyclerView {
            val recyclerView: BorderRecyclerView = super.onCreateRecyclerView(
                inflater,
                parent,
                savedInstanceState
            ) as BorderRecyclerView
            recyclerView.fixEdgeEffect(false)
            recyclerView.borderViewDelegate.setBorderVisibilityChangedListener { top, _, _, _ ->
                binding.appbar.isRaised = !top
            }
            return recyclerView
        }
    }

    private fun newIntent(context: Context): Intent {
        return Intent(context, SettingsActivity::class.java)
    }

    private fun newIntent(savedInstanceState: Bundle, context: Context): Intent {
        return newIntent(context)
            .putExtra(savedInstanceStateExtra, savedInstanceState)
    }

    private fun restart() {
        val savedInstanceState = Bundle()
        onSaveInstanceState(savedInstanceState)
        finish()
        startActivity(newIntent(savedInstanceState, this))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}