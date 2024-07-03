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
import com.seriouslyhypersonic.processor.previewable.InjectFunction.addInjectionFunctionImports
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
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
            .addInjectionFunctionImports()
            .addType(
                TypeSpec
                    .interfaceBuilder(InjectFunction.Type.SomeViewModel(classDeclaration))
                    .addKdoc(
                        "The view model contract associated with [%T].",
                        classDeclaration.toClassName()
                    )
                    .addProperties(contractPropertySpecsFor(properties))
                    .addFunctions(contractMethodSpecsFor(methods))
                    .build()
            )
            .addFunction(InjectFunction.create(classDeclaration))
            .apply {
                if (generateModel) {
                    addType(
                        TypeSpec
                            .classBuilder(
                                name = classDeclaration.simpleName
                                    .asString()
                                    .replace("ViewModel", "PreviewViewModel")
                            )
                            .addKdoc(
                                "The preview data class synthesized for previewing [%T].",
                                classDeclaration.toClassName()
                            )
                            .addModifiers(KModifier.DATA)
                            .addSuperinterface(InjectFunction.Type.SomeViewModel(classDeclaration))
                            .primaryConstructor(
                                FunSpec
                                    .constructorBuilder()
                                    .addParameters(modelConstructorParametersFor(properties))
                                    .build()
                            )
                            .addProperties(modelPropertySpecsFor(properties))
                            .addFunctions(modelMethodSpecsFor(methods))
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

private object InjectFunction {
    fun create(classDeclaration: KSClassDeclaration) = FunSpec
        .builder("inject${classDeclaration.simpleName.asString()}")
        .addKdoc(
            """
            Injects [%T] or the preview associated with it if the system is currently under preview 
        """.trimIndent(),
            classDeclaration.toClassName()
        )
        .addAnnotation(Annotation.Composable)
        .addParameter(Parameter.Qualifier)
        .addParameter(Parameter.ViewModelOwner)
        .addParameter(Parameter.Key)
        .addParameter(Parameter.Extras)
        .addParameter(Parameter.Scope)
        .addParameter(Parameter.Parameters)
        .addCode(Implementation.create(classDeclaration))
        .returns(Type.SomeViewModel(classDeclaration))
        .build()

    fun FileSpec.Builder.addInjectionFunctionImports() = this
        .addImport(Type.LocalVmStoreOwner.packageName, Type.LocalVmStoreOwner.simpleName)
        .addImport(Function.DefaultExtras.packageName, Function.DefaultExtras.simpleName)
        .addImport(Type.LocalKoinScope.packageName, Type.LocalKoinScope.simpleName)
        .addImport(Function.InjectViewModel.packageName, Function.InjectViewModel.simpleName)

    object Parameter {
        val Qualifier = ParameterSpec
            .builder(
                name = "qualifier",
                type = ClassName(packageName = "org.koin.core.qualifier", "Qualifier")
                    .copy(nullable = true),
            )
            .defaultValue("null")
            .build()

        val ViewModelOwner = ParameterSpec
            .builder(
                name = "viewModelStoreOwner",
                type = ClassName(packageName = "androidx.lifecycle", "ViewModelStoreOwner")
            )
            .defaultValue(
                """
                checkNotNull(LocalViewModelStoreOwner.current) {
                    "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
                }
            """.trimIndent()
            )
            .build()

        val Key = ParameterSpec
            .builder(name = "key", type = String::class.asClassName().copy(nullable = true))
            .defaultValue("null")
            .build()

        val Extras = ParameterSpec
            .builder(
                name = "extras",
                type = ClassName(packageName = "androidx.lifecycle.viewmodel", "CreationExtras")
            )
            .defaultValue("defaultExtras(viewModelStoreOwner)")
            .build()

        val Scope = ParameterSpec
            .builder(name = "scope", type = ClassName(packageName = "org.koin.core.scope", "Scope"))
            .defaultValue("LocalKoinScope.current")
            .build()

        val Parameters = ParameterSpec
            .builder(
                name = "parameters",
                type = ClassName(packageName = "org.koin.core.parameter", "ParametersDefinition")
                    .copy(nullable = true)
            )
            .defaultValue("null")
            .build()
    }

    object Type {
        val LocalVmStoreOwner = ClassName(
            packageName = "androidx.lifecycle.viewmodel.compose",
            "LocalViewModelStoreOwner"
        )

        val LocalKoinScope = ClassName(packageName = "org.koin.compose", "LocalKoinScope")

        @Suppress("FunctionName")
        fun SomeViewModel(classDeclaration: KSClassDeclaration) = ClassName(
            packageName = classDeclaration.packageName.asString(),
            "Some${classDeclaration.simpleName.asString()}"
        )
    }

    object Function {
        val DefaultExtras = ClassName(packageName = "org.koin.androidx.compose", "defaultExtras")
        val InjectViewModel = ClassName(
            packageName = "com.seriouslyhypersonic.library.kotlin",
            "injectViewModel"
        )
    }

    object Annotation {
        val Composable = ClassName(packageName = "androidx.compose.runtime", "Composable")
    }

    object Implementation {
        fun create(classDeclaration: KSClassDeclaration) = CodeBlock
            .builder()
            .add(
                """
                return injectViewModel<%T, Some${classDeclaration.simpleName.asString()}>(
                    qualifier,
                    viewModelStoreOwner,
                    key,
                    extras,
                    scope,
                    parameters
                )
            """.trimIndent(),
                classDeclaration.toClassName()
            )
            .build()
    }
}