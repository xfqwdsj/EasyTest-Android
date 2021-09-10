package xyz.xfqlittlefan.easytest.activity.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class QueryActivityViewModel : ViewModel() {
    var text by mutableStateOf("")
    var expanded by mutableStateOf(false)
    var uploaded by mutableStateOf(false)
}