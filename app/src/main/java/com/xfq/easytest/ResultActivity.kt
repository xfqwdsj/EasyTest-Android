package com.xfq.easytest

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.xfq.bottomdialog.BottomDialog
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
        if (score != null && correctness != null && questions != null) {
            var scoreString = "["
            var correctnessString = "["
            for (i in score.indices) {
                scoreString += "${score[i]}, "
                correctnessString += "${correctness[i]}, "
            }
            scoreString = "${scoreString.substring(0, scoreString.length - 2)}]"
            correctnessString = "${correctnessString.substring(0, correctnessString.length - 2)}]"
            BottomDialog().create(this).apply {
                setTitle("临时结果显示")
                setContent("分数：$scoreString\n正确性：$correctnessString")
                show()
            }
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