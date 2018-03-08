package cn.mw.ethwallet.views

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 17:44
 * @description
 */
class NonSwipeViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {

    var enabled1: Boolean

    init {
        this.enabled1 = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (this.enabled1) {
            super.onTouchEvent(event)
        } else false
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return if (this.enabled1) {
            super.onInterceptTouchEvent(event)
        } else false
    }

    fun setPagingEnabled(enabled: Boolean) {
        this.enabled1 = enabled
    }
}