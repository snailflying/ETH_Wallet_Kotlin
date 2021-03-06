package cn.mw.ethwallet.domain.response

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 10/03/2018 22:16
 * @description
 */
data class PriceChart(var date: Double = 0.0,
                      var high: Double = 0.0,
                      var low: Double = 0.0,
                              var open: Double = 0.0,
                      var close: Double = 0.0,
                      var volume: Double = 0.0,
                      var quoteVolume: Double = 0.0,
                      var weightedAverage: Double = 0.0)
