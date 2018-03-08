package cn.mw.ethwallet.activities

//import kotlinx.android.synthetic.main.pl_base_pattern_activity.*
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cn.mw.ethwallet.R
import kotlinx.android.synthetic.main.app_lock_activity.*

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 11:50
 * @description
 */
open class BasePatternActivity : AppCompatActivity() {

//    protected var mMessageText: TextView
//    protected var mPatternView: PatternView
//    protected var mButtonContainer: LinearLayout
//    protected var mLeftButton: Button
//    protected var mRightButton: Button

    private val clearPatternRunnable = Runnable {
        // clearPattern() resets display mode to DisplayMode.Correct.
        pl_pattern.clearPattern()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.app_lock_activity)
//        mMessageText = findViewById(R.id.pl_message_text) as TextView
//        mPatternView = findViewById(R.id.pl_pattern) as PatternView
//        mButtonContainer = findViewById(R.id.pl_button_container) as LinearLayout
//        mLeftButton = findViewById(R.id.pl_left_button) as Button
//        mRightButton = findViewById(R.id.pl_right_button) as Button
    }

    protected fun removeClearPatternRunnable() {
        pl_pattern.removeCallbacks(clearPatternRunnable)
    }

    protected fun postClearPatternRunnable() {
        removeClearPatternRunnable()
        pl_pattern.postDelayed(clearPatternRunnable, CLEAR_PATTERN_DELAY_MILLI.toLong())
    }

    companion object {

        private val CLEAR_PATTERN_DELAY_MILLI = 2000
    }
}