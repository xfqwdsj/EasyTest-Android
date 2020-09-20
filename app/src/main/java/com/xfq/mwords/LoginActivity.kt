package com.xfq.mwords

import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cn.leancloud.AVUser
import com.xfq.bottomdialog.BottomDialog
import com.xfq.bottomdialog.EditDialog
import com.xfq.mwords.MyClass.getResString
import com.xfq.mwords.MyClass.setInsert
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        root.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setInsert(MyClass.INSERT_TOP, toolbar)
        setInsert(MyClass.INSERT_BOTTOM, root)
        login.setOnClickListener {
            if (username.text.toString() != "" && password.text.toString() != "") {
                AVUser.logIn(username.text.toString(), password.text.toString()).subscribe(object : Observer<AVUser?> {
                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(t: AVUser) {
                        finish()
                    }
                    override fun onError(throwable: Throwable) {
                        BottomDialog().create(this@LoginActivity).apply {
                            setTitle(R.string.failed)
                            setContent(this@LoginActivity.resources.getString(R.string.error, throwable))
                            setButton1(android.R.string.yes) {
                                close()
                            }
                            show()
                        }
                    }
                    override fun onComplete() {}
                })
            }
        }
        signup.setOnClickListener {
            EditDialog().create(this).apply {
                getEdit()!!.hint = getResString(R.string.email)
                getEdit()!!.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                setTitle(R.string.signup)
                setButton(android.R.string.yes) {
                    close()
                    if (username.text.toString() != "" && password.text.toString() != "" && getText() != "") {
                        val avUser = AVUser()
                        avUser.username = username.text.toString()
                        avUser.password = password.text.toString()
                        avUser.email = getText()
                        avUser.signUpInBackground().subscribe(object : Observer<AVUser?> {
                            override fun onSubscribe(disposable: Disposable) {}
                            override fun onNext(t: AVUser) {
                                AVUser.logOut()
                                finish()
                            }
                            override fun onError(throwable: Throwable) {
                                BottomDialog().create(this@LoginActivity).apply {
                                    setTitle(R.string.failed)
                                    setContent(this@LoginActivity.resources.getString(R.string.error, throwable))
                                    setButton1(android.R.string.yes) {
                                        close()
                                    }
                                }
                            }
                            override fun onComplete() {}
                        })
                    }
                }
                show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return false
    }
}