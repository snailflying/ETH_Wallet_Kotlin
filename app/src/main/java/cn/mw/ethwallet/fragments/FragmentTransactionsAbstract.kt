package cn.mw.ethwallet.fragments

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import cn.mw.ethwallet.R
import cn.mw.ethwallet.activities.AddressDetailActivity
import cn.mw.ethwallet.activities.BaseApplication
import cn.mw.ethwallet.activities.RequestEtherActivity
import cn.mw.ethwallet.activities.SendActivity
import cn.mw.ethwallet.adapters.TransactionAdapter
import cn.mw.ethwallet.domain.TransactionDisplay
import cn.mw.ethwallet.utils.AddressNameConverter
import cn.mw.ethwallet.utils.Dialogs
import cn.mw.ethwallet.utils.WalletStorage
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import java.util.*

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 17:27
 * @description
 */
abstract class FragmentTransactionsAbstract : Fragment(), View.OnClickListener, View.OnCreateContextMenuListener {

    protected lateinit var recyclerView: RecyclerView
    protected var walletAdapter: TransactionAdapter? = null

    private var wallets: MutableList<TransactionDisplay> = ArrayList<TransactionDisplay>()

    protected var ac: Activity? = null
    protected var address: String? = null
    protected var swipeLayout: SwipeRefreshLayout? = null
    @get:Synchronized
    private var requestCount = 0
        private set  // used to count to two (since internal and normal transactions are each one request). Gets icnreased once one request is finished. If it is two, notifyDataChange is called (display transactions)
    protected var requestTx: FloatingActionButton? = null
    protected lateinit var send: FloatingActionButton
    protected lateinit var nothingToShow: FrameLayout
    protected lateinit var fabmenu: FloatingActionMenu

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_transaction, container, false)

        ac = this.activity!!
        if (arguments != null) {
            address = arguments!!.getString("ADDRESS")
            (rootView.findViewById(R.id.infoText) as TextView).setText(R.string.trans_no_trans_found)
        }

        nothingToShow = rootView.findViewById(R.id.nothingToShow) as FrameLayout
        recyclerView = rootView.findViewById(R.id.recycler_view) as RecyclerView
        walletAdapter = TransactionAdapter(wallets, ac!!, this, this)
        val mgr = LinearLayoutManager(ac!!.applicationContext)
        recyclerView.layoutManager = mgr
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = walletAdapter
        val dividerItemDecoration = DividerItemDecoration(recyclerView.context,
                mgr.orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)
        swipeLayout = rootView.findViewById(R.id.swipeRefreshLayout2) as SwipeRefreshLayout?
        swipeLayout!!.setColorSchemeColors(ac!!.resources.getColor(R.color.colorPrimary))
        swipeLayout!!.setOnRefreshListener { update(true) }

        send = rootView.findViewById(R.id.newTransaction) as FloatingActionButton
        requestTx = rootView.findViewById(R.id.requestTx) as FloatingActionButton?
        fabmenu = rootView.findViewById(R.id.fabmenu) as FloatingActionMenu

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (address != null) return
                if (dy > 0)
                    fabmenu.hideMenu(true)
                else if (dy < 0)
                    fabmenu.showMenu(true)
            }
        })


        requestTx!!.setOnClickListener { openRequestActivity() }
        send.setOnClickListener { openSendActivity() }

        update(false)
        walletAdapter!!.notifyDataSetChanged()

        if ((ac!!.application as BaseApplication).isGooglePlayBuild) {
            (ac!!.application as BaseApplication).track("Transaction Fragment")
        }
        return rootView
    }

    private fun openSendActivity() {
        if (WalletStorage.getInstance(this!!.ac!!).fullOnly.size === 0) {
            Dialogs.noFullWallet(ac!!)
        } else {
            val newTrans = Intent(ac, SendActivity::class.java)
            if (address != null)
                newTrans.putExtra("FROM_ADDRESS", address)
            ac!!.startActivityForResult(newTrans, SendActivity.REQUEST_CODE)
        }
    }

    private fun openRequestActivity() {
        if (WalletStorage.getInstance(ac!!).get()!!.size === 0) {
            Dialogs.noWallet(ac!!)
        } else {
            val newTrans = Intent(ac, RequestEtherActivity::class.java)
            ac!!.startActivity(newTrans)
        }
    }

    fun notifyDataSetChanged() {
        if (walletAdapter != null)
            walletAdapter!!.notifyDataSetChanged()
    }

    abstract fun update(force: Boolean)

    @Synchronized
    fun addRequestCount() {
        requestCount++
    }

    @Synchronized
    fun getRequestCount(): Int {
        return requestCount
    }

    @Synchronized
    fun resetRequestCount() {
        requestCount = 0
    }

    internal fun onItemsLoadComplete() {
        if (swipeLayout == null) return
        swipeLayout!!.isRefreshing = false
    }

    @Synchronized
    protected fun getWallets(): MutableList<TransactionDisplay> {
        return wallets
    }

    @Synchronized
    fun setWallets(wal: List<TransactionDisplay>) {
        wallets = wal as MutableList<TransactionDisplay>
    }


    @Synchronized
    fun addToWallets(w: List<TransactionDisplay>) {
        wallets.addAll(w)
        Collections.sort<TransactionDisplay>(getWallets()) { o1, o2 -> o1.compareTo(o2) }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo) {
        menu.setHeaderTitle(R.string.trans_menu_title)
        menu.add(0, 100, 0, R.string.trans_menu_changename)//groupId, itemId, order, title
        menu.add(0, 101, 0, R.string.trans_menu_viewreceiver)
        menu.add(0, 102, 0, R.string.trans_menu_openinb)
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        var position = -1
        try {
            position = TransactionAdapter.calculateBoxPosition(walletAdapter!!.position)
        } catch (e: Exception) {
            e.printStackTrace()
            return super.onContextItemSelected(item)
        }

        //Log.d("wubalabadubdub","Wuuu: "+item.getItemId());
        when (item!!.itemId) {
            100 -> { // Change Address Name
                setName(wallets[position].getToAddress())
            }
            101 -> { // Open in AddressDetailActivity
                val i = Intent(ac, AddressDetailActivity::class.java)
                i.putExtra("ADDRESS", wallets[position].getToAddress())
                startActivity(i)
            }
            102 -> { // Open in Browser
                val url = "https://etherscan.io/tx/" + wallets[position].txHash
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            }
        }
        return super.onContextItemSelected(item)
    }


    fun setName(address: String) {
        val builder: AlertDialog.Builder
        if (Build.VERSION.SDK_INT >= 24)
        // Otherwise buttons on 7.0+ are nearly invisible
            builder = AlertDialog.Builder(ac!!, R.style.AlertDialogTheme)
        else
            builder = AlertDialog.Builder(ac!!)
        builder.setTitle(R.string.name_other_address)

        val input = EditText(ac)
        input.setText(AddressNameConverter.getInstance(ac!!).get(address))
        input.inputType = InputType.TYPE_CLASS_TEXT
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
        builder.setNegativeButton(R.string.button_cancel, DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        builder.show()
    }

    override fun onClick(view: View) {
        if (ac == null) return
        val itemPosition = TransactionAdapter.calculateBoxPosition(recyclerView.getChildLayoutPosition(view))
        if (itemPosition >= wallets.size) return
        Dialogs.showTXDetails(ac!!, wallets[itemPosition])
    }
}