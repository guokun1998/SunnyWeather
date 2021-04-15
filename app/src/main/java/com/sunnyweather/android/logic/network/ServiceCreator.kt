package com.sunnyweather.android.logic.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * 服务创建类，由Retrofit提供服务，单例
 */
object ServiceCreator {
    private const val BASE_URL = "https://api.caiyunapp.com/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /**
     * 创建服务
     */
    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)

    /**
     * inline内联函数，减少lambda函数创建匿名类的开销。但也不应在复杂函数中使用
     * 传统的泛型是会在程序运行的过程中进行擦除操作，而使用reified修饰的泛型，通过反编译二进制表现就是将泛型替换成具体的类型，不进行类型擦除。
     * 实际该函数的作用仅是简化参数输入：以泛型参数作为函数参数
     */
    inline fun <reified T> create(): T = create(T::class.java)

}