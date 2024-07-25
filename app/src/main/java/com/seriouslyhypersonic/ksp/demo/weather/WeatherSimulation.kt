package com.seriouslyhypersonic.ksp.demo.weather

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.seriouslyhypersonic.ksp.demo.weather.data.forecast.WeatherForecast

data class WeatherSimulation(val gradient: Brush, val foreground: Color) {
    companion object {
        val ClearSkies = WeatherSimulation(
            gradient = Brush.verticalGradient(
                0f to Color(red = 109, green = 171, blue = 247),
                0.33f to Color(red = 117, green = 195, blue = 255),
                1f to Color(red = 217, green = 238, blue = 255)
            ),
            foreground = Color(red = 2, green = 44, blue = 77)
        )

        fun simulationFor(description: WeatherForecast.Description) = when (description) {
            WeatherForecast.Description.ClearSkies -> ClearSkies
        }
    }
}
