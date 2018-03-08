package cn.mw.ethwallet.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import cn.mw.ethwallet.R
import cn.mw.ethwallet.domain.TransactionDisplay
import cn.mw.ethwallet.domain.WalletDisplay
import cn.mw.ethwallet.utils.AddressNameConverter
import cn.mw.ethwallet.utils.Blockies
import cn.mw.ethwallet.utils.ExchangeCalculator
import me.grantland.widget.AutofitTextView

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 17:59
 * @description
 */
class WalletAdapter(private val boxlist: List<WalletDisplay>, private val context: Context, private val listener: View.OnClickListener, private val contextMenuListener: View.OnCreateContextMenuListener) : RecyclerView.Adapter<WalletAdapter.MyViewHolder>() {
    private var lastPosition = -1
    var position: Int = 0

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var walletname: TextView
        var walletbalance: TextView
        var addressimage: ImageView
        var type: ImageView
        internal var walletaddress: AutofitTextView
        var container: LinearLayout

        init {
            walletaddress = view.findViewById(R.id.walletaddress) as AutofitTextView
            walletname = view.findViewById(R.id.walletname) as TextView
            walletbalance = view.findViewById(R.id.walletbalance) as TextView
            addressimage = view.findViewById(R.id.addressimage) as ImageView
            type = view.findViewById(R.id.type) as ImageView
            container = view.findViewById(R.id.container) as LinearLayout
        }

        fun clearAnimation() {
            container.clearAnimation()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_w_address, parent, false)

        itemView.setOnClickListener(listener)
        itemView.setOnCreateContextMenuListener(contextMenuListener)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val box = boxlist[position]

        holder.itemView.setOnLongClickListener {
            this.position = position
            false
        }
        holder.walletaddress.setText(box.publicKey)
        val walletname = AddressNameConverter.getInstance(context).get(box.publicKey!!)
        holder.walletname.text = walletname ?: "New Wallet"
        if (box.type !== WalletDisplay.CONTACT)
            holder.walletbalance.setText(ExchangeCalculator.instance.displayBalanceNicely(ExchangeCalculator.instance.convertRate(box.balance, ExchangeCalculator.instance.current.rate)) + " " + ExchangeCalculator.instance.currencyShort)
        holder.addressimage.setImageBitmap(Blockies.createIcon(box.publicKey!!))

        holder.type.visibility = if (box.type === TransactionDisplay.NORMAL || box.type === WalletDisplay.CONTACT) View.INVISIBLE else View.VISIBLE

        setAnimation(holder.container, position)
    }

    override fun onViewRecycled(holder: WalletAdapter.MyViewHolder) {
        holder.itemView.setOnLongClickListener(null)
        super.onViewRecycled(holder)
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(context, if (position > lastPosition) R.anim.up_from_bottom else R.anim.down_from_bottom)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

    override fun onViewDetachedFromWindow(holder: MyViewHolder) {
        holder.clearAnimation()
    }


    override fun getItemCount(): Int {
        return boxlist.size
    }
}