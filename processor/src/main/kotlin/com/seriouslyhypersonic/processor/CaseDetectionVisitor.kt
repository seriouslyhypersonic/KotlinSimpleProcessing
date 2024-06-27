package com.seriouslyhypersonic.processor

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Modifier
import com.seriouslyhypersonic.ktx.snakeToPascalCase
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.writeTo

abstract class CaseDetectionVisitor(protected val generator: CodeGenerator) : KSVisitorVoid() {
    protected fun fileSpecFor(declaration: KSClassDeclaration) = FileSpec.builder(
        packageName = declaration.packageName.asString(),
        fileName = declaration.simpleName.asString() + "CaseDetection"
    )
}

class EnumCaseDetectionVisitor(generator: CodeGenerator) : CaseDetectionVisitor(generator) {
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val entries = classDeclaration.declarations
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.ENUM_ENTRY }

        fileSpecFor(classDeclaration)
            .addProperties(propertiesFor(entries, classDeclaration))
            .build()
            .writeTo(generator, aggregating = true)
    }

    private fun propertiesFor(
        entries: Sequence<KSClassDeclaration>,
        enumClass: KSClassDeclaration
    ) = entries
        .map { entry ->
            PropertySpec
                .builder(
                    name = "is" + entry.simpleName.asString().snakeToPascalCase(),
                    type = Boolean::class,
                    modifiers = listOfNotNull(enumClass.getVisibility().toKModifier())
                )
                .addKdoc(
                    "Returns `true` if this [%T] is [%T], `false` otherwise.",
                    enumClass.toClassName(), entry.toClassName()
                )
                .receiver(enumClass.toClassName())
                .getter(
                    FunSpec
                        .getterBuilder()
                        .addCode("return this == %T", entry.toClassName())
                        .build()
                )
                .build()
        }
        .toList()
}

class SealedClassCaseDetection(generator: CodeGenerator) : CaseDetectionVisitor(generator) {
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        require(Modifier.SEALED in classDeclaration.modifiers) {
            "CaseDetection is only applicable to sealed classes or sealed interfaces"
        }

        val entries = classDeclaration.getSealedSubclasses()

        fileSpecFor(classDeclaration)
            .addProperties(propertiesFor(entries, sealedClass = classDeclaration))
            .build()
            .writeTo(generator, aggregating = true)
    }

    private fun propertiesFor(
        entries: Sequence<KSClassDeclaration>,
        sealedClass: KSClassDeclaration
    ) = entries
        .map { entry ->
            PropertySpec
                .builder(
                    name = "is" + entry.simpleName.asString(),
                    type = Boolean::class,
                    modifiers = listOfNotNull(sealedClass.getVisibility().toKModifier())
                )
                .addKdoc(
                    "Returns `true` if this [%T] is [%T], `false` otherwise.",
                    sealedClass.toClassName(), entry.toClassName()
                )
                .receiver(sealedClass.toClassName())
                .getter(
                    FunSpec
                        .getterBuilder()
                        .addCode("return this is %T", entry.toClassName())
                        .build()
                )
                .build()
        }
        .toList()
}
