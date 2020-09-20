package com.xfq.mwords

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.FileUtils.delete
import com.blankj.utilcode.util.FileUtils.isFileExists
import com.blankj.utilcode.util.ZipUtils.unzipFile
import com.google.android.material.snackbar.Snackbar
import com.xfq.bottomdialog.EditDialog
import com.xfq.mwords.MyClass.getResString
import com.xfq.mwords.MyClass.setInsert
import kotlinx.android.synthetic.main.activity_download.*


class DownloadActivity : AppCompatActivity() {
    private var downloadManager: DownloadManager? = null
    private var downloadId: Long? = null
    private var fileName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)
        root.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setInsert(MyClass.INSERT_TOP, toolbar)
        setInsert(MyClass.INSERT_BOTTOM, root)
        button.setOnClickListener {
            download("https://xfqwdsj.github.io/mword/words.zip")
        }
        button.setOnLongClickListener {
            EditDialog().create(this).apply {
                getEdit()!!.hint = getResString(R.string.source)
                getEdit()!!.inputType = InputType.TYPE_TEXT_VARIATION_URI
                setTitle(R.string.custom_source)
                setButton(android.R.string.yes) {
                    close()
                    if (getText() != "") {
                        download(getText())
                    }
                }
                show()
            }
            true
        }
    }

    private fun download(url: String) {
        fileName = getFileName(url)
        if (fileName == null) {
            Snackbar.make(root, R.string.failed, Snackbar.LENGTH_LONG).show()
            return
        }
        if (!isFileExists(getExternalFilesDir("")!!.path + "/" + fileName)) {
            val request = DownloadManager.Request(Uri.parse(url))
            request.setTitle(fileName)
            request.setDescription(getResString(R.string.app_name))
            request.setDestinationInExternalFilesDir(this, "", fileName)
            downloadManager = this.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadId = downloadManager!!.enqueue(request)
            this.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        } else {
            Snackbar.make(root, R.string.file_exists, Snackbar.LENGTH_LONG).setAction(android.R.string.yes) {
                delete(getExternalFilesDir("")!!.path + "/" + fileName)
                val request = DownloadManager.Request(Uri.parse(url))
                request.setTitle(fileName)
                request.setDescription(getResString(R.string.app_name))
                request.setDestinationInExternalFilesDir(this, "", fileName)
                downloadManager = this.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                downloadId = downloadManager!!.enqueue(request)
                this.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
            }.show()
        }
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            checkStatus()
        }
    }

    private fun checkStatus() {
        val query = DownloadManager.Query()
        downloadId?.let { query.setFilterById(it) }
        val cursor: Cursor? = downloadManager?.query(query)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                    DownloadManager.STATUS_PAUSED -> {
                    }
                    DownloadManager.STATUS_PENDING -> {
                    }
                    DownloadManager.STATUS_RUNNING -> {
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        if (unzipFile(getExternalFilesDir("")!!.path + "/" + fileName, getExternalFilesDir("")!!.path + "/") != null) {
                            delete(getExternalFilesDir("")!!.path + "/" + fileName)
                            Snackbar.make(root, R.string.success, Snackbar.LENGTH_LONG).show()
                        } else {
                            delete(getExternalFilesDir("")!!.path + "/" + fileName)
                            Snackbar.make(root, R.string.failed, Snackbar.LENGTH_LONG).show()
                        }
                        cursor.close()
                        this.unregisterReceiver(receiver)
                    }
                    DownloadManager.STATUS_FAILED -> {
                        Snackbar.make(root, R.string.failed, Snackbar.LENGTH_LONG).show()
                        cursor.close()
                        this.unregisterReceiver(receiver)
                    }
                }
            }
        }
    }

    private fun getFileName(path: String): String? {
        val start = path.lastIndexOf("/")
        return if (start != -1) {
            path.substring(start + 1)
        } else {
            null
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
