package cn.mw.ethwallet.fragments

import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import cn.mw.ethwallet.activities.MainActivity
import cn.mw.ethwallet.domain.response.TransactionDisplay
import cn.mw.ethwallet.interfaces.AppBarStateChangeListener
import cn.mw.ethwallet.interfaces.StorableWallet
import cn.mw.ethwallet.network.EtherscanAPI1
import cn.mw.ethwallet.network.RequestCache
import cn.mw.ethwallet.network.ResponseParser
import cn.mw.ethwallet.utils.WalletStorage
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import java.io.IOException
import java.math.BigInteger

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 20:01
 * @description
 */
class FragmentTransactionsAll : FragmentTransactionsAbstract() {

    protected var unconfirmed: TransactionDisplay? = null
    private var unconfirmed_addedTime: Long = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = super.onCreateView(inflater, container, savedInstanceState)
        val ac = this.ac as MainActivity
        if (ac != null && ac!!.appBar != null) {
            ac!!.appBar!!.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
                override fun onStateChanged(appBarLayout: AppBarLayout, state: State) {
                    if (state === State.COLLAPSED) {
                        fabmenu.hideMenu(true)
                    } else {
                        fabmenu.showMenu(true)
                    }
                }
            })
        }
        return rootView
    }


    override fun update(force: Boolean) {
        if (ac == null) return
        getWallets().clear()
        if (swipeLayout != null)
            swipeLayout!!.setRefreshing(true)
        resetRequestCount()
        val storedwallets = ArrayList<StorableWallet>(WalletStorage.getInstance(ac!!).get())
        if (storedwallets.size == 0) {
            nothingToShow.setVisibility(View.VISIBLE)
            onItemsLoadComplete()
        } else {
            nothingToShow.setVisibility(GONE)
            for (i in storedwallets.indices) {
                try {
                    val currentWallet = storedwallets.get(i)

                    /*EtherscanAPI.instance.getNormalTransactions(currentWallet.pubKey, object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            if (isAdded) {
                                ac!!.runOnUiThread(Runnable {
                                    onItemsLoadComplete()
                                    (ac as MainActivity).snackError("No internet connection")
                                })
                            }
                        }

                        @Throws(IOException::class)
                        override fun onResponse(call: Call, response: Response) {
                            val restring = response.body()!!.string()
                            if (restring != null && restring.length > 2)
                                RequestCache.instance.put(RequestCache.TYPE_TXS_NORMAL, currentWallet.pubKey, restring)
                            val w = ArrayList<TransactionDisplay>(ResponseParser.parseTransactions(restring, "Unnamed Address", currentWallet.pubKey, TransactionDisplay.NORMAL))
                            if (isAdded) {
                                ac!!.runOnUiThread(Runnable { onComplete(w, storedwallets) })
                            }
                        }
                    }, force)*/
                    EtherscanAPI1.instance.getNormalTransactions(ac!!, currentWallet.pubKey, force)
                            .subscribe({
                                object : SingleObserver<String> {
                                    override fun onSuccess(t: String) {
                                        if (t.length > 2)
                                            RequestCache.instance.put(RequestCache.TYPE_TXS_NORMAL, currentWallet.pubKey, t)
                                        val w = ArrayList<TransactionDisplay>(ResponseParser.parseTransactions(t, "Unnamed Address", currentWallet.pubKey, TransactionDisplay.NORMAL))
                                        if (isAdded) {
                                            ac!!.runOnUiThread(Runnable { onComplete(w, storedwallets) })
                                        }
                                    }

                                    override fun onSubscribe(d: Disposable) {
                                    }

                                    override fun onError(e: Throwable) {
                                        if (isAdded) {
                                            onItemsLoadComplete()
                                            (ac as MainActivity).snackError("No internet connection")
                                        }
                                    }
                                }

                            }, {
                                if (isAdded) {
                                    onItemsLoadComplete()
                                    (ac as MainActivity).snackError("No internet connection")
                                }
                            })
                    /*EtherscanAPI.instance.getInternalTransactions(currentWallet.pubKey, object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            if (isAdded) {
                                ac!!.runOnUiThread(Runnable {
                                    onItemsLoadComplete()
                                    (ac as MainActivity).snackError("No internet connection")
                                })
                            }
                        }

                        @Throws(IOException::class)
                        override fun onResponse(call: Call, response: Response) {
                            val restring = response.body()!!.string()
                            if (restring != null && restring.length > 2)
                                RequestCache.instance.put(RequestCache.TYPE_TXS_INTERNAL, currentWallet.pubKey, restring)
                            val w = ArrayList<TransactionDisplay>(ResponseParser.parseTransactions(restring, "Unnamed Address", currentWallet.pubKey, TransactionDisplay.CONTRACT))
                            if (isAdded) {
                                ac!!.runOnUiThread(Runnable { onComplete(w, storedwallets) })
                            }
                        }
                    }, force)*/
                    EtherscanAPI1.instance.getInternalTransactions(ac!!, currentWallet.pubKey, force)
                            .subscribe({
                                object : SingleObserver<String> {
                                    override fun onSuccess(t: String) {
                                        if (t.length > 2)
                                            RequestCache.instance.put(RequestCache.TYPE_TXS_INTERNAL, currentWallet.pubKey, t)
                                        val w = ArrayList<TransactionDisplay>(ResponseParser.parseTransactions(t, "Unnamed Address", currentWallet.pubKey, TransactionDisplay.CONTRACT))
                                        if (isAdded) {
                                            ac!!.runOnUiThread(Runnable { onComplete(w, storedwallets) })
                                        }
                                    }

                                    override fun onSubscribe(d: Disposable) {
                                    }

                                    override fun onError(e: Throwable) {
                                        if (isAdded) {
                                            onItemsLoadComplete()
                                            (ac as MainActivity).snackError("No internet connection")
                                        }
                                    }
                                }

                            }, {
                                if (isAdded) {
                                    onItemsLoadComplete()
                                    (ac as MainActivity).snackError("No internet connection")
                                }
                            })
                } catch (e: IOException) {
                    if (isAdded) {
                        if (ac != null)
                            (ac as MainActivity).snackError("Can't fetch account balances. No connection?")

                        // So "if(getRequestCount() >= storedwallets.size()*2)" limit can be reached even if there are expetions for certain addresses (2x because of internal and normal)
                        addRequestCount()
                        addRequestCount()
                        onItemsLoadComplete()
                        e.printStackTrace()
                    }
                }

            }
        }
    }

    private fun onComplete(w: java.util.ArrayList<TransactionDisplay>, storedwallets: java.util.ArrayList<StorableWallet>) {
        addToWallets(w)
        addRequestCount()
        if (getRequestCount() >= storedwallets.size * 2) {
            onItemsLoadComplete()

            // If transaction was send via App and has no confirmations yet (Still show it when users refreshes for 10 minutes)
            if (unconfirmed_addedTime + 10 * 60 * 1000 < System.currentTimeMillis())
            // After 10 minutes remove unconfirmed (should now have at least 1 confirmation anyway)
                unconfirmed = null
            if (unconfirmed != null && getWallets().size > 0) {
                if (getWallets()[0].amount === unconfirmed!!.amount) {
                    unconfirmed = null
                } else {
                    getWallets().add(0, unconfirmed!!)
                }
            }

            nothingToShow.setVisibility(if (getWallets().size === 0) View.VISIBLE else GONE)
            walletAdapter!!.notifyDataSetChanged()
        }
    }


    fun addUnconfirmedTransaction(from: String, to: String, amount: BigInteger) {
        unconfirmed = TransactionDisplay(from, to, amount, 0, System.currentTimeMillis(), "", TransactionDisplay.NORMAL, "", "0", 0, 1, 1, false)
        unconfirmed_addedTime = System.currentTimeMillis()
        getWallets().add(0, unconfirmed!!)
        notifyDataSetChanged()
    }

}