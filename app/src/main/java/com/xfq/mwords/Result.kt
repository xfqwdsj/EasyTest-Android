package com.xfq.mwords

import org.litepal.crud.LitePalSupport
import java.util.*


class Result : LitePalSupport() {
    var id: Int? = null
        get() = field
        set(value) {
            field = value
        }

    var user: String? = null
        get() = field
        set(value) {
            field = value
        }

    var unit: String? = null
        get() = field
        set(value) {
            field = value
        }

    var time: Int? = null
        get() = field
        set(value) {
            field = value
        }

    var help: Int? = null
        get() = field
        set(value) {
            field = value
        }

    var customHelp: Int? = null
        get() = field
        set(value) {
            field = value
        }

    var createdAt: Date? = null
        get() = field
        set(value) {
            field = value
        }
}