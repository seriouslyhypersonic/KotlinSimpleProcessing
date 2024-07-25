package com.seriouslyhypersonic.library.kotlin.content

import android.content.ContentResolver
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

public interface ContentProviderContract<V> {
    public val uri: Uri
    public val projection: Array<String>
    public val ContentValues.value: V
    public val Cursor.value: V
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

public fun <V> ContentResolver.insert(contract: ContentProviderContract<V>, value: V) {
    insert(contract.uri, contract.run { value.toContentValues() })
}

public fun <V> ContentResolver.bulkInsert(contract: ContentProviderContract<V>, values: List<V>) {
    bulkInsert(contract.uri, contract.run { values.toArrayOfContentValues() })
}
