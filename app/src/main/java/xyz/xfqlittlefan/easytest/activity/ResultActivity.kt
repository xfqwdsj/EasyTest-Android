package xyz.xfqlittlefan.easytest.activity

import android.os.Bundle
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.activity.base.BaseActivity
import xyz.xfqlittlefan.easytest.databinding.ActivityResultBinding

class ResultActivity : BaseActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setAppBar(binding.appBar, binding.toolbar)
        binding.recyclerView.borderViewDelegate.setBorderVisibilityChangedListener { top, _, _, _ ->
            binding.appBar.isRaised = !top
        }

        if (intent.getBooleanExtra("uploaded", false)) {

        }

    }
}