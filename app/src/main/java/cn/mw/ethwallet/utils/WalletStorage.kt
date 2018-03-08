package cn.mw.ethwallet.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.preference.PreferenceManager
import cn.mw.ethwallet.BuildConfig
import cn.mw.ethwallet.activities.MainActivity
import cn.mw.ethwallet.domain.FullWallet
import cn.mw.ethwallet.domain.WatchWallet
import cn.mw.ethwallet.interfaces.StorableWallet
import org.json.JSONException
import org.web3j.crypto.CipherException
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils
import java.io.*
import java.util.*

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 17:06
 * @description
 */
class WalletStorage private constructor(context: Context) {

    private lateinit var mapdb: ArrayList<StorableWallet>
    private var walletToExport: String? = null // Used as temp if users wants to export but still needs to grant write permission

    val fullOnly: ArrayList<String>
        @Synchronized get() {
            val erg = ArrayList<String>()
            if (mapdb!!.size == 0) return erg
            for (i in mapdb!!.indices) {
                val cur = mapdb!![i]
                if (cur is FullWallet)
                    erg.add(cur.pubKey)
            }
            return erg
        }

    init {
        try {
            load(context)
        } catch (e: Exception) {
            e.printStackTrace()
            mapdb = ArrayList<StorableWallet>()
        }

        if (mapdb!!.size == 0)
        // Try to find local wallets
            checkForWallets(context)
    }

    @Synchronized
    fun add(addresse: StorableWallet, context: Context): Boolean {
        for (i in mapdb!!.indices)
            if (mapdb!![i].pubKey.equals(addresse.pubKey, true)) return false
        mapdb!!.add(addresse)
        save(context)
        return true
    }

    @Synchronized
    fun get(): ArrayList<StorableWallet>? {
        return mapdb
    }

    @Synchronized
    fun isFullWallet(addr: String): Boolean {
        if (mapdb!!.size == 0) return false
        for (i in mapdb!!.indices) {
            val cur = mapdb!![i]
            if (cur is FullWallet && cur.pubKey.equals(addr, true))
                return true
        }
        return false
    }

    fun removeWallet(address: String, context: Context) {
        var position = -1
        for (i in mapdb!!.indices) {
            if (mapdb!![i].pubKey.equals(address, true)) {
                position = i
                break
            }
        }
        if (position >= 0) {
            if (mapdb!![position] is FullWallet)
            // IF full wallet delete private key too
                File(context.filesDir, address.substring(2, address.length)).delete()
            mapdb!!.removeAt(position)
        }
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.remove(address)
        editor.apply()
        save(context)
    }

    fun checkForWallets(c: Context) {
        // Full wallets
        val wallets = c.filesDir.listFiles() ?: return
        for (i in wallets.indices) {
            if (wallets[i].isFile) {
                if (wallets[i].name.length == 40) {
                    add(FullWallet("0x" + wallets[i].name, wallets[i].name), c)
                }
            }
        }

        // Watch only
        val preferences = PreferenceManager.getDefaultSharedPreferences(c)
        val allEntries = preferences.all
        for ((key) in allEntries) {

            //todo: mapdb-> mapdb.toString
            if (key.length == 42 && !mapdb!!.toString().contains(key))
                add(WatchWallet(key), c)
        }
        if (mapdb!!.size > 0)
            save(c)
    }

    fun importingWalletsDetector(c: MainActivity) {
        if (!ExternalStorageHandler.hasReadPermission(c)) {
            ExternalStorageHandler.askForPermissionRead(c)
            return
        }
        val wallets = File(Environment.getExternalStorageDirectory().absolutePath + "/Lunary/").listFiles()
        if (wallets == null) {
            Dialogs.noImportWalletsFound(c)
            return
        }
        val foundImports = ArrayList<File>()
        for (i in wallets.indices) {
            if (wallets[i].isFile) {
                if (wallets[i].name.startsWith("UTC") && wallets[i].name.length >= 40) {
                    foundImports.add(wallets[i]) // Mist naming
                } else if (wallets[i].name.length >= 40) {
                    val position = wallets[i].name.indexOf(".json")
                    if (position < 0) continue
                    val addr = wallets[i].name.substring(0, position)
                    if (addr.length == 40 && !mapdb!!.toString().contains("0x" + wallets[i].name)) {
                        foundImports.add(wallets[i]) // Exported with Lunary
                    }
                }
            }
        }
        if (foundImports.size == 0) {
            Dialogs.noImportWalletsFound(c)
            return
        }
        Dialogs.importWallets(c, foundImports)
    }

    fun setWalletForExport(wallet: String) {
        walletToExport = wallet
    }

    fun exportWallet(c: Activity): Boolean {
        return exportWallet(c, false)
    }

    @Throws(Exception::class)
    fun importWallets(c: Context, toImport: ArrayList<File>) {
        for (i in toImport.indices) {

            val address = stripWalletName(toImport[i].name)
            if (address.length == 40) {
                copyFile(toImport[i], File(c.filesDir, address))
                if (!BuildConfig.DEBUG)
                    toImport[i].delete()
                WalletStorage.getInstance(c).add(FullWallet("0x" + address, address), c)
                AddressNameConverter.getInstance(c).put("0x" + address, "Wallet " + ("0x" + address).substring(0, 6), c)

                val mediaScannerIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                val fileContentUri = Uri.fromFile(toImport[i]) // With 'permFile' being the File object
                mediaScannerIntent.data = fileContentUri
                c.sendBroadcast(mediaScannerIntent) // With 'this' being the context, e.g. the activity

            }
        }
    }

    private fun exportWallet(c: Activity, already: Boolean): Boolean {
        if (walletToExport == null) return false
        if (walletToExport!!.startsWith("0x"))
            walletToExport = walletToExport!!.substring(2)

        if (ExternalStorageHandler.hasPermission(c)) {
            val folder = File(Environment.getExternalStorageDirectory(), "Lunary")
            if (!folder.exists()) folder.mkdirs()

            val storeFile = File(folder, walletToExport!! + ".json")
            try {
                copyFile(File(c.filesDir, walletToExport!!), storeFile)
            } catch (e: IOException) {
                return false
            }

            // fix, otherwise won't show up via USB
            val mediaScannerIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val fileContentUri = Uri.fromFile(storeFile) // With 'permFile' being the File object
            mediaScannerIntent.data = fileContentUri
            c.sendBroadcast(mediaScannerIntent) // With 'this' being the context, e.g. the activity
            return true
        } else if (!already) {
            ExternalStorageHandler.askForPermission(c)
            return exportWallet(c, true)
        } else {
            return false
        }
    }


    @Throws(IOException::class)
    private fun copyFile(src: File, dst: File) {
        val inChannel = FileInputStream(src).channel
        val outChannel = FileOutputStream(dst).channel
        try {
            inChannel!!.transferTo(0, inChannel.size(), outChannel)
        } finally {
            inChannel?.close()
            outChannel?.close()
        }
    }

    @Throws(IOException::class, JSONException::class, CipherException::class)
    fun getFullWallet(context: Context, password: String, wallet: String): Credentials {
        var wallet = wallet
        if (wallet.startsWith("0x"))
            wallet = wallet.substring(2, wallet.length)
        return WalletUtils.loadCredentials(password, File(context.filesDir, wallet))
    }


    @Synchronized
    fun save(context: Context) {
        val fout: FileOutputStream
        try {
            fout = FileOutputStream(File(context.filesDir, "wallets.dat"))
            val oos = ObjectOutputStream(fout)
            oos.writeObject(mapdb)
            oos.close()
            fout.close()
        } catch (e: Exception) {
        }

    }

    @Synchronized
    @Throws(IOException::class, ClassNotFoundException::class)
    fun load(context: Context) {
        val fout = FileInputStream(File(context.filesDir, "wallets.dat"))
        val oos = ObjectInputStream(BufferedInputStream(fout))
        mapdb = oos.readObject() as ArrayList<StorableWallet>
        oos.close()
        fout.close()
    }

    companion object {
        private var instance: WalletStorage? = null

        fun getInstance(context: Context): WalletStorage {
            if (instance == null) {
                instance = WalletStorage(context)
            }
            return instance!!
        }

        fun stripWalletName(s: String): String {
            var s = s
            if (s.lastIndexOf("--") > 0)
                s = s.substring(s.lastIndexOf("--") + 2)
            if (s.endsWith(".json"))
                s = s.substring(0, s.indexOf(".json"))
            return s
        }
    }

}