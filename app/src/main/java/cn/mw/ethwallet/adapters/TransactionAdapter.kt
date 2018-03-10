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
import cn.mw.ethwallet.domain.request.TransactionDisplay
import cn.mw.ethwallet.utils.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 17:28
 * @description
 */
class TransactionAdapter(private val boxlist: List<TransactionDisplay>, private val context: Context, private val clickListener: View.OnClickListener, private val contextMenuListener: View.OnCreateContextMenuListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var lastPosition = -1
    private val dateformat = SimpleDateFormat("dd. MMMM yyyy, HH:mm", Locale.getDefault())
    var position: Int = 0

    override fun getItemViewType(position: Int): Int {
        if (!Settings.displayAds) return CONTENT
        return if (position % LIST_AD_DELTA == 0) {
            AD
        } else CONTENT
    }

    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var month: TextView
        var walletbalance: TextView
        var walletname: TextView
        var other_address: TextView
        var plusminus: TextView
        var my_addressicon: ImageView
        var other_addressicon: ImageView
        var type: ImageView
        var error: ImageView
        val container: LinearLayout

        init {
            month = view.findViewById(R.id.month) as TextView
            walletbalance = view.findViewById(R.id.walletbalance) as TextView
            plusminus = view.findViewById(R.id.plusminus) as TextView
            walletname = view.findViewById(R.id.walletname) as TextView
            other_address = view.findViewById(R.id.other_address) as TextView

            my_addressicon = view.findViewById(R.id.my_addressicon) as ImageView
            other_addressicon = view.findViewById(R.id.other_addressicon) as ImageView
            type = view.findViewById(R.id.type) as ImageView
            error = view.findViewById(R.id.error) as ImageView
            container = view.findViewById(R.id.container) as LinearLayout
        }

        fun clearAnimation() {
            container.clearAnimation()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == CONTENT) {
            val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.view_w_transaction, parent, false)
            itemView.setOnCreateContextMenuListener(contextMenuListener)
            itemView.setOnClickListener(clickListener)
            return MyViewHolder(itemView)
        } else {
            return AdRecyclerHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_w_transaction_ad, parent, false))
        }
    }

    override fun onBindViewHolder(holder_: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == CONTENT) {
            val holder = holder_ as MyViewHolder
            val box = boxlist[calculateBoxPosition(position)]

            holder.itemView.setOnLongClickListener {
                this@TransactionAdapter.position = position
                false
            }

            holder.walletbalance.text = ExchangeCalculator.instance.displayBalanceNicely(ExchangeCalculator.instance.convertRate(Math.abs(box.amount), ExchangeCalculator.instance.current.rate)) + " " + ExchangeCalculator.instance.currencyShort

            val walletname = AddressNameConverter.getInstance(context).get(box.getFromAddress())
            holder.walletname.text = walletname ?: box.walletName

            val toName = AddressNameConverter.getInstance(context).get(box.getToAddress())
            holder.other_address.text = if (toName == null) box.getToAddress() else toName + " (" + box.getToAddress().substring(0, 10) + ")"
            holder.plusminus.text = if (box.amount > 0) "+" else "-"

            holder.plusminus.setTextColor(context.resources.getColor(if (box.amount > 0) R.color.etherReceived else R.color.etherSpent))
            holder.walletbalance.setTextColor(context.resources.getColor(if (box.amount > 0) R.color.etherReceived else R.color.etherSpent))
            holder.container.alpha = 1f
            if (box.confirmationStatus === 0) {
                holder.month.text = "Unconfirmed"
                holder.month.setTextColor(context.resources.getColor(R.color.unconfirmedNew))
                holder.container.alpha = 0.75f
            } else if (box.confirmationStatus > 12) {
                holder.month.text = dateformat.format(Date(box.date))
                holder.month.setTextColor(context.resources.getColor(R.color.normalBlack))
            } else {
                holder.month.text = box.confirmationStatus.toString() + " / 12 Confirmations"
                holder.month.setTextColor(context.resources.getColor(R.color.unconfirmed))
            }

            holder.type.visibility = if (box.type === TransactionDisplay.NORMAL) View.INVISIBLE else View.VISIBLE
            holder.error.visibility = if (box.isError) View.VISIBLE else View.GONE
            holder.my_addressicon.setImageBitmap(Blockies.createIcon(box.getFromAddress().toLowerCase()))
            holder.other_addressicon.setImageBitmap(Blockies.createIcon(box.getToAddress().toLowerCase()))

            setAnimation(holder.container, position)
        } else {
            val holder = holder_ as AdRecyclerHolder
            holder.loadAd(context)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        (holder as? MyViewHolder)?.itemView?.setOnLongClickListener(null)
        super.onViewRecycled(holder)
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(context, if (position > lastPosition) R.anim.up_from_bottom else R.anim.down_from_bottom)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }


    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        (holder as? MyViewHolder)?.clearAnimation()
    }

    override fun getItemCount(): Int {
        if (!Settings.displayAds) return boxlist.size
        var additionalContent = 1
        if (boxlist.size > 0 && boxlist.size >= LIST_AD_DELTA) {
            additionalContent += boxlist.size / (LIST_AD_DELTA - 1)
        }
        return boxlist.size + additionalContent
    }

    companion object {

        private val CONTENT = 0
        private val AD = 1
        private val LIST_AD_DELTA = 9

        fun calculateBoxPosition(position: Int): Int {
            if (!Settings.displayAds) return position
            return if (position < LIST_AD_DELTA) position - 1 else position - position / LIST_AD_DELTA - 1
        }
    }
}