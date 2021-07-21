package xyz.xfqlittlefan.easytest.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.util.MyClass.getResString

class TestPagerAdapter(
    private val viewList: List<View>,
    private val positionList: List<Int>,
    private val context: Context
) : PagerAdapter() {
    var submitted = false

    override fun getCount(): Int = viewList.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        container.removeView(viewList[position])
        container.addView(viewList[position])
        return viewList[position]
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(viewList[position])
    }

    override fun getPageTitle(position: Int): CharSequence =
        if (!submitted || position != viewList.size - 1) context.resources.getString(
            R.string.question_number,
            positionList[position] + 1
        ) else getResString(R.string.result)
}