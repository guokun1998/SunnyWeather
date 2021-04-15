package com.sunnyweather.android.logic.model

import com.google.gson.annotations.SerializedName

/**
 * 地点回复
 */
data class PlaceResponse(val status: String, val places: List<Place>)

/**
 * 地点
 */
data class Place(val name: String, val location: Location, @SerializedName("formatted_address") val address: String)

/**
 * 位置
 */
data class Location(val lng: String, val lat: String)
