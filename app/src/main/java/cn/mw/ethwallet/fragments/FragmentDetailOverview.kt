package cn.mw.ethwallet.fragments

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import cn.mw.ethwallet.R
import cn.mw.ethwallet.activities.AddressDetailActivity
import cn.mw.ethwallet.activities.SendActivity
import cn.mw.ethwallet.adapters.TokenAdapter
import cn.mw.ethwallet.domain.mod.WatchWallet
import cn.mw.ethwallet.domain.response.Balance
import cn.mw.ethwallet.domain.response.TokenDisplay
import cn.mw.ethwallet.interfaces.AppBarStateChangeListener
import cn.mw.ethwallet.interfaces.LastIconLoaded
import cn.mw.ethwallet.network.EtherscanAPI1
import cn.mw.ethwallet.utils.*
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import java.io.IOException
import java.math.BigDecimal
import java.util.*

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 22:15
 * @description
 */
class FragmentDetailOverview : Fragment(), View.OnClickListener, View.OnCreateContextMenuListener, LastIconLoaded {

    private val TAG = "FragmentDetailOverview"

    private var ac: AddressDetailActivity? = null
    private lateinit var ethaddress: String
    private var type: Byte = 0
    private var balance: TextView? = null
    private var address: TextView? = null
    private var currency: TextView? = null
    private var icon: ImageView? = null
    private var header: LinearLayout? = null
    internal var balanceDouble = BigDecimal("0")
    private var fabmenu: FloatingActionMenu? = null
    private var recyclerView: RecyclerView? = null
    private var walletAdapter: TokenAdapter? = null
    private val token = ArrayList<TokenDisplay>()
    private var swipeLayout: SwipeRefreshLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_detail_ov, container, false)

        ac = this.activity as AddressDetailActivity
        ethaddress = arguments!!.getString("ADDRESS")
        type = arguments!!.getByte("TYPE")

        icon = rootView.findViewById(R.id.addressimage) as ImageView?
        address = rootView.findViewById(R.id.ethaddress) as TextView?
        balance = rootView.findViewById(R.id.balance) as TextView?
        currency = rootView.findViewById(R.id.currency) as TextView?
        header = rootView.findViewById(R.id.header) as LinearLayout?
        fabmenu = rootView.findViewById(R.id.fabmenu) as FloatingActionMenu?

        val cur = ExchangeCalculator.instance.current
        balanceDouble = BigDecimal(arguments!!.getDouble("BALANCE"))
        balance!!.setText(ExchangeCalculator.instance.convertRateExact(balanceDouble, cur.rate) + "")
        currency!!.setText(cur.name)

        recyclerView = rootView.findViewById(R.id.recycler_view) as RecyclerView?
        walletAdapter = TokenAdapter(token, ac!!, this, this)
        val mgr = LinearLayoutManager(ac!!.getApplicationContext())
        recyclerView!!.layoutManager = mgr
        recyclerView!!.itemAnimator = DefaultItemAnimator()
        recyclerView!!.adapter = walletAdapter
        val dividerItemDecoration = DividerItemDecoration(recyclerView!!.context, mgr.orientation)
        recyclerView!!.addItemDecoration(dividerItemDecoration)

        swipeLayout = rootView.findViewById(R.id.swipeRefreshLayout2) as SwipeRefreshLayout?
        swipeLayout!!.setColorSchemeColors(ac!!.getResources().getColor(R.color.colorPrimary))
        swipeLayout!!.setOnRefreshListener {
            try {
                update(true)
            } catch (e: IOException) {
                if (ac != null)
                    ac!!.snackError("Connection problem")
                e.printStackTrace()
            }
        }

        header!!.setOnClickListener {
            val cur = ExchangeCalculator.instance.next()
            balance!!.setText(ExchangeCalculator.instance.convertRateExact(balanceDouble, cur.rate) + "")
            currency!!.setText(cur.name)
            walletAdapter!!.notifyDataSetChanged()
            if (ac != null)
                ac!!.broadCastDataSetChanged()
        }

        icon!!.setImageBitmap(Blockies.createIcon(ethaddress, 24))
        address!!.text = ethaddress

        val fab_setName = rootView.findViewById(R.id.set_name) as FloatingActionButton
        fab_setName.setOnClickListener { setName() }

        val send_ether = rootView.findViewById(R.id.send_ether) as FloatingActionButton // Send Ether to
        send_ether.setOnClickListener {
            if (WalletStorage.getInstance(ac!!).fullOnly.size === 0) {
                Dialogs.noFullWallet(ac!!)
            } else {
                val tx = Intent(ac, SendActivity::class.java)
                tx.putExtra("TO_ADDRESS", ethaddress)
                ac!!.startActivityForResult(tx, SendActivity.REQUEST_CODE)
            }
        }

        val send_ether_from = rootView.findViewById(R.id.send_ether_from) as FloatingActionButton
        send_ether_from.setOnClickListener {
            if (WalletStorage.getInstance(ac!!).fullOnly.size === 0) {
                Dialogs.noFullWallet(ac!!)
            } else {
                val tx = Intent(ac, SendActivity::class.java)
                tx.putExtra("FROM_ADDRESS", ethaddress)
                ac!!.startActivityForResult(tx, SendActivity.REQUEST_CODE)
            }
        }

        val fab_add = rootView.findViewById(R.id.add_as_watch) as FloatingActionButton
        fab_add.setOnClickListener {
            val suc = WalletStorage.getInstance(ac!!).add(WatchWallet(ethaddress!!), ac!!)
            Handler().postDelayed(
                    { ac!!.snackError(ac!!.getResources().getString(if (suc) R.string.main_ac_wallet_added_suc else R.string.main_ac_wallet_added_er)) }, 100)
        }

        if (type == AddressDetailActivity.OWN_WALLET) {
            fab_add.visibility = View.GONE
        }
        if (!WalletStorage.getInstance(ac!!).isFullWallet(ethaddress!!)) {
            send_ether_from.visibility = View.GONE
        }

        if (ac!!.appBar != null) {
            ac!!.appBar!!.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
                override fun onStateChanged(appBarLayout: AppBarLayout, state: State) {
                    if (state === State.COLLAPSED) {
                        fabmenu!!.hideMenu(true)
                    } else {
                        fabmenu!!.showMenu(true)
                    }
                }
            })
        }
        try {
            update(false)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return rootView
    }


    @Throws(IOException::class)
    fun update(force: Boolean) {
        token.clear()
        balanceDouble = BigDecimal("0")
        /*EtherscanAPI.instance.getBalance(ethaddress!!, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                ac!!.runOnUiThread(Runnable {
                    ac!!.snackError("Can't connect to network")
                    onItemsLoadComplete()
                })
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val ethbal: BigDecimal
                try {
                    ethbal = BigDecimal(ResponseParser.parseBalance(response.body()!!.string()))
                    token.add(0, TokenDisplay("Ether", "ETH", ethbal.multiply(BigDecimal(1000.0)), 3, 1.0, "", "", 0.0, 0))
                    balanceDouble = balanceDouble.add(ethbal)
                } catch (e: JSONException) {
                    ac!!.runOnUiThread(Runnable { onItemsLoadComplete() })
                    e.printStackTrace()
                }

                val cur = ExchangeCalculator.instance.current
                ac!!.runOnUiThread(Runnable {
                    // balance.setText(ExchangeCalculator.instance.convertRateExact(balanceDouble, ExchangeCalculator.instance.getInstance));
                    balance!!.setText(ExchangeCalculator.instance.convertRateExact(balanceDouble, cur.rate) + "")
                    currency!!.setText(cur.name)
                    walletAdapter!!.notifyDataSetChanged()
                })
            }
        })*/
        EtherscanAPI1.instance.getBalance(ac!!, ethaddress)
                .subscribe(
                        object : SingleObserver<Balance> {
                            override fun onSuccess(t: Balance) {
                                val ethbal: BigDecimal
                                val result = if (t.result == "0") "0" else BigDecimal(t.result).divide(BigDecimal(1000000000000000000.0), 7, BigDecimal.ROUND_UP).toPlainString()
                                ethbal = BigDecimal(result)
                                token.add(0, TokenDisplay("Ether", "ETH", ethbal.multiply(BigDecimal(1000.0)), 3, 1.0, "", "", 0.0, 0))
                                balanceDouble = balanceDouble.add(ethbal)

                                val cur = ExchangeCalculator.instance.current
                                balance!!.setText(ExchangeCalculator.instance.convertRateExact(balanceDouble, cur.rate) + "")
                                currency!!.setText(cur.name)
                                walletAdapter!!.notifyDataSetChanged()
                            }

                            override fun onSubscribe(d: Disposable) {
                            }

                            override fun onError(e: Throwable) {
                                ac!!.snackError("Can't connect to network")
                                onItemsLoadComplete()
                            }

                        }

                )

        /*EtherscanAPI.instance.getTokenBalances(ethaddress!!, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                ac!!.runOnUiThread(Runnable {
                    ac!!.snackError("Can't connect to network")
                    onItemsLoadComplete()
                })
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                try {
                    val restring = response.body()!!.string()
                    if (restring != null && restring.length > 2)
                        RequestCache.instance.put(RequestCache.TYPE_TOKEN, ethaddress!!, restring)
                    token.addAll(ResponseParser.parseTokens(ac!!, restring, this@FragmentDetailOverview))

                    balanceDouble = balanceDouble.add(BigDecimal(ExchangeCalculator.instance.sumUpTokenEther(token)))

                    val cur = ExchangeCalculator.instance.current
                    ac!!.runOnUiThread(Runnable {
                        balance!!.setText(ExchangeCalculator.instance.convertRateExact(balanceDouble, cur.rate) + "")
                        currency!!.setText(cur.name)
                        walletAdapter!!.notifyDataSetChanged()
                        onItemsLoadComplete()
                    })
                } catch (e: Exception) {
                    ac!!.runOnUiThread(Runnable { onItemsLoadComplete() })
                    //ac.snackError("Invalid server response");
                }

            }
        }, force)*/
        EtherscanAPI1.instance.getTokenBalances(ac!!, ethaddress, force)

                .subscribe(
                        object : SingleObserver<List<TokenDisplay>> {
                            override fun onSuccess(t: List<TokenDisplay>) {

                                for (temp in t) {
                                    EtherscanAPI1.instance.loadTokenIcon(ac!!, temp.name!!)
                                }

                                token.addAll(t)
                                balanceDouble = balanceDouble.add(BigDecimal(ExchangeCalculator.instance.sumUpTokenEther(token)))
                                val cur = ExchangeCalculator.instance.current
                                balance!!.setText(ExchangeCalculator.instance.convertRateExact(balanceDouble, cur.rate) + "")
                                currency!!.setText(cur.name)
                                walletAdapter!!.notifyDataSetChanged()
                                onItemsLoadComplete()
                                Log.e(TAG, "cur.name:" + cur.name)
                            }

                            override fun onSubscribe(d: Disposable) {
                            }

                            override fun onError(e: Throwable) {
                                onItemsLoadComplete()
                            }


                        }

                )

    }

    fun setName() {
        val builder: AlertDialog.Builder
        if (Build.VERSION.SDK_INT >= 24)
        // Otherwise buttons on 7.0+ are nearly invisible
            builder = AlertDialog.Builder(ac!!, R.style.AlertDialogTheme)
        else
            builder = AlertDialog.Builder(ac!!)
        if (type == AddressDetailActivity.OWN_WALLET)
            builder.setTitle(R.string.name_your_address)
        else
            builder.setTitle(R.string.name_this_address)

        val input = EditText(ac)
        input.setText(AddressNameConverter.getInstance(ac!!).get(ethaddress!!))
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

        container.addView(input)
        builder.setView(container)
        input.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                val inputMgr = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
            }
        }
        builder.setPositiveButton(R.string.button_ok, DialogInterface.OnClickListener { dialog, which ->
            val inputMgr = input.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMgr.hideSoftInputFromWindow(input.windowToken, 0)
            AddressNameConverter.getInstance(ac!!).put(ethaddress, input.text.toString(), ac!!)
            ac!!.setTitle(input.text.toString())
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
    }

    override fun onClick(view: View) {
        if (ac == null) return
        val itemPosition = recyclerView!!.getChildLayoutPosition(view)
        if (itemPosition == 0 || itemPosition >= token.size) return   // if clicked on Ether
        Dialogs.showTokenetails(ac!!, token[itemPosition])
    }

    override fun onLastIconDownloaded() {
        if (walletAdapter != null && ac != null) {
            ac!!.runOnUiThread(Runnable { walletAdapter!!.notifyDataSetChanged() })
        }
    }
}