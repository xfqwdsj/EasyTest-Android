package xyz.xfqlittlefan.easytest.util

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.util.MyClass.getResString

class TestPagerAdapter(
    private val mViewList: List<View>,
    private val mPositionList: List<Int>,
    private val mContext: Context
) : PagerAdapter() {
    var submitted = false

    override fun getCount(): Int = mViewList.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        container.removeView(mViewList[position])
        container.addView(mViewList[position])
        return mViewList[position]
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(mViewList[position])
    }

    override fun getPageTitle(position: Int): CharSequence =
        if (!submitted || position != mViewList.size - 1) mContext.resources.getString(
            R.string.question_number,
            mPositionList[position] + 1
        ) else getResString(R.string.result)
}