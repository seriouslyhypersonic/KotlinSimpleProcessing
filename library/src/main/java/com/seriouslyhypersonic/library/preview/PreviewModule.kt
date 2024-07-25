package com.seriouslyhypersonic.library.preview

import com.seriouslyhypersonic.library.AnyKClass
import com.seriouslyhypersonic.library.AnyPreviewSpec
import org.koin.core.parameter.ParametersHolder
import kotlin.reflect.KClass

//region PreviewModule
/**
 * A group of [PreviewSpec]s that can be used while previewing.
 */
@KoinPreviewDsl
public class PreviewModule internal constructor(
    previewSpecs: MutableMap<AnyKClass, AnyPreviewSpec> = mutableMapOf()
) {
    private var _specs = previewSpecs
    internal val specs: Map<AnyKClass, AnyPreviewSpec> get() = _specs

    public fun preview(specification: PreviewSpecScope.() -> AnyPreviewSpec) {
        val spec = specification(SinglePreviewSpecScope)
        _specs[spec.target] = spec
    }

    private object SinglePreviewSpecScope : PreviewSpecScope
}

/** Creates a new [PreviewModule]. */
public fun previewModule(builder: PreviewModule.() -> Unit): PreviewModule =
    PreviewModule().apply(builder)
//endregion

//region PreviewSpec builder
/**
 * A specification for a definition replacement when previewing.
 * @param I The actual type that is used at runtime.
 * @param P The type that is used while previewing.
 */
public class PreviewSpec<I : Any, P : Any>(
    /** The [KClass] of the actual type [I] that is replaced while previewing. */
    internal val target: KClass<I>,
    /** A builder for the previewing instance of type [P]. */
    internal val previewer: ParametersHolder.() -> P
)

/**
 * A scope inside which it it possible to define [PreviewSpec]s using [with].
 */
@KoinPreviewDsl
public interface PreviewSpecScope

/**
 * Creates a new [PreviewSpec] where this [KClass] represents the target that will be replaces with
 * an instance produced by the [previewer] builder while previewing.
 * @param previewer A builder for the instance used while previewing.
 */
context(PreviewSpecScope)
public inline infix fun <VM : I, reified I, P : I> KClass<VM>.with(
    noinline previewer: ParametersHolder.() -> P
): PreviewSpec<I, P> where I : Any =
    PreviewSpec(target = I::class, previewer = previewer)
//endregion
