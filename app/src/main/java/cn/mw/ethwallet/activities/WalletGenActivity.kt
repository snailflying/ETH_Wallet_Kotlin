package cn.mw.ethwallet.activities

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import cn.mw.ethwallet.R
import cn.mw.ethwallet.utils.Dialogs
import cn.mw.ethwallet.utils.Settings
import kotlinx.android.synthetic.main.activity_wallet_gen.*

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 18:35
 * @description
 */
class WalletGenActivity : SecureAppCompatActivity() {

    //    private var password: EditText? = null
//    private var passwordConfirm: EditText? = null
//    private var coord: CoordinatorLayout? = null
//    private var walletGenText: TextView? = null
//    private var toolbar_title: TextView? = null
    private var privateKeyProvided: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet_gen)

//        password = findViewById<EditText>(R.id.password)
//        passwordConfirm = findViewById<EditText>(R.id.passwordConfirm)
//        walletGenText = findViewById<TextView>(R.id.walletGenText)
//        toolbar_title = findViewById<TextView>(R.id.toolbar_title)


//        coord = findViewById<CoordinatorLayout>(R.id.main_content)

//        val mEmailSignInButton = findViewById(R.id.email_sign_in_button)
        email_sign_in_button.setOnClickListener { genCheck() }

        if (intent.hasExtra("PRIVATE_KEY")) {
            privateKeyProvided = intent.getStringExtra("PRIVATE_KEY")
            walletGenText!!.text = resources.getText(R.string.import_text)
            toolbar_title!!.setText(R.string.import_title)
            email_sign_in_button.setText(R.string.import_button)
        }

        if ((this.application as BaseApplication).isGooglePlayBuild) {
            (this.application as BaseApplication).track("Walletgen Activity")
        }
    }

    private fun genCheck() {
        if (passwordConfirm!!.text.toString() != password!!.text.toString()) {
            snackError(resources.getString(R.string.error_incorrect_password))
            return
        }
        if (!isPasswordValid(passwordConfirm!!.text.toString())) {
            snackError(resources.getString(R.string.error_invalid_password))
            return
        }
        Dialogs.writeDownPassword(this)
    }

    fun gen() {
        Settings.walletBeingGenerated = true // Lock so a user can only generate one wallet at a time

        // For statistics only
        if ((this.application as BaseApplication).isGooglePlayBuild) {
            (this.application as BaseApplication).event("Wallet generated")
        }

        val data = Intent()
        data.putExtra("PASSWORD", passwordConfirm!!.text.toString())
        if (privateKeyProvided != null)
            data.putExtra("PRIVATE_KEY", privateKeyProvided)
        setResult(RESULT_OK, data)
        finish()
    }


    fun snackError(s: String) {
        if (main_content == null) return
        val mySnackbar = Snackbar.make(main_content!!, s, Snackbar.LENGTH_SHORT)
        mySnackbar.show()
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 9
    }

    companion object {

        val REQUEST_CODE = 401
    }


}