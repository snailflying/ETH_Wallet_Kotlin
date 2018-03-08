package cn.mw.ethwallet.views

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import java.util.*

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 20:00
 * @description
 */
class HourXFormatter : IAxisValueFormatter {

    internal var c = GregorianCalendar()

    override fun getFormattedValue(value: Float, axis: AxisBase): String {
        c.timeInMillis = value.toLong() * 1000
        return c.get(Calendar.HOUR_OF_DAY).toString() + ":00 "
    }
}