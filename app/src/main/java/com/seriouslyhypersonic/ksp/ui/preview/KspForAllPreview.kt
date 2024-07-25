package com.seriouslyhypersonic.ksp.ui.preview

import androidx.compose.runtime.Composable
import com.seriouslyhypersonic.ksp.ui.theme.KspForAllTheme
import com.seriouslyhypersonic.library.preview.KoinPreview

@Composable
fun KspForAllPreview(content: @Composable () -> Unit) {
    KspForAllTheme {
        KoinPreview(PreviewModule().module, content = content)
    }
}
