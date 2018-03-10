package cn.mw.ethwallet.utils

import android.content.Context
import cn.mw.ethwallet.domain.request.WalletDisplay
import java.io.*
import java.util.*

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 17:02
 * @description
 */
class AddressNameConverter private constructor(context: Context) {

    private var addressbook: HashMap<String, String>? = null
    private val wellknown_addresses: HashMap<String, String>

    val asAddressbook: ArrayList<WalletDisplay>
        get() {
            val erg = ArrayList<WalletDisplay>()
            for ((key, value) in addressbook!!) {
                erg.add(WalletDisplay(value, key))
            }
            Collections.sort(erg)
            return erg
        }

    init {
        try {
            load(context)
            if (!contains("0xa9981a33f6b1a18da5db58148b2357f22b44e1e0")) {
                put("0xa9981a33f6b1a18da5db58148b2357f22b44e1e0", "Mercury Development ✓", context)
            }
        } catch (e: Exception) {
            addressbook = HashMap()
            put("0xa9981a33f6b1a18da5db58148b2357f22b44e1e0", "Mercury Development ✓", context)
        }

        wellknown_addresses = WellKnownAddresses()
    }

    @Synchronized
    fun put(addresse: String, name: String?, context: Context) {
        if (name == null || name.length == 0)
            addressbook!!.remove(addresse)
        else
            addressbook!![addresse] = if (name.length > 22) name.substring(0, 22) else name
        save(context)
    }

    operator fun get(addresse: String): String? {
        return addressbook!![addresse]
    }

    operator fun contains(addresse: String): Boolean {
        return addressbook!!.containsKey(addresse)
    }

    @Synchronized
    fun save(context: Context) {
        val fout: FileOutputStream
        try {
            fout = FileOutputStream(File(context.filesDir, "namedb.dat"))
            val oos = ObjectOutputStream(fout)
            oos.writeObject(addressbook)
            oos.close()
            fout.close()
        } catch (e: Exception) {
        }

    }

    @Synchronized
    @Throws(IOException::class, ClassNotFoundException::class)
    fun load(context: Context) {
        val fout = FileInputStream(File(context.filesDir, "namedb.dat"))
        val oos = ObjectInputStream(BufferedInputStream(fout))
        addressbook = oos.readObject() as HashMap<String, String>
        oos.close()
        fout.close()
    }

    companion object {
        private var instance: AddressNameConverter? = null

        fun getInstance(context: Context): AddressNameConverter {
            if (instance == null)
                instance = AddressNameConverter(context)
            return instance!!
        }
    }

}