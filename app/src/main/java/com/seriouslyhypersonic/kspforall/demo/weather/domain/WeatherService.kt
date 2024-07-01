package com.seriouslyhypersonic.kspforall.demo.weather.domain

interface WeatherService {
    suspend fun fetchForecast(): WeatherForecast
}