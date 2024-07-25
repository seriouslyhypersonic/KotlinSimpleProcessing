package com.seriouslyhypersonic.kspforall.demo.weather.data.forecast

interface WeatherService {
    suspend fun fetchForecast(): WeatherForecast
}