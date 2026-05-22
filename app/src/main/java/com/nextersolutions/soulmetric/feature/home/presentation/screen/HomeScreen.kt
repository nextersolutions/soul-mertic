package com.nextersolutions.soulmetric.feature.home.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
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
import com.nextersolutions.soulmetric.core.domain.model.Survey
import com.nextersolutions.soulmetric.feature.home.presentation.viewmodel.HomeEffect
import com.nextersolutions.soulmetric.feature.home.presentation.viewmodel.HomeIntent
import com.nextersolutions.soulmetric.feature.home.presentation.viewmodel.HomeViewModel
import com.nextersolutions.soulmetric.ui.components.EmptyState
import com.nextersolutions.soulmetric.ui.components.LoadingIndicator
import com.nextersolutions.soulmetric.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSurvey: (String) -> Unit,
    onNavigateToResults: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is HomeEffect.NavigateToSurvey -> onNavigateToSurvey(effect.surveyId)
                HomeEffect.NavigateToResults -> onNavigateToResults()
                is HomeEffect.ShowSnackbar -> snackbarHost.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        containerColor = Background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                HomeHeader(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { viewModel.onIntent(HomeIntent.RefreshSurveys) },
                    onResults = { viewModel.onIntent(HomeIntent.NavigateToResults) }
                )
            }

            item { Spacer(Modifier.height(24.dp)) }
            item {
                Text(
                    text = stringResource(R.string.home_available_surveys),
                    style = MaterialTheme.typography.titleLarge,
                    color = OnSurface,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(12.dp))
            }

            if (state.isLoading) {
                item {
                    LoadingIndicator(
                        Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            } else if (state.surveys.isEmpty()) {
                item {
                    EmptyState(
                        icon = "📋",
                        title = stringResource(R.string.home_no_surveys_title),
                        subtitle = stringResource(R.string.home_no_surveys_subtitle),
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                items(state.surveys, key = { it.id }) { survey ->
                    SurveyCard(
                        survey = survey,
                        onClick = { viewModel.onIntent(HomeIntent.OpenSurvey(survey.id)) },
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onResults: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(Brush.linearGradient(listOf(Purple700, BlueViolet)))
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SoulMetric",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HeaderIconButton(onClick = onRefresh) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    HeaderIconButton(onClick = onResults) {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Understand yourself better",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun HeaderIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
        content = { content() }
    )
}

@Composable
private fun SurveyCard(survey: Survey, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Purple100),
                contentAlignment = Alignment.Center
            ) {
                Text("📊", style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = survey.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = OnSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (survey.description.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = survey.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurface60,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${survey.questions.size} questions",
                    style = MaterialTheme.typography.labelMedium,
                    color = Purple600
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = OnSurface40)
        }
    }
}
