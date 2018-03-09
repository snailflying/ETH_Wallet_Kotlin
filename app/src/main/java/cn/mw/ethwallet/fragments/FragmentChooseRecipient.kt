package cn.mw.ethwallet.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import cn.mw.ethwallet.R
import cn.mw.ethwallet.activities.BaseApplication
import cn.mw.ethwallet.activities.QRScanActivity
import cn.mw.ethwallet.activities.SendActivity
import cn.mw.ethwallet.adapters.WalletAdapter
import cn.mw.ethwallet.domain.WalletDisplay
import cn.mw.ethwallet.utils.AddressNameConverter

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 17:57
 * @description
 */
class FragmentChooseRecipient : Fragment(), View.OnClickListener, View.OnCreateContextMenuListener {

    private var recyclerView: RecyclerView? = null
    private var walletAdapter: WalletAdapter? = null
    private val wallets = java.util.ArrayList<WalletDisplay>()
    private var ac: SendActivity? =null
    private var qr: ImageButton? = null
    private var send: Button? = null
    private var addressBox: EditText? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_recipient, container, false)

        ac = this.activity as SendActivity

        qr = rootView.findViewById(R.id.scan_button) as ImageButton?
        send = rootView.findViewById(R.id.send) as Button?
        addressBox = rootView.findViewById(R.id.receiver) as EditText?

        recyclerView = rootView.findViewById(R.id.recycler_view) as RecyclerView?
        walletAdapter = WalletAdapter(wallets, ac!!, this, this)
        val mgr = LinearLayoutManager(ac!!.getApplicationContext())
        recyclerView!!.layoutManager = mgr
        recyclerView!!.itemAnimator = DefaultItemAnimator()
        recyclerView!!.adapter = walletAdapter
        val dividerItemDecoration = DividerItemDecoration(recyclerView!!.context,
                mgr.orientation)
        recyclerView!!.addItemDecoration(dividerItemDecoration)


        qr!!.setOnClickListener {
            val qr = Intent(ac, QRScanActivity::class.java)
            qr.putExtra("TYPE", QRScanActivity.SCAN_ONLY)
            ac!!.startActivityForResult(qr, QRScanActivity.REQUEST_CODE)
        }


        send!!.setOnClickListener {
            if (addressBox!!.text.toString().length > 15 && addressBox!!.text.toString().startsWith("0x"))
                ac!!.nextStage(addressBox!!.text.toString())
            else
                ac!!.snackError("Invalid Recipient")
        }

        update()

        if ((ac!!.getApplication() as BaseApplication).isGooglePlayBuild) {
            (ac!!.getApplication() as BaseApplication).track("Recipient Fragment")
        }

        return rootView
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo) {
        menu.setHeaderTitle(R.string.addressbook_menu_title)
        menu.add(0, 400, 0, R.string.addressbook_menu_remove)
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
            400 // Remove
            -> {
                AddressNameConverter.getInstance(ac!!).put(wallets[position].publicKey!!, null, ac!!)
                wallets.removeAt(position)
                if (walletAdapter != null)
                    walletAdapter!!.notifyDataSetChanged()
            }
        }
        return super.onContextItemSelected(item)
    }


    fun setRecipientAddress(address: String) {
        if (addressBox == null) return
        addressBox!!.setText(address)
    }

    fun update() {
        if (ac == null) return
        wallets.clear()

        wallets.addAll(ArrayList<WalletDisplay>(AddressNameConverter.getInstance(ac!!).asAddressbook))
        walletAdapter!!.notifyDataSetChanged()
    }

    override fun onClick(view: View) {
        val itemPosition = recyclerView!!.getChildLayoutPosition(view)
        addressBox!!.setText(wallets[itemPosition].publicKey)
    }
}