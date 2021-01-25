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
import com.xfq.easytest.databinding.ActivityIdQueryBinding

class IDQueryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIdQueryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIdQueryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setInset(MyClass.INSET_TOP, binding.toolbar)
        setInset(MyClass.INSET_BOTTOM, binding.root)
        binding.button.setOnClickListener {
            if (binding.editText.text.toString() != "") {
                if (binding.online.isChecked) {
                    Intent(this, ResultActivity::class.java).apply {
                        putExtra("type", RESULT_TYPE_UPLOADED)
                        putExtra("id", binding.editText.text.toString())
                        startActivity(this)
                    }
                } else if (binding.localUploaded.isChecked) {
                    Intent(this, ResultActivity::class.java).apply {
                        putExtra("type", RESULT_TYPE_LOCAL_UPLOADED)
                        putExtra("id", binding.editText.text.toString())
                        startActivity(this)
                    }
                } else if (binding.local.isChecked) {
                    Intent(this, ResultActivity::class.java).apply {
                        putExtra("type", RESULT_TYPE_NO_UPLOADED_SAVED)
                        putExtra("id", binding.editText.text.toString().toInt())
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
