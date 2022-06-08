package com.hellomydoc

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

class IntroPageAdapter(private val mContext: Context) : PagerAdapter() {
    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val customPagerEnum = CustomPagerEnum.values()[position]
        val inflater = LayoutInflater.from(mContext)
        val layout = inflater.inflate(customPagerEnum.layoutResId, collection, false) as ViewGroup
        collection.addView(layout)
        return layout
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return CustomPagerEnum.values().size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getPageTitle(position: Int): CharSequence? {
        val customPagerEnum = CustomPagerEnum.values()[position]
        return "" //mContext.getString(customPagerEnum.getTitleResId());
    }
}
