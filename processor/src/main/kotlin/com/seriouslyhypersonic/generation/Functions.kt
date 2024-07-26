package com.seriouslyhypersonic.generation

import com.squareup.kotlinpoet.ClassName

internal object Functions {
    val CursorGet = ClassName(packageName = "com.seriouslyhypersonic.library.ktx", "get")
    val DefaultExtras = ClassName(packageName = "org.koin.androidx.compose", "defaultExtras")
    val InjectViewModel = ClassName(
        packageName = "com.seriouslyhypersonic.library.preview",
        "injectViewModel"
    )
}
