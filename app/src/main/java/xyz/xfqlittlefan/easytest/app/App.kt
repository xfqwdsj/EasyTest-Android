package xyz.xfqlittlefan.easytest.app

import android.app.Application
import cn.leancloud.LeanCloud
import xyz.xfqlittlefan.easytest.util.UtilClass
import xyz.xfqlittlefan.easytest.util.ThemeUtil
import org.litepal.LitePal
import rikka.material.app.DayNightDelegate

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        LeanCloud.initialize(this, "1KbhNSxDBKNOvAiGQEJqPE6B-gzGzoHsz", "aWxGGJwdeWE7rmxyLWIbkuyT", "https://1kbhnsxd.lc-cn-n1-shared.com")
        LitePal.initialize(this)
        UtilClass.init(this)

        //DayNightDelegate.setApplicationContext(this)
        //DayNightDelegate.setDefaultNightMode(ThemeUtil.getDarkTheme())
    }
}