package com.xfq.easytest

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

class TestPagerAdaper(private val mViewList: List<View>, private val context: Context) : PagerAdapter() {

    override fun getCount(): Int = mViewList.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        container.addView(mViewList[position])
        return mViewList[position]
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(mViewList[position])
    }

    override fun getPageTitle(position: Int): CharSequence? = context.resources.getString(R.string.question_number, position + 1)
}