package com.seriouslyhypersonic.ktx

internal fun String.snakeToPascalCase() = this
    .split("_")
    .joinToString(separator = "") { component ->
        component.lowercase().replaceFirstChar { it.uppercase() }
    }
