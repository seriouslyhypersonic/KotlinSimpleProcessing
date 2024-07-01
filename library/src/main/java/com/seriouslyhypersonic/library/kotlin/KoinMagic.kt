package com.seriouslyhypersonic.library.kotlin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import org.koin.androidx.compose.defaultExtras
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.LocalKoinScope
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.core.scope.Scope

// Welcome to the Koin magic corner.

/**
 * Magically injects a [ViewModel] instance from Koin. Unlike [koinViewModel] this injector is
 * magical because injected instance may be replaced for other instances appropriate for previewing.
 * To provide replacements define one or more [PreviewModule]s and use [KoinPreview] in previews.
 */
@Composable
public inline fun <reified VM : ViewModel> injectViewModel(
    qualifier: Qualifier? = null,
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    },
    key: String? = null,
    extras: CreationExtras = defaultExtras(viewModelStoreOwner),
    scope: Scope? = null,
    noinline parameters: ParametersDefinition? = null,
): VM = if (LocalInspectionMode.current) {
    LocalPreviewContext.current.resolvePreviewFor(target = VM::class, parameters)
} else {
    koinViewModel(
        qualifier,
        viewModelStoreOwner,
        key,
        extras,
        scope = scope ?: LocalKoinScope.current,
        parameters
    )
}

@PublishedApi
internal val LocalPreviewContext: ProvidableCompositionLocal<PreviewContext> =
    compositionLocalOf { PreviewContext(specs = emptyMap()) }

/**
 * Replaces all magically injected instances with the replacements defined in the [PreviewSpec]s of
 * each of the provided [modules].
 * @param modules The [PreviewModule] defining the [PreviewSpec]s used for replacement.
 * @param content The [Composable] content where magically injected instances will be replaced.
 */
@Composable
public fun KoinPreview(vararg modules: PreviewModule, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalPreviewContext provides PreviewContext(modules.toList()),
        content = content
    )
}
