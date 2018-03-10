package cn.mw.ethwallet.fragments

import android.content.*
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.AppBarLayout
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import cn.mw.ethwallet.R
import cn.mw.ethwallet.activities.*
import cn.mw.ethwallet.adapters.WalletAdapter
import cn.mw.ethwallet.domain.request.WalletDisplay
import cn.mw.ethwallet.interfaces.AppBarStateChangeListener
import cn.mw.ethwallet.interfaces.PasswordDialogCallback
import cn.mw.ethwallet.interfaces.StorableWallet
import cn.mw.ethwallet.network.EtherscanAPI
import cn.mw.ethwallet.network.ResponseParser
import cn.mw.ethwallet.utils.*
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import uk.co.deanwild.materialshowcaseview.PrefsManager
import uk.co.deanwild.materialshowcaseview.PrefsManager.SEQUENCE_NEVER_STARTED
import uk.co.deanwild.materialshowcaseview.shape.RectangleShape
import java.io.IOException
import java.math.BigInteger


/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 16:48
 * @description
 */
abstract class FragmentWalletsAbstract : Fragment(), View.OnClickListener, View.OnCreateContextMenuListener {

    protected var recyclerView: RecyclerView? = null
    protected var walletAdapter: WalletAdapter? = null
    protected var wallets: MutableList<WalletDisplay> = java.util.ArrayList<WalletDisplay>()
    protected var ac: MainActivity? = null
    internal var balance = 0.0
    protected var balanceView: TextView? = null
    protected var swipeLayout: SwipeRefreshLayout? = null
    protected var nothingToShow: FrameLayout? = null
    protected var fabmenu: FloatingActionMenu? = null
    protected var gen_fab: FloatingActionButton? = null

    val displayedWalletCount: Int
        get() = wallets.size

    abstract fun adHandling(rootView: View)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_wallets, container, false)

        ac = this.activity as MainActivity

        adHandling(rootView)

        nothingToShow = rootView.findViewById(R.id.nothingToShow) as FrameLayout?
        val leftPress = rootView.findViewById(R.id.wleft) as ImageView
        val rightPress = rootView.findViewById(R.id.wright) as ImageView
        balanceView = rootView.findViewById(R.id.balance) as TextView?
        swipeLayout = rootView.findViewById(R.id.swipeRefreshLayout) as SwipeRefreshLayout?
        swipeLayout!!.setColorSchemeColors(ac!!.getResources().getColor(R.color.colorPrimary))
        swipeLayout!!.setOnRefreshListener {
            balance = 0.0
            try {
                update()
            } catch (e: IOException) {
                if (ac != null)
                    ac!!.snackError("Can't fetch account balances. No connection?")
                e.printStackTrace()
            }
        }

        ExchangeCalculator.instance.index = (ac!!.getPreferences(Context.MODE_PRIVATE).getInt("main_index", 0))

        leftPress.setOnClickListener {
            val cur = ExchangeCalculator.instance.previous()
            balanceView!!.setText(ExchangeCalculator.instance.displayBalanceNicely(ExchangeCalculator.instance.convertRate(balance, cur.rate)) + " " + cur.name)
            ac!!.broadCastDataSetChanged()
            walletAdapter!!.notifyDataSetChanged()
            val editor = ac!!.getPreferences(Context.MODE_PRIVATE).edit()
            editor.putInt("main_index", ExchangeCalculator.instance.index)
            editor.apply()
        }

        rightPress.setOnClickListener {
            val cur = ExchangeCalculator.instance.next()
            balanceView!!.setText(ExchangeCalculator.instance.displayBalanceNicely(ExchangeCalculator.instance.convertRate(balance, cur.rate)) + " " + cur.name)
            ac!!.broadCastDataSetChanged()
            walletAdapter!!.notifyDataSetChanged()
            val editor = ac!!.getPreferences(Context.MODE_PRIVATE).edit()
            editor.putInt("main_index", ExchangeCalculator.instance.index)
            editor.apply()
        }


        recyclerView = rootView.findViewById(R.id.recycler_view) as RecyclerView?
        walletAdapter = WalletAdapter(wallets, ac!!, this, this)
        val mgr = LinearLayoutManager(ac!!.getApplicationContext())
        recyclerView!!.layoutManager = mgr
        recyclerView!!.itemAnimator = DefaultItemAnimator()
        recyclerView!!.adapter = walletAdapter
        val dividerItemDecoration = DividerItemDecoration(recyclerView!!.context, mgr.orientation)
        recyclerView!!.addItemDecoration(dividerItemDecoration)


        walletAdapter!!.notifyDataSetChanged()

        fabmenu = rootView.findViewById(R.id.fabmenu) as FloatingActionMenu?
        val scan_fab = rootView.findViewById(R.id.scan_fab) as FloatingActionButton
        val add_fab = rootView.findViewById(R.id.add_fab) as FloatingActionButton
        gen_fab = rootView.findViewById(R.id.gen_fab) as FloatingActionButton

        gen_fab!!.setOnClickListener { generateDialog() }

        scan_fab.setOnClickListener {
            val scanQR = Intent(ac, QRScanActivity::class.java)
            scanQR.putExtra("TYPE", QRScanActivity.SCAN_ONLY)
            ac!!.startActivityForResult(scanQR, QRScanActivity.REQUEST_CODE)
        }
        add_fab.setOnClickListener { Dialogs.addWatchOnly(ac!!) }

        if (ac != null && ac!!.appBar != null) {
            ac!!.appBar!!.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
                override fun onStateChanged(appBarLayout: AppBarLayout, state: AppBarStateChangeListener.State) {
                    if (state === State.COLLAPSED) {
                        fabmenu!!.hideMenu(true)
                    } else {
                        fabmenu!!.showMenu(true)
                    }
                }
            })
        }

        try {
            update()
        } catch (e: IOException) {
            if (ac != null)
                ac!!.snackError("Can't fetch account balances. No connection?")
        }

        if ((ac!!.getApplication() as BaseApplication).isGooglePlayBuild) {
            (ac!!.getApplication() as BaseApplication).track("Wallet Fragment")
        }

        return rootView
    }

    @Throws(IOException::class)
    fun update() {
        if (ac == null) return
        wallets.clear()
        balance = 0.0
        val storedwallets = ArrayList<StorableWallet>(WalletStorage.getInstance(ac!!).get())

        if (storedwallets.size == 0) {
            nothingToShow!!.visibility = View.VISIBLE
            onItemsLoadComplete()
        } else {
            nothingToShow!!.visibility = View.GONE
            EtherscanAPI.instance.getBalances(storedwallets, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (ac != null)
                        ac!!.snackError("Can't fetch account balances. Invalid response.")
                    val w = java.util.ArrayList<WalletDisplay>()
                    for (cur in storedwallets)
                        w.add(WalletDisplay(AddressNameConverter.getInstance(ac!!).get(cur.pubKey)!!, cur.pubKey, BigInteger("-1"), WalletDisplay.CONTACT))

                    ac!!.runOnUiThread(Runnable {
                        wallets.addAll(w)
                        walletAdapter!!.notifyDataSetChanged()
                        onItemsLoadComplete()
                    })
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    val w: List<WalletDisplay>
                    try {
                        w = ResponseParser.parseWallets(response.body()!!.string(), storedwallets, ac!!)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return
                    }

                    ac!!.runOnUiThread(Runnable {
                        wallets.addAll(w)
                        walletAdapter!!.notifyDataSetChanged()
                        for (i in wallets.indices) {
                            balance += wallets[i].balance
                        }
                        balanceView!!.setText(ExchangeCalculator.instance.displayBalanceNicely(ExchangeCalculator.instance.convertRate(balance, ExchangeCalculator.instance.current.rate)) + " " + ExchangeCalculator.instance.current.name)
                        onItemsLoadComplete()
                    })
                }
            })
        }
    }


    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo) {
        menu.setHeaderTitle(R.string.wallet_menu_title)
        menu.add(0, 200, 0, R.string.wallet_menu_changename)
        menu.add(0, 201, 0, R.string.wallet_menu_copyadd)
        menu.add(0, 202, 0, R.string.wallet_menu_share)
        menu.add(0, 203, 0, R.string.wallet_menu_export)
        menu.add(0, 204, 0, R.string.wallet_menu_private_key)
        menu.add(0, 205, 0, R.string.wallet_menu_delete)
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        var position = -1
        try {
            position = walletAdapter!!.position
        } catch (e: Exception) {
            e.printStackTrace()
            return super.onContextItemSelected(item)
        }

        when (item!!.itemId) {
            200 // Change Address Name
            -> setName(wallets[position].publicKey!!)
            201 -> {
                if (ac == null) return true
                val clipboard = ac!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("", wallets[position].publicKey!!)
                clipboard.primaryClip = clip
                Toast.makeText(ac, R.string.wallet_menu_action_copied_to_clipboard, Toast.LENGTH_SHORT).show()
            }
            202 -> {
                val i = Intent(Intent.ACTION_SEND)
                i.type = "text/plain"
                i.putExtra(Intent.EXTRA_TEXT, wallets[position].publicKey!!)
                startActivity(Intent.createChooser(i, "Share via"))
            }
            203 -> {
                val finalPosition = position
                if (wallets[finalPosition].type === WalletDisplay.NORMAL) {
                    Dialogs.exportWallet(ac!!, DialogInterface.OnClickListener { dialog, which ->
                        WalletStorage.getInstance(ac!!).setWalletForExport(wallets[finalPosition].publicKey!!)
                        export()
                        dialog.dismiss()
                    })
                } else {
                    Dialogs.cantExportNonWallet(ac!!)
                }
            }
            204 -> {
                val finalPosition2 = position
                if (wallets[finalPosition2].type === WalletDisplay.NORMAL) {
                    Dialogs.askForPasswordAndDecode(ac!!, wallets[finalPosition2].publicKey!!, object : PasswordDialogCallback {
                        override fun success(password: String) {
                            val i = Intent(ac, PrivateKeyActivity::class.java)
                            i.putExtra(PrivateKeyActivity.PASSWORD, password)
                            i.putExtra(PrivateKeyActivity.ADDRESS, wallets[finalPosition2].publicKey)
                            startActivity(i)
                        }

                        override fun canceled() {}
                    })
                } else {
                    Dialogs.cantExportNonWallet(ac!!)
                }
            }
            205 -> confirmDelete(wallets[position].publicKey!!, wallets[position].type)
        }
        return super.onContextItemSelected(item)
    }

    fun export() {
        val suc = WalletStorage.getInstance(ac!!).exportWallet(ac!!)
        if (suc)
            ac!!.snackError(getString(R.string.wallet_suc_exported))
        else
            ac!!.snackError(getString(R.string.wallet_no_permission))
    }

    fun generateDialog() {
        if (!Settings.walletBeingGenerated) {
            val genI = Intent(ac, WalletGenActivity::class.java)
            ac!!.startActivityForResult(genI, WalletGenActivity.REQUEST_CODE)
        } else {
            val builder: AlertDialog.Builder
            if (Build.VERSION.SDK_INT >= 24)
            // Otherwise buttons on 7.0+ are nearly invisible
                builder = AlertDialog.Builder(ac!!, R.style.AlertDialogTheme)
            else
                builder = AlertDialog.Builder(ac!!)
            builder.setTitle(R.string.wallet_one_at_a_time)
            builder.setMessage(R.string.wallet_one_at_a_time_text)
            builder.setNeutralButton(R.string.button_ok, DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
            builder.show()
        }
    }

    fun confirmDelete(address: String, type: Byte) {
        val builder: AlertDialog.Builder
        if (Build.VERSION.SDK_INT >= 24)
        // Otherwise buttons on 7.0+ are nearly invisible
            builder = AlertDialog.Builder(ac!!, R.style.AlertDialogTheme)
        else
            builder = AlertDialog.Builder(ac!!)
        builder.setTitle(R.string.wallet_removal_title)

        if (type == WalletDisplay.WATCH_ONLY)
            builder.setMessage(R.string.wallet_removal_sure)
        else if (type == WalletDisplay.NORMAL)
            builder.setMessage(getString(R.string.wallet_removal_privkey) + address)
        else if (type == java.lang.Byte.MAX_VALUE) {
            builder.setMessage(getString(R.string.wallet_removal_last_warning) + address)
        }
        builder.setPositiveButton(R.string.button_yes, DialogInterface.OnClickListener { dialog, which ->
            if (type == WalletDisplay.WATCH_ONLY || type == java.lang.Byte.MAX_VALUE) {
                WalletStorage.getInstance(ac!!).removeWallet(address, ac!!)
                dialog.dismiss()
                try {
                    update()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            } else {
                confirmDelete(address, java.lang.Byte.MAX_VALUE)
            }
        })
        builder.setNegativeButton(R.string.button_no, DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
        builder.show()

    }

    fun setName(address: String) {
        val builder: AlertDialog.Builder
        if (Build.VERSION.SDK_INT >= 24)
        // Otherwise buttons on 7.0+ are nearly invisible
            builder = AlertDialog.Builder(ac!!, R.style.AlertDialogTheme)
        else
            builder = AlertDialog.Builder(ac!!)
        builder.setTitle(R.string.name_your_wallet)

        val input = EditText(ac)
        input.setText(AddressNameConverter.getInstance(ac!!).get(address))
        input.setSingleLine()
        val container = FrameLayout(ac!!)
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.leftMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
        params.topMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
        params.bottomMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
        params.rightMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
        input.layoutParams = params
        input.setSelection(input.text.length)

        input.inputType = InputType.TYPE_CLASS_TEXT
        container.addView(input)
        builder.setView(container)
        input.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                val inputMgr = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
            }
        }
        builder.setPositiveButton(R.string.button_ok, DialogInterface.OnClickListener { dialog, which ->
            AddressNameConverter.getInstance(ac!!).put(address, input.text.toString(), ac!!)
            val inputMgr = input.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMgr.hideSoftInputFromWindow(input.windowToken, 0)
            notifyDataSetChanged()
            dialog.dismiss()
        })
        builder.setNegativeButton(R.string.button_cancel, DialogInterface.OnClickListener { dialog, which ->
            val inputMgr = input.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMgr.hideSoftInputFromWindow(input.windowToken, 0)
            dialog.cancel()
        })

        builder.show()

    }

    internal fun onItemsLoadComplete() {
        if (swipeLayout == null) return
        swipeLayout!!.isRefreshing = false
        Handler().postDelayed({ handleShowCase() }, 1000)
    }

    private fun handleShowCase() {
        if (ac!!.getSharedPreferences("material_showcaseview_prefs", 0).getInt("status_BACKUP_WALLET", SEQUENCE_NEVER_STARTED) !== PrefsManager.SEQUENCE_FINISHED
                && ac!!.getSharedPreferences("material_showcaseview_prefs", 0).getInt("status_GENERATE_WALLET", SEQUENCE_NEVER_STARTED) === PrefsManager.SEQUENCE_FINISHED
                && recyclerView != null && recyclerView!!.childCount > 0) {
            ac!!.setSelectedPage(1)
            MaterialShowcaseView.Builder(ac)
                    .setTarget(recyclerView!!.getChildAt(0))
                    .setDismissText(getString(R.string.showcase_got_it))
                    .setContentText(getString(R.string.show_case_backup))
                    .setDelay(150)
                    .setDismissOnTargetTouch(true)
                    .setDismissOnTouch(true)
                    .setShape(RectangleShape(Rect(), true))
                    .singleUse("BACKUP_WALLET")
                    .show()
        }

        if (ac!!.getSharedPreferences("material_showcaseview_prefs", 0).getInt("status_GENERATE_WALLET", SEQUENCE_NEVER_STARTED) !== PrefsManager.SEQUENCE_FINISHED) {
            ac!!.setSelectedPage(1)
            fabmenu!!.open(true)
            MaterialShowcaseView.Builder(ac)
                    .setTarget(gen_fab)
                    .setDismissText(getString(R.string.showcase_got_it))
                    .setContentText(getString(R.string.show_case_wallet_gen_text))
                    .setDelay(150)
                    .setDismissOnTargetTouch(true)
                    .setDismissOnTouch(true)
                    .singleUse("GENERATE_WALLET")
                    .show()
        }
    }

    fun notifyDataSetChanged() {
        if (walletAdapter != null)
            walletAdapter!!.notifyDataSetChanged()
        updateBalanceText()
    }

    fun updateBalanceText() {
        if (balanceView != null)
            balanceView!!.setText(ExchangeCalculator.instance.displayBalanceNicely(ExchangeCalculator.instance.convertRate(balance, ExchangeCalculator.instance.current.rate)) + " " + ExchangeCalculator.instance.current.name)
    }

    override fun onClick(view: View) {
        val itemPosition = recyclerView!!.getChildLayoutPosition(view)
        if (itemPosition >= wallets.size) return
        val detail = Intent(ac, AddressDetailActivity::class.java)
        detail.putExtra("ADDRESS", wallets[itemPosition].publicKey)
        detail.putExtra("BALANCE", wallets[itemPosition].balance)
        detail.putExtra("TYPE", AddressDetailActivity.OWN_WALLET)
        startActivity(detail)
    }
}