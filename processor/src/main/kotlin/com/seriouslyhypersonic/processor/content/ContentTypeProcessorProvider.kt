package com.seriouslyhypersonic.processor.content

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * [SymbolProcessorProvider] that provides a [ContentTypeProcessor].
 */
@AutoService(SymbolProcessorProvider::class)
public class ContentTypeProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        ContentTypeProcessor(
            generator = environment.codeGenerator,
            logger = environment.logger,
            options = environment.options
        )
}
