package com.sunnyweather.android.ui.weather

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import com.sunnyweather.android.R
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import com.sunnyweather.android.ui.place.PlaceViewModel
import kotlinx.android.synthetic.main.activity_weather.*
import kotlinx.android.synthetic.main.forecast.*
import kotlinx.android.synthetic.main.fragment_place.*
import kotlinx.android.synthetic.main.life_index.*
import kotlinx.android.synthetic.main.now.*
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity() {
    val weatherViewModel by lazy { ViewModelProvider(this).get(WeatherViewModel::class.java) }
    val placeViewModel by lazy { ViewModelProvider(this).get(PlaceViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        // 滑动菜单
        navBtn.setOnClickListener {
            // 打开滑动菜单
            drawerLayout.openDrawer(GravityCompat.START)
        }
        // 绑定滑动菜单
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {}
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {
                // 展示历史记录
                searchPlaceEdit.text = null
                placeHistoryListLayout.removeAllViews()
                val savedPlaceHistory = placeViewModel.getSavedPlaceHistory()
                for (place in savedPlaceHistory) {
                    val view = LayoutInflater.from(this@WeatherActivity).inflate(R.layout.place_history_item, placeHistoryListLayout, false)
                    val placeHistoryInfo = view.findViewById(R.id.placeHistoryInfo) as TextView
                    val deletePlaceHistory = view.findViewById(R.id.deletePlaceHistory) as Button
                    placeHistoryInfo.text = place.name
                    // 删除按钮事件
                    deletePlaceHistory.setOnClickListener {
                        placeViewModel.deletePlaceHistory(place)
                        placeHistoryListLayout.removeView(view)
                    }
                    // 文本绑定跳转
                    placeHistoryInfo.setOnClickListener {
                        placeViewModel.savePlace(place)
                        weatherViewModel.changeLocation(place)
                        weatherViewModel.refreshWeather(place.location.lng, place.location.lat)
                        drawerLayout.closeDrawer(GravityCompat.START)
                    }
                    placeHistoryListLayout.addView(view)
                    placeHistoryListLayout.visibility = View.VISIBLE
                }
            }

            // 当滑动菜单隐藏时
            override fun onDrawerClosed(drawerView: View) {
                // 隐藏输入法
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        })

        // 取出数据
        if (weatherViewModel.locationLng.isEmpty()) {
            weatherViewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }
        if (weatherViewModel.locationLat.isEmpty()) {
            weatherViewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if (weatherViewModel.placeName.isEmpty()) {
            weatherViewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }
        // 开启观察weatherLiveData，变更后展示
        weatherViewModel.weatherLiveData.observe(this, Observer { result ->
            val weather = result.getOrNull()
            if (weather != null) {
                showWeatherInfo(weather)
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            // 得到数据后将刷新条隐藏
            swipeRefresh.isRefreshing = false
        })

        // 下拉刷新
        // 下拉刷新进度条颜色
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        refreshWeather()
        // 监听器
        swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }

    }

    /**
     * 刷新天气，调用viewModel刷新并显示下拉进度条
     */
    fun refreshWeather() {
        weatherViewModel.refreshWeather(weatherViewModel.locationLng, weatherViewModel.locationLat)
        // 显示下拉进度条
        swipeRefresh.isRefreshing = true
    }

    /**
     * 展示天气信息
     */
    private fun showWeatherInfo(weather: Weather) {
        // 填充数据
        placeName.text = weatherViewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily
        // 填充now.xml布局中的数据
        val currentTempText = "${realtime.temperature.toInt()} ℃"
        currentTemp.text = currentTempText
        currentSky.text = getSky(realtime.skycon).info
        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        currentAQI.text = currentPM25Text
        nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)
        // 填充forecast.xml布局中的数据
        forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for (i in 0 until days) {
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false)
            val dateInfo = view.findViewById(R.id.dateInfo) as TextView
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
            temperatureInfo.text = tempText
            forecastLayout.addView(view)
        }
        // 填充life_index.xml布局中的数据
        val lifeIndex = daily.lifeIndex
        coldRiskText.text = lifeIndex.coldRisk[0].desc
        dressingText.text = lifeIndex.dressing[0].desc
        ultravioletText.text = lifeIndex.ultraviolet[0].desc
        carWashingText.text = lifeIndex.carWashing[0].desc
        // 可见
        weatherLayout.visibility = View.VISIBLE
    }
}
