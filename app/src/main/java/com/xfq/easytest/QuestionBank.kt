package com.xfq.easytest

import kotlin.properties.Delegates

class QuestionBank {
    lateinit var name: String
    lateinit var description: String
    lateinit var url: String
    var random by Delegates.notNull<Boolean>()
    var status by Delegates.notNull<Int>()
    lateinit var children: List<QuestionBank>
}