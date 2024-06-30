package com.seriouslyhypersonic.kspforall.weather.domain

interface WeatherService {
    suspend fun fetchForecast(): WeatherForecast
}