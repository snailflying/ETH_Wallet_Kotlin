package cn.mw.ethwallet.domain.response

import java.math.BigDecimal
import java.math.BigInteger

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 14:28
 * @description
 */
data class TokenDisplay(var name: String?, var shorty: String?, var balance: BigDecimal?, var digits: Int, var usdprice: Double, var contractAddr: String?, var totalSupply: String?, var holderCount: Double, var createdAt: Long) : Comparable<Any> {

    /**
     * Uses digits and balance to create a double value
     *
     * @return Token balance in double
     */
    val balanceDouble: Double
        get() = balance!!.divide(BigDecimal("10").pow(digits)).toDouble()

    /**
     * Uses digits and total supply to create a long value
     *
     * @return Token supply in long
     */
    val totalSupplyLong: Double
        get() = BigInteger(totalSupply).divide(BigInteger("10").pow(digits)).toDouble()

    override operator fun compareTo(other: Any): Int {
        return (other as TokenDisplay).shorty!!.compareTo(shorty!!)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as TokenDisplay?

        if (digits != that!!.digits) return false
        return if (name != that.name) false else shorty == that.shorty

    }

    override fun hashCode(): Int {
        var result = name!!.hashCode()
        result = 31 * result + shorty!!.hashCode()
        result = 31 * result + digits
        return result
    }
}