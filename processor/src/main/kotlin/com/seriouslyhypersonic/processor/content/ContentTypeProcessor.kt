package com.seriouslyhypersonic.processor.content

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.validate
import com.seriouslyhypersonic.annotations.ColumnName
import com.seriouslyhypersonic.annotations.ContentType
import com.seriouslyhypersonic.generation.Functions
import com.seriouslyhypersonic.generation.Types
import com.seriouslyhypersonic.ktx.classesAnnotatedWith
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

@OptIn(KspExperimental::class)
internal class ContentTypeProcessor(
    private val generator: CodeGenerator,
    @Suppress("unused") private val logger: KSPLogger,
    @Suppress("unused") private val options: Map<String, String>
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .classesAnnotatedWith(ContentType::class)
            .filter { it.validate() }

        if (!symbols.iterator().hasNext()) return emptyList()

        symbols.forEach { declaration ->
            val annotation = declaration.getAnnotationsByType(ContentType::class).first()
            val finalContext = declaration.accept(
                visitor = ContentValueVisitor(),
                data = annotation.run { ContentValueContext(authority, path, declaration) }
            )

            ContractBuilder(finalContext)
                .build()
                .writeTo(generator, aggregating = true)
        }

        val unprocessedSymbols = symbols.filterNot { it.validate() }.toList()
        return unprocessedSymbols
    }
}

@OptIn(KspExperimental::class)
private class ContractBuilder(private val context: ContentValueContext) {
    fun build(): FileSpec = fileSpecBuilderFor(context.declaration)
        .addImport(packageName = Functions.CursorGet.packageName, Functions.CursorGet.simpleName)
        .addType(
            TypeSpec
                .objectBuilder(name = "${context.declaration.simpleName.asString()}Contract")
                .addSuperinterface(
                    Types.ContentTypeContract.parameterizedBy(context.declaration.toClassName())
                )
                .addProperties(contractComponents(context))
                .apply { context.properties.forEach { addProperty(columnSpecFor(it)) } }
                .addProperty(uriProperty())
                .addProperty(projectionProperty(context))
                .addProperty(contentValuesExtension(context))
                .addProperty(cursorExtension(context))
                .addFunction(valueToContentValuesExtension(context))
                .addFunction(toMatcherFunction())
                .addFunction(toCursorExtension())
                .addFunction(matrixCursorExtension(context))
                .build()
        )
        .build()

    private fun fileSpecBuilderFor(declaration: KSClassDeclaration) = FileSpec.builder(
        packageName = declaration.packageName.asString(),
        fileName = declaration.simpleName.asString() + "Contract"
    )

    private fun columnSpecFor(property: KSPropertyDeclaration): PropertySpec {
        val columnName = property
            .getAnnotationsByType(ColumnName::class)
            .firstOrNull()?.name ?: property.simpleName.asString().uppercase()

        return constPropertySpec<String>(
            name = property.simpleName.asString().uppercase(),
            value = columnName
        ).addKdoc(
            "The name of the column holding the [%T.${property.simpleName.asString()}].",
            context.declaration.toClassName()
        ).build()
    }

    private fun contractComponents(context: ContentValueContext) = listOf(
        constPropertySpec(name = "AUTHORITY", value = context.authority)
            .addKdoc(
                "The authority for the [%T] publishing [%T].",
                Types.ContentProvider, context.declaration.toClassName()
            )
            .build(),
        constPropertySpec(name = "PATH", value = context.path)
            .addKdoc(
                "The path to the table of the [%T] publishing [%T].",
                Types.ContentProvider, context.declaration.toClassName()
            )
            .build(),
        constPropertySpec(name = "CODE", value = 0, format = "%L").build()
    )

    private fun uriProperty() = PropertySpec
        .builder(name = "uri", type = Types.Uri, KModifier.OVERRIDE)
        .initializer("Uri.parse(\"content://\$AUTHORITY/\$PATH\")")
        .build()

    private fun projectionProperty(context: ContentValueContext) = PropertySpec
        .builder(
            name = "projection",
            Array::class.parameterizedBy(String::class),
            KModifier.OVERRIDE
        )
        .initializer(
            "arrayOf(${context.properties.joinToString { it.simpleName.asString().uppercase() }})"
        )
        .build()

    private fun contentValuesExtension(context: ContentValueContext) = PropertySpec
        .builder(name = "value", type = context.declaration.toClassName(), KModifier.OVERRIDE)
        .receiver(Types.ContentValues)
        .getter(constructorFromGet(context, cast = true))
        .build()

    private fun cursorExtension(context: ContentValueContext) = PropertySpec
        .builder(name = "value", type = context.declaration.toClassName(), KModifier.OVERRIDE)
        .receiver(Types.Cursor)
        .getter(constructorFromGet(context, cast = false))
        .build()

    private fun constructorFromGet(context: ContentValueContext, cast: Boolean) = FunSpec
        .getterBuilder()
        .addCode(buildString {
            append("return ")
            append(context.declaration.simpleName.asString())
            append("(")
            append(constructorParametersFromGet(context, cast))
            append("⇤)")
        })
        .build()

    private fun constructorParametersFromGet(
        context: ContentValueContext,
        cast: Boolean
    ) = buildString {
        appendLine()
        append("⇥")
        context.properties.forEach { property ->
            append(property.simpleName.asString())
            append(" = ")
            append("get(")
            append(property.simpleName.asString().uppercase())
            append(")")
            if (cast) {
                append(" as ${property.type.resolve().declaration.simpleName.asString()}")
            }
            appendLine(",")
        }
    }

    private fun valueToContentValuesExtension(context: ContentValueContext) = FunSpec
        .builder(name = "toContentValues")
        .addModifiers(KModifier.OVERRIDE)
        .receiver(context.declaration.toClassName())
        .returns(Types.ContentValues)
        .addCode(buildString {
            appendLine("return ContentValues().apply {")
            append("⇥")
            context.properties.forEach { property ->
                append("put(${property.simpleName.asString().uppercase()}, ")
                appendLine("${property.simpleName.asString()})")
            }
            append("⇤}")
        })
        .build()

    private fun toMatcherFunction() = FunSpec
        .builder(name = "toMatcher")
        .addModifiers(KModifier.OVERRIDE)
        .returns(Types.UriMatcher)
        .addCode(buildString {
            appendLine("return UriMatcher(UriMatcher.NO_MATCH)")
            appendLine("⇥.apply { addURI(AUTHORITY, PATH, CODE) }⇤")
        })
        .build()

    private fun toCursorExtension() = FunSpec
        .builder(name = "toCursor")
        .addModifiers(KModifier.OVERRIDE)
        .returns(Types.MatrixCursor)
        .addCode("return MatrixCursor(projection)")
        .build()

    private fun matrixCursorExtension(context: ContentValueContext) = FunSpec
        .builder(name = "addAsRow")
        .addModifiers(KModifier.OVERRIDE)
        .receiver(Types.MatrixCursor)
        .addParameter(name = "value", type = context.declaration.toClassName())
        .addCode(buildString {
            appendLine("addRow(arrayOf(")
            append("⇥")
            context.properties.forEach { appendLine("value.${it.simpleName.asString()},") }
            append("⇤")
            appendLine("))")
        })
        .build()

    private inline fun <reified T> constPropertySpec(
        name: String,
        value: T,
        format: String = "%S"
    ) = PropertySpec
        .builder(name, type = T::class.asTypeName(), KModifier.CONST)
        .initializer(format, value)
}
