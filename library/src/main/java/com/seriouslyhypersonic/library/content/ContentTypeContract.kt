package com.seriouslyhypersonic.library.content

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

/**
 * Contract for reading from and writing to a [ContentProvider] with a table of values of type [T].
 * Each column of the [ContentProvider] holds one the properties of type [T].
 */
public interface ContentTypeContract<T> {
    /** The [Uri] of the table of [T] values. */
    public val uri: Uri

    /** The list of colums with which [T] can be constructed. */
    public val projection: Array<String>

    /** The value [T] encoded in these [ContentValues]. */
    public val ContentValues.value: T

    /** The value [T] encoded in results of query provided by this [Cursor]. */
    public val Cursor.value: T

    /** Encodes this value [T] as [ContentValues]. */
    public fun T.toContentValues(): ContentValues

    /** Creates a [MatrixCursor] whose columns populate the properties of [T]. */
    public fun toCursor(): MatrixCursor

    /** Creates an [UriMatcher] to match agains the table publising values of [T]. */
    public fun toMatcher(): UriMatcher

    /** Adds a new row to the [ContentProvider] publishing [T]. */
    public fun MatrixCursor.addAsRow(value: T)
}

/**
 * Convenience extension that encodes a list of values of type [T] for which there is a
 * [ContentTypeContract] as an array of [ContentValues].
 */
context(ContentTypeContract<T>)
public fun <T> List<T>.toArrayOfContentValues(): Array<ContentValues> =
    map { it.toContentValues() }.toTypedArray()

/**
 * Convenience extension that new rows to the [ContentProvider] publishing [T].
 */
context(ContentTypeContract<T>)
public fun <T> MatrixCursor.addAsRows(values: List<T>) {
    values.forEach { addAsRow(it) }
}

/**
 * Inserts a row into the table of the [ContentProvider] publising values of type [T].
 */
public fun <T> ContentResolver.insert(contract: ContentTypeContract<T>, value: T) {
    insert(contract.uri, contract.run { value.toContentValues() })
}

/**
 * Inserts rows into the table of the [ContentProvider] publishing values of type [T].
 */
public fun <T> ContentResolver.bulkInsert(contract: ContentTypeContract<T>, values: List<T>) {
    bulkInsert(contract.uri, contract.run { values.toArrayOfContentValues() })
}
