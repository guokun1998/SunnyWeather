package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.dao.PlaceDao
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.model.PlaceHistory
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.*
import kotlin.coroutines.CoroutineContext

object Repository {

    // 缓存placeHistoryList
    private var placeHistoryList = LinkedList<Place>(PlaceDao.getSavedPlaceHistory().placeHistoryList)

    // 简单版
    // 仓库返回数据，可以做缓存操作
//    fun searchPlaces(query: String) = liveData(Dispatchers.IO) {
//        val result = try {
//            // 调用实际的HTTP请求
//            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
//            if (placeResponse.status == "ok") {
//                val places = placeResponse.places
//                Result.success(places)
//            } else {
//                Result.failure(RuntimeException("response status is${placeResponse.status}"))
//            }
//        } catch (e: Exception) {
//            Result.failure<List<Place>>(e)
//        }
//        // 提交liveData数据的更新
//        emit(result)
//    }

    /**
     * 升级版
     * 查询地点
     * Dispatchers.IO表示线程走IO线程，非主线程
     */
    fun searchPlaces(query: String) = fire(Dispatchers.IO) {
        val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
        if (placeResponse.status == "ok") {
            val places = placeResponse.places
            Result.success(places)
        } else {
            Result.failure(RuntimeException("response status is ${placeResponse.status}"))
        }
    }

    // 简单版
//    fun refreshWeather(lng: String, lat: String) = liveData(Dispatchers.IO) {
//        val result = try {
//            // 多协程作用域
//            coroutineScope {
//                // 协程1
//                val deferredRealtime = async {
//                    SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
//                }
//                // 协程2
//                val deferredDaily = async {
//                    SunnyWeatherNetwork.getDailyWeather(lng, lat)
//                }
//                // 函数使用async 调用await()即可获取执行后的结果
//                val realtimeResponse = deferredRealtime.await()
//                val dailyResponse = deferredDaily.await()
//                if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
//                    val weather = Weather(realtimeResponse.result.realtime, dailyResponse.result.daily)
//                    Result.success(weather)
//                } else {
//                    Result.failure(
//                        RuntimeException(
//                            "realtime response status is ${realtimeResponse.status}" + "daily response status is ${dailyResponse.status}"
//                        )
//                    )
//                }
//            }
//        } catch (e: Exception) {
//            Result.failure<Weather>(e)
//        }
//        // 返回结果
//        emit(result)
//    }

    /**
     * 升级版
     * 刷新天气，合并了获取实时天气和每天天气的结果
     */
    fun refreshWeather(lng: String, lat: String) = fire(Dispatchers.IO) {
        coroutineScope {
            // 函数使用async 调用await()即可获取执行后的结果
            // 异步进行了请求
            val deferredRealtime = async {
                SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
            }
            val deferredDaily = async {
                SunnyWeatherNetwork.getDailyWeather(lng, lat)
            }
            val realtimeResponse = deferredRealtime.await()
            val dailyResponse = deferredDaily.await()
            // 返回结果
            if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                val weather = Weather(realtimeResponse.result.realtime, dailyResponse.result.daily)
                Result.success(weather)
            } else {
                Result.failure(
                    RuntimeException(
                        "realtime response status is ${realtimeResponse.status}" + "daily response status is ${dailyResponse.status}"
                    )
                )
            }
        }
    }


    /**
     * suspend关键字，表示所有传入的Lambda表达式中的代码也是拥有挂起函数上下文的
     * 该函数通过使用高阶函数，封装了LiveData，提前进行了try catch，之后再调用就不必try catch了
     */
    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) = liveData<Result<T>>(context) {
        val result = try {
            block()
        } catch (e: Exception) {
            Result.failure<T>(e)
        }
        emit(result)
    }

    /**
     * 存储地点和地点信息到历史记录
     */
    fun savePlaceAndPlaceInformation(place: Place) {
        PlaceDao.savePlace(place)
        placeHistoryList.remove(place)
        placeHistoryList.addFirst(place)
        PlaceDao.savePlaceHistory(PlaceHistory(placeHistoryList))
    }

    fun savePlace(place: Place) = PlaceDao.savePlace(place)

    /**
     * 接口，获取已保存的地点
     */
    fun getSavedPlace() = PlaceDao.getSavedPlace()

    /**
     * 接口，判断是否已存储
     */
    fun isPlaceSaved() = PlaceDao.isPlaceSaved()

    fun savePlaceHistory(placeHistory: PlaceHistory) = PlaceDao.savePlaceHistory(placeHistory)

    fun getSavedPlaceHistory() : List<Place> {
        return placeHistoryList
    }

    fun deletePlaceHistory(place: Place) {
        placeHistoryList.remove(place)
        PlaceDao.savePlaceHistory(PlaceHistory(placeHistoryList))
    }
}