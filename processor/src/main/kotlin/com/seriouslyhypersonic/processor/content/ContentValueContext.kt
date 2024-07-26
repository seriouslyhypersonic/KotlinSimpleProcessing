package com.seriouslyhypersonic.processor.content

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.seriouslyhypersonic.annotations.ContentType

/**
 * Processing context for [ContentType] annotations.
 */
internal data class ContentValueContext(
    val authority: String,
    val path: String,
    val declaration: KSClassDeclaration,
    val properties: List<KSPropertyDeclaration> = emptyList()
)
