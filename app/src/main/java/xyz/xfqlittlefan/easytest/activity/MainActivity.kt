package xyz.xfqlittlefan.easytest.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import androidx.preference.PreferenceManager
import cn.leancloud.LCUser
import com.google.android.material.snackbar.Snackbar
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.activity.base.BaseActivity
import xyz.xfqlittlefan.easytest.databinding.ActivityMainBinding
import xyz.xfqlittlefan.easytest.widget.BlurBehindDialogBuilder
import okhttp3.*
import org.litepal.LitePal
import java.io.IOException
import java.net.URL

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private var currentUser: LCUser? = null

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        LitePal.getDatabase()

        setAppBar(binding.appBar, binding.toolbar)
        binding.scroll.borderViewDelegate.setBorderVisibilityChangedListener { top, _, _, _ ->
            binding.appBar.isRaised = !top
        }

        binding.card1.setOnClickListener {
            Intent(this, SelectQuestionBankActivity::class.java).apply {
                val urlList = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                    .getString("custom_source", "")!!
                    .split("\n").toMutableList()
                urlList.add(0, "https://gitee.com/xfqwdsj/easy-test/raw/master/question-bank-index.json")
                putStringArrayListExtra("urlList", ArrayList(urlList))
                startActivity(this)
            }
        }
        binding.card2.setOnClickListener {
            Snackbar.make(binding.root, R.string.coming_soon, Snackbar.LENGTH_SHORT).show()
        }
        binding.card3.setOnClickListener {
            if (currentUser == null) {
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                Snackbar.make(binding.root, R.string.coming_soon, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.logout) {
                        LCUser.logOut()
                        onResume()
                    }
                    .show()
            }
        }
        binding.card4.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onResume() {
        super.onResume()
        currentUser = LCUser.getCurrentUser()
        if (currentUser != null) {
            binding.text32.setText(R.string.account_summary)
        } else {
            binding.text32.setText(R.string.account_summary_no_logged_in)
        }
    }
}