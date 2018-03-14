package cn.mw.ethwallet.activities

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.widget.Toolbar
import android.widget.TextView
import cn.mw.ethwallet.R
import cn.mw.ethwallet.fragments.FragmentChooseRecipient
import cn.mw.ethwallet.fragments.FragmentSend
import cn.mw.ethwallet.views.NonSwipeViewPager

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 17:43
 * @description
 */
class SendActivity : SecureAppCompatActivity() {

    private var mViewPager: NonSwipeViewPager? = null
    private lateinit var fragments: Array<Fragment?>

    private var title: TextView? = null
    private var coord: CoordinatorLayout? = null
    internal lateinit var adapter: FragmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chooserecepient)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        title = findViewById(R.id.toolbar_title) as TextView

        coord = findViewById(R.id.main_content) as CoordinatorLayout

        fragments = arrayOfNulls(2)
        fragments[0] = FragmentChooseRecipient()
        fragments[1] = FragmentSend()
        val bundle = Bundle()

        if (intent.hasExtra("TO_ADDRESS"))
            bundle.putString("TO_ADDRESS", intent.getStringExtra("TO_ADDRESS"))
        if (intent.hasExtra("AMOUNT"))
            bundle.putString("AMOUNT", intent.getStringExtra("AMOUNT"))
        if (intent.hasExtra("FROM_ADDRESS"))
            bundle.putString("FROM_ADDRESS", intent.getStringExtra("FROM_ADDRESS"))

        fragments[1]!!.arguments = bundle

        adapter = FragmentAdapter(supportFragmentManager)

        mViewPager = findViewById(R.id.container) as NonSwipeViewPager
        mViewPager!!.setPagingEnabled(false)
        mViewPager!!.adapter = adapter

        if (intent.hasExtra("TO_ADDRESS"))
            mViewPager!!.currentItem = 1
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == QRScanActivity.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (fragments[0] == null) return
                (fragments[0] as FragmentChooseRecipient).setRecipientAddress(data!!.getStringExtra("ADDRESS"))
            } else {
                val mySnackbar = Snackbar.make(this.coord!!,
                        this.resources.getString(R.string.main_ac_wallet_added_fatal), Snackbar.LENGTH_SHORT)
                mySnackbar.show()
            }
        }
    }

    fun nextStage(toAddress: String) {
        mViewPager!!.currentItem = 1

        if (fragments[1] == null) return
        (fragments[1] as FragmentSend).setToAddress(toAddress, this)
    }


    internal inner class FragmentAdapter(private val mFragmentManager: FragmentManager) : FragmentPagerAdapter(mFragmentManager) {

        override fun getItem(position: Int): Fragment? {
            return fragments[position]
        }

        override fun getCount(): Int {
            return 2
        }
    }

    fun setTitle(s: String) {
        if (title != null) {
            title!!.text = s
            val mySnackbar = Snackbar.make(this.coord!!,
                    this@SendActivity.resources.getString(R.string.detail_acc_name_changed_suc), Snackbar.LENGTH_SHORT)
            mySnackbar.show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    @JvmOverloads
    fun snackError(s: String, length: Int = Snackbar.LENGTH_SHORT) {
        if (coord == null) return
        val mySnackbar = Snackbar.make(coord!!, s, length)
        mySnackbar.show()
    }

    companion object {

        val REQUEST_CODE = 200
    }

}