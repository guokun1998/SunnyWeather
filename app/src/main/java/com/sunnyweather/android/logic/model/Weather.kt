package com.sunnyweather.android.logic.model

/**
 * 天气数据模型类
 */
data class Weather(val realtime: RealtimeResponse.Realtime, val daily: DailyResponse.Daily)