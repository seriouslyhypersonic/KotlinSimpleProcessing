package com.seriouslyhypersonic.ksp.demo.weather.data.location

import android.content.ContentProvider
import android.content.ContentValues
import android.net.Uri
import com.seriouslyhypersonic.library.content.addAsRows

class LocationProvider : ContentProvider() {
    private var locations = emptyList<Location>()
    private val matcher = LocationContract.toMatcher()

    override fun onCreate() =  true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ) = if (matcher.match(uri) == LocationContract.CODE) {
        LocationContract.run { toCursor().apply { addAsRows(locations) } }
    } else {
        null
    }

    override fun getType(uri: Uri) = error("getType not permitted")
    override fun insert(uri: Uri, values: ContentValues?) = error("Only bulk insert is permitted")
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) =
        error("delete not permitted")

    override fun bulkInsert(uri: Uri, values: Array<out ContentValues>): Int {
        require(matcher.match(uri) == LocationContract.CODE) {
            "bulkInsert is only permitted for ${LocationContract.PATH} table"
        }
        locations = values.map { with(LocationContract) { it.value } }
        context?.contentResolver?.notifyChange(uri, null)
        return locations.size
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ) = error("update not permitted")
}
