package com.sunnyweather.android.logic.network

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * 服务总控，包括地点服务和天气服务
 */
object SunnyWeatherNetwork {
    // 地点服务
    /**
     * 传入封装接口类PlaceService
     */
    private val placeService = ServiceCreator.create<PlaceService>()

    /**
     * 查询地点接口函数，协程操作
     */
    suspend fun searchPlaces(query: String) = placeService.searchPlaces(query).await()



    // 天气服务
    /**
     * 传入封装接口类WeatherService
     */
    private val weatherService = ServiceCreator.create(WeatherService::class.java)

    /**
     * 查询每日天气协程接口函数
     */
    suspend fun getDailyWeather(lng: String, lat: String) = weatherService.getDailyWeather(lng, lat).await()

    /**
     * 获取实时天气协程接口函数
     */
    suspend fun getRealtimeWeather(lng: String, lat: String) = weatherService.getRealtimeWeather(lng, lat).await()

    /**
     * 协程函数，为所有的Call<T>的查询结果函数新增函数.await()
     * 服务接口类返回的为Call<T>
     * 为请求的服务绑定一个callback（retrofit提供），onResponse/onFailure，请求结束后再返回结果
     */
    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (body != null) continuation.resume(body)
                    else continuation.resumeWithException(RuntimeException("response body is null"))
                }
                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }

}