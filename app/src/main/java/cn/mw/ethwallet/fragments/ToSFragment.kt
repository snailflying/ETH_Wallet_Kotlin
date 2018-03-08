package cn.mw.ethwallet.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import cn.mw.ethwallet.R
import kotlinx.android.synthetic.main.tos_layout.*

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 14:10
 * @description
 */
class ToSFragment : Fragment() {

//    private var tos: TextView? = null
//    private var read: CheckBox? = null
    var isToSChecked = false
        private set

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        tostext.text = Html.fromHtml(activity!!.resources.getString(R.string.tos))

        readCheckBox.setOnClickListener { isToSChecked = readCheckBox.isChecked }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.tos_layout, container, false)
    }
}