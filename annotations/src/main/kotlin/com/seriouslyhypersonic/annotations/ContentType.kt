package com.seriouslyhypersonic.annotations

/**
 * The [ContentType] annotation is used to generate a contract for reading from and writing to a
 * `ContentProvider` table in a type-safe manner using the annotated type as the data model for a
 * single row of that table.
 *
 * For example, annotating the `Location` data class with [ContentType]
 * ```
 * @ContentType(authority = "com.some.authority", path = "/locations")
 * data class Location(
 *     @ColumnName("LOCATION_NAME") val name: String,
 *     @ColumnName("LOCATION_DISTRICT") val district: String,
 *     val longitude: Float,
 *     val latitude: Float
 * )
 * ```
 * will generate a `LocationContract` with which you may observe the `ContentProvider` by using one
 * of the `ContentResolver` extensions `observeValue`/`observeValueOrNull`/`observeValues`:
 * ```
 * context.contentResolver
 *     .observeValue(LocationContract)
 *     .map { /* it: Location */ }
 * ```
 *
 * The column names of the `ContentProvider` used to populate the model class are assumed to be the
 * capitalized name of each model property. If you need to provide a custom column name  for any of
 * the model properties, you may do so by annotating the applicable properties with [ColumnName].
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class ContentType(
    /** The authority of the `ContentProvider` */
    val authority: String,
    /** The path to the table containing the columns that will populate the model. */
    val path: String,
    /**
     * The code that is returned when a `URI` is matched against the given components. Must be
     * positive.
     */
    val code: Int = 0
)
