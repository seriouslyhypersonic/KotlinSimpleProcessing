package com.seriouslyhypersonic.kspforall.demo.weather.data.location

import android.content.ContentValues
import android.content.UriMatcher
import android.database.MatrixCursor
import android.net.Uri
import com.seriouslyhypersonic.annotations.ColumnName
import com.seriouslyhypersonic.annotations.ContentObject
import com.seriouslyhypersonic.library.kotlin.content.ContentProviderContract

@ContentObject(authorithy = "com.seriouslyhypersonic.kspforall.provider", path = "/locations")
data class Location(
    @ColumnName("LOCATION_NAME") val name: String,
    @ColumnName("LOCATION_DISTRICT") val district: String,
    val longitude: Float,
    val latitude: Float
)

object LocationContract : ContentProviderContract<Location> {
    const val AUTHORITY = "com.seriouslyhypersonic.kspforall.provider"
    const val PATH = "/locations"
    const val CODE = 0
    const val NAME = "NAME"
    const val DISTRICT = "DISTRICT"
    const val LONGITUDE = "LONGITUDE"
    const val LATITUDE = "LATITUDE"
    val Uri: Uri = android.net.Uri.parse("content://$AUTHORITY/$PATH")

    override val ContentValues.value
        get() = Location(
            name = get(NAME) as String,
            district = get(DISTRICT) as String,
            longitude = get(LONGITUDE) as Float,
            latitude = get(LATITUDE) as Float
        )

    override fun Location.toContentValues() = ContentValues().apply {
        put(NAME, name)
        put(DISTRICT, district)
        put(LONGITUDE, longitude)
        put(LATITUDE, latitude)
    }

    override fun toMatcher() = UriMatcher(UriMatcher.NO_MATCH)
        .apply { addURI(AUTHORITY, PATH, CODE) }

    override fun toCursor() = MatrixCursor(arrayOf(NAME, DISTRICT, LONGITUDE, LATITUDE))

    override fun MatrixCursor.addAsRow(value: Location) {
        addRow(arrayOf(value.name, value.district, value.longitude, value.latitude))
    }
}

