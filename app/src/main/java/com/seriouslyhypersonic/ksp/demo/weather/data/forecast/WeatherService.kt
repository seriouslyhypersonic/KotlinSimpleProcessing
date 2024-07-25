package com.seriouslyhypersonic.ksp.demo.weather.data.forecast

interface WeatherService {
    suspend fun fetchForecast(): WeatherForecast
}
