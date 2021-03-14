package com.xfq.easytest

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import cn.leancloud.AVUser
import com.google.android.material.appbar.MaterialToolbar
import com.xfq.bottomdialog.BottomDialog
import com.xfq.easytest.MyClass.INSET_TOP
import com.xfq.easytest.MyClass.setInset
import com.xfq.easytest.databinding.ActivityMainBinding
import okhttp3.*
import org.litepal.LitePal
import java.io.IOException
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var headerView: View? = null
    private var currentUser: AVUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        createDb()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.appbar.setInset(INSET_TOP)
        headerView = binding.navigationView.getHeaderView(0)
        headerView!!.findViewById<MaterialToolbar>(R.id.appbar).setInset(INSET_TOP)
        val headerToolbar = headerView!!.findViewById<MaterialToolbar>(R.id.toolbar)
        headerToolbar.inflateMenu(R.menu.drawer_menu)
        headerToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.account -> {
                    if (currentUser == null) {
                        startActivity(Intent(this, LoginActivity::class.java))
                    } else {

                    }
                    return@setOnMenuItemClickListener true
                }
                R.id.logout -> {
                    AVUser.logOut()
                    onResume()
                    return@setOnMenuItemClickListener true
                }
            }
            false
        }
        binding.navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.test -> startActivity(Intent(this, SelectQuestionBankActivity::class.java))
                R.id.query -> startActivity(Intent(this, IDQueryActivity::class.java))
            }
            false
        }
        //setInset(MyClass.INSET_BOTTOM, binding.navigationView)
        val actionBarDrawerToggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.expanded, R.string.collapsed)
        actionBarDrawerToggle.syncState()
        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle)
        try {
            getNetworkStatus(URL("https://xfqwdsj.gitee.io/easy-test/"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        val headerToolbar = headerView!!.findViewById<MaterialToolbar>(R.id.toolbar)
        currentUser = AVUser.getCurrentUser()
        if (currentUser != null) {
            headerToolbar.title = currentUser!!.username
            headerToolbar.menu.findItem(R.id.logout).isVisible = true
        } else {
            headerToolbar.setTitle(R.string.app_name)
            headerToolbar.menu.findItem(R.id.logout).isVisible = false
        }
    }

    private fun getNetworkStatus(url: URL) {
        val dialog = BottomDialog().create(this).apply {
            setTitle(R.string.network_getting)
            setContent(R.string.please_wait)
            setCancelAble(false)
            show()
        }
        get(url, dialog)
    }

    private fun createDb() {
        LitePal.getDatabase()
    }

    private fun get(url: URL, dialog: BottomDialog) {
        val request = Request.Builder()
                .url(url)
                .removeHeader("User-Agent")
                .addHeader("User-Agent", WebView(this).settings.userAgentString)
                .build()
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                dialog.close()
                BottomDialog().create(this@MainActivity).apply {
                    setTitle(R.string.failed)
                    setContent(R.string.about_to_exit)
                    setButton1(android.R.string.ok) {
                        close()
                        finish()
                    }
                    setCancelAble(false)
                    show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                dialog.close()
                if (response.code != 200) {
                    runOnUiThread {
                        BottomDialog().create(this@MainActivity).apply {
                            setTitle(R.string.failed)
                            setContent(R.string.about_to_exit)
                            setButton1(android.R.string.ok) {
                                close()
                                finish()
                            }
                            setCancelAble(false)
                            show()
                        }
                    }
                }
                response.close()
            }
        })
    }
}