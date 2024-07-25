package com.seriouslyhypersonic.library.factory

import android.os.Handler
import android.os.HandlerThread

/**
 * Returns a [Handler] with the looper from a [HandlerThread].
 * @param name The name of the [Handler].
 */
@Suppress("FunctionName")
public fun WorkerThreadHandler(name: String? = null): Handler {
    val thread = HandlerThread(name ?: "WorkerThreadHandler").apply { start() }
    return Handler(thread.looper)
}
