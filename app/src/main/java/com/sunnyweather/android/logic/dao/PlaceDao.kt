package com.sunnyweather.android.logic.dao

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.model.PlaceHistory
import java.util.*

/**
 * 地点信息的DAO层，负责存取数据，这里使用sharedPreferences存储信息。单例类
 */
object PlaceDao {

    /**
     * 存储地点信息
     */
    fun savePlace(place: Place) {
        sharedPreferences().edit {
            putString("place", Gson().toJson(place))
        }
    }

    fun savePlaceHistory(placeHistory: PlaceHistory) {
        sharedPreferences().edit {
            putString("placeHistory", Gson().toJson(placeHistory))
        }
    }

    fun getSavedPlace(): Place {
        val placeJson = sharedPreferences().getString("place", "")
        return Gson().fromJson(placeJson, Place::class.java)
    }

    fun getSavedPlaceHistory(): PlaceHistory {
        val placeHistoryJson = sharedPreferences().getString("placeHistory", "")
        return Gson().fromJson(placeHistoryJson, PlaceHistory::class.java) ?: PlaceHistory(LinkedList())
    }

    fun isPlaceSaved() = sharedPreferences().contains("place")

    private fun sharedPreferences() = SunnyWeatherApplication.context.getSharedPreferences("sunny_weather", Context.MODE_PRIVATE)
}
