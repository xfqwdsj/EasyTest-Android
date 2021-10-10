package xyz.xfqlittlefan.easytest.activity.base

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat

open class ComposeBaseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }
}