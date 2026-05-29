package com.nextersolutions.soulmetric.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nextersolutions.soulmetric.core.domain.model.Survey
import com.nextersolutions.soulmetric.core.domain.model.SurveyResult
import com.nextersolutions.soulmetric.ui.util.SurveyIconProvider
import com.nextersolutions.soulmetric.ui.theme.Divider
import com.nextersolutions.soulmetric.ui.theme.OnSurface
import com.nextersolutions.soulmetric.ui.theme.OnSurface40
import com.nextersolutions.soulmetric.ui.theme.OnSurface60
import com.nextersolutions.soulmetric.ui.theme.Purple100
import com.nextersolutions.soulmetric.ui.theme.Purple600
import com.nextersolutions.soulmetric.ui.theme.Purple700
import com.nextersolutions.soulmetric.ui.theme.Surface
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SoulMetricCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 20.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

@Composable
fun SurveyCard(
    survey: Survey,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
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
                Text(SurveyIconProvider.iconFor(survey), style = MaterialTheme.typography.headlineSmall)
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
                    VerticalSpacer(4.dp)
                    Text(
                        text = survey.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurface60,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                VerticalSpacer(8.dp)
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

@Composable
fun ResultCard(
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
                    VerticalSpacer(4.dp)
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurface60
                    )
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = OnSurface40
                    )
                }
            }

            if (result.score != null || result.scoreDescription != null) {
                VerticalSpacer(16.dp)
                HorizontalDivider(color = Divider)
                VerticalSpacer(16.dp)

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
                        HorizontalSpacer(12.dp)
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

