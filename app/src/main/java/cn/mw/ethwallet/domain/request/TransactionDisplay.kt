package cn.mw.ethwallet.domain.request

import java.math.BigDecimal
import java.math.BigInteger

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 16:56
 * @description
 */
class TransactionDisplay(private var fromAddress: String?, private var toAddress: String?, amount: BigInteger, var confirmationStatus: Int, var date: Long, var walletName: String?, var type: Byte, var txHash: String?, var nounce: String?, var block: Long, var gasUsed: Int, var gasprice: Long, var isError: Boolean) : Comparable<Any> {
    var amountNative: BigInteger? = null
        private set

    val amount: Double
        get() =
            BigDecimal(amountNative).divide(BigDecimal(1000000000000000000.0), 8, BigDecimal.ROUND_UP).toDouble()

    init {
        this.amountNative = amount
    }

    fun getFromAddress(): String {
        return fromAddress!!.toLowerCase()
    }

    fun setFromAddress(fromAddress: String) {
        this.fromAddress = fromAddress
    }

    fun getToAddress(): String {
        return toAddress!!.toLowerCase()
    }

    fun setToAddress(toAddress: String) {
        this.toAddress = toAddress
    }

    fun setAmount(amount: BigInteger) {
        this.amountNative = amount
    }

    override fun toString(): String {
        return "TransactionDisplay{" +
                "fromAddress='" + fromAddress + '\''.toString() +
                ", toAddress='" + toAddress + '\''.toString() +
                ", amount=" + amountNative +
                ", confirmationStatus=" + confirmationStatus +
                ", date='" + date + '\''.toString() +
                ", walletName='" + walletName + '\''.toString() +
                '}'.toString()
    }

    override operator fun compareTo(other: Any): Int {
        if (this.date < (other as TransactionDisplay).date)
            return 1
        return if (this.date == other.date) 0 else -1
    }

    companion object {

        val NORMAL: Byte = 0
        val CONTRACT: Byte = 1
    }
}