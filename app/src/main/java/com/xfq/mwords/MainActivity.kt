package com.xfq.mwords

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import cn.leancloud.AVUser
import com.xfq.bottomdialog.BottomDialog
import com.xfq.mwords.MyClass.setInsert
import kotlinx.android.synthetic.main.activity_main.*
import org.litepal.LitePal
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class MainActivity : AppCompatActivity() {
    private var headerView: View? = null
    private var currentUser: AVUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createDb()
        views()
        setSupportActionBar(toolbar)
        initToggle()
        try {
            getNetworkStatus(URL("https://xfqwdsj.github.io/mword/"))
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
    }

    private fun views() {
        MainLayout.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        setInsert(MyClass.INSERT_TOP, toolbar)
        headerView = navigationView.getHeaderView(0)
        val headerLinear = headerView!!.findViewById<LinearLayout>(R.id.navLinear)
        headerLinear.setOnClickListener {
            if (currentUser == null) {
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                BottomDialog().create(this).apply {
                    setTitle(R.string.logout)
                    setContent(R.string.ask_logout)
                    setButton1(android.R.string.yes) {
                        close()
                        AVUser.logOut()
                        onResume()
                    }
                    setButton2(android.R.string.no) {
                        close()
                    }
                    show()
                }
            }
        }
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.mwords -> startActivity(Intent(this, MwordsActivity::class.java))
                R.id.query -> startActivity(Intent(this, IDQueryActivity::class.java))
                R.id.download -> startActivity(Intent(this, DownloadActivity::class.java))
            }
            false
        }
        setInsert(MyClass.INSERT_BOTTOM, navigationView)
    }

    private fun initToggle() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close)
        actionBarDrawerToggle.syncState()
        drawerLayout!!.addDrawerListener(actionBarDrawerToggle)
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
        val textView = headerView!!.findViewById<TextView>(R.id.navText)
        textView.setText(R.string.login)
        currentUser = AVUser.getCurrentUser()
        if (currentUser != null) {
            textView.text = currentUser!!.username
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
        Thread {
            run {
                try {
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
                } catch (e: IOException) {
                    dialog.close()
                    runOnUiThread {
                        askOffline()
                    }
                }
            }
        }.start()
    }

    private fun askOffline() {
        BottomDialog().create(this).apply {
            setTitle(R.string.failed)
            setContent(R.string.ask_offline_mode)
            setButton1(android.R.string.yes) {
                close()
                //离线模式
            }
            setButton2(android.R.string.no) {
                close()
                finish()
            }
            setCancelAble(false)
            show()
        }
    }
}