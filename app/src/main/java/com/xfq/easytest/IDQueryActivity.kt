package com.xfq.easytest

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.xfq.easytest.MyClass.RESULT_TYPE_LOCAL_UPLOADED
import com.xfq.easytest.MyClass.RESULT_TYPE_NO_UPLOADED_SAVED
import com.xfq.easytest.MyClass.RESULT_TYPE_UPLOADED
import com.xfq.easytest.MyClass.setInset
import kotlinx.android.synthetic.main.activity_id_query.*

class IDQueryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_id_query)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setInset(MyClass.INSERT_TOP, toolbar)
        setInset(MyClass.INSERT_BOTTOM, root)
        button.setOnClickListener {
            if (editText.text.toString() != "") {
                if (online.isChecked) {
                    Intent(this, ResultActivity::class.java).apply {
                        putExtra("type", RESULT_TYPE_UPLOADED)
                        putExtra("id", editText.text.toString())
                        startActivity(this)
                    }
                } else if (localUploaded.isChecked) {
                    Intent(this, ResultActivity::class.java).apply {
                        putExtra("type", RESULT_TYPE_LOCAL_UPLOADED)
                        putExtra("id", editText.text.toString())
                        startActivity(this)
                    }
                } else if (local.isChecked) {
                    Intent(this, ResultActivity::class.java).apply {
                        putExtra("type", RESULT_TYPE_NO_UPLOADED_SAVED)
                        putExtra("id", editText.text.toString().toInt())
                        startActivity(this)
                    }
                }
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
