package com.xfq.mwords

import android.app.Application
import cn.leancloud.AVOSCloud
import org.litepal.LitePal

class ModulesInit : Application() {
    override fun onCreate() {
        super.onCreate()
        AVOSCloud.initialize(this, "1KbhNSxDBKNOvAiGQEJqPE6B-gzGzoHsz", "aWxGGJwdeWE7rmxyLWIbkuyT", "https://1kbhnsxd.lc-cn-n1-shared.com")
        LitePal.initialize(this)
        MyClass.init(this)
    }
}