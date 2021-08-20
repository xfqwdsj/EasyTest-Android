package xyz.xfqlittlefan.easytest.activity.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cn.leancloud.LCUser

class MainActivityViewModel: ViewModel() {
    var logged by mutableStateOf(LCUser.getCurrentUser() != null)
        private set

    fun update() {
        logged = LCUser.getCurrentUser() != null
    }
}