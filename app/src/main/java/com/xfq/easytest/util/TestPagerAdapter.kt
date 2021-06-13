package com.xfq.easytest.util

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import androidx.viewpager.widget.PagerAdapter
import com.xfq.easytest.R
import com.xfq.easytest.util.MyClass.getResString

class TestPagerAdapter(
    private val mBindingList: List<ViewBinding>,
    private val mPositionList: List<Int>,
    private val mContext: Context
) : PagerAdapter() {
    var submitted = false

    override fun getCount(): Int = mBindingList.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        container.removeView(mBindingList[position].root)
        container.addView(mBindingList[position].root)
        return mBindingList[position]
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(mBindingList[position].root)
    }

    override fun getPageTitle(position: Int): CharSequence =
        if (!submitted || position != mBindingList.size - 1) mContext.resources.getString(
            R.string.question_number,
            mPositionList[position] + 1
        ) else getResString(R.string.result)
}