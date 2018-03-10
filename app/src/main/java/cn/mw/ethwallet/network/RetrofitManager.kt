package cn.mw.ethwallet.network

import cn.mw.ethwallet.domain.api.APIService
import cn.mw.ethwallet.network.interceptor.HeaderInterceptor
import cn.mw.ethwallet.network.interceptor.LoggingInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 10/03/2018 10:04
 * @description
 */
object RetrofitManager {

    private var mRetrofit: Retrofit? = null
    private var mOkHttpClient: OkHttpClient? = null

    private val okHttpClient: OkHttpClient
        get() {
            if (null == mOkHttpClient) {
                val builder = OkHttpClient.Builder()
                builder.writeTimeout((30 * 1000).toLong(), TimeUnit.MILLISECONDS)
                builder.readTimeout((20 * 1000).toLong(), TimeUnit.MILLISECONDS)
                builder.connectTimeout((15 * 1000).toLong(), TimeUnit.MILLISECONDS)

                val loggingInterceptor = LoggingInterceptor.Builder()
                        .loggable(true)
                        .request()
                        .requestTag("Request")
                        .response()
                        .responseTag("Response")
                        .build()
                builder.addInterceptor(HeaderInterceptor())
                builder.addInterceptor(loggingInterceptor)

                mOkHttpClient = builder.build()
            }
            return mOkHttpClient!!
        }


    fun retrofit(): Retrofit {
        if (mRetrofit == null) {
            mRetrofit = Retrofit.Builder()
                    .baseUrl(APIService.API_BASE_SERVER_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(okHttpClient)
                    .build()
        }

        return mRetrofit!!

    }


}