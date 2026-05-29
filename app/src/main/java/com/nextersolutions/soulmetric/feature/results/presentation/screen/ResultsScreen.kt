package com.nextersolutions.soulmetric.feature.results.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nextersolutions.soulmetric.R
import com.nextersolutions.soulmetric.feature.results.presentation.viewmodel.ResultsIntent
import com.nextersolutions.soulmetric.feature.results.presentation.viewmodel.ResultsViewModel
import com.nextersolutions.soulmetric.ui.components.EmptyState
import com.nextersolutions.soulmetric.ui.components.LoadingIndicator
import com.nextersolutions.soulmetric.ui.components.ResultCard
import com.nextersolutions.soulmetric.ui.theme.Background
import com.nextersolutions.soulmetric.ui.theme.BlueViolet
import com.nextersolutions.soulmetric.ui.theme.Purple700
import java.time.format.DateTimeFormatter

@Composable
fun ResultsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ResultsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy · HH:mm")

    Scaffold(containerColor = Background) { padding ->
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(Brush.linearGradient(listOf(Purple700, BlueViolet)))
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            stringResource(R.string.results_title),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                        Text(
                            "${state.results.size} ${stringResource(R.string.results_count_suffix)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(0.75f)
                        )
                    }
                }
            }

            if (state.isLoading) {
                LoadingIndicator(Modifier.fillMaxSize())
            } else if (state.results.isEmpty()) {
                EmptyState(
                    icon = "📭",
                    title = stringResource(R.string.results_empty_title),
                    subtitle = stringResource(R.string.results_empty_subtitle),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.results, key = { it.id }) { result ->
                        ResultCard(
                            result = result,
                            dateFormatter = dateFormatter,
                            onDelete = { viewModel.onIntent(ResultsIntent.Delete(result.id)) }
                        )
                    }
                }
            }
        }
    }
}
