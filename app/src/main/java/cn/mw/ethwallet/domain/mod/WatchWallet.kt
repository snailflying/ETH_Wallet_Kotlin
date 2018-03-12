package cn.mw.ethwallet.domain.mod

import cn.mw.ethwallet.interfaces.StorableWallet
import java.io.Serializable

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 17:00
 * @description
 */
class WatchWallet(override var pubKey: String) : StorableWallet, Serializable {
    override var dateAdded: Long = 0

    init {
        this.pubKey = pubKey.toLowerCase()
        this.dateAdded = System.currentTimeMillis()
    }

    companion object {

        private const val serialVersionUID = -146500951598835658L
    }
}