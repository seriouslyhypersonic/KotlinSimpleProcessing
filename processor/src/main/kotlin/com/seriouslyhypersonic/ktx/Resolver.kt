package com.seriouslyhypersonic.ktx

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import kotlin.reflect.KClass

internal fun <A : Any> Resolver.symbolsAnnotatedWith(klass: KClass<A>) =
    getSymbolsWithAnnotation(klass.qualifiedName.orEmpty())

internal fun <A : Any> Resolver.classesAnnotatedWith(klass: KClass<A>) = this
    .symbolsAnnotatedWith(klass)
    .filterIsInstance<KSClassDeclaration>()
