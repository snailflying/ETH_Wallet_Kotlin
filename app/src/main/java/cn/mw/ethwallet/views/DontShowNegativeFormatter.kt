package cn.mw.ethwallet.views

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 20:00
 * @description
 */
class DontShowNegativeFormatter(private val dispalyInUsd: Boolean) : IAxisValueFormatter {

    override fun getFormattedValue(value: Float, axis: AxisBase): String {
        return if (dispalyInUsd) {
            if (value >= 0) value.toInt().toString() + "" else ""
        } else {
            if (value >= 0) (Math.floor((value * 1000).toDouble()) / 1000).toString() + "" else ""
        }
    }
}