package com.nextersolutions.soulmetric.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nextersolutions.soulmetric.core.domain.model.Answer
import com.nextersolutions.soulmetric.core.domain.model.Question
import com.nextersolutions.soulmetric.ui.theme.BlueViolet
import com.nextersolutions.soulmetric.ui.theme.Divider
import com.nextersolutions.soulmetric.ui.theme.OnSurface
import com.nextersolutions.soulmetric.ui.theme.OnSurface40
import com.nextersolutions.soulmetric.ui.theme.OnSurface60
import com.nextersolutions.soulmetric.ui.theme.Purple100
import com.nextersolutions.soulmetric.ui.theme.Purple600
import com.nextersolutions.soulmetric.ui.theme.Purple700
import com.nextersolutions.soulmetric.ui.theme.Surface

@Composable
fun QuestionContent(
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
            VerticalSpacer(4.dp)
            Text("Optional", style = MaterialTheme.typography.labelMedium, color = OnSurface60)
        }
        VerticalSpacer(32.dp)

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
                        .background(
                            if (isSelected) Brush.linearGradient(
                                listOf(
                                    Purple700,
                                    BlueViolet
                                )
                            ) else Brush.linearGradient(listOf(Surface, Surface))
                        )
                        .border(
                            if (isSelected) 0.dp else 1.5.dp,
                            Divider,
                            RoundedCornerShape(16.dp)
                        )
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
        VerticalSpacer(12.dp)
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
                HorizontalSpacer(12.dp)
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
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 140.dp),
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
