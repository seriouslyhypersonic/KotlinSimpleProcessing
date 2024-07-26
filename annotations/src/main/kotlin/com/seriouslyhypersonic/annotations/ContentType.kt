package com.seriouslyhypersonic.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class ContentType(
    val authority: String,
    val path: String,
    val code: Int = 0
)
