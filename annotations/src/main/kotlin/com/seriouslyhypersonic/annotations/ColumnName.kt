package com.seriouslyhypersonic.annotations

/**
 * Provides a custom column name associated for a [ContentType] property.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
public annotation class ColumnName(
    /** The `ContentProvider` column name that populates a [ContentType] property. */
    val name: String
)
