package com.seriouslyhypersonic.processor

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class CaseDetectionProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = CaseDetectionProcessor(
        generator = environment.codeGenerator,
        logger = environment.logger,
        options = environment.options
    )
}
