package com.nextersolutions.soulmetric.feature.survey.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nextersolutions.soulmetric.R
import com.nextersolutions.soulmetric.core.domain.model.Answer
import com.nextersolutions.soulmetric.core.domain.model.Question
import com.nextersolutions.soulmetric.feature.survey.presentation.viewmodel.*
import com.nextersolutions.soulmetric.ui.components.GradientButton
import com.nextersolutions.soulmetric.ui.components.LoadingIndicator
import com.nextersolutions.soulmetric.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SurveyScreen(
    onNavigateBack: () -> Unit,
    onSurveyCompleted: () -> Unit,
    viewModel: SurveyViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

    BackHandler { if (state.canGoBack) viewModel.onIntent(SurveyIntent.PreviousQuestion) else onNavigateBack() }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                SurveyEffect.NavigateBack -> onNavigateBack()
                is SurveyEffect.NavigateToResult -> onSurveyCompleted()
                is SurveyEffect.ShowError -> snackbarHost.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHost) }, containerColor = Background) { padding ->
        if (state.isLoading) {
            LoadingIndicator(Modifier.fillMaxSize())
            return@Scaffold
        }

        val survey = state.survey ?: return@Scaffold

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(Brush.linearGradient(listOf(Purple700, BlueViolet)))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(survey.title, style = MaterialTheme.typography.titleLarge, color = Color.White, modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                    // Progress bar
                    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                "${state.currentQuestionIndex + 1} / ${state.totalQuestions}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(0.8f)
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { state.progress },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.25f)
                        )
                    }
                }
            }

            // Question area
            AnimatedContent(
                targetState = state.currentQuestionIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                    }
                },
                modifier = Modifier.weight(1f)
            ) { _ ->
                state.currentQuestion?.let { question ->
                    QuestionContent(
                        question = question,
                        currentAnswer = state.answers[question.id],
                        onAnswerScale = { id, v -> viewModel.onIntent(SurveyIntent.AnswerScale(id, v)) },
                        onAnswerChoice = { id, optId, optText -> viewModel.onIntent(SurveyIntent.AnswerChoice(id, optId, optText)) },
                        onAnswerText = { id, t -> viewModel.onIntent(SurveyIntent.AnswerText(id, t)) }
                    )
                }
            }

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.canGoBack) {
                    OutlinedButton(
                        onClick = { viewModel.onIntent(SurveyIntent.PreviousQuestion) },
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Purple700)
                    ) {
                        Text(stringResource(R.string.survey_back))
                    }
                }
                GradientButton(
                    text = if (state.isLastQuestion) stringResource(R.string.survey_submit) else stringResource(R.string.survey_next),
                    onClick = { viewModel.onIntent(SurveyIntent.NextQuestion) },
                    enabled = state.canGoNext && !state.isSubmitting,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuestionContent(
    question: Question,
    currentAnswer: Answer?,
    onAnswerScale: (String, Int) -> Unit,
    onAnswerChoice: (String, String, String) -> Unit,
    onAnswerText: (String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(
            text = question.text,
            style = MaterialTheme.typography.displayMedium,
            color = OnSurface
        )
        if (!question.required) {
            Spacer(Modifier.height(4.dp))
            Text("Optional", style = MaterialTheme.typography.labelMedium, color = OnSurface60)
        }
        Spacer(Modifier.height(32.dp))

        when (question) {
            is Question.Scale -> ScaleQuestion(
                question = question,
                selectedValue = (currentAnswer as? Answer.ScaleAnswer)?.value,
                onSelect = { onAnswerScale(question.id, it) }
            )
            is Question.Choice -> ChoiceQuestion(
                question = question,
                selectedOptionId = (currentAnswer as? Answer.ChoiceAnswer)?.selectedOptionId,
                onSelect = { optId, optText -> onAnswerChoice(question.id, optId, optText) }
            )
            is Question.TextInput -> TextQuestion(
                currentValue = (currentAnswer as? Answer.TextAnswer)?.value ?: "",
                onValueChange = { onAnswerText(question.id, it) }
            )
        }
    }
}

@Composable
private fun ScaleQuestion(
    question: Question.Scale,
    selectedValue: Int?,
    onSelect: (Int) -> Unit
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            for (i in question.min..question.max) {
                val isSelected = selectedValue == i
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) Brush.linearGradient(listOf(Purple700, BlueViolet)) else Brush.linearGradient(listOf(Surface, Surface)))
                        .border(if (isSelected) 0.dp else 1.5.dp, Divider, RoundedCornerShape(16.dp))
                        .clickable { onSelect(i) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = i.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isSelected) Color.White else OnSurface
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(question.minLabel, style = MaterialTheme.typography.bodySmall, color = OnSurface60)
            Text(question.maxLabel, style = MaterialTheme.typography.bodySmall, color = OnSurface60)
        }
    }
}

@Composable
private fun ChoiceQuestion(
    question: Question.Choice,
    selectedOptionId: String?,
    onSelect: (String, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        question.options.forEach { option ->
            val isSelected = selectedOptionId == option.id
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) Purple100 else Surface)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) Purple600 else Divider,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelect(option.id, option.text) }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = { onSelect(option.id, option.text) },
                    colors = RadioButtonDefaults.colors(selectedColor = Purple700)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = option.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) Purple700 else OnSurface
                )
            }
        }
    }
}

@Composable
private fun TextQuestion(currentValue: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = currentValue,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp),
        placeholder = { Text("Type your answer here...", color = OnSurface40) },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Purple600,
            unfocusedBorderColor = Divider,
            focusedContainerColor = Surface,
            unfocusedContainerColor = Surface
        ),
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = OnSurface),
        maxLines = 8
    )
}
