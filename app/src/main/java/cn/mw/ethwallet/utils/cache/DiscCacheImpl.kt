package cn.mw.ethwallet.utils.cache

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 10/03/2018 19:00
 * @description
 */
class DiscCacheImpl private constructor(cacheDir: File, max_size: Long, max_count: Int) : CacheApi {
    private val cacheManager: CacheManager

    init {
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            throw RuntimeException("can't make dirs in " + cacheDir.absolutePath)
        }
        cacheManager = CacheManager(cacheDir, max_size, max_count)
    }

    /**--------------String相关操作-------------- */

    /**
     * 保存 String数据 到 缓存中
     *
     * @param key
     * @param value
     */
    override fun put(key: String, value: String) {
        val file = cacheManager.newFile(key)
        var out: BufferedWriter? = null
        try {
            out = BufferedWriter(FileWriter(file), 1024)
            out.write(value)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (out != null) {
                try {
                    out.flush()
                    out.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            cacheManager.put(file)
        }
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
        val file = cacheManager.get(key)
        if (!file.exists()) {
            return null
        }

        var removeFile = false
        var `in`: BufferedReader? = null
        try {
            `in` = BufferedReader(FileReader(file))
            var readString = ""
//            val currentLine: String = `in`.readLine()
            readString += `in`.readLine()

//            while ((currentLine ) != null) {
//            }
            if (!CacheUtils.isDue(readString)) {
                return CacheUtils.clearDateInfo(readString)
            } else {
                removeFile = true
                return null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            if (`in` != null) {
                try {
                    `in`.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            if (removeFile) {
                remove(key)
            }
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
        try {
            return JSONObject(JSONString)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
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
        try {
            return JSONArray(JSONString)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
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
        val file = cacheManager.newFile(key)
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(file)
            out.write(value)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (out != null) {
                try {
                    out.flush()
                    out.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            cacheManager.put(file)
        }
    }

    /**
     * 保存 byte数据 到 缓存中
     *
     * @param key
     * @param value
     * @param saveTime 保存的时间，单位：秒
     */
    override fun put(key: String, value: ByteArray, saveTime: Int) {
        put(key, CacheUtils.newByteArrayWithDateInfo(saveTime, value))
    }

    /**
     * 获取 byte 数据
     *
     * @param key
     * @return byte 数据
     */
    override fun getBytes(key: String): ByteArray? {
        var RAFile: RandomAccessFile? = null
        var removeFile = false
        try {
            val file = cacheManager.get(key)
            if (!file.exists()) {
                return null
            }
            RAFile = RandomAccessFile(file, "r")
            val byteArray = ByteArray(RAFile.length().toInt())
            RAFile.read(byteArray)
            if (!CacheUtils.isDue(byteArray)) {
                return CacheUtils.clearDateInfo(byteArray)
            } else {
                removeFile = true
                return null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            if (RAFile != null) {
                try {
                    RAFile.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            if (removeFile) {
                remove(key)
            }
        }
    }

    /**
     * 保存 Serializable数据到 缓存中
     *
     * @param key
     * @param value
     * @param saveTime 保存的时间，单位：秒
     */
//    @JvmOverloads
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
//    @JvmOverloads
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
        return cacheManager.remove(key)
    }

    /**
     * 清除所有数据
     */
    override fun clear() {
        cacheManager.clear()
    }

    private inner class CacheManager(internal var cacheDir: File, private val sizeLimit: Long, private val countLimit: Int) {
        private val cacheSize: AtomicLong
        private val cacheCount: AtomicInteger
        private val lastUsageDates = Collections.synchronizedMap(HashMap<File, Long>())

        init {
            cacheSize = AtomicLong()
            cacheCount = AtomicInteger()
            calculateCacheSizeAndCacheCount()
        }

        /**
         * 计算 cacheSize和cacheCount
         */
        private fun calculateCacheSizeAndCacheCount() {
            Thread(Runnable {
                var size = 0
                var count = 0
                val cachedFiles = cacheDir.listFiles()
                if (cachedFiles != null) {
                    for (cachedFile in cachedFiles) {
                        size += calculateSize(cachedFile).toInt()
                        count += 1
                        lastUsageDates[cachedFile] = cachedFile.lastModified()
                    }
                    cacheSize.set(size.toLong())
                    cacheCount.set(count)
                }
            }).start()
        }

        internal fun put(file: File) {
            var curCacheCount = cacheCount.get()
            while (curCacheCount + 1 > countLimit) {
                val freedSize = removeNext()
                cacheSize.addAndGet(-freedSize)

                curCacheCount = cacheCount.addAndGet(-1)
            }
            cacheCount.addAndGet(1)

            val valueSize = calculateSize(file)
            var curCacheSize = cacheSize.get()
            while (curCacheSize + valueSize > sizeLimit) {
                val freedSize = removeNext()
                curCacheSize = cacheSize.addAndGet(-freedSize)
            }
            cacheSize.addAndGet(valueSize)

            val currentTime = System.currentTimeMillis()
            file.setLastModified(currentTime)
            lastUsageDates[file] = currentTime
        }

        internal operator fun get(key: String): File {
            val file = newFile(key)
            val currentTime = System.currentTimeMillis()
            file.setLastModified(currentTime)
            lastUsageDates[file] = currentTime

            return file
        }

        internal fun newFile(key: String): File {
            return File(cacheDir, key.hashCode().toString() + "")
        }

        internal fun remove(key: String): Boolean {
            val file = get(key)
            return file.delete()
        }

        internal fun clear() {
            lastUsageDates.clear()
            cacheSize.set(0)
            val files = cacheDir.listFiles()
            if (files != null) {
                for (f in files) {
                    f.delete()
                }
            }
        }

        /**
         * 移除旧的文件
         *
         * @return
         */
        private fun removeNext(): Long {
            if (lastUsageDates.isEmpty()) {
                return 0
            }

            var oldestUsage: Long = 0
            var mostLongUsedFile: File? = null
            val entries = lastUsageDates.entries
            synchronized(lastUsageDates) {
                for ((key, lastValueUsage) in entries) {
                    if (mostLongUsedFile == null) {
                        mostLongUsedFile = key
                        oldestUsage = lastValueUsage
                    } else {
                        if (lastValueUsage < oldestUsage) {
                            oldestUsage = lastValueUsage
                            mostLongUsedFile = key
                        }
                    }
                }
            }

            val fileSize = calculateSize(mostLongUsedFile)
            if (mostLongUsedFile!!.delete()) {
                lastUsageDates.remove(mostLongUsedFile)
            }
            return fileSize
        }

        private fun calculateSize(file: File?): Long {
            return file!!.length()
        }
    }

    companion object {

        private val MAX_SIZE = 1000 * 1000 * 50 // 50 mb
        private val MAX_COUNT = Integer.MAX_VALUE // 不限制存放数据的数量
        private val mInstanceMap = HashMap<String, DiscCacheImpl>()

        operator fun get(context: Context, cacheName: String): CacheApi {
            val cacheDir = FileUtils.getDiskCacheDir(context.applicationContext, cacheName)
            return if (cacheDir == null || !cacheDir.exists() && !cacheDir.mkdirs()) {
                CacheEmpty()
            } else {
                get(cacheDir, MAX_SIZE.toLong(), MAX_COUNT)
            }
        }

        internal operator fun get(cacheDir: File, max_size: Long, max_count: Int): CacheApi {
            var cache: DiscCacheImpl? = mInstanceMap[cacheDir.absoluteFile.toString() + "_" + android.os.Process.myPid()]
            if (cache == null) {
                cache = DiscCacheImpl(cacheDir, max_size, max_count)
                mInstanceMap[cacheDir.absolutePath + "_" + android.os.Process.myPid()] = cache
            }
            return cache
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