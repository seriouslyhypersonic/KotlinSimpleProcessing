package com.seriouslyhypersonic.library.content

import android.content.ContentProvider
import android.content.ContentResolver
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import com.seriouslyhypersonic.library.factory.WorkerThreadHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.withContext

/**
 * Returns a [Flow] of a [DatabaseQuery] everytime the content of the table identified by the
 * provided [uri] changes and with which it is possible to access the result set retrieved by the
 * underlying [ContentProvider].
 *
 * Upon collection subscribers will immediately receive a notification for the initial data while
 * subsequent emissions are triggered by a [ContentObserver]. When subscription finishes, the
 * underlying [ContentObserver] is automatically unregistered.
 *
 * __Important__: this flow emits a [DatabaseQuery] which must be explicitly invoked to perform the
 * actually database query. Therefore, each emission does __not__ immediately query the underlying
 * [ContentProvider].
 *
 * __Original author__: Hugo Fernandes [cuub](https://github.com/cuub)
 *
 * @param uri The URI, using the content:// scheme, for the content to retrieve.
 * @param projection A list of which columns to return. Passing `null` will return all columns,
 * which is inefficient.
 * @param selection A filter declaring which rows to return, formatted as an SQL WHERE clause
 * (excluding the WHERE itself). Passing `null` will return all rows for the given URI.
 * @param selectionArgs You may include ?s in selection, which will be replaced by the values from
 * selectionArgs, in the order that they appear in the selection. The values will be bound as
 * Strings.
 * @param sortOrder How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER
 * BY itself). Passing `null` will use the default sort order, which may be unordered.
 */
public fun ContentResolver.observe(
    uri: Uri,
    projection: Array<String>? = null,
    selection: String? = null,
    selectionArgs: Array<String>? = null,
    sortOrder: String? = null,
    notifyForDescendants: Boolean = false,
    handler: Handler = WorkerThreadHandler(name = "Observe handler for $uri")
): Flow<DatabaseQuery> = callbackFlow {
    val query = DatabaseQuery { query(uri, projection, selection, selectionArgs, sortOrder) }
    val observer = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            trySend(query)
        }
    }
    if (resolves(uri)) {
        send(query)
        registerContentObserver(uri, notifyForDescendants, observer)
    } else {
        close(ContentProviderNotFound(uri))
    }
    awaitClose { unregisterContentObserver(observer) }
}

public fun <V> ContentResolver.observeValueOrNull(
    contract: ContentProviderContract<V>,
    selection: String? = null,
    selectionArgs: Array<String>? = null,
    sortOrder: String? = null,
    notifyForDescendants: Boolean = false,
    handler: Handler = WorkerThreadHandler(name = "Observe handler for ${contract.uri}"),
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
): Flow<V?> = observe(
    uri = contract.uri,
    projection = contract.projection,
    selection, selectionArgs, sortOrder, notifyForDescendants, handler
).mapRowOrNull(dispatcher) { contract.run { it.value } }

public fun <V> ContentResolver.observeValue(
    contract: ContentProviderContract<V>,
    selection: String? = null,
    selectionArgs: Array<String>? = null,
    sortOrder: String? = null,
    notifyForDescendants: Boolean = false,
    handler: Handler = WorkerThreadHandler(name = "Observe handler for ${contract.uri}"),
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
): Flow<V> = observeValueOrNull(
    contract,
    selection,
    selectionArgs,
    sortOrder,
    notifyForDescendants,
    handler,
    dispatcher
).filterNotNull()

public fun <V> ContentResolver.observeValues(
    contract: ContentProviderContract<V>,
    selection: String? = null,
    selectionArgs: Array<String>? = null,
    sortOrder: String? = null,
    notifyForDescendants: Boolean = false,
    handler: Handler = WorkerThreadHandler(name = "Observe handler for ${contract.uri}"),
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
): Flow<List<V>> = observe(
    uri = contract.uri,
    projection = contract.projection,
    selection, selectionArgs, sortOrder, notifyForDescendants, handler
).mapRows(dispatcher) { contract.run { it.value } }

/**
 * Transforms a result set with a single row into a value of type [T] by applying the
 * [transform]ation to the [Cursor] returned as the result of performing the [DatabaseQuery] emitted
 * upstream.
 *
 * __Important__: it is an error to use this operator if the result set contains more than one row.
 * Use `LIMIT 1` on the underlying SQL query to prevent this.
 *
 * __Note__: this operator actually performs the [DatabaseQuery] emitted upstream before and ignores
 * empty result sets and any queries that results in a `null` [Cursor].
 *
 * __Original author__: Hugo Fernandes [cuub](https://github.com/cuub)
 *
 * @param dispatcher The [CoroutineDispatcher] used to perform the [transform]ation.
 * @param transform The transformation that maps [Cursor] to the value of type [T].
 */
public fun <T : Any> Flow<DatabaseQuery>.mapRow(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    transform: suspend (Cursor) -> T
): Flow<T> = mapRowOrNull(dispatcher, transform).filterNotNull()

/**
 * Tries to transform a result set with a single row into a value of type [T] by applying the
 * [transform]ation to the [Cursor] returned as the result of performing the [DatabaseQuery] emitted
 * upstream. If the [transform]ation fails (i.e. returns `null`), then `null` is emitted.
 *
 * __Important__: it is an error to use this operator if the result set contains more than one row.
 * Use `LIMIT 1` on the underlying SQL query to prevent this. If the result set is empty, then
 * `null` is  emitted.
 *
 * __Note__: this operator actually performs the [DatabaseQuery] emitted upstream and ignores any
 * queries that results in a `null` [Cursor].
 *
 * __Original author__: Hugo Fernandes [cuub](https://github.com/cuub)
 *
 * @param dispatcher The [CoroutineDispatcher] used to perform the [transform]ation.
 * @param transform The transformation that maps [Cursor] to the value of type [T].
 */
public fun <T : Any> Flow<DatabaseQuery>.mapRowOrNull(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    transform: suspend (Cursor) -> T?
): Flow<T?> = transform { query ->
    query()?.use { cursor ->
        val item = withContext(dispatcher) {
            if (cursor.moveToNext()) {
                val transformed = transform(cursor)
                check(!cursor.moveToNext()) {
                    "Mapping a single row but cursor returned more than one"
                }
                transformed
            } else {
                null
            }
        }
        emit(item)
    }
}

/**
 * Transforms a result set with multiple rows into a list of values of type [T] by applying the
 * [transform]ation to the [Cursor] returned as the result of performing the [DatabaseQuery] emitted
 * upstream.
 *
 * __Important__: Be careful using this operator as it will always consume the entire cursor and
 * create objects for each row, every time the upstream flow emits a new [DatabaseQuery]. On tables
 * whose queries update frequently or very large result sets, this can result in the creation of
 * many objects.
 *
 * __Note__: this operator actually performs the [DatabaseQuery] emitted upstream and ignores empty
 * result sets and any [DatabaseQuery] that results in a `null` [Cursor].
 *
 * __Original author__: Hugo Fernandes [cuub](https://github.com/cuub)
 *
 * @param dispatcher The [CoroutineDispatcher] used to perform the [transform]ation.
 * @param transform The transformation that maps [Cursor] to the value of type [T].
 */
public fun <T> Flow<DatabaseQuery>.mapRows(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    transform: suspend (Cursor) -> T
): Flow<List<T>> = transform { query ->
    query()?.use { cursor ->
        val items: List<T>
        withContext(dispatcher) {
            items = buildList(capacity = cursor.count) {
                while (cursor.moveToNext()) {
                    add(transform(cursor))
                }
            }
        }
        emit(items)
    }
}

private fun ContentResolver.resolves(uri: Uri) =
    acquireContentProviderClient(uri)?.use { true } ?: false
