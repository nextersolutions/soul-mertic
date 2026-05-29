package com.nextersolutions.soulmetric.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nextersolutions.soulmetric.ui.theme.OnSurface
import com.nextersolutions.soulmetric.ui.theme.OnSurface60

@Composable
fun EmptyState(
    icon: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(icon, style = MaterialTheme.typography.displayLarge)

        VerticalSpacer(16.dp)

        Text(title, style = MaterialTheme.typography.headlineSmall, color = OnSurface)

        VerticalSpacer(8.dp)

        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurface60,
            textAlign = TextAlign.Center
        )
    }
}