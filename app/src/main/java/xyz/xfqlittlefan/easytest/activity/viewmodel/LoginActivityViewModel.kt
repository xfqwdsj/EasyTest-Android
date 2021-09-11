package xyz.xfqlittlefan.easytest.activity.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cn.leancloud.LCUser
import io.reactivex.disposables.CompositeDisposable

class LoginActivityViewModel : ViewModel() {
    var finish by mutableStateOf({ })

    var signUp by mutableStateOf(false)

    var username by mutableStateOf("")
    var usernameError by mutableStateOf(false)
    var password by mutableStateOf("")
    var passwordError by mutableStateOf(false)

    var nickname by mutableStateOf("")
    var nicknameError by mutableStateOf(false)
    var email by mutableStateOf("")
    var emailError by mutableStateOf(false)

    var dialog by mutableStateOf(false)
    var message by mutableStateOf("")
        private set

    val compositeDisposable = CompositeDisposable()

    val onDone: () -> Unit = {
        if (signUp) {
            if (username.isEmpty()) {
                usernameError = true
            }
            if (password.isEmpty()) {
                passwordError = true
            }
            if (nickname.isEmpty()) {
                nicknameError = true
            }
            if (email.isEmpty()) {
                emailError = true
            }
            if (!(usernameError || passwordError || nicknameError || emailError)) {
                val lcUser = LCUser()
                lcUser.username = username
                lcUser.password = password
                lcUser.email = email
                lcUser.put("nickname", nickname)
                compositeDisposable.add(lcUser.signUpInBackground().subscribe({
                    LCUser.logOut()
                    finish()
                }, {
                    dialog = true
                    message = it.toString()
                }))
            }
        } else {
            if (username.isEmpty()) {
                usernameError = true
            }
            if (password.isEmpty()) {
                passwordError = true
            }
            if (!(usernameError || passwordError)) {
                compositeDisposable.add(LCUser.logIn(username, password).subscribe({
                    finish()
                }, {
                    dialog = true
                    message = it.localizedMessage ?: it.toString()
                }))
            }
        }
    }
}