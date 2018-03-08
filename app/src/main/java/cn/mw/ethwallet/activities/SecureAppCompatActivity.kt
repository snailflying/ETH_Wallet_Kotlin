package cn.mw.ethwallet.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 11:49
 * @description
 */
open class SecureAppCompatActivity : AppCompatActivity() {

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == AppLockActivity.REQUEST_CODE) {
            AppLockActivity.handleLockResponse(this, resultCode)
        }
    }

    public override fun onResume() {
        super.onResume()
        AppLockActivity.protectWithLock(this, true)
    }

    public override fun onPause() {
        super.onPause()
        AppLockActivity.protectWithLock(this, false)
    }
}