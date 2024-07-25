package com.seriouslyhypersonic.annotations

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
public annotation class ColumnName(val name: String)