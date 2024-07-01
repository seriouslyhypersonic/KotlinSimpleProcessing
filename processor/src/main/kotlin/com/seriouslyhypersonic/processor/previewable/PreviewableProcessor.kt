package com.seriouslyhypersonic.processor.previewable

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate
import com.seriouslyhypersonic.annotations.Previewable
import com.seriouslyhypersonic.ktx.classesAnnotatedWith
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * [SymbolProcessor] for [PreviewableProcessor] annotations.
 */
internal class PreviewableProcessor(
    private val generator: CodeGenerator,
    @Suppress("unused") private val logger: KSPLogger,
    @Suppress("unused") private val options: Map<String, String>
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .classesAnnotatedWith(Previewable::class)
            .filter { it.validate() } // Filters out symbols deferred to other rounds

        if (!symbols.iterator().hasNext()) return emptyList()

        symbols.forEach { declaration ->
            requireViewModel(declaration)
            declaration.accept(visitor = PreviewableVisitor(generator, logger), data = Unit)
        }

        val unprocessedSymbols = symbols.filterNot { it.validate() }.toList()
        return unprocessedSymbols
    }

    private fun requireViewModel(declaration: KSClassDeclaration) {
        val viewModelName = declaration.simpleName.asString()
        val extendsSomeViewModel = declaration.superTypes.any { supertype ->
            val someViewModelDeclaration = supertype.resolve().declaration.simpleName.asString()
            someViewModelDeclaration == "Some$viewModelName"
        }
        check(extendsSomeViewModel) {
            buildString {
                appendLine("$viewModelName must extend Some$viewModelName. Please declare:")
                append("abstract class Some$viewModelName : ${viewModelName}Contract, ViewModel()")
            }
        }
    }
}

@OptIn(KspExperimental::class)
internal class PreviewableVisitor(
    private val generator: CodeGenerator,
    @Suppress("unused") private val logger: KSPLogger
) : KSVisitorVoid() {
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val generateModel = classDeclaration
            .getAnnotationsByType(Previewable::class)
            .first()
            .generateModel

        val properties = classDeclaration.getAllProperties()
            .filter(::isViewModelContractDeclaration)

        val methods = classDeclaration.getAllFunctions()
            .filter(::isViewModelContractDeclaration)
            .filterNot { it.simpleName.asString() in IgnoreMethodNames }

        fileSpecBuilderFor(classDeclaration)
            .addType(
                TypeSpec
                    .interfaceBuilder(name = "${classDeclaration.simpleName.asString()}Contract")
                    .addProperties(contractPropertySpecsFor(properties))
                    .addFunctions(contractMethodSpecsFor(methods))
                    .build()
            )
            .apply {
                if (generateModel) {
                    addType(
                        TypeSpec
                            .classBuilder(
                                name = classDeclaration.simpleName
                                    .asString()
                                    .replace("ViewModel", "PreviewViewModel")
                            )
                            .addModifiers(KModifier.DATA)
                            .superclass(
                                ClassName(
                                    packageName = classDeclaration.packageName.asString(),
                                    "Some${classDeclaration.simpleName.asString()}"
                                )
                            )
                            .primaryConstructor(
                                FunSpec
                                    .constructorBuilder()
                                    .addParameters(modelConstructorParametersFor(properties))
                                    .build()
                            )
                            .addProperties(modelPropertySpecsFor(properties))
                            .addFunctions(modelMethodSpecsFor(methods))
                            .addType(TypeSpec.companionObjectBuilder().build())
                            .build()
                    )
                }
            }
            .build()
            .writeTo(codeGenerator = generator, aggregating = true)
    }

    private fun isViewModelContractDeclaration(declaration: KSDeclaration) =
        Modifier.OVERRIDE in declaration.modifiers &&
                !declaration.modifiers.any { it == Modifier.PROTECTED }

    private fun fileSpecBuilderFor(declaration: KSClassDeclaration) = FileSpec.builder(
        packageName = declaration.packageName.asString(),
        fileName = "${declaration.simpleName.asString()}Contract"
    )

    private fun propertySpecBuilderFor(property: KSPropertyDeclaration) = PropertySpec.builder(
        name = property.simpleName.asString(),
        type = property.type.resolve().toTypeName()
    )

    private fun contractPropertySpecsFor(properties: Sequence<KSPropertyDeclaration>) = properties
        .map { propertySpecBuilderFor(it).build() }
        .toList()

    private fun modelPropertySpecsFor(properties: Sequence<KSPropertyDeclaration>) = properties
        .map { property ->
            propertySpecBuilderFor(property)
                .addModifiers(KModifier.OVERRIDE)
                .initializer(property.simpleName.asString())
                .build()
        }
        .toList()

    private fun modelConstructorParametersFor(
        properties: Sequence<KSPropertyDeclaration>
    ) = properties
        .map { property ->
            ParameterSpec
                .builder(name = property.simpleName.asString(), type = property.type.toTypeName())
                .build()
        }
        .toList()

    private fun methodSpecBuilderFor(method: KSFunctionDeclaration) = FunSpec
        .builder(name = method.simpleName.asString())
        .apply {
            method.returnType?.toTypeName()?.let { returns(it) }

            val modifiers = method.modifiers.toMutableSet()
                .apply { remove(Modifier.OVERRIDE) }
                .mapNotNull { it.toKModifier() }
            addModifiers(modifiers)
        }

    private fun contractMethodSpecsFor(methods: Sequence<KSFunctionDeclaration>) = methods
        .map { methodSpecBuilderFor(it).addModifiers(KModifier.ABSTRACT).build() }
        .toList()

    private fun modelMethodSpecsFor(methods: Sequence<KSFunctionDeclaration>) = methods
        .map { method ->
            method.returnType?.let {
                val type = it.resolve()
                check(
                    value = type.declaration is KSClassDeclaration &&
                            type.declaration.qualifiedName!!.asString() == "kotlin.Unit"
                ) {
                    "Method '${method.simpleName.asString()}' of generated model must return Unit"
                }
            }
            methodSpecBuilderFor(method)
                .addModifiers(KModifier.OVERRIDE)
                .addComment("no-op")
                .build()
        }
        .toList()

    private companion object {
        val IgnoreMethodNames =
            listOf("addCloseable", "equals", "getCloseable", "hashCode", "toString")
    }
}
