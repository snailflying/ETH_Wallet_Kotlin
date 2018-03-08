package cn.mw.ethwallet.activities

import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.widget.TextView
import cn.mw.ethwallet.R
import cn.mw.ethwallet.fragments.FragmentDetailOverview
import cn.mw.ethwallet.fragments.FragmentDetailShare
import cn.mw.ethwallet.fragments.FragmentTransactions
import cn.mw.ethwallet.utils.AddressNameConverter

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 17:24
 * @description
 */ class AddressDetailActivity : SecureAppCompatActivity() {

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private var mViewPager: ViewPager? = null
    private lateinit var fragments: Array<Fragment?>
    private var address: String? = null
    private var type: Byte = 0
    private var title: TextView? = null
    private var coord: CoordinatorLayout? = null
    var appBar: AppBarLayout? = null
        private set

    override protected fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        address = intent.getStringExtra("ADDRESS")
        type = intent.getByteExtra("TYPE", SCANNED_WALLET)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        title = findViewById(R.id.toolbar_title) as TextView
        val walletname = AddressNameConverter.getInstance(this).get(address!!)
        title!!.text = if (type == OWN_WALLET) walletname ?: "Unnamed Wallet" else "Address"

        coord = findViewById(R.id.main_content) as CoordinatorLayout
        appBar = findViewById(R.id.appbar) as AppBarLayout

        fragments = arrayOfNulls(3)
        fragments[0] = FragmentDetailShare()
        fragments[1] = FragmentDetailOverview()
        fragments[2] = FragmentTransactions()
        val bundle = Bundle()
        bundle.putString("ADDRESS", address)
        bundle.putDouble("BALANCE", intent.getDoubleExtra("BALANCE", 0.0))
        bundle.putByte("TYPE", type)
        fragments!![0]!!.arguments = bundle
        fragments!![1]!!.arguments = bundle
        fragments!![2]!!.arguments = bundle

        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        mViewPager = findViewById(R.id.container) as ViewPager
        mViewPager!!.adapter = mSectionsPagerAdapter

        val tabLayout = findViewById(R.id.tabs) as TabLayout
        tabLayout.setupWithViewPager(mViewPager)
        tabLayout.setupWithViewPager(mViewPager)

        tabLayout.getTabAt(0)!!.setIcon(R.drawable.ic_action_share)
        tabLayout.getTabAt(1)!!.setIcon(R.drawable.ic_wallet)
        tabLayout.getTabAt(2)!!.setIcon(R.drawable.ic_transactions)
        mViewPager!!.currentItem = 1

        mViewPager!!.offscreenPageLimit = 3
    }

    fun setTitle(s: String) {
        if (title != null) {
            title!!.text = s
            val mySnackbar = Snackbar.make(this.coord!!,
                    this@AddressDetailActivity.resources.getString(R.string.detail_acc_name_changed_suc), Snackbar.LENGTH_SHORT)
            mySnackbar.show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun snackError(s: String) {
        if (coord == null) return
        val mySnackbar = Snackbar.make(coord!!, s, Snackbar.LENGTH_SHORT)
        mySnackbar.show()
    }

    fun broadCastDataSetChanged() {
        if (fragments != null && fragments!![2] != null) {
            (fragments!![2] as FragmentTransactions).notifyDataSetChanged()
        }
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment? {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments!!.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return ""
        }
    }

    companion object {

        val OWN_WALLET: Byte = 0
        val SCANNED_WALLET: Byte = 1
    }

}