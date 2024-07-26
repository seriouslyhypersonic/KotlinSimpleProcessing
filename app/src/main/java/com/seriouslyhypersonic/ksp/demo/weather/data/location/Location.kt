package com.seriouslyhypersonic.ksp.demo.weather.data.location

import com.seriouslyhypersonic.annotations.ColumnName
import com.seriouslyhypersonic.annotations.ContentType

@ContentType(authority = "com.seriouslyhypersonic.ksp.provider", path = "/locations")
data class Location(
    @ColumnName("LOCATION_NAME") val name: String,
    @ColumnName("LOCATION_DISTRICT") val district: String,
    val longitude: Float,
    val latitude: Float
)
