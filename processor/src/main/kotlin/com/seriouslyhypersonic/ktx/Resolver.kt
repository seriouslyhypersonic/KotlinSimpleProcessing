package com.seriouslyhypersonic.ktx

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import kotlin.reflect.KClass

fun <A : Any> Resolver.symbolsAnnotatedWith(klass: KClass<A>) =
    getSymbolsWithAnnotation(klass.qualifiedName.orEmpty())

fun <A : Any> Resolver.classesAnnotatedWith(klass: KClass<A>) = this
    .symbolsAnnotatedWith(klass)
    .filterIsInstance<KSClassDeclaration>()
