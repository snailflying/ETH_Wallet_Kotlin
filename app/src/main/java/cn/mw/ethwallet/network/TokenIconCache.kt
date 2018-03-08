package cn.mw.ethwallet.network

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.*
import java.util.*

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 16:32
 * @description
 */
class TokenIconCache private constructor(c: Context) {

    private var cache: HashMap<String, ByteArray>? = null

    init {
        try {
            load(c)

        } catch (e: Exception) {
            cache = HashMap()
        }

        Log.d("iconmap", cache!!.toString())
    }

    operator fun get(s: String): Bitmap? {
        if (cache!![s] == null) return null
        val options = BitmapFactory.Options()
        options.inSampleSize = calculateInSampleSize(options, 20, 31)
        return BitmapFactory.decodeByteArray(cache!![s], 0, cache!![s]!!.size, options)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun put(c: Context, s: String, b: Bitmap?): Boolean {
        if (b == null || cache!!.containsKey(s)) return false
        val stream = ByteArrayOutputStream()
        b.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()

        cache!![s] = byteArray
        try {
            save(c)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return true
    }

    operator fun contains(s: String): Boolean {
        return cache!!.containsKey(s)
    }

    @Throws(Exception::class)
    fun save(activity: Context) {
        var outputStream: ObjectOutputStream? = null
        var fos: OutputStream? = null
        try {
            fos = BufferedOutputStream(FileOutputStream(File(activity.filesDir, "tokeniconcache.dat")))
            outputStream = ObjectOutputStream(fos)
            outputStream.writeObject(cache)
        } finally {
            if (outputStream != null)
                outputStream.close()
            if (fos != null)
                fos.close()
        }
    }

    @Throws(Exception::class)
    fun load(activity: Context) {
        var inputStream: ObjectInputStream? = null
        try {
            inputStream = ObjectInputStream(
                    BufferedInputStream(FileInputStream(File(activity.filesDir, "tokeniconcache.dat"))))
            cache = inputStream.readObject() as HashMap<String, ByteArray>
        } finally {
            if (inputStream != null)
                inputStream.close()
        }
    }

    companion object {
        private var instance: TokenIconCache? = null

        fun getInstance(c: Context): TokenIconCache {
            if (instance == null)
                instance = TokenIconCache(c)
            return instance!!
        }
    }

}