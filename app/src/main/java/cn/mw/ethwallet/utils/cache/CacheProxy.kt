package cn.mw.ethwallet.utils.cache

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 11/03/2018 19:05
 * @description
 */
class CacheProxy private constructor(context: Context, cacheName: String, withDisc: Boolean) : CacheApi {

    private val memCache: CacheApi
    private var discCache: CacheApi? = null

    init {
        if (withDisc) {
            discCache = DiscCacheImpl[context, cacheName]
        } else {
            discCache = CacheEmpty()
        }
        memCache = MemCacheImpl.get()
    }

    override fun put(key: String, value: String) {
        memCache.put(key, value)
        discCache!!.put(key, value)
    }

    override fun put(key: String, value: String, saveTime: Int) {
        memCache.put(key, value, saveTime)
        discCache!!.put(key, value, saveTime)
    }

    override fun getString(key: String): String? {
        return if (!TextUtils.isEmpty(memCache.getString(key))) {
            memCache.getString(key)
        } else {
            discCache!!.getString(key)
        }
    }

    override fun put(key: String, value: JSONObject) {
        memCache.put(key, value)
        discCache!!.put(key, value)
    }

    override fun put(key: String, value: JSONObject, saveTime: Int) {
        memCache.put(key, value, saveTime)
        discCache!!.put(key, value, saveTime)
    }

    override fun getJSONObject(key: String): JSONObject? {
        return if (memCache.getJSONObject(key) != null) {
            memCache.getJSONObject(key)
        } else {
            discCache!!.getJSONObject(key)
        }
    }

    override fun put(key: String, value: JSONArray) {
        memCache.put(key, value)
        discCache!!.put(key, value)
    }

    override fun put(key: String, value: JSONArray, saveTime: Int) {
        memCache.put(key, value, saveTime)
        discCache!!.put(key, value, saveTime)
    }

    override fun getJSONArray(key: String): JSONArray? {
        return if (memCache.getJSONArray(key) != null) {
            memCache.getJSONArray(key)
        } else {
            discCache!!.getJSONArray(key)
        }
    }

    override fun put(key: String, value: ByteArray) {
        memCache.put(key, value)
        discCache!!.put(key, value)
    }

    override fun put(key: String, value: ByteArray, saveTime: Int) {
        memCache.put(key, value, saveTime)
        discCache!!.put(key, value, saveTime)
    }

    override fun getBytes(key: String): ByteArray? {
        return if (memCache.getBytes(key) != null) {
            memCache.getBytes(key)
        } else {
            discCache!!.getBytes(key)
        }
    }

    fun put(key: String, value: Serializable) {
        memCache.put(key, value)
        discCache!!.put(key, value)
    }

    override fun put(key: String, value: Serializable, saveTime: Int) {
        memCache.put(key, value, saveTime)
        discCache!!.put(key, value, saveTime)
    }

    override fun getObject(key: String): Any? {
        return if (memCache.getObject(key) != null) {
            memCache.getObject(key)
        } else {
            discCache!!.getObject(key)
        }
    }

    fun put(key: String, value: Parcelable) {
        memCache.put(key, value)
        discCache!!.put(key, value)
    }

    override fun put(key: String, value: Parcelable, saveTime: Int) {
        memCache.put(key, value, saveTime)
        discCache!!.put(key, value, saveTime)
    }

    override fun getParcelObject(key: String): Parcel? {
        return if (memCache.getParcelObject(key) != null) {
            memCache.getParcelObject(key)
        } else {
            discCache!!.getParcelObject(key)
        }
    }

    override fun <T> getObject(key: String, creator: Parcelable.Creator<T>): T? {
        return if (memCache.getObject(key, creator) != null) {
            memCache.getObject(key, creator)
        } else {
            discCache!!.getObject(key, creator)
        }
    }

    override fun remove(key: String): Boolean {
        memCache.remove(key)
        return discCache!!.remove(key)
    }

    override fun clear() {
        memCache.clear()
        discCache!!.clear()
    }

    companion object {

        internal operator fun get(context: Context, cacheName: String, withDisc: Boolean): CacheApi {
            return CacheProxy(context, cacheName, withDisc)
        }
    }
}