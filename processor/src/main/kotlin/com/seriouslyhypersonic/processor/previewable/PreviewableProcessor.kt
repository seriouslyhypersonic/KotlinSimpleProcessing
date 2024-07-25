package com.seriouslyhypersonic.processor.previewable

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.seriouslyhypersonic.annotations.Previewable
import com.seriouslyhypersonic.ktx.classesAnnotatedWith
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName

/**
 * [SymbolProcessor] for [PreviewableProcessor] annotations.
 */
internal class PreviewableProcessor(
    private val generator: CodeGenerator,
    @Suppress("unused") private val logger: KSPLogger,
    @Suppress("unused") private val options: Map<String, String>
) : SymbolProcessor {
    // We need this because KSP will run the processor a second time once the symbol is valid (i.e.
    // once the interace has been generated).
    private val visited = mutableSetOf<String>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .classesAnnotatedWith(Previewable::class)
            .filterNot { it.qualifiedName?.asString() in visited }
        // We skip the validation as the Previewable ViewModels will always be deffered because the
        // interface has not yet been generated and we need to process the class anyways.
//            .filter { it.validate() } // Filters out symbols deferred to other rounds

        if (!symbols.iterator().hasNext()) return emptyList()

        symbols
            .forEach { declaration ->
                requireContract(declaration)
                declaration.accept(visitor = PreviewableVisitor(generator, logger), data = Unit)
            }

        visited.addAll(symbols.map { it.qualifiedName?.asString().orEmpty() })

        val unprocessedSymbols = symbols.filterNot { it.validate() }.toList()
        return unprocessedSymbols
    }

    private fun requireContract(declaration: KSClassDeclaration) {
        val viewModelName = declaration.simpleName.asString()
        val extendsSomeViewModel = declaration.superTypes.any { supertype ->
            val someViewModelDeclaration = supertype.element.toString()
            someViewModelDeclaration == "Some$viewModelName"
        }
        check(extendsSomeViewModel) { "$viewModelName must extend Some$viewModelName." }
    }
}
