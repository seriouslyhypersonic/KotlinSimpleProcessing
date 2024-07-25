package com.seriouslyhypersonic.library.ktx

import android.database.Cursor

/**
 * Returns the value of the requested column as [T]. The result whether this method throws an
 * exception when the column value is `null` or the column value is not of type [T] is
 * implementation-defined.
 *
 * Supported types are: [Boolean], [Int], [Short], [Long], [String], [ByteArray]
 *
 * See [Cursor.getInt], [Cursor.getLong], [Cursor.getString], [Cursor.getBlob]
 *
 * @throws [IllegalArgumentException] if the requested column does no exist.
 * @throws [IllegalArgumentException] if type [T] is not one of the supported types.
 */
public inline operator fun <reified T> Cursor.get(columnName: String): T = when {
    T::class == Boolean::class -> getInt(getColumnIndexOrThrow(columnName)) == 1
    T::class == Int::class -> getInt(getColumnIndexOrThrow(columnName))
    T::class == Short::class -> getShort(getColumnIndexOrThrow(columnName))
    T::class == Long::class -> getLong(getColumnIndexOrThrow(columnName))
    T::class == Float::class -> getFloat(getColumnIndexOrThrow(columnName))
    T::class == String::class -> getString(getColumnIndexOrThrow(columnName))
    T::class == ByteArray::class -> getBlob(getColumnIndexOrThrow(columnName))
    else -> IllegalArgumentException(
        "Unsupported column type: ${T::class.qualifiedName} for column: $columnName"
    )
} as T
