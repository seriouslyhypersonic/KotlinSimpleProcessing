package com.seriouslyhypersonic.processor.casedetection

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * [SymbolProcessorProvider] that provides a [CaseDetectionProcessor].
 */
public class CaseDetectionProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        CaseDetectionProcessor(
            generator = environment.codeGenerator,
            logger = environment.logger,
            options = environment.options
        )
}
