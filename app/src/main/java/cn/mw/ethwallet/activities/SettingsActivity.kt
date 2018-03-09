package cn.mw.ethwallet.activities

import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.ViewGroup
import cn.mw.ethwallet.R
import cn.mw.ethwallet.interfaces.AdDialogResponseHandler
import cn.mw.ethwallet.utils.Dialogs
import cn.mw.ethwallet.utils.Settings

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 20:10
 * @description
 */
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()

        fragmentManager.beginTransaction().replace(android.R.id.content,
                PrefsFragment()).commit()
    }

    private fun setupActionBar() {
        val rootView = findViewById(R.id.action_bar_root)//id from appcompat

        if (rootView != null) {
            rootView as ViewGroup
            val view = layoutInflater.inflate(R.layout.activity_settings, rootView, false)
            rootView.addView(view, 0)

            val toolbar = findViewById(R.id.toolbar) as Toolbar
            setSupportActionBar(toolbar)
        }

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    class PrefsFragment : PreferenceFragment() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.pref_general)

            val zeroAmountTx = findPreference("zeroAmountSwitch") as SwitchPreference
            zeroAmountTx.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, o ->
                Settings.showTransactionsWithZero = !zeroAmountTx.isChecked
                true
            }
            if ((activity.application as BaseApplication).isGooglePlayBuild) {
                val adSwitch = findPreference("showAd") as SwitchPreference
                adSwitch.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, o ->
                    if (adSwitch.isChecked) {
                        Dialogs.adDisable(activity, object : AdDialogResponseHandler {
                            override fun continueSettingChange(mContinue: Boolean) {
                                if (mContinue) {
                                    adSwitch.isChecked = false
                                    Settings.displayAds = false
                                }
                            }
                        })
                        false
                    } else {
                        Settings.displayAds = true
                        true
                    }
                }
            }
        }
    }

    companion object {

        val REQUEST_CODE = 800
    }

}