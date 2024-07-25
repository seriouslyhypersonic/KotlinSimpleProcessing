package com.seriouslyhypersonic.processor.casedetection

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.validate
import com.seriouslyhypersonic.annotations.CaseDetection
import com.seriouslyhypersonic.ktx.classesAnnotatedWith

/**
 * [SymbolProcessor] for [CaseDetection] annotations.
 */
internal class CaseDetectionProcessor(
    private val generator: CodeGenerator,
    @Suppress("unused") private val logger: KSPLogger,
    @Suppress("unused") private val options: Map<String, String>
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .classesAnnotatedWith(CaseDetection::class)
            .filter { it.validate() } // Filters out symbols deferred to other rounds

        if (!symbols.iterator().hasNext()) return emptyList()

        symbols.forEach { declaration ->
            when (declaration.classKind) {
                ClassKind.ENUM_CLASS ->
                    declaration.accept(EnumCaseDetectionVisitor(generator), Unit)

                ClassKind.INTERFACE, ClassKind.CLASS ->
                    declaration.accept(SealedClassCaseDetectionVisitor(generator), Unit)

                else -> error("CaseDetection is only applicable to enums, classes or interfaces")
            }
        }

        val unprocessedSymbols = symbols.filterNot { it.validate() }.toList()
        return unprocessedSymbols
    }
}

