package xyz.xfqlittlefan.easytest.data

import androidx.annotation.Keep
import org.litepal.crud.LitePalSupport

@Keep
class Result : LitePalSupport() {
    var question: String? = null
    var state: String? = null
    var url: String? = null
    var setId: String? = null
    var id: Long? = null
    val isCorrect: Boolean
        get() = question != null && state != null && setId != null && url != null
}