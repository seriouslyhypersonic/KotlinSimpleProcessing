package com.seriouslyhypersonic.kspforall.demo.weather.data.location

import android.content.Context
import com.seriouslyhypersonic.library.content.bulkInsert
import org.koin.core.annotation.Single

@Single
class LocationProviderUpdater(private val context: Context) {
    fun update() {
        context.contentResolver.bulkInsert(LocationContract, values = Locations)
    }

    private companion object {
        val Locations = listOf(
            Location(name = "Cascais", district = "Lisbon", longitude = 123f, latitude = 123f),
            Location(name = "Lisbon", district = "Lisbon", longitude = 123f, latitude = 123f),
            Location(name = "Vilamoura", district = "Faro", longitude = 123f, latitude = 123f),
            Location(name = "Porto", district = "Porto", longitude = 123f, latitude = 123f),
            Location(name = "Aveiro", district = "Aveiro", longitude = 123f, latitude = 123f)
        )
    }
}
