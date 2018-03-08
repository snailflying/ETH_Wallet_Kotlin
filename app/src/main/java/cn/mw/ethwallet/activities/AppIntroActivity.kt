package cn.mw.ethwallet.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.widget.Toast
import cn.mw.ethwallet.R
import cn.mw.ethwallet.fragments.ToSFragment
import com.github.paolorotolo.appintro.AppIntro2
import com.github.paolorotolo.appintro.AppIntroFragment

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 14:09
 * @description
 */
class AppIntroActivity : AppIntro2() {
    private var tosFragment: ToSFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addSlide(AppIntroFragment.newInstance(getString(R.string.app_intro_title), getString(R.string.app_intro_text), R.drawable.ether_intro, Color.parseColor("#49627e")))
        tosFragment = ToSFragment()
        addSlide(tosFragment!!)

        showSkipButton(false)
        isProgressButtonEnabled = true
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        if (tosFragment!!.isToSChecked) {
            val data = Intent()
            data.putExtra("TOS", true)
            setResult(Activity.RESULT_OK, data)
            finish()
        } else
            Toast.makeText(this, R.string.app_intro_please_agree, Toast.LENGTH_SHORT).show()
    }

    override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)
    }

    companion object {

        val REQUEST_CODE = 602
    }
}