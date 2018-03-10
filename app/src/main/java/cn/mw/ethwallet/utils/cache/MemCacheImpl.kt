package cn.mw.ethwallet.utils.cache

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.util.*

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 10/03/2018 17:01
 * @description
 */
class MemCacheImpl private constructor(max_size: Int) : CacheApi {
    private val TAG = "MemCacheImpl"
    private val lruCache: LruCache<String, String>


    init {
        lruCache = object : LruCache<String, String>(max_size) {
             override fun sizeOf(key: String, value: String): Int {
                // 重写此方法来衡量每个字符段的大小。
                return value.toByteArray().size / 1024
            }
        }
    }

    /**--------------String相关操作-------------- */

    /**
     * 保存 String数据 到 缓存中
     *
     * @param key
     * @param value
     */
    override fun put(key: String, value: String) {
        lruCache.put(key, value)
    }

    /**
     * 保存String数据到缓存中
     *
     * @param key
     * @param value
     * @param saveTime 保存的时间，单位：秒
     */
    override fun put(key: String, value: String, saveTime: Int) {
        put(key, CacheUtils.newStringWithDateInfo(saveTime, value))
    }

    /**
     * 获取String数据
     *
     * @param key
     * @return String 数据
     */
    override fun getString(key: String): String? {
        val value = lruCache.get(key)
        if (TextUtils.isEmpty(value)) {
            return null
        }

        if (!CacheUtils.isDue(value!!)) {
            return CacheUtils.clearDateInfo(value)
        } else {
            remove(key)
            return null
        }

    }

    /**--------------JSONObject相关操作-------------- */

    /**
     * 保存 JSONObject数据 到 缓存中
     *
     * @param key
     * @param value
     */
    override fun put(key: String, value: JSONObject) {
        put(key, value.toString())
    }

    /**
     * 保存 JSONObject数据 到 缓存中
     *
     * @param key
     * @param value
     * @param saveTime 保存的时间，单位：秒
     */
    override fun put(key: String, value: JSONObject, saveTime: Int) {
        put(key, value.toString(), saveTime)
    }

    /**
     * 获取JSONObject数据
     *
     * @param key
     * @return JSONObject数据
     */
    override fun getJSONObject(key: String): JSONObject? {
        val JSONString = getString(key)
        return if (TextUtils.isEmpty(JSONString)) {
            null
        } else {
            try {
                JSONObject(JSONString)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

        }

    }

    /**--------------JSONArray相关操作-------------- */

    /**
     * 保存 JSONArray数据 到 缓存中
     *
     * @param key
     * @param value
     */
    override fun put(key: String, value: JSONArray) {
        put(key, value.toString())
    }

    /**
     * 保存 JSONArray数据 到 缓存中
     *
     * @param key
     * @param value
     * @param saveTime 保存的时间，单位：秒
     */
    override fun put(key: String, value: JSONArray, saveTime: Int) {
        put(key, value.toString(), saveTime)
    }

    /**
     * 读取JSONArray数据
     *
     * @param key
     * @return JSONArray数据
     */
    override fun getJSONArray(key: String): JSONArray? {
        val JSONString = getString(key)
        return if (TextUtils.isEmpty(JSONString)) {
            null
        } else {
            try {
                JSONArray(JSONString)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

        }

    }

    /**--------------byte[]相关操作-------------- */

    /**
     * 保存 byte数据 到 缓存中
     *
     * @param key
     * @param value
     */
    override fun put(key: String, value: ByteArray) {

        Log.i(TAG, "value size:" + value.size)
        put(key, Base64Util.encode(value))
    }

    /**
     * 保存 byte数据 到 缓存中
     *
     * @param key
     * @param value
     * @param saveTime 保存的时间，单位：秒
     */
    override fun put(key: String, value: ByteArray, saveTime: Int) {
        put(key, Arrays.toString(value), saveTime)
    }

    /**
     * 获取 byte 数据
     *
     * @param key
     * @return byte 数据
     */
    override fun getBytes(key: String): ByteArray? {
        val byteString = getString(key)
        return if (TextUtils.isEmpty(byteString)) {
            null
        } else {
            Base64Util.decode(byteString!!)
        }

    }

    /**
     * 保存 Serializable数据到 缓存中
     *
     * @param key
     * @param value
     * @param saveTime 保存的时间，单位：秒
     */
    override fun put(key: String, value: Serializable, saveTime: Int) {
        var baos: ByteArrayOutputStream? = null
        var oos: ObjectOutputStream? = null
        try {
            baos = ByteArrayOutputStream()
            oos = ObjectOutputStream(baos)
            oos.writeObject(value)
            val data = baos.toByteArray()
            if (saveTime != -1) {
                put(key, data, saveTime)
            } else {
                put(key, data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                oos!!.close()
            } catch (e: IOException) {
            }

        }
    }

    /**
     * 获取可序列化的数据
     *
     * @param key
     * @return Serializable 数据
     */
    override fun getObject(key: String): Any? {
        val data = getBytes(key)
        if (data != null) {
            var bais: ByteArrayInputStream? = null
            var ois: ObjectInputStream? = null
            try {
                bais = ByteArrayInputStream(data)
                ois = ObjectInputStream(bais)
                return ois.readObject()
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            } finally {
                try {
                    if (bais != null) {
                        bais.close()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                try {
                    if (ois != null) {
                        ois.close()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
        return null
    }

    /**
     * 保存 Parcelable数据到 缓存中
     *
     * @param key
     * @param value
     * @param saveTime 保存的时间，单位：秒
     */
    override fun put(key: String, value: Parcelable, saveTime: Int) {

        try {
            val data = ParcelableUtils.marshall(value)
            if (saveTime != -1) {
                put(key, data, saveTime)
            } else {
                put(key, data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 获取Parcel，如果要转换成相应的class，则
     * Parcel parcel = cache.getParcelObject(key)
     * MyClass myclass = new MyClass(parcel); // Or MyClass.CREATOR.createFromParcel(parcel).
     *
     * @param key
     * @return Parcel 数据
     */
    override fun getParcelObject(key: String): Parcel? {
        val data = getBytes(key)
        return if (data != null) {
            ParcelableUtils.unmarshall(data)
        } else null
    }

    /**
     * 获取可序列化的数据
     * MyClass myclass = cache.getObject(key, MyClass.CREATOR);
     *
     * @param key
     * @param creator
     * @param <T>
     * @return
    </T> */
    override fun <T> getObject(key: String, creator: Parcelable.Creator<T>): T? {
        val data = getBytes(key)
        return if (data != null) {
            ParcelableUtils.unmarshall(data, creator)
        } else null
    }

    /**
     * 删除某个key
     *
     * @param key
     * @return 是否删除成功
     */
    override fun remove(key: String): Boolean {
        lruCache.remove(key)
        return true
    }

    /**
     * 清除所有数据
     */
    override fun clear() {
        lruCache.evictAll()
    }

    companion object {

        private var cache: MemCacheImpl? = null

        fun get(): CacheApi {
            // 计算可使用的最大内存
            val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
            // 取八分之一的可用内存作为缓存
            val cacheSize = maxMemory / 8
            return get(cacheSize)
        }

        internal operator fun get(max_size: Int): CacheApi {
            if (cache == null) {

                cache = MemCacheImpl(max_size)
            }
            return cache!!
        }
    }


}
/**--------------Serializable相关操作-------------- */
/**
 * 保存序列化的数据 到 缓存中
 *
 * @param key
 * @param value
 */
/**--------------Parcelable相关操作-------------- */
/**
 * 保存序列化的数据 到 缓存中
 *
 * @param key
 * @param value
 */