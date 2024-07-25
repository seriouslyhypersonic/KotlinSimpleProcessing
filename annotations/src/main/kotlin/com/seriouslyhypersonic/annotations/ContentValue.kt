package com.seriouslyhypersonic.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class ContentValue(
    val authorithy: String,
    val path: String,
    val code: Int = 0,
    val writable: Boolean = false
)