package com.seriouslyhypersonic.library.kotlin

import org.koin.core.parameter.ParametersDefinition
import org.koin.core.parameter.ParametersHolder
import kotlin.reflect.KClass

@PublishedApi
internal class PreviewContext(private val specs: Map<AnyKClass, AnyPreviewSpec>) {
    constructor(modules: List<PreviewModule>) : this(specs = modules.map { it.specs }.flatten())

    fun <I : Any> resolvePreviewFor(target: KClass<I>, parameters: ParametersDefinition?): I {
        val preview = requireNotNull(specs[target]) {
            "Did not find any preview specification for ${target.simpleName}. Did you provide one?"
        }.previewer(parameters?.invoke() ?: ParametersHolder())
        @Suppress("UNCHECKED_CAST")
        return requireNotNull(preview as? I) { "Cannot cast previewer to type ${target.simpleName}" }
    }

    private companion object {
        fun <K, V> Collection<Map<K, V>>.flatten(): Map<K, V> = mutableMapOf<K, V>()
            .apply { this@flatten.forEach { map -> putAll(map) } }
    }
}
