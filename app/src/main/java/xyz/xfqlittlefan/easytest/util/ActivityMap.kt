package xyz.xfqlittlefan.easytest.util

import android.app.Activity

object ActivityMap {
    private val mList: MutableList<Activity> = ArrayList()

    fun addActivity(activity: Activity) {
        mList.add(activity)
    }

    fun removeActivity(activity: Activity) {
        activity.finish()
        mList.remove(activity)
    }

    fun clear() {
        for (activity in mList) {
            activity.finish()
        }
        mList.removeAll(mList)
    }
}