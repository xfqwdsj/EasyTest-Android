package xyz.xfqlittlefan.easytest.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.activity.ui.theme.EasyTestTheme

class QueryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EasyTestTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        var text by remember { mutableStateOf("") }
                        TextField(
                            value = text,
                            onValueChange = { text = it },
                            modifier = Modifier
                                .padding(5.dp)
                                .fillMaxWidth(),
                            label = {
                                Text(stringResource(id = R.string.app_name))
                            }
                        )
                        Button(
                            onClick = {
                                startActivity(Intent(this@QueryActivity, ResultActivity::class.java).apply {
                                    putExtra("id", text)
                                })
                                finish()
                            }
                        ) {
                            Text(stringResource(id = R.string.app_name))
                        }
                    }
                }
            }
        }
    }
}
