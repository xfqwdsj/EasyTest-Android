package com.xfq.mwords

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.xfq.mwords.MyClass.setInsert
import kotlinx.android.synthetic.main.activity_id_query.*

class IDQueryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_id_query)
        root.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setInsert(MyClass.INSERT_TOP, toolbar)
        setInsert(MyClass.INSERT_BOTTOM, root)
        button.setOnClickListener {
            if (editText.text.toString() != "") {
                if (online.isChecked) {
                    Intent(this, ResultActivity::class.java).apply {
                        putExtra("uploaded", true)
                        putExtra("id", editText.text.toString())
                        startActivity(this)
                    }
                } else if (localUploaded.isChecked) {
                    Intent(this, ResultActivity::class.java).apply {
                        putExtra("uploaded", true)
                        putExtra("saved", true)
                        putExtra("id", editText.text.toString())
                        startActivity(this)
                    }
                } else if (local.isChecked) {
                    Intent(this, ResultActivity::class.java).apply {
                        putExtra("uploaded", false)
                        putExtra("saved", true)
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
