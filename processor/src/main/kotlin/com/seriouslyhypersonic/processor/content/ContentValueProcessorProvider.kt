package com.seriouslyhypersonic.processor.content

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * [SymbolProcessorProvider] that provides a [ContentValueProcessor].
 */
public class ContentValueProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        ContentValueProcessor(
            generator = environment.codeGenerator,
            logger = environment.logger,
            options = environment.options
        )
}
