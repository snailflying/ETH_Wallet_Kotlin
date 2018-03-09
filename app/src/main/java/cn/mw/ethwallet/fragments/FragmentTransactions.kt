package cn.mw.ethwallet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import cn.mw.ethwallet.R
import cn.mw.ethwallet.activities.AddressDetailActivity
import cn.mw.ethwallet.domain.TransactionDisplay
import cn.mw.ethwallet.network.EtherscanAPI
import cn.mw.ethwallet.network.RequestCache
import cn.mw.ethwallet.network.ResponseParser
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 17:26
 * @description
 */
class FragmentTransactions : FragmentTransactionsAbstract() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = super.onCreateView(inflater, container, savedInstanceState)
        send.setVisibility(GONE)
        requestTx!!.setVisibility(GONE)
        fabmenu.setVisibility(View.GONE)
        return rootView
    }

    override fun update(force: Boolean) {
        if (ac == null) return
        resetRequestCount()
        getWallets().clear()
        if (swipeLayout != null)
            swipeLayout!!.setRefreshing(true)

        try {
            EtherscanAPI.instance.getNormalTransactions(this.address!!, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (isAdded()) {
                        ac!!.runOnUiThread(Runnable {
                            onItemsLoadComplete()
                            (ac as AddressDetailActivity).snackError(getString(R.string.err_no_con))
                        })
                    }
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    val restring = response.body()!!.string()
                    if (restring != null && restring.length > 2)
                        RequestCache.instance.put(RequestCache.TYPE_TXS_NORMAL, address!!, restring)
                    val w = ArrayList<TransactionDisplay>(ResponseParser.parseTransactions(restring, "Unnamed Address", address!!, TransactionDisplay.NORMAL))
                    if (isAdded()) {
                        ac!!.runOnUiThread(Runnable { onComplete(w) })
                    }
                }
            }, force)
            EtherscanAPI.instance.getInternalTransactions(address!!, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (isAdded()) {
                        ac!!.runOnUiThread(Runnable {
                            onItemsLoadComplete()
                            (ac as AddressDetailActivity).snackError(getString(R.string.err_no_con))
                        })
                    }
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    val restring = response.body()!!.string()
                    if (restring != null && restring.length > 2)
                        RequestCache.instance.put(RequestCache.TYPE_TXS_INTERNAL, address!!, restring)
                    val w = ArrayList<TransactionDisplay>(ResponseParser.parseTransactions(restring, "Unnamed Address", address!!, TransactionDisplay.CONTRACT))
                    if (isAdded()) {
                        ac!!.runOnUiThread(Runnable { onComplete(w) })
                    }
                }
            }, force)
        } catch (e: IOException) {
            if (ac != null)
                (ac as AddressDetailActivity).snackError("Can't fetch account balances. No connection?")
            onItemsLoadComplete()
            e.printStackTrace()
        }

    }

    private fun onComplete(w: List<TransactionDisplay>) {
        addToWallets(w)
        addRequestCount()
        if (getRequestCount() >= 2) {
            onItemsLoadComplete()
            nothingToShow.setVisibility(if (getWallets().size === 0) View.VISIBLE else View.GONE)
            walletAdapter!!.notifyDataSetChanged()
        }
    }

}