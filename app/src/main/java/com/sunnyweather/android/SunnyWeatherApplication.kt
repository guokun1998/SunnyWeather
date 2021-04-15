package com.sunnyweather.android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 * 提供全局上下文
 */
class SunnyWeatherApplication : Application() {
    /**
     * 静态成员
     */
    companion object {
        // API Token
        const val TOKEN = "N5v9ty0FMiTUHSWN"
        // 全局上下文
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}
