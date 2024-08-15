package com.seriouslyhypersonic.processor.tools

import org.intellij.lang.annotations.Language
import java.io.File
import kotlin.test.assertEquals

/**
 * Asserts the the provided [File] contains the [expected] Kotlin source code.
 * @param file The [File] containing the Kotlin source code.
 * @param expected The expected content of the file a Kotlin source code.
 * @param trimIndent Flag indicating if indentation of the [expected] content should be trimmed.
 */
fun assertKotlinSource(
    file: File,
    @Language("kotlin") expected: String,
    trimIndent: Boolean = true
) {
    assertEquals(
        expected = expected.run { if (trimIndent) trimIndent() else this },
        actual = file.bufferedReader().use { it.readText() }
    )
}
