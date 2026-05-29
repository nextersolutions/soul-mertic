package com.nextersolutions.soulmetric.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.nextersolutions.soulmetric.ui.theme.BlueViolet
import com.nextersolutions.soulmetric.ui.theme.Purple700
import androidx.compose.foundation.layout.statusBarsPadding

@Composable
fun GradientHeader(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(Brush.linearGradient(listOf(Purple700, BlueViolet)))
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        content = content
    )
}
