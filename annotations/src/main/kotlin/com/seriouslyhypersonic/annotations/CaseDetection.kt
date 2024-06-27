package com.seriouslyhypersonic.annotations

/**
 * The [CaseDetection] annotation is used to generate convenience extension properties that check
 * the identity of enum class cases or the identity of sealed classes / interfaces.
 *
 * For example, on enum classes such as
 * ```
 * @CaseDetection
 * enum class Direction {
 *     Up, Down, Left, Right
 * }
 * ```
 * [CaseDetection] will generate extension properties like
 * ```
 * /**
 *  * Returns `true` if this [Direction] is [Direction.Up], `false` otherwise.
 *  */
 * public val Direction.isUp: Boolean
 *   get() = this == Direction.Up
 * ```
 * whereas on seal classes / interfaces such as
 * ```
 * @CaseDetection
 * sealed class Device(val brand: String) {
 *     class Laptop(brand: String) : Device(brand)
 *     class Smartphone(brand: String) : Device(brand)
 *     class Tablet(brand: String) : Device(brand)
 * }
 * ```
 * [CaseDetection] will generate extension properties like
 * ```
 * /**
 *  * Returns `true` if this [Device] is [Device.Laptop], `false` otherwise.
 *  */
 * public val Device.isLaptop: Boolean
 *   get() = this is Device.Laptop
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class CaseDetection
