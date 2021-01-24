package com.xfq.easytest

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import cn.leancloud.AVUser
import com.google.android.material.appbar.MaterialToolbar
import com.xfq.bottomdialog.BottomDialog
import com.xfq.easytest.MyClass.setInset
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import org.litepal.LitePal
import java.io.IOException
import java.net.URL

class MainActivity : AppCompatActivity() {
    private var headerView: View? = null
    private var currentUser: AVUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createDb()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        headerView = navigationView.getHeaderView(0)
        val headerToolbar = headerView!!.findViewById<MaterialToolbar>(R.id.toolbar)
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { myView, windowInsets ->
            myView.setPadding(myView.paddingLeft, windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top, myView.paddingRight, myView.paddingBottom)
            headerToolbar.setPadding(headerToolbar.paddingLeft, windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top, headerToolbar.paddingRight, headerToolbar.paddingBottom)
            windowInsets
        }
        //setInset(MyClass.INSERT_TOP, toolbar)
        //setInset(MyClass.INSERT_TOP, headerToolbar)
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
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.mwords -> startActivity(Intent(this, SelectQuestionBankActivity::class.java))
                R.id.query -> startActivity(Intent(this, IDQueryActivity::class.java))
                R.id.download -> startActivity(Intent(this, DownloadActivity::class.java))
            }
            false
        }
        setInset(MyClass.INSERT_BOTTOM, navigationView)
        val actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close)
        actionBarDrawerToggle.syncState()
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
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
        /*
            val httpURLConnection = url.openConnection() as HttpURLConnection
            httpURLConnection.requestMethod = "GET"
            if (httpURLConnection.responseCode == 200) {
                dialog.close()
            } else {
                dialog.close()
                runOnUiThread {
                    askOffline()
                }
            }
         */
        val client = OkHttpClient()
        val request = Request.Builder()
                .url(url)
                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                dialog.close()
                askOffline()
            }

            override fun onResponse(call: Call, response: Response) {
                dialog.close()
            }
        })
    }

    private fun askOffline() {
        BottomDialog().create(this).apply {
            setTitle(R.string.failed)
            setContent(R.string.ask_offline_mode)
            setButton1(android.R.string.ok) {
                close()
                //离线模式
            }
            setButton2(android.R.string.cancel) {
                close()
                finish()
            }
            setCancelAble(false)
            show()
        }
    }
}