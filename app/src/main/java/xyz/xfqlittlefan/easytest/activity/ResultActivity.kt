package xyz.xfqlittlefan.easytest.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import okhttp3.internal.toLongOrDefault
import org.litepal.LitePal
import org.litepal.extension.find
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.activity.base.BaseActivity
import xyz.xfqlittlefan.easytest.activity.ui.theme.EasyTestTheme
import xyz.xfqlittlefan.easytest.data.Result
import xyz.xfqlittlefan.easytest.util.UtilClass.getGson

class ResultActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EasyTestTheme {
                ProvideWindowInsets {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text(stringResource(R.string.result)) },
                                navigationIcon = {
                                    IconButton(onClick = { super.onBackPressed() }) {
                                        Icon(Icons.Filled.ArrowBack, stringResource(R.string.back))
                                    }
                                },
                                backgroundColor = MaterialTheme.colors.background,
                                contentPadding = rememberInsetsPaddingValues(insets = LocalWindowInsets.current.statusBars)
                            )
                        },
                        bottomBar = {
                            Spacer(Modifier.navigationBarsHeight())
                        }
                    ) { contentPadding ->
                        LazyColumn(contentPadding = contentPadding) {
                            val id = intent.getStringExtra("id")
                            if (id != null) {
                                if (intent.getBooleanExtra("uploaded", false)) {
                                    TODO()
                                } else {
                                    val result = LitePal.find<Result>(id.toLongOrDefault(-1))
                                    if (result != null) {
                                        val question = getGson()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Item(question: String, userAnswer: String, score: Float, correctness: Int) {

    }
}