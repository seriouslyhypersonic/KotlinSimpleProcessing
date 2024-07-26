package com.seriouslyhypersonic.processor.content

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate
import com.google.devtools.ksp.visitor.KSEmptyVisitor

internal class ContentValueVisitor : KSEmptyVisitor<ContentValueContext, ContentValueContext>() {
    override fun defaultHandler(node: KSNode, data: ContentValueContext) = data

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: ContentValueContext
    ): ContentValueContext {
        val name = classDeclaration.qualifiedName?.asString().orEmpty()

        check(classDeclaration.run { classKind == ClassKind.CLASS && Modifier.DATA in modifiers }) {
            "Only data classses may be annotated with @ContentValue: '$name' is not a data class."
        }

        val properties = classDeclaration.getDeclaredProperties()
            .filter { it.run { hasBackingField && validate() } }

        check(properties.iterator().hasNext()) {
            "Class '$name' must have at least one property with backing field"
        }

        val context = properties.fold(initial = data) { currentData, property ->
            visitPropertyDeclaration(property, currentData)
        }

        return context
    }

    override fun visitPropertyDeclaration(
        property: KSPropertyDeclaration,
        data: ContentValueContext
    ): ContentValueContext = data.copy(properties = data.properties + property)
}
