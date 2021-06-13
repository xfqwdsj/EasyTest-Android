package com.xfq.easytest

import android.app.Application
import cn.leancloud.AVOSCloud
import com.xfq.easytest.util.MyClass
import com.xfq.easytest.util.ThemeUtil
import org.litepal.LitePal
import rikka.material.app.DayNightDelegate

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        AVOSCloud.initialize(
            this,
            "1KbhNSxDBKNOvAiGQEJqPE6B-gzGzoHsz",
            "aWxGGJwdeWE7rmxyLWIbkuyT",
            "https://1kbhnsxd.lc-cn-n1-shared.com"
        )
        LitePal.initialize(this)
        MyClass.init(this)

        DayNightDelegate.setApplicationContext(this)
        DayNightDelegate.setDefaultNightMode(ThemeUtil.getDarkTheme())
    }
}