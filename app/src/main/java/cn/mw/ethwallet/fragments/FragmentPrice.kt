package cn.mw.ethwallet.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import cn.mw.ethwallet.R
import cn.mw.ethwallet.activities.BaseApplication
import cn.mw.ethwallet.activities.MainActivity
import cn.mw.ethwallet.network.EtherscanAPI1
import cn.mw.ethwallet.utils.ExchangeCalculator
import cn.mw.ethwallet.views.DontShowNegativeFormatter
import cn.mw.ethwallet.views.HourXFormatter
import cn.mw.ethwallet.views.WeekXFormatter
import cn.mw.ethwallet.views.YearXFormatter
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.safframework.lifecycle.RxLifecycle
import java.io.IOException
import java.util.*

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 19:54
 * @description
 */
class FragmentPrice : Fragment() {

    private var priceChart: LineChart? = null
    private var price: TextView? = null
    private var chartTitle: TextView? = null
    private var swipeLayout: SwipeRefreshLayout? = null
    private var left: ImageView? = null
    private var right: ImageView? = null
    private var ac: MainActivity? = null
    private var colorPadding: LinearLayout? = null
    private var priceSwitch: LinearLayout? = null

    private var displayType = 1
    private var displayInUsd = true // True = USD, False = BTC
    private var refreshChart = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_price, container, false)

        ac = activity as MainActivity
        TITLE_TEXTS = arrayOf(getString(R.string.last_24_hours), getString(R.string.last_7_days), getString(R.string.last_30_days), getString(R.string.last_year))

        priceChart = rootView.findViewById(R.id.chart1) as LineChart?
        chartTitle = rootView.findViewById(R.id.chartTitle) as TextView?
        price = rootView.findViewById(R.id.price) as TextView?
        left = rootView.findViewById(R.id.wleft) as ImageView?
        right = rootView.findViewById(R.id.wright) as ImageView?
        priceSwitch = rootView.findViewById(R.id.priceSwitch) as LinearLayout?

        priceSwitch!!.setOnClickListener {
            displayInUsd = !displayInUsd
            refreshChart = true
            update(true)
            general()

            if (ac != null && ac!!.getPreferences(Context.MODE_PRIVATE) != null) {
                val editor = ac!!.getPreferences(Context.MODE_PRIVATE).edit()
                editor.putBoolean("price_displayInUsd", displayInUsd)
                editor.apply()
            }
        }

        if (ac != null && ac!!.getPreferences(Context.MODE_PRIVATE) != null)
            displayInUsd = ac!!.getPreferences(Context.MODE_PRIVATE).getBoolean("price_displayInUsd", true)

        if (ac != null && ac!!.getPreferences(Context.MODE_PRIVATE) != null)
            displayType = ac!!.getPreferences(Context.MODE_PRIVATE).getInt("displaytype_chart", 1)

        left!!.setOnClickListener { previous() }

        right!!.setOnClickListener { next() }
        colorPadding = rootView.findViewById(R.id.colorPadding) as LinearLayout?

        swipeLayout = rootView.findViewById(R.id.swipeRefreshLayout2) as SwipeRefreshLayout?
        swipeLayout!!.setColorSchemeColors(ac!!.getResources().getColor(R.color.colorPrimary))
        swipeLayout!!.setOnRefreshListener {
            updateExchangeRates()
            update(false)
        }

        if ((ac!!.getApplication() as BaseApplication).isGooglePlayBuild) {
            (ac!!.getApplication() as BaseApplication).track("Price Fragment")
        }

        swipeLayout!!.isRefreshing = true
        update(true)
        general()

        priceChart!!.visibility = View.INVISIBLE
        return rootView
    }

    private operator fun next() {
        refreshChart = true
        displayType = (displayType + 1) % PERIOD.size
        general()
        update(true)
    }

    private fun previous() {
        refreshChart = true
        displayType = if (displayType > 0) displayType - 1 else PERIOD.size - 1
        general()
        update(true)
    }

    private fun general() {
        priceChart!!.visibility = View.INVISIBLE
        chartTitle!!.text = TITLE_TEXTS!![displayType]
        colorPadding!!.setBackgroundColor(resources.getColor(R.color.colorPrimaryLittleDarker))
        if (ac != null && ac!!.getPreferences(Context.MODE_PRIVATE) != null) {
            val editor = ac!!.getPreferences(Context.MODE_PRIVATE).edit()
            editor.putInt("displaytype_chart", displayType)
            editor.apply()
        }
    }

    @Throws(IOException::class)
    private fun loadPriceData(time: Long, period: Int) {

        /*EtherscanAPI.instance.getPriceChart(System.currentTimeMillis() / 1000 - time, period, displayInUsd, object : Callback { // 1467321600,
            override fun onFailure(call: Call, e: IOException) {
                if (ac == null) return
                ac!!.runOnUiThread(Runnable {
                    try {
                        onItemsLoadComplete()
                        ac!!.snackError(getString(R.string.err_no_con), Snackbar.LENGTH_LONG)
                    } catch (e: Exception) {
                    }
                })
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val yVals = ArrayList<Entry>()
                try {
                    val data = JSONArray(response.body()!!.string())
                    val exchangeRate = ExchangeCalculator.instance.rateForChartDisplay
                    val commas = (if (displayInUsd) 100 else 10000).toFloat()
                    for (i in 0 until data.length()) {
                        val o = data.getJSONObject(i)
                        yVals.add(Entry(o.getLong("date").toFloat(),
                                Math.floor(o.getDouble("high") * exchangeRate * commas.toDouble()).toFloat() / commas))
                    }
                    if (ac == null) return
                    ac!!.runOnUiThread(Runnable {
                        priceChart!!.visibility = View.VISIBLE
                        onItemsLoadComplete()
                        if (isAdded) {
//                            setupChart(priceChart, getData(yVals), resources.getColor(R.color.colorPrimaryLittleDarker))
                            update(false)
                        }
                    })

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        }

        )*/
        EtherscanAPI1.instance.getPriceChart(System.currentTimeMillis() / 1000 - time, period, displayInUsd)
                .compose(RxLifecycle.bind(this).toLifecycleTransformer())
                .subscribe({
                    if (!it.isEmpty()) {
                        val yVals = ArrayList<Entry>()
//                            val exchangeRate = ExchangeCalculator.instance.rateForChartDisplay
                        val exchangeRate = 5
                        val commas = (if (displayInUsd) 100 else 10000).toFloat()
                        for (price in it) {
                            yVals.add(Entry(price.date.toFloat(),
                                    Math.floor(price.high * exchangeRate * commas.toDouble()).toFloat() / commas))

                        }
                        priceChart!!.visibility = View.VISIBLE
                        onItemsLoadComplete()
                        if (isAdded) {
                            setupChart(priceChart, getData(yVals), resources.getColor(R.color.colorPrimaryLittleDarker))
                            update(false)
                        }
                    }


                }, {
                    if (ac != null) {
                        onItemsLoadComplete()
                        ac!!.snackError(getString(R.string.err_no_con), Snackbar.LENGTH_LONG)
                    }

                })
    }

    private fun setupChart(chart: LineChart?, data: LineData, color: Int) {
        (data.getDataSetByIndex(0) as LineDataSet).setCircleColorHole(color)
        chart!!.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setTouchEnabled(false)
        chart.isDragEnabled = false
        chart.setScaleEnabled(true)
        chart.setPinchZoom(false)
        chart.setBackgroundColor(color)
        chart.setViewPortOffsets(0f, 23f, 0f, 0f)
        chart.data = data
        val l = chart.legend
        l.isEnabled = false

        chart.axisLeft.isEnabled = true
        chart.axisLeft.setDrawGridLines(false)
        chart.axisLeft.setDrawAxisLine(false)
        chart.axisLeft.spaceTop = 10f
        chart.axisLeft.spaceBottom = 30f
        chart.axisLeft.axisLineColor = 0xFFFFFF
        chart.axisLeft.textColor = 0xFFFFFF
        chart.axisLeft.setDrawTopYLabelEntry(true)
        chart.axisLeft.labelCount = 10

        chart.xAxis.isEnabled = true
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.setDrawAxisLine(false)
        chart.xAxis.axisLineColor = 0xFFFFFF
        chart.xAxis.textColor = 0xFFFFFF

        val tf = Typeface.DEFAULT

        // X Axis
        val xAxis = chart.xAxis
        xAxis.typeface = tf
        xAxis.removeAllLimitLines()

        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE

        xAxis.textColor = Color.argb(150, 255, 255, 255)

        if (displayType == 1 || displayType == 2)
        // Week and Month
            xAxis.valueFormatter = WeekXFormatter()
        else if (displayType == 0)
        //  Day
            xAxis.valueFormatter = HourXFormatter()
        else
            xAxis.valueFormatter = YearXFormatter() // Year

        // Y Axis
        val leftAxis = chart.axisLeft
        leftAxis.removeAllLimitLines()
        leftAxis.typeface = tf
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
        leftAxis.textColor = Color.argb(150, 255, 255, 255)
        leftAxis.valueFormatter = DontShowNegativeFormatter(displayInUsd)
        chart.axisRight.isEnabled = false // Deactivates horizontal lines

        chart.animateX(1300)
        chart.notifyDataSetChanged()
    }

    private fun getData(yVals: ArrayList<Entry>): LineData {
        val set1 = LineDataSet(yVals, "")
        set1.lineWidth = 1.45f
        set1.color = Color.argb(240, 255, 255, 255)
        set1.setCircleColor(Color.WHITE)
        set1.highLightColor = Color.WHITE
        set1.fillColor = resources.getColor(R.color.chartFilled)
        set1.setDrawCircles(false)
        set1.setDrawValues(false)
        set1.setDrawFilled(true)
        set1.fillFormatter = IFillFormatter { dataSet, dataProvider -> priceChart!!.axisLeft.axisMinimum }

        return LineData(set1)
    }

    fun updateExchangeRates() {
        try {
            refreshChart = false
            ExchangeCalculator.instance.updateExchangeRates(ac!!, if (ac != null) ac!!.getPreferences(Context.MODE_PRIVATE).getString("maincurrency", "USD") else "USD", ac!!)
            onItemsLoadComplete()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    @SuppressLint("SetTextI18n")
    fun update(updateChart: Boolean) {
        if (price != null)
            price!!.text = (if (displayInUsd)
                ExchangeCalculator.instance.displayUsdNicely(ExchangeCalculator.instance.usdPrice) + " " + ExchangeCalculator.instance.mainCurreny.name
            else
                ExchangeCalculator.instance.displayEthNicely(ExchangeCalculator.instance.btcPrice) + " BTC"
                    )

        if (refreshChart && updateChart) {
            try {
                loadPriceData(TIMESTAMPS[displayType].toLong(), PERIOD[displayType])
            } catch (e: IOException) {
                e.printStackTrace()
            }

            refreshChart = true
        }
        onItemsLoadComplete()
    }

    internal fun onItemsLoadComplete() {
        if (swipeLayout == null) return
        if (colorPadding == null) return
        Handler(Looper.getMainLooper()).post {
            swipeLayout!!.isRefreshing = false
            colorPadding!!.setBackgroundColor(-0xfa58767)
        }

    }

    companion object {

        private val TIMESTAMPS = intArrayOf(86400, // 24 hours
                604800, // Week
                2678400, // Month
                31536000 // Year
        )

        private var TITLE_TEXTS: Array<String>? = null

        private val PERIOD = intArrayOf(300, 1800, 14400, 86400)
    }
}