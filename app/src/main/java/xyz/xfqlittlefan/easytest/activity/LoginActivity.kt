package xyz.xfqlittlefan.easytest.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.activity.base.ComposeBaseActivity
import xyz.xfqlittlefan.easytest.activity.viewmodel.LoginActivityViewModel
import xyz.xfqlittlefan.easytest.util.UtilClass
import xyz.xfqlittlefan.easytest.util.UtilClass.getDark
import xyz.xfqlittlefan.easytest.widget.Autofill
import xyz.xfqlittlefan.easytest.widget.BackIcon
import xyz.xfqlittlefan.easytest.widget.MaterialContainer
import xyz.xfqlittlefan.easytest.widget.VerticalSpacer

class LoginActivity : ComposeBaseActivity() {
    private val viewModel by viewModels<LoginActivityViewModel>()

    @OptIn(ExperimentalAnimationApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.finish = { finish() }
        setContent {
            MaterialContainer(
                themeKey = UtilClass.theme,
                darkTheme = getDark(),
                title = stringResource(id = R.string.login),
                navigationIcon = { BackIcon { super.onBackPressed() } }
            ) { contentPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(contentPadding)
                ) {
                    val autoFill = LocalAutofill.current

                    VerticalSpacer(size = 10.dp)
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                    ) {
                        Autofill(
                            autofillTypes = listOf(AutofillType.Username),
                            onFill = {
                                viewModel.username = it
                                viewModel.usernameError = false
                            }
                        ) { node ->
                            TextField(
                                value = viewModel.username,
                                onValueChange = {
                                    viewModel.username = it
                                    viewModel.usernameError = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged {
                                        autoFill?.apply {
                                            if (it.isFocused) {
                                                requestAutofillForNode(node)
                                            } else {
                                                cancelAutofillForNode(node)
                                            }
                                        }
                                    },
                                label = { Text(text = stringResource(id = R.string.username)) },
                                isError = viewModel.usernameError,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Next),
                                singleLine = true,
                                shape = RectangleShape
                            )
                        }
                        Autofill(
                            autofillTypes = listOf(AutofillType.Password),
                            onFill = {
                                viewModel.password = it
                                viewModel.passwordError = false
                            }
                        ) { node ->
                            TextField(
                                value = viewModel.password,
                                onValueChange = {
                                    viewModel.password = it
                                    viewModel.passwordError = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged {
                                        autoFill?.apply {
                                            if (it.isFocused) {
                                                requestAutofillForNode(node)
                                            } else {
                                                cancelAutofillForNode(node)
                                            }
                                        }
                                    },
                                label = { Text(text = stringResource(id = R.string.password)) },
                                isError = viewModel.passwordError,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = if (viewModel.signUp) ImeAction.Next else ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = { viewModel.onDone() }),
                                singleLine = true,
                                shape = RectangleShape
                            )
                        }
                    }
                    VerticalSpacer(size = 10.dp)
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        elevation = 0.dp
                    ) {
                        Column {
                            TextButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { viewModel.signUp = !viewModel.signUp },
                                shape = RectangleShape
                            ) {
                                Text(stringResource(id = R.string.sign_up))
                            }
                            AnimatedVisibility(visible = viewModel.signUp) {
                                Column {
                                    TextField(
                                        value = viewModel.nickname,
                                        onValueChange = {
                                            viewModel.nickname = it
                                            viewModel.nicknameError = false
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text(text = stringResource(id = R.string.nickname)) },
                                        isError = viewModel.nicknameError,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                                        singleLine = true,
                                        shape = RectangleShape
                                    )
                                    TextField(
                                        value = viewModel.email,
                                        onValueChange = {
                                            viewModel.email = it
                                            viewModel.emailError = false
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text(text = stringResource(id = R.string.email)) },
                                        isError = viewModel.emailError,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(onDone = { viewModel.onDone() }),
                                        singleLine = true,
                                        shape = RectangleShape
                                    )
                                }
                            }
                        }
                    }
                    VerticalSpacer(size = 10.dp)
                    Button(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .fillMaxWidth(),
                        onClick = viewModel.onDone,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        AnimatedContent(
                            targetState = viewModel.signUp,
                            transitionSpec = { fadeIn() with fadeOut() }
                        ) {
                            Text(stringResource(if (it) R.string.sign_up else R.string.login))
                        }
                    }
                    if (viewModel.dialog) {
                        AlertDialog(
                            onDismissRequest = { },
                            confirmButton = {
                                TextButton(onClick = {
                                    viewModel.dialog = false
                                    finish()
                                }) {
                                    Text(text = stringResource(id = android.R.string.ok))
                                }
                            },
                            title = { Text(text = stringResource(id = R.string.failed)) },
                            text = { Text(text = stringResource(id = R.string.error, formatArgs = arrayOf(viewModel.message))) }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.compositeDisposable.dispose()
    }
}