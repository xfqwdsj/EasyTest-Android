package xyz.xfqlittlefan.easytest.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.material.composethemeadapter.MdcTheme
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.activity.base.BaseActivity
import xyz.xfqlittlefan.easytest.databinding.ActivityResultBinding

class ResultActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MdcTheme {
                Content()
            }
        }
    }

    @Composable
    fun Content() {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.result)) },
                    navigationIcon = {
                        IconButton(onClick = { super.onBackPressed() }) {
                            Icon(Icons.Filled.ArrowBack, stringResource(R.string.back))
                        }
                    }
                )
            }
        ) {
            if (intent.getBooleanExtra("uploaded", false)) {

            } else {

            }
        }
    }
}