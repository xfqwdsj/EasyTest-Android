package xyz.xfqlittlefan.easytest.activity

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.widget.LinearLayout
import cn.leancloud.LCUser
import com.google.android.material.textfield.TextInputEditText
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.activity.base.BaseActivity
import xyz.xfqlittlefan.easytest.databinding.ActivityLoginBinding
import xyz.xfqlittlefan.easytest.util.MyClass.getResString
import xyz.xfqlittlefan.easytest.widget.BlurBehindDialogBuilder
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setAppBar(binding.appBar, binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.login.setOnClickListener {
            if (binding.username.text.toString() != "" && binding.password.text.toString() != "") {
                LCUser.logIn(binding.username.text.toString(), binding.password.text.toString()).subscribe(object : Observer<LCUser?> {
                        override fun onSubscribe(d: Disposable) {}
                        override fun onNext(t: LCUser) {
                            finish()
                        }

                        override fun onError(throwable: Throwable) {
                            BlurBehindDialogBuilder(this@LoginActivity)
                                .setTitle(R.string.failed)
                                .setMessage(this@LoginActivity.resources.getString(R.string.error, throwable))
                                .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int -> }
                                .show()
                        }

                        override fun onComplete() {}
                    })
            }
        }
        binding.signup.setOnClickListener {
            val layout = layoutInflater.inflate(R.layout.dialog_input, LinearLayout(this), false) as LinearLayout
            val input = layout.getChildAt(0) as TextInputEditText
            input.hint = getResString(R.string.email)
            input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            BlurBehindDialogBuilder(this)
                .setTitle(R.string.signup)
                .setView(layout)
                .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->
                    if (binding.username.text.toString() != "" && binding.password.text.toString() != "" && input.text.toString() != "") {
                        val lcUser = LCUser()
                        lcUser.username = binding.username.text.toString()
                        lcUser.password = binding.password.text.toString()
                        lcUser.email = input.text.toString()
                        lcUser.signUpInBackground().subscribe(object : Observer<LCUser?> {
                            override fun onSubscribe(disposable: Disposable) {}
                            override fun onNext(t: LCUser) {
                                LCUser.logOut()
                                finish()
                            }

                            override fun onError(throwable: Throwable) {
                                BlurBehindDialogBuilder(this@LoginActivity)
                                    .setTitle(R.string.failed)
                                    .setMessage(this@LoginActivity.resources.getString(R.string.error, throwable))
                                    .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int -> }
                                    .show()
                            }

                            override fun onComplete() {}
                        })
                    }
                }
                .show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            super.onBackPressed()
            return true
        }
        return false
    }
}