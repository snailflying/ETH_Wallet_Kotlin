package cn.mw.ethwallet.adapters

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import cn.mw.ethwallet.R
import cn.mw.ethwallet.domain.response.TokenDisplay
import cn.mw.ethwallet.network.TokenIconCache
import cn.mw.ethwallet.utils.ExchangeCalculator

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 22:20
 * @description
 */
class TokenAdapter(private val boxlist: List<TokenDisplay>, private val context: Context, private val listener: View.OnClickListener, private val contextMenuListener: View.OnCreateContextMenuListener) : RecyclerView.Adapter<TokenAdapter.MyViewHolder>() {
    private var lastPosition = -1
    var position: Int = 0

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var name: TextView
        var nativebalance: TextView
        var etherbalance: TextView
        var shorty: TextView? = null
        var image: TextView
        var container: LinearLayout

        init {
            nativebalance = view.findViewById(R.id.nativebalance) as TextView
            name = view.findViewById(R.id.tokenname) as TextView
            image = view.findViewById(R.id.addressimage) as TextView
            etherbalance = view.findViewById(R.id.etherbalance) as TextView
            container = view.findViewById(R.id.container) as LinearLayout
        }

        fun clearAnimation() {
            container.clearAnimation()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_w_token, parent, false)

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

        holder.name.setText(box.name)
        val tbalance = box.balanceDouble
        holder.nativebalance.setText(ExchangeCalculator.instance.displayEthNicely(tbalance) + " " + box.shorty)
        holder.etherbalance.setText(
                ExchangeCalculator.instance.displayEthNicely(
                        ExchangeCalculator.instance.convertRate(
                                ExchangeCalculator.instance.convertTokenToEther(tbalance, box.usdprice),
                                ExchangeCalculator.instance.current.rate
                        )) + " " + ExchangeCalculator.instance.current.shorty)
        if (box.contractAddr != null && box.contractAddr!!.length > 3) {
            holder.image.text = ""
            var iconName = box.name
            if (iconName!!.indexOf(" ") > 0)
                iconName = iconName.substring(0, iconName.indexOf(" "))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                holder.image.background = BitmapDrawable(context.resources, TokenIconCache.getInstance(context).get(iconName))
            }
        } else {
            holder.image.text = "Ξ"
            holder.image.setBackgroundResource(0)
            holder.etherbalance.setText(
                    (ExchangeCalculator.instance.displayEthNicely(
                            ExchangeCalculator.instance.convertRate(tbalance, ExchangeCalculator.instance.current.rate)) + " " +
                            ExchangeCalculator.instance.current.shorty))
        }

        setAnimation(holder.container, position)
    }

    override fun onViewRecycled(holder: TokenAdapter.MyViewHolder) {
        holder.itemView.setOnLongClickListener(null)
        super.onViewRecycled(holder)
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(context, if ((position > lastPosition)) R.anim.up_from_bottom else R.anim.down_from_bottom)
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