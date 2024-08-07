package com.seriouslyhypersonic.ksp.demo.weather.data.forecast

data class WeatherForecast(
    val location: String,
    val temperature: Temperature,
    val description: Description
) {
    data class Temperature(val current: Int, val perceived: Int, val max: Int, val min: Int)
    enum class Description(val message: String) {
        ClearSkies(message = "Clear skies")
    }
}
