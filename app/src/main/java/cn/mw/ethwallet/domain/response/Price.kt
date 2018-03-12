package cn.mw.ethwallet.domain.response

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 12/03/2018 23:30
 * @description
 */
data class Price(val result: List<PriceData>){

    data class PriceData(val account:String,val balance: String)
}
