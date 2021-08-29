package xyz.xfqlittlefan.easytest.activity.base

import android.annotation.SuppressLint
import android.content.res.Resources
import android.content.res.Resources.Theme
import android.graphics.Color
import android.os.Build
import androidx.core.view.WindowInsetsCompat
import rikka.core.res.resolveColor
import xyz.xfqlittlefan.easytest.util.ThemeUtil
import rikka.core.util.ResourceUtils.resolveColor
import rikka.material.app.MaterialActivity
import xyz.xfqlittlefan.easytest.R


open class BaseActivity : MaterialActivity() {
    override fun onApplyUserThemeResource(theme: Theme, isDecorView: Boolean) {
        theme.applyStyle(ThemeUtil.getNightThemeStyleRes(this), true)
        theme.applyStyle(ThemeUtil.getColorThemeStyleRes(), true)
    }

    override fun computeUserThemeKey(): String? {
        return ThemeUtil.getColorTheme() + ThemeUtil.getNightTheme(this)
    }

    @SuppressLint("MissingSuperCall")
    override fun onApplyTranslucentSystemBars() {
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.setOnApplyWindowInsetsListener { _, windowInsets ->
            if (WindowInsetsCompat.toWindowInsetsCompat(windowInsets).getInsets(WindowInsetsCompat.Type.systemBars()).bottom >= Resources.getSystem().displayMetrics.density * 40) {
                window.navigationBarColor = resolveColor(
                    theme,
                    android.R.attr.navigationBarColor
                ) and 0x00ffffff or -0x20000000
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    window.navigationBarDividerColor = theme.resolveColor(R.attr.navigationBarDividerColor)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = false
                }
            } else {
                window.navigationBarColor = Color.TRANSPARENT
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = true
                }
            }
            windowInsets
        }
    }
}