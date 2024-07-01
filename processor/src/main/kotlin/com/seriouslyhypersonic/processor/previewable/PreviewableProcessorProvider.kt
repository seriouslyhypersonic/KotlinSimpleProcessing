package com.seriouslyhypersonic.processor.previewable

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * [SymbolProcessorProvider] that provides a [PreviewableProcessor].
 */
public class PreviewableProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        PreviewableProcessor(
            generator = environment.codeGenerator,
            logger = environment.logger,
            options = environment.options
        )
}
