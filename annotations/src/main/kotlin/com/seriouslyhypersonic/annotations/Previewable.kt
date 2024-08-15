package com.seriouslyhypersonic.annotations


/**
 * The [Previewable] annotation is used to generate an interface for a declared `ViewModel` which
 * can be used to easily preview `ViewModel`s.
 *
 * Example:
 * ```
 * @Previewable
 * class MainScreenViewModel : SomeMainScreenViewModel, ViewModel() {
 *     override var title by mutableStateOf("MainScreen")
 *         private set
 *
 *     override var greeting by mutableStateOf("Good morning!")
 *         private set
 *
 *     ...
 *
 *     override updateGreeting() {
 *         // update logic
 *     }
 * }
 * ```
 * will generate a `SomeMainScreenViewModel` that exposes the properties and methods marked with
 * `override`:
 * ```
 * interface SomeMainScreenViewModel {
 *     val title: String
 *     val greeting: String
 *     fun updateGreeting()
 * }
 * ```
 * This interface can then be used to create a preview view model as a simple data class:
 * ```
 * data class MainScreenPreviewViewModel(
 *     override val title: String,
 *     override val greeting: String,
 * ) : SomeMainScreenViewModel {
 *     override update() { /* no-op */ }
 * }
 * ```
 * The preview data
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class Previewable(val generateModel: Boolean = true)
