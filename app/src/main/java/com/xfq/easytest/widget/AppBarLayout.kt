package com.xfq.easytest.widget

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.appbar.AppBarLayout
import rikka.material.widget.RaisedView

class AppBarLayout : AppBarLayout, RaisedView {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        orientation = VERTICAL
    }

    var mIsRaised = false

    override fun isRaised(): Boolean = mIsRaised

    override fun setRaised(raised: Boolean) {
        if (mIsRaised != raised) {
            mIsRaised = raised
            refreshDrawableState()
        }
    }
}