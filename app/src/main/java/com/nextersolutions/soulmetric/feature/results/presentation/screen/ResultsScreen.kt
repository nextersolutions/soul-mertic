package com.nextersolutions.soulmetric.feature.results.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nextersolutions.soulmetric.R
import com.nextersolutions.soulmetric.core.domain.model.SurveyResult
import com.nextersolutions.soulmetric.feature.results.presentation.viewmodel.ResultsIntent
import com.nextersolutions.soulmetric.feature.results.presentation.viewmodel.ResultsViewModel
import com.nextersolutions.soulmetric.ui.components.EmptyState
import com.nextersolutions.soulmetric.ui.components.LoadingIndicator
import com.nextersolutions.soulmetric.ui.theme.*
import java.time.ZoneId
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
            // Header
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

@Composable
private fun ResultCard(
    result: SurveyResult,
    dateFormatter: DateTimeFormatter,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val date = result.completedAt.atZone(ZoneId.systemDefault()).format(dateFormatter)

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete result?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                }) { Text("Cancel") }
            }
        )
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.surveyTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = OnSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(date, style = MaterialTheme.typography.bodySmall, color = OnSurface60)
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = OnSurface40)
                }
            }

            if (result.score != null || result.scoreDescription != null) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Divider)
                Spacer(Modifier.height(16.dp))

                if (result.score != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Purple100),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                result.score.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = Purple700
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        if (result.scoreDescription != null) {
                            Text(
                                result.scoreDescription,
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurface
                            )
                        }
                    }
                } else if (result.scoreDescription != null) {
                    Text(
                        result.scoreDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurface
                    )
                }
            }
        }
    }
}
