package com.xfq.easytest.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import cn.leancloud.AVUser
import com.xfq.bottomdialog.BottomDialog
import com.xfq.bottomdialog.EditDialog
import com.xfq.easytest.R
import com.xfq.easytest.activity.base.BaseActivity
import com.xfq.easytest.databinding.ActivityLoginBinding
import com.xfq.easytest.util.MyClass.getResString
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setAppBar(binding.appbar, binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.login.setOnClickListener {
            if (binding.username.text.toString() != "" && binding.password.text.toString() != "") {
                AVUser.logIn(binding.username.text.toString(), binding.password.text.toString())
                    .subscribe(object : Observer<AVUser?> {
                        override fun onSubscribe(d: Disposable) {}
                        override fun onNext(t: AVUser) {
                            finish()
                        }

                        override fun onError(throwable: Throwable) {
                            BottomDialog().create(this@LoginActivity).apply {
                                setTitle(R.string.failed)
                                setContent(
                                    this@LoginActivity.resources.getString(
                                        R.string.error,
                                        throwable
                                    )
                                )
                                setButton1(android.R.string.ok) {
                                    close()
                                }
                                show()
                            }
                        }

                        override fun onComplete() {}
                    })
            }
        }
        binding.signup.setOnClickListener {
            EditDialog().create(this).apply {
                getEdit()!!.hint = getResString(R.string.email)
                getEdit()!!.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                setTitle(R.string.signup)
                setButton(android.R.string.ok) {
                    close()
                    if (binding.username.text.toString() != "" && binding.password.text.toString() != "" && getText() != "") {
                        val avUser = AVUser()
                        avUser.username = binding.username.text.toString()
                        avUser.password = binding.password.text.toString()
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
                                    setContent(
                                        this@LoginActivity.resources.getString(
                                            R.string.error,
                                            throwable
                                        )
                                    )
                                    setButton1(android.R.string.ok) {
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