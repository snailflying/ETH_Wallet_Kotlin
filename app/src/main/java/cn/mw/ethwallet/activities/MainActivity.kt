package cn.mw.ethwallet.activities

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.View
import cn.mw.ethwallet.R
import cn.mw.ethwallet.domain.request.WatchWallet
import cn.mw.ethwallet.fragments.FragmentPrice
import cn.mw.ethwallet.fragments.FragmentTransactionsAll
import cn.mw.ethwallet.fragments.FragmentWallets
import cn.mw.ethwallet.interfaces.NetworkUpdateListener
import cn.mw.ethwallet.services.NotificationLauncher
import cn.mw.ethwallet.services.WalletGenService
import cn.mw.ethwallet.utils.*
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import okhttp3.Response
import java.io.IOException
import java.math.BigDecimal
import java.security.Security

class MainActivity : SecureAppCompatActivity(), NetworkUpdateListener {

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private var mViewPager: ViewPager? = null
    lateinit var fragments: Array<Fragment?>
    private var tabLayout: TabLayout? = null
    private var coord: CoordinatorLayout? = null
    var preferences: SharedPreferences? = null
        private set
    var appBar: AppBarLayout? = null
        private set
    private var generateRefreshCount: Int = 0

    override protected fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // App Intro
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        if (preferences!!.getLong("APP_INSTALLED", 0) == 0L) {
            val intro = Intent(this, AppIntroActivity::class.java)
            startActivityForResult(intro, AppIntroActivity.REQUEST_CODE)
        }

        Settings.displayAds = preferences!!.getBoolean("showAd", true)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        // ------------------------- Material Drawer ---------------------------------
        val headerResult = AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.ethereum_bg)
                .build()

        val wip = DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .withSelectedItem(-1)

                .addDrawerItems(
                        PrimaryDrawerItem().withName(getResources().getString(R.string.drawer_import)).withIcon(R.drawable.ic_action_wallet3),
                        PrimaryDrawerItem().withName(getResources().getString(R.string.action_settings)).withIcon(R.drawable.ic_setting),
                        PrimaryDrawerItem().withName(getResources().getString(R.string.drawer_about)).withIcon(R.drawable.ic_about),
                        PrimaryDrawerItem().withName(getResources().getString(R.string.reddit)).withIcon(R.drawable.ic_reddit)
                )
                .withOnDrawerItemClickListener { view, position, drawerItem ->
                    selectItem(position)
                    false
                }
                .withOnDrawerListener(object : Drawer.OnDrawerListener {

                    override fun onDrawerOpened(drawerView: View) {

                    }

                    override fun onDrawerClosed(drawerView: View) {
                        //changeStatusBarColor();
                    }

                    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                        //changeStatusBarTranslucent();
                    }
                })


        val result = wip.build()

        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        result.actionBarDrawerToggle.isDrawerIndicatorEnabled = true

        // ------------------------------------------------------------------------

        coord = findViewById(R.id.main_content) as CoordinatorLayout
        appBar = findViewById(R.id.appbar) as AppBarLayout

        fragments = arrayOfNulls(3)
        fragments[0] = FragmentPrice()
        fragments[1] = FragmentWallets()
        fragments[2] = FragmentTransactionsAll()


        mSectionsPagerAdapter = SectionsPagerAdapter(getSupportFragmentManager())
        mViewPager = findViewById(R.id.container) as ViewPager
        mViewPager!!.adapter = mSectionsPagerAdapter

        tabLayout = findViewById(R.id.tabs) as TabLayout
        tabLayout!!.setupWithViewPager(mViewPager)
        tabLayout!!.setupWithViewPager(mViewPager)

        tabLayout!!.getTabAt(0)!!.setIcon(R.drawable.ic_price)
        tabLayout!!.getTabAt(1)!!.setIcon(R.drawable.ic_wallet)
        tabLayout!!.getTabAt(2)!!.setIcon(R.drawable.ic_transactions)

        try {
            ExchangeCalculator.instance.updateExchangeRates(preferences!!.getString("maincurrency", "USD"), this)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        Settings.initiate(this)
        NotificationLauncher.instance.start(this)

        if (getIntent().hasExtra("STARTAT")) { //  Click on Notification, show Transactions
            if (tabLayout != null)
                tabLayout!!.getTabAt(getIntent().getIntExtra("STARTAT", 2))!!.select()
            broadCastDataSetChanged()
        } else if (Settings.startWithWalletTab) { // if enabled in setting select wallet tab instead of price tab
            if (tabLayout != null)
                tabLayout!!.getTabAt(1)!!.select()
        }

        mViewPager!!.offscreenPageLimit = 3

        //Security.removeProvider("BC");
        Security.insertProviderAt(org.spongycastle.jce.provider.BouncyCastleProvider(), 1)

    }

    fun setSelectedPage(i: Int) {
        if (mViewPager != null)
            mViewPager!!.setCurrentItem(i, true)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            ExternalStorageHandler.REQUEST_WRITE_STORAGE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (fragments != null && fragments!![1] != null)
                        (fragments!![1] as FragmentWallets).export()
                } else {
                    snackError(getString(R.string.main_grant_permission_export))
                }
                return
            }
            ExternalStorageHandler.REQUEST_READ_STORAGE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        WalletStorage.getInstance(this).importingWalletsDetector(this)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                } else {
                    snackError(getString(R.string.main_grant_permission_import))
                }
                return
            }
        }
    }

    override fun onResume() {
        super.onResume()
        broadCastDataSetChanged()

        // Update wallets if activity resumed and a new wallet was found (finished generation or added as watch only address)
        if (fragments != null && fragments!![1] != null && WalletStorage.getInstance(this).get()!!.size !== (fragments!![1] as FragmentWallets).displayedWalletCount) {
            try {
                (fragments!![1] as FragmentWallets).update()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    override fun onPause() {
        super.onPause()
        /* if(preferences == null)
            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("APP_PAUSED", true);
        editor.apply();*/
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == QRScanActivity.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val type = data.getByteExtra("TYPE", QRScanActivity.SCAN_ONLY)
                if (type == QRScanActivity.SCAN_ONLY) {
                    if (data.getStringExtra("ADDRESS").length != 42 || !data.getStringExtra("ADDRESS").startsWith("0x")) {
                        snackError("Invalid Ethereum address!")
                        return
                    }
                    val watch = Intent(this, AddressDetailActivity::class.java)
                    watch.putExtra("ADDRESS", data.getStringExtra("ADDRESS"))
                    startActivity(watch)
                } else if (type == QRScanActivity.ADD_TO_WALLETS) {
                    if (data.getStringExtra("ADDRESS").length != 42 || !data.getStringExtra("ADDRESS").startsWith("0x")) {
                        snackError("Invalid Ethereum address!")
                        return
                    }
                    val suc = WalletStorage.getInstance(this).add(WatchWallet(data.getStringExtra("ADDRESS")), this)
                    Handler().postDelayed(
                            {
                                if (fragments != null && fragments!![1] != null) {
                                    try {
                                        (fragments!![1] as FragmentWallets).update()
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }

                                }
                                if (tabLayout != null)
                                    tabLayout!!.getTabAt(1)!!.select()
                                val mySnackbar = Snackbar.make(this!!.coord!!,
                                        this@MainActivity.getResources().getString(if (suc) R.string.main_ac_wallet_added_suc else R.string.main_ac_wallet_added_er), Snackbar.LENGTH_SHORT)
                                if (suc)
                                    AddressNameConverter.getInstance(this@MainActivity).put(data.getStringExtra("ADDRESS"), "Watch " + data.getStringExtra("ADDRESS").substring(0, 6), this@MainActivity)

                                mySnackbar.show()
                            }, 100)
                } else if (type == QRScanActivity.REQUEST_PAYMENT) {
                    if (WalletStorage.getInstance(this).fullOnly.size === 0) {
                        Dialogs.noFullWallet(this)
                    } else {
                        val watch = Intent(this, SendActivity::class.java)
                        watch.putExtra("TO_ADDRESS", data.getStringExtra("ADDRESS"))
                        watch.putExtra("AMOUNT", data.getStringExtra("AMOUNT"))
                        startActivity(watch)
                    }
                } else if (type == QRScanActivity.PRIVATE_KEY) {
                    if (OwnWalletUtils.isValidPrivateKey(data.getStringExtra("ADDRESS"))) {
                        importPrivateKey(data.getStringExtra("ADDRESS"))
                    } else {
                        this.snackError("Invalid private key!")
                    }
                }
            } else {
                val mySnackbar = Snackbar.make(coord!!,
                        this@MainActivity.getResources().getString(R.string.main_ac_wallet_added_fatal), Snackbar.LENGTH_SHORT)
                mySnackbar.show()
            }
        } else if (requestCode == WalletGenActivity.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val generatingService = Intent(this, WalletGenService::class.java)
                generatingService.putExtra("PASSWORD", data.getStringExtra("PASSWORD"))
                if (data.hasExtra("PRIVATE_KEY"))
                    generatingService.putExtra("PRIVATE_KEY", data.getStringExtra("PRIVATE_KEY"))
                startService(generatingService)

                val handler = Handler()
                generateRefreshCount = 0
                val walletcount = WalletStorage.getInstance(this).fullOnly.size
                val runnable = object : Runnable {
                    override fun run() {
                        try {
                            if (walletcount < WalletStorage.getInstance(this@MainActivity).fullOnly.size) {
                                (fragments!![1] as FragmentWallets).update()
                                return
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        if (generateRefreshCount++ < 8)
                            handler.postDelayed(this, 3000)
                    }
                }
                handler.postDelayed(runnable, 4000)
            }
        } else if (requestCode == SendActivity.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (fragments == null || fragments!![2] == null) return
                (fragments!![2] as FragmentTransactionsAll).addUnconfirmedTransaction(data.getStringExtra("FROM_ADDRESS"), data.getStringExtra("TO_ADDRESS"), BigDecimal("-" + data.getStringExtra("AMOUNT")).multiply(BigDecimal("1000000000000000000")).toBigInteger())
                if (tabLayout != null)
                    tabLayout!!.getTabAt(2)!!.select()
            }
        } else if (requestCode == AppIntroActivity.REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                finish()
            } else {
                val editor = preferences!!.edit()
                editor.putLong("APP_INSTALLED", System.currentTimeMillis())
                editor.commit()
            }
        } else if (requestCode == SettingsActivity.REQUEST_CODE) {
            if (preferences!!.getString("maincurrency", "USD") != ExchangeCalculator.instance.mainCurreny.name) {
                try {
                    ExchangeCalculator.instance.updateExchangeRates(preferences!!.getString("maincurrency", "USD"), this)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                Handler().postDelayed(
                        {
                            if (fragments != null) {
                                if (fragments!![0] != null)
                                    (fragments!![0] as FragmentPrice).update(true)
                                if (fragments!![1] != null) {
                                    (fragments!![1] as FragmentWallets).updateBalanceText()
                                    (fragments!![1] as FragmentWallets).notifyDataSetChanged()
                                }
                                if (fragments!![2] != null)
                                    (fragments!![2] as FragmentTransactionsAll).notifyDataSetChanged()
                            }
                        }, 950)
            }

        }
    }

    fun importPrivateKey(privatekey: String) {
        val genI = Intent(this, WalletGenActivity::class.java)
        genI.putExtra("PRIVATE_KEY", privatekey)
        startActivityForResult(genI, WalletGenActivity.REQUEST_CODE)
    }

    @JvmOverloads
    fun snackError(s: String, length: Int = Snackbar.LENGTH_SHORT) {
        if (coord == null) return
        val mySnackbar = Snackbar.make(coord!!, s, length)
        mySnackbar.show()
    }

    private fun selectItem(position: Int) {
        when (position) {
            1 -> {
                try {
                    WalletStorage.getInstance(this).importingWalletsDetector(this)

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            2 -> {
                val settings = Intent(this, SettingsActivity::class.java)
                startActivityForResult(settings, SettingsActivity.REQUEST_CODE)
            }
            3 -> {
                AlertDialog.Builder(this)
                        .setTitle(R.string.about_us)
                        .setMessage(R.string.about_us_content)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show()
            }
            4 -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse("https://github.com/snailflying/ETH_Wallet_Kotlin")
                startActivity(i)
            }
            5 -> {
                if (WalletStorage.getInstance(this).fullOnly.size === 0) {
                    Dialogs.noFullWallet(this)
                } else {
                    val donate = Intent(this, SendActivity::class.java)
                    donate.putExtra("TO_ADDRESS", "0xa9981a33f6b1A18da5Db58148B2357f22B44e1e0")
                    startActivity(donate)
                }
            }
            else -> {
                return
            }
        }
    }

    fun broadCastDataSetChanged() {
        if (fragments != null && fragments!![1] != null && fragments!![2] != null) {
            (fragments!![1] as FragmentWallets).notifyDataSetChanged()
            (fragments!![2] as FragmentTransactionsAll).notifyDataSetChanged()
        }
    }

    override fun onUpdate(s: Response) {
        runOnUiThread(Runnable {
            broadCastDataSetChanged()
            if (fragments != null && fragments!![0] != null) {
                (fragments!![0] as FragmentPrice).update(true)
            }
        })
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment? {
            return fragments!![position]
        }

        override fun getCount(): Int {
            return fragments!!.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return ""
        }
    }

    companion object {

        // Spongy Castle Provider
        init {
            Security.insertProviderAt(org.spongycastle.jce.provider.BouncyCastleProvider(), 1)
        }
    }
}
