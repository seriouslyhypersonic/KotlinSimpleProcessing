package com.seriouslyhypersonic.library.kotlin.content

import android.content.ContentValues
import android.content.UriMatcher
import android.database.MatrixCursor

public interface ContentProviderContract<V> {
    public val ContentValues.value: V
    public fun V.toContentValues(): ContentValues
    public fun toCursor(): MatrixCursor
    public fun toMatcher(): UriMatcher
    public fun MatrixCursor.addAsRow(value: V)
}

context(ContentProviderContract<V>)
public fun <V> List<V>.toArrayOfContentValues(): Array<ContentValues> =
    map { it.toContentValues() }.toTypedArray()

context(ContentProviderContract<V>)
public fun <V> MatrixCursor.addAsRows(values: List<V>) {
    values.forEach { addAsRow(it) }
}
