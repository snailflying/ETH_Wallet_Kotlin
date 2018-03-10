package cn.mw.ethwallet.domain.request

import cn.mw.ethwallet.interfaces.StorableWallet
import java.io.Serializable

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 16:58
 * @description
 */
class FullWallet(override var pubKey: String, var path: String?) : StorableWallet, Serializable {
    override var dateAdded: Long = 0

    init {
        this.pubKey = pubKey.toLowerCase()
        this.dateAdded = System.currentTimeMillis()
    }

    companion object {

        private const val serialVersionUID = 2622313531196422839L
    }
}