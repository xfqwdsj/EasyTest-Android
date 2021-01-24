package com.xfq.easytest

import org.litepal.crud.LitePalSupport
import java.util.*


class Result : LitePalSupport() {
    var id: Int? = null

    var user: String? = null

    var unit: String? = null

    var time: Int? = null

    var help: Int? = null

    var customHelp: Int? = null

    var createdAt: Date? = null
}