package com.seriouslyhypersonic.processor.previewable

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Modifier
import com.seriouslyhypersonic.generation.Functions
import com.seriouslyhypersonic.generation.Types
import com.seriouslyhypersonic.annotations.Previewable
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
                TypeSpec.interfaceBuilder(InjectFunction.Type.SomeViewModel(classDeclaration))
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
                        TypeSpec.classBuilder(
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
                                FunSpec.constructorBuilder()
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
            ParameterSpec.builder(
                name = property.simpleName.asString(),
                type = property.type.toTypeName()
            )
                .build()
        }
        .toList()

    private fun methodSpecBuilderFor(method: KSFunctionDeclaration) =
        FunSpec.builder(name = method.simpleName.asString())
            .addParameters(
                method.parameters.map { parameter ->
                    ParameterSpec.builder(
                        name = parameter.name!!.asString(),
                        type = parameter.type.toTypeName(),
                        modifiers = listOfNotNull(
                            KModifier.CROSSINLINE.takeIf { parameter.isCrossInline },
                            KModifier.NOINLINE.takeIf { parameter.isNoInline },
                            KModifier.VARARG.takeIf { parameter.isVararg }
                        )
                    ).build()
                }
            )
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
        .addAnnotation(Types.Composable)
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
        .addImport(
            Types.LocalViewModelStoreOwner.packageName,
            Types.LocalViewModelStoreOwner.simpleName
        )
        .addImport(Functions.DefaultExtras.packageName, Functions.DefaultExtras.simpleName)
        .addImport(Types.LocalKoinScope.packageName, Types.LocalKoinScope.simpleName)
        .addImport(Functions.InjectViewModel.packageName, Functions.InjectViewModel.simpleName)

    object Parameter {
        val Qualifier = ParameterSpec
            .builder(name = "qualifier", type = Types.Qualifier.copy(nullable = true))
            .defaultValue("null")
            .build()

        val ViewModelOwner = ParameterSpec
            .builder(name = "viewModelStoreOwner", type = Types.ViewModelStoreOwner)
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
            .builder(name = "extras", type = Types.CreationExtras)
            .defaultValue("defaultExtras(viewModelStoreOwner)")
            .build()

        val Scope = ParameterSpec
            .builder(name = "scope", type = Types.Scope)
            .defaultValue("LocalKoinScope.current")
            .build()

        val Parameters = ParameterSpec
            .builder(name = "parameters", type = Types.ParametersDefintion.copy(nullable = true))
            .defaultValue("null")
            .build()
    }

    object Type {
        @Suppress("FunctionName")
        fun SomeViewModel(classDeclaration: KSClassDeclaration) = ClassName(
            packageName = classDeclaration.packageName.asString(),
            "Some${classDeclaration.simpleName.asString()}"
        )
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

