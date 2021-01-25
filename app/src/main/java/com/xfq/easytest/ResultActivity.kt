package com.xfq.easytest

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import cn.leancloud.AVObject
import cn.leancloud.AVQuery
import cn.leancloud.AVUser
import com.google.android.material.snackbar.Snackbar
import com.xfq.bottomdialog.BottomDialog
import com.xfq.easytest.MyClass.RESULT_TYPE_LOCAL_UPLOADED
import com.xfq.easytest.MyClass.RESULT_TYPE_NO_UPLOADED_NO_SAVE
import com.xfq.easytest.MyClass.RESULT_TYPE_NO_UPLOADED_SAVED
import com.xfq.easytest.MyClass.RESULT_TYPE_UPLOADED
import com.xfq.easytest.MyClass.getResString
import com.xfq.easytest.MyClass.setInset
import com.xfq.easytest.databinding.ActivityResultBinding
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.litepal.LitePal
import org.litepal.extension.find
import java.text.DateFormat.getDateInstance
import java.text.DateFormat.getTimeInstance
import java.util.*


class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val intent = this.intent
        val type = intent.getIntExtra("type", -1)
        if (type == -1) {
            BottomDialog().create(this).apply {
                setTitle(R.string.failed)
                setContent(R.string.unknown_error)
                setCancelAble(false)
                setButton1(android.R.string.ok) {
                    close()
                    finish()
                }
            }
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setInset(MyClass.INSET_TOP, binding.toolbar)
        setInset(MyClass.INSET_BOTTOM, binding.root)
        when (type) {
            RESULT_TYPE_UPLOADED -> {
                val id = intent.getStringExtra("id")
                if (id == null) {
                    finish()
                }
                val queryObject = AVQuery<AVObject>("MwordsResult")
                queryObject.getInBackground(id).subscribe(object : Observer<AVObject> {
                    override fun onNext(result: AVObject) {
                        val queryUser = AVQuery<AVObject>("_User")
                        queryUser.getInBackground(result.get("userid") as String).subscribe(object : Observer<AVObject> {
                            override fun onComplete() {}
                            override fun onSubscribe(a: Disposable) {}
                            override fun onNext(user: AVObject) {
                                val username = user.get("username") as String
                                val nickname = user.get("nickname") as String
                                val unit = result.get("unit") as String
                                val time = result.get("timer") as Int
                                val customHelp = result.get("diyhelp") as Int
                                val help = result.get("help") as Int
                                val createdAt = getDateInstance().format(result.createdAt) + " " + getTimeInstance().format(result.createdAt)
                                val updatedAt = getDateInstance().format(result.updatedAt) + " " + getTimeInstance().format(result.updatedAt)
                                binding.textView.text = this@ResultActivity.resources.getString(R.string.result_text_uploaded, nickname, username, unit, getTime(time), help.toString(), checkCustomHelp(customHelp), createdAt, updatedAt)
                                binding.toolbar.title = this@ResultActivity.resources.getString(R.string.result_title, nickname)
                                binding.button1.visibility = View.VISIBLE
                                binding.button1.setOnClickListener {
                                    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clipData = ClipData.newPlainText("Label", id)
                                    clipboardManager.setPrimaryClip(clipData)
                                }
                            }

                            override fun onError(e: Throwable) {
                                BottomDialog().create(this@ResultActivity).apply {
                                    setTitle(R.string.failed)
                                    setContent(this@ResultActivity.resources.getString(R.string.error, e))
                                    setButton1(android.R.string.ok) {
                                        close()
                                    }
                                }
                            }
                        })
                    }

                    override fun onComplete() {}
                    override fun onSubscribe(d: Disposable) {}
                    override fun onError(e: Throwable) {
                        BottomDialog().create(this@ResultActivity).apply {
                            setTitle(R.string.failed)
                            setContent(this@ResultActivity.resources.getString(R.string.error, e))
                            setButton1(android.R.string.ok) {
                                close()
                            }
                        }
                    }
                })
            }
            RESULT_TYPE_NO_UPLOADED_NO_SAVE -> {
                val unit = intent.getStringExtra("unit")
                val time = intent.getIntExtra("time", 9999)
                val help = intent.getIntExtra("help", 9999)
                val customHelp = intent.getIntExtra("customHelp", 0)
                binding.textView.text = this.resources.getString(R.string.result_text_no_uploaded, unit, getTime(time), help.toString(), checkCustomHelp(customHelp), getResString(R.string.unknown))
                binding.toolbar.title = getResString(R.string.offline_result)
                binding.button2.visibility = View.VISIBLE
                binding.button2.setOnClickListener {
                    val user = intent.getStringExtra("user")
                    val result = Result()
                    if (user != null) result.user = user
                    result.unit = unit
                    result.time = time
                    result.help = help
                    result.customHelp = intent.getIntExtra("customHelp", 0)
                    result.createdAt = Date(intent.getLongExtra("createdAt", 0))
                    if (result.save()) {
                        BottomDialog().create(this@ResultActivity).apply {
                            setTitle(R.string.success)
                            setContent(R.string.upload_success)
                            setButton1(android.R.string.ok) {
                                close()
                                finish()
                                Intent(this@ResultActivity, ResultActivity::class.java).apply {
                                    putExtra("uploaded", false)
                                    putExtra("saved", true)
                                    putExtra("id", result.id)
                                    startActivity(this)
                                }
                            }
                            setCancelAble(false)
                            show()
                        }
                    } else {
                        Snackbar.make(binding.root, R.string.failed, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
            RESULT_TYPE_NO_UPLOADED_SAVED -> {
                val id = intent.getIntExtra("id", 1).toLong()
                val result = LitePal.find<Result>(id)
                if (result != null) {
                    binding.textView.text = this.resources.getString(R.string.result_text_no_uploaded, result.unit, getTime(result.time!!), result.help.toString(), checkCustomHelp(result.customHelp!!), getDate(result.createdAt!!))
                    binding.toolbar.title = getResString(R.string.offline_result)
                    binding.button1.visibility = View.VISIBLE
                    binding.button1.setOnClickListener {
                        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clipData = ClipData.newPlainText("Label", result.id.toString())
                        clipboardManager.setPrimaryClip(clipData)
                    }
                    binding.button3.visibility = View.VISIBLE
                    binding.button3.setOnClickListener {
                        val user: String = (if (result.user != null) result.user else if (AVUser.getCurrentUser() != null) AVUser.getCurrentUser().objectId else null)
                                ?: return@setOnClickListener
                        val avObject = AVObject("LocalMwordsResult").apply {
                            put("unit", result.unit)
                            put("timer", result.time)
                            put("diyhelp", result.customHelp)
                            put("help", result.help)
                            put("userid", user)
                        }
                        avObject.saveInBackground().subscribe(object : Observer<AVObject> {
                            override fun onComplete() {}
                            override fun onSubscribe(d: Disposable) {}
                            override fun onNext(t: AVObject) {
                                BottomDialog().create(this@ResultActivity).apply {
                                    setTitle(R.string.success)
                                    setContent(R.string.upload_success)
                                    setButton1(android.R.string.ok) {
                                        close()
                                        finish()
                                        Intent(this@ResultActivity, ResultActivity::class.java).apply {
                                            putExtra("type", RESULT_TYPE_LOCAL_UPLOADED)
                                            putExtra("id", avObject.objectId)
                                            startActivity(this)
                                        }
                                    }
                                    setCancelAble(false)
                                    show()
                                }
                            }

                            override fun onError(e: Throwable) {
                                BottomDialog().create(this@ResultActivity).apply {
                                    setTitle(R.string.failed)
                                    setContent(this@ResultActivity.resources.getString(R.string.error, e))
                                    setCancelAble(false)
                                    setButton1(android.R.string.ok) {
                                        close()
                                        finish()
                                    }
                                    show()
                                }
                            }
                        })
                    }
                }
            }
            RESULT_TYPE_LOCAL_UPLOADED -> {
                val id = intent.getStringExtra("id")
                if (id == null) {
                    finish()
                }
                val queryObject = AVQuery<AVObject>("LocalMwordsResult")
                queryObject.getInBackground(id).subscribe(object : Observer<AVObject> {
                    override fun onNext(result: AVObject) {
                        val queryUser = AVQuery<AVObject>("_User")
                        queryUser.getInBackground(result.get("userid") as String).subscribe(object : Observer<AVObject> {
                            override fun onComplete() {}
                            override fun onSubscribe(a: Disposable) {}
                            override fun onNext(user: AVObject) {
                                val username = user.getString("username")
                                val nickname = user.getString("nickname")
                                val unit = result.getString("unit")
                                val time = result.getInt("timer")
                                val customHelp = result.getInt("diyhelp")
                                val help = result.getInt("help")
                                val createdAt = getDate(result.createdAt)
                                val updatedAt = getDate(result.updatedAt)
                                binding.textView.text = this@ResultActivity.resources.getString(R.string.result_text_uploaded, nickname, username, unit, getTime(time), help.toString(), checkCustomHelp(customHelp), createdAt, updatedAt)
                                binding.toolbar.title = this@ResultActivity.resources.getString(R.string.result_title, nickname)
                                binding.warning.visibility = View.VISIBLE
                                binding.button1.visibility = View.VISIBLE
                                binding.button1.setOnClickListener {
                                    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clipData = ClipData.newPlainText("Label", id)
                                    clipboardManager.setPrimaryClip(clipData)
                                }
                            }

                            override fun onError(e: Throwable) {
                                BottomDialog().create(this@ResultActivity).apply {
                                    setTitle(R.string.failed)
                                    setContent(this@ResultActivity.resources.getString(R.string.error, e))
                                    setCancelAble(false)
                                    setButton1(android.R.string.ok) {
                                        close()
                                        finish()
                                    }
                                    show()
                                }
                            }
                        })
                    }

                    override fun onComplete() {}
                    override fun onSubscribe(d: Disposable) {}
                    override fun onError(e: Throwable) {
                        BottomDialog().create(this@ResultActivity).apply {
                            setTitle(R.string.failed)
                            setContent(this@ResultActivity.resources.getString(R.string.error, e))
                            setCancelAble(false)
                            setButton1(android.R.string.ok) {
                                close()
                                finish()
                            }
                            show()
                        }
                    }
                })
            }
        }
    }

    private fun getDate(date: Date): String {
        return getDateInstance().format(date) + " " + getTimeInstance().format(date)
    }

    private fun checkCustomHelp(customHelp: Int): String {
        return if (customHelp == 0)
            getResString(R.string.prompt_all)
        else
            customHelp.toString()
    }

    private fun getTime(time: Int): String {
        val timeMinutesString: String
        val timeSecondsString: String
        var timeMinutes = 0
        var timeSeconds = time
        while (timeSeconds >= 60) {
            timeSeconds -= 60
            timeMinutes++
        }
        timeSecondsString = if (timeSeconds < 10) {
            "0$timeSeconds"
        } else {
            timeSeconds.toString()
        }
        timeMinutesString = timeMinutes.toString()
        return "$timeMinutesString:$timeSecondsString"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return false
    }
}
