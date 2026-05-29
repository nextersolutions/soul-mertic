package com.nextersolutions.soulmetric.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun VerticalSpacer(height: Dp) {
    Spacer(Modifier.height(height))
}

@Composable
fun HorizontalSpacer(width: Dp) {
    Spacer(Modifier.width(width))
}

@Composable
fun ColumnScope.WeightSpacer(value: Float = 1f) {
    Spacer(Modifier.weight(value))
}

@Composable
fun RowScope.WeightSpacer(value: Float = 1f) {
    Spacer(Modifier.weight(value))
}
