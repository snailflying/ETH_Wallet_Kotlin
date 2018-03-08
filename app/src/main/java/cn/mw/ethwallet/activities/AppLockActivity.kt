package cn.mw.ethwallet.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.support.annotation.RequiresApi
import android.util.Log
import android.view.View
import android.widget.Toast
import cn.mw.ethwallet.R
import cn.mw.ethwallet.interfaces.FingerprintListener
import cn.mw.ethwallet.utils.AppLockUtils
import cn.mw.ethwallet.utils.FingerprintHelper
import kotlinx.android.synthetic.main.app_lock_activity.*
import me.zhanghai.android.patternlock.PatternUtils
import me.zhanghai.android.patternlock.PatternView
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 11:57
 * @description
 */
class AppLockActivity : BasePatternActivity(), PatternView.OnPatternListener, FingerprintListener {

    private var mNumFailedAttempts = 0
    private var hasFingerprintSupport = false
    private var fingerprintHelper: FingerprintHelper? = null

    private var keyStore: KeyStore? = null
    private var cipher: Cipher? = null
    private var fingerprintManager: FingerprintManager? = null
    private var cryptoObject: FingerprintManager.CryptoObject? = null
    private var unlockedWithoutFprint = false

    private var sharedPreferences: SharedPreferences? = null

    override fun onBackPressed() {
        unlockedFirst = false
    }

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pl_message_text.setText(R.string.pl_draw_pattern_to_unlock)
        //pl_pattern.setInStealthMode(true);
        pl_pattern.setOnPatternListener(this)
//        fingerprintcontainer = findViewById(R.id.fingerprintcontainer) as LinearLayout
        hasFingerprintSupport = AppLockUtils.hasDeviceFingerprintSupport(this)

        if (hasFingerprintSupport()) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            setupFingerprintStuff()
        }

        fingerprintcontainer.visibility = if (hasFingerprintSupport) View.VISIBLE else View.GONE

        if (savedInstanceState == null) {
            mNumFailedAttempts = 0
        } else {
            mNumFailedAttempts = savedInstanceState.getInt("num_failed_attempts")
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun setupFingerprintStuff() {
        fingerprintManager = this.getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
        fingerprintHelper = FingerprintHelper(this)
        try {
            generateKey()

            if (cipherInit()) {
                cryptoObject = FingerprintManager.CryptoObject(cipher!!)
                fingerprintHelper!!.startAuth(fingerprintManager!!, cryptoObject!!)
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun cipherInit(): Boolean {
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        }

        try {
            keyStore!!.load(null)
            val key = keyStore!!.getKey("Lunary", null) as SecretKey
            cipher!!.init(Cipher.ENCRYPT_MODE, key)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected fun generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore")
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        try {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

            keyStore!!.load(null)
            keyGenerator.init(KeyGenParameterSpec.Builder("Lunary",
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build())
            keyGenerator.generateKey()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    protected override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("num_failed_attempts", mNumFailedAttempts)
    }


    override fun onPatternDetected(pattern: List<PatternView.Cell>) {
        if (sharedPreferences!!.getLong("WRONG_PATTERN_LOCK", 0) != 0L && sharedPreferences!!.getLong("WRONG_PATTERN_LOCK", 0) > System.currentTimeMillis() - 60 * 1000) {
            pl_message_text.setText("Locked for 1 minute!")
            postClearPatternRunnable()
            return
        }
        if (isPatternCorrect(pattern)) {
            unlockedWithoutFprint = true
            mNumFailedAttempts = 0
            onConfirmed()
        } else {
            pl_message_text.setText(R.string.pl_wrong_pattern)
            pl_pattern.setDisplayMode(PatternView.DisplayMode.Wrong)
            postClearPatternRunnable()
            onWrongPattern()
        }
    }

    fun hasFingerprintSupport(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && hasFingerprintSupport
    }

    protected override fun onPause() {
        super.onPause()

        if (fingerprintHelper != null && hasFingerprintSupport())
            fingerprintHelper!!.cancel()
    }

    override fun onResume() {
        super.onResume()
        if (sharedPreferences == null)
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        if (fingerprintHelper != null && hasFingerprintSupport())
            setupFingerprintStuff()
    }

    protected fun isPatternCorrect(pattern: List<PatternView.Cell>): Boolean {
        return PatternUtils.patternToSha1String(pattern) == sharedPreferences!!.getString("APP_LOCK_PATTERN", "")
    }

    protected fun onConfirmed() {
        setResult(RESULT_OK)
        finish()
    }

    protected fun onWrongPattern() {
        ++mNumFailedAttempts
        if (mNumFailedAttempts >= 5) {
            val editor = sharedPreferences!!.edit()
            editor.putLong("WRONG_PATTERN_LOCK", System.currentTimeMillis())
            editor.commit()
            pl_message_text.setText("Locked for 1 minute!")
            mNumFailedAttempts = 0
        }
    }

    override fun onPatternStart() {
        removeClearPatternRunnable()
        pl_pattern.setDisplayMode(PatternView.DisplayMode.Correct)
    }

    override fun onPatternCleared() {
        removeClearPatternRunnable()
    }

    override fun onPatternCellAdded(pattern: List<PatternView.Cell>) {}

    override fun authenticationFailed(error: String) {
        Log.d("fingerprintauth", "FAILED: " + error)
        if (!unlockedWithoutFprint && error != "Fingerprint operation cancelled.")
            Toast.makeText(this, "You are not authorized!", Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun authenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
        Log.d("fingerprintauth", "SUCCESS!")
        onConfirmed()
    }

    companion object {

        val REQUEST_CODE = 1000

        private var pausedFirst = false
        private var unlockedFirst = false

        fun protectWithLock(c: Activity, onResume: Boolean) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(c.applicationContext)
            if (!preferences.getBoolean("use_app_lock", false)) return

            Log.d("secureactivity", onResume.toString() + " ||>> " + unlockedFirst)
            if (!onResume && unlockedFirst) { //pausedFirst
                val editor = preferences.edit()
                editor.putLong("APP_UNLOCKED", System.currentTimeMillis())
                editor.apply()
            }

            // Ask for login if pw protection is enabled and last login is more than 4 minutes ago
            if (preferences.getLong("APP_UNLOCKED", 0) <= System.currentTimeMillis() - 4 * 60 * 1000
                    && onResume && !pausedFirst && preferences.getString("APP_LOCK_PATTERN", "") != "") {
                val patternLock = Intent(c, AppLockActivity::class.java)
                c.startActivityForResult(patternLock, AppLockActivity.REQUEST_CODE)
            }
            pausedFirst = onResume
        }

        fun handleLockResponse(c: Activity, resultCode: Int) {
            if (resultCode != RESULT_OK) {
                c.finish()
                unlockedFirst = false
            } else {
                val preferences = PreferenceManager.getDefaultSharedPreferences(c.applicationContext)
                val editor = preferences.edit()
                editor.putLong("APP_UNLOCKED", System.currentTimeMillis())
                editor.apply()
                unlockedFirst = true
            }
        }
    }
}