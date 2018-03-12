package cn.mw.ethwallet.domain.response

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 12/03/2018 15:12
 * @description
\ */
data class EtherPrice(var result: EtherPriceData) {
    data class EtherPriceData(var ethbtc: Double, var ethusd: Double)
}