package io.github.lumyuan.turingbox.ui.compose

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.ui.Modifier
import io.github.lumyuan.turingbox.ui.theme.TuringBoxTheme

fun ComponentActivity.setContentUI(
    parent: CompositionContext? = null,
    modifier: Modifier = Modifier.fillMaxSize(),
    content: @Composable () -> Unit
) {
    setContent(
        parent
    ) {
        TuringBoxTheme(
            modifier = modifier,
            content = content
        )
    }
}