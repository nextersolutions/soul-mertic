package com.nextersolutions.soulmetric.feature.home.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nextersolutions.soulmetric.R
import com.nextersolutions.soulmetric.feature.home.presentation.viewmodel.HomeEffect
import com.nextersolutions.soulmetric.feature.home.presentation.viewmodel.HomeIntent
import com.nextersolutions.soulmetric.feature.home.presentation.viewmodel.HomeViewModel
import com.nextersolutions.soulmetric.ui.components.EmptyState
import com.nextersolutions.soulmetric.ui.components.LoadingIndicator
import com.nextersolutions.soulmetric.ui.components.SurveyCard
import com.nextersolutions.soulmetric.ui.components.VerticalSpacer
import com.nextersolutions.soulmetric.ui.theme.Background
import com.nextersolutions.soulmetric.ui.theme.OnSurface
import com.nextersolutions.soulmetric.ui.theme.OnSurface60
import com.nextersolutions.soulmetric.ui.theme.Purple600
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                HomeHeader(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { viewModel.onIntent(HomeIntent.RefreshSurveys) },
                    onResults = { viewModel.onIntent(HomeIntent.NavigateToResults) }
                )
            }

            item { VerticalSpacer(24.dp) }

            if (state.isSyncing) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = Purple600
                        )
                        Text(
                            text = "Updating surveys...",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurface60
                        )
                    }
                    VerticalSpacer(6.dp)
                }
            }

            item {
                Text(
                    text = stringResource(R.string.home_available_surveys),
                    style = MaterialTheme.typography.titleLarge,
                    color = OnSurface,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                VerticalSpacer(12.dp)
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
