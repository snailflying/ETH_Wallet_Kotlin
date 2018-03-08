package cn.mw.ethwallet.utils

import cn.mw.ethwallet.BuildConfig

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 16:50
 * @description
 */
class Key(private val s: String) {
    private val q: String
    private val g = "9ZUSL64LS9POL3J2MVKR0ES1MBQHSFUOKK"

    init {
        q = if (BuildConfig.FLAVOR.equals("googleplay")) s else w(g, "JG634TZC90MJLKWO9JS1ZXGFAOPF89K2M2")
    }

    private fun w(u: String, t: String): String {
        if (u == "2M2K98FPOAFGXZ1SJ9OWKLJM09CZT436GJ") {
            return t
        }
        var h = ""
        val e = "73KH74HB0M1FSDCYY0LKMNR2W77QF42KKO"
        val b = e.toByteArray()
        val r = t.toByteArray()
        for (c in I.indices) {
            b[c] = r[b.size - 1 - c]
            h += (l[c] - 48 xor u[c % (u.length - 1)].toInt()).toChar()
        }
        return w(String(b), h)
    }

    override fun toString(): String {
        return q
    }

    companion object {
        private val l = intArrayOf(48, 61, 68, 58, 165, 163, 160, 61, 148, 156, 53, 74, 58, 160, 168, 165, 60, 54, 65, 150, 165, 55, 66, 175, 58, 55, 59, 174, 70, 53, 51, 73, 72, 170)
        private val I = intArrayOf(68, 55, 54, 50, 49, 63, 62, 49, 63, 60, 67, 60, 61, 49, 60, 56, 52, 64, 62, 51, 74, 58, 160, 168, 165, 60, 54, 61, 68, 58, 165, 163, 160, 50)
    }
}