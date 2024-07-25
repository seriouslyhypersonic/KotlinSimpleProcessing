package com.seriouslyhypersonic.library.content

import android.database.Cursor
import androidx.annotation.WorkerThread

/**
 * A database query that is only performed when [invoke]d.
 */
public fun interface DatabaseQuery {
    /**
     * Executes the query to the underlying database and returns a [Cursor] providing random
     * read-write access to the result set.
     *
     * @return The [Cursor] with which the result set returned by this [DatabaseQuery] can be
     * accessed or `null` if there is a problem with the underlying store or if it crashed.
     * __Important__: Callers are responsible to explicitly close the cursor.
     */
    @WorkerThread
    public operator fun invoke(): Cursor?
}
