package com.seriouslyhypersonic.generation

import com.squareup.kotlinpoet.ClassName

internal object Types {
    val Composable = ClassName(packageName = "androidx.compose.runtime", "Composable")
    val ContentTypeContract = ClassName(
        packageName = "com.seriouslyhypersonic.library.content",
        "ContentTypeContract"
    )
    val ContentProvider = ClassName(packageName = "android.content", "ContentProvider")
    val ContentValues = ClassName(packageName = "android.content", "ContentValues")
    val Cursor = ClassName(packageName = "android.database", "Cursor")
    val CreationExtras = ClassName(packageName = "androidx.lifecycle.viewmodel", "CreationExtras")
    val LocalKoinScope = ClassName(packageName = "org.koin.compose", "LocalKoinScope")
    val LocalViewModelStoreOwner = ClassName(
        packageName = "androidx.lifecycle.viewmodel.compose",
        "LocalViewModelStoreOwner"
    )
    val MatrixCursor = ClassName(packageName = "android.database", "MatrixCursor")
    val Qualifier = ClassName(packageName = "org.koin.core.qualifier", "Qualifier")
    val ParametersDefintion =
        ClassName(packageName = "org.koin.core.parameter", "ParametersDefinition")
    val Scope = ClassName(packageName = "org.koin.core.scope", "Scope")
    val Uri = ClassName(packageName = "android.net", "Uri")
    val UriMatcher = ClassName(packageName = "android.content", "UriMatcher")
    val ViewModelStoreOwner = ClassName(packageName = "androidx.lifecycle", "ViewModelStoreOwner")
}
