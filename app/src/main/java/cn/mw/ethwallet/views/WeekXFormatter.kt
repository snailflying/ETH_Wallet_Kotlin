package cn.mw.ethwallet.views

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import java.util.*

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 19:59
 * @description
 */
class WeekXFormatter : IAxisValueFormatter {

    internal var c = GregorianCalendar()

    override fun getFormattedValue(value: Float, axis: AxisBase): String {
        c.timeInMillis = value.toLong() * 1000
        return c.get(Calendar.DAY_OF_MONTH).toString() + ". "
    }
}