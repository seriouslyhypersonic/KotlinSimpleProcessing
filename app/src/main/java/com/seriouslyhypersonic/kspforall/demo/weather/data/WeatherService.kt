package com.seriouslyhypersonic.kspforall.demo.weather.data

interface WeatherService {
    suspend fun fetchForecast(): WeatherForecast
}