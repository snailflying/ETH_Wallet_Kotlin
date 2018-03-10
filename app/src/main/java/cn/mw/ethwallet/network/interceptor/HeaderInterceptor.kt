package cn.mw.ethwallet.network.interceptor

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.io.IOException

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 2017/7/13 11:44 AM
 * @description
 */

class HeaderInterceptor : Interceptor {


    private fun bodyToString(request: Request): String {
        try {
            val copy = request.newBuilder().build()
            val buffer = Buffer()
            if (copy.body() == null) {
                return ""
            }
            copy.body()!!.writeTo(buffer)
            return buffer.readUtf8()
        } catch (e: IOException) {
            return "{\"err\": \"" + e.message + "\"}"
        }

    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()
        val requestBuilder = request.newBuilder()

        // 对request的body进d5加密
//        requestBuilder.addHeader("md5", Util.md5(bodyToString(request)))
        requestBuilder.addHeader("os_type", "ANDROID")
//        requestBuilder.addHeader("device_id", "99000549029255")

        return chain.proceed(requestBuilder.build())
    }
}