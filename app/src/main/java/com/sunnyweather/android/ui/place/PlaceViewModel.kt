package com.sunnyweather.android.ui.place

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Place

class PlaceViewModel : ViewModel() {

    //liveData动态数据
    private val searchLiveData = MutableLiveData<String>()

    val placeList = ArrayList<Place>()

    /**
     * 由于非直接返回数据，无法观察，需要switchMap转换，这样activity可以观测
     * 监听searchLiveData变化时，调用查询
     */
    val placeLiveData = Transformations.switchMap(searchLiveData) { query ->
        Repository.searchPlaces(query)
    }

    /**
     * 变更searchLiveData，变更后placeLiveData会查询并且变更
     */
    fun searchPlaces(query: String) {
        searchLiveData.value = query
    }

    fun savePlace(place: Place) = Repository.savePlace(place)
    fun getSavedPlace() = Repository.getSavedPlace()
    fun isPlaceSaved() = Repository.isPlaceSaved()
}
