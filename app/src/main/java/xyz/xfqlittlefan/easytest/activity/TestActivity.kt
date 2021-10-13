package xyz.xfqlittlefan.easytest.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.activity.base.BaseActivity
import xyz.xfqlittlefan.easytest.activity.viewmodel.TestActivityViewModel
import xyz.xfqlittlefan.easytest.widget.MaterialContainer

class TestActivity : BaseActivity() {
    val viewModel by viewModels<TestActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialContainer(
                title = R.string.test,
                onBack = { super.onBackPressed() }
            ) {

            }
        }
    }
}