package com.xfq.easytest

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.android.material.snackbar.Snackbar
import com.xfq.easytest.MyClass.setInset
import com.xfq.easytest.databinding.ActivityIdQueryBinding
import org.litepal.LitePal
import org.litepal.extension.find

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
            val result = LitePal.find<Result>(binding.editText.text.toString().toLong())
            if (result != null) {
                Intent(this, ResultActivity::class.java).apply {
                    putExtra("result", result)
                    startActivity(this)
                }
            } else {
                Snackbar.make(binding.root, R.string.not_found, Snackbar.LENGTH_LONG).show()
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
