package com.seriouslyhypersonic.ktx

fun String.snakeToPascalCase() = this
    .split("_")
    .joinToString(separator = "") { component ->
        component.lowercase().replaceFirstChar { it.uppercase() }
    }
