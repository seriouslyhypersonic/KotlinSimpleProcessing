package com.seriouslyhypersonic.kspforall.ui.preview

import androidx.compose.runtime.Composable
import com.seriouslyhypersonic.kspforall.ui.theme.KspForAllTheme
import com.seriouslyhypersonic.library.kotlin.preview.KoinPreview

@Composable
fun KspForAllPreview(content: @Composable () -> Unit) {
    KspForAllTheme {
        KoinPreview(PreviewModule().module, content = content)
    }
}
