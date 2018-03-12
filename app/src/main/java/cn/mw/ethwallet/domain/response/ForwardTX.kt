package cn.mw.ethwallet.domain.response

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 12/03/2018 23:16
 * @description
 */
data class ForwardTX(var result: String, var error: ForwardTxError) {

    data class ForwardTxError(var message: String)
}
