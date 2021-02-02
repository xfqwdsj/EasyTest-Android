package com.xfq.easytest

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.xfq.easytest.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        MyClass.setInset(MyClass.INSET_TOP, binding.toolbar)

        val score = intent.getFloatArrayExtra("score")
        val correctness = intent.getIntArrayExtra("correctness")
        val questions = intent.getParcelableArrayExtra("questions")
        val positions = intent.getIntArrayExtra("positions")
        if (score != null && correctness != null && questions != null && positions != null) {

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return false
    }
}