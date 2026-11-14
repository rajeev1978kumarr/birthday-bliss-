package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.LetterTemplates
import com.example.data.model.QuizQuestionItem
import com.example.ui.viewmodel.CardStudioUiState
import com.example.ui.viewmodel.CardStudioViewModel

@Composable
fun QuizBuilderSection(
    state: CardStudioUiState,
    viewModel: CardStudioViewModel,
    modifier: Modifier = Modifier,
) {
    var questionText by remember { mutableStateOf("") }
    var opt1 by remember { mutableStateOf("") }
    var opt2 by remember { mutableStateOf("") }
    var opt3 by remember { mutableStateOf("") }
    var opt4 by remember { mutableStateOf("") }
    var correctIdx by remember { mutableIntStateOf(0) }
    var revealNote by remember { mutableStateOf("") }

    Column(
        modifier = modifier.padding(bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Quiz, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Loved-One Interactive Quiz",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Text(
                    text = "Add custom questions for the birthday star! When they tap an option in the card viewer, confetti pops and your secret note appears.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FilledTonalButton(
                    onClick = { viewModel.addStarterQuizPack() },
                    modifier = Modifier.testTag("add_starter_quiz_button"),
                    shape = RoundedCornerShape(50),
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add 3 Starter Questions")
                }
            }
        }

        // Custom question form
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Create Your Own Question",
                    style = MaterialTheme.typography.titleMedium,
                )
                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Question *") },
                    placeholder = { Text("e.g., Where was our favorite trip together?") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quiz_question_input"),
                )

                Text(
                    text = "Answer Options (tap circle to mark the right answer)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                val optionInputs = listOf(
                    Triple(0, "Option 1 *", opt1) to { v: String -> opt1 = v },
                    Triple(1, "Option 2 *", opt2) to { v: String -> opt2 = v },
                    Triple(2, "Option 3 (Optional)", opt3) to { v: String -> opt3 = v },
                    Triple(3, "Option 4 (Optional)", opt4) to { v: String -> opt4 = v },
                )

                optionInputs.forEach { (meta, onChange) ->
                    val (idx, label, value) = meta
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        RadioButton(
                            selected = correctIdx == idx,
                            onClick = { correctIdx = idx },
                        )
                        OutlinedTextField(
                            value = value,
                            onValueChange = onChange,
                            label = { Text(label) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quiz_option_${idx + 1}_input"),
                        )
                    }
                }

                OutlinedTextField(
                    value = revealNote,
                    onValueChange = { revealNote = it },
                    label = { Text("Sweet Reveal Note (Shown after they click)") },
                    placeholder = { Text("e.g., Haan! Best sunset memory ever 💖") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quiz_reveal_note_input"),
                )

                val rawOptions = listOf(opt1, opt2, opt3, opt4)
                val validOptions = rawOptions.map { it.trim() }.filter { it.isNotEmpty() }
                val canAdd = questionText.isNotBlank() && validOptions.size >= 2

                Button(
                    onClick = {
                        // Map selected radio index safely onto filtered list
                        val chosenText = rawOptions.getOrNull(correctIdx)?.trim()
                        val mappedIndex = validOptions.indexOfFirst { it == chosenText }.takeIf { it >= 0 } ?: 0
                        viewModel.addQuizQuestion(
                            QuizQuestionItem(
                                question = questionText.trim(),
                                options = validOptions,
                                correctIndex = mappedIndex,
                                revealNote = revealNote.trim(),
                            )
                        )
                        questionText = ""
                        opt1 = ""
                        opt2 = ""
                        opt3 = ""
                        opt4 = ""
                        revealNote = ""
                        correctIdx = 0
                    },
                    enabled = canAdd,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_quiz_question_button"),
                    shape = RoundedCornerShape(50),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Question to Quiz")
                }
            }
        }

        // Added Questions List
        Text(
            text = "Quiz Questions (${state.quizQuestions.size})",
            style = MaterialTheme.typography.titleMedium,
        )

        if (state.quizQuestions.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "No quiz questions added yet. Add your own above or tap \"Add 3 Starter Questions\"!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }
        } else {
            state.quizQuestions.forEachIndexed { index, item ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Q${index + 1}. ${item.question}",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(onClick = { viewModel.removeQuizQuestion(item.id) }) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = "Remove question",
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                        item.options.forEachIndexed { optIdx, opt ->
                            val isCorrect = optIdx == item.correctIndex
                            Text(
                                text = "${if (isCorrect) "✓" else "•"} $opt",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (item.revealNote.isNotBlank()) {
                            Text(
                                text = "Note: ${item.revealNote}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LetterComposerSection(
    state: CardStudioUiState,
    viewModel: CardStudioViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MailOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Heartfelt Birthday Letter",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Text(
                    text = "Write a long-form personal letter! When your loved one opens this surprise, it arrives sealed inside an interactive wax-seal envelope.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Text(
            text = "Starter Letter Templates",
            style = MaterialTheme.typography.labelLarge,
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(LetterTemplates.all, key = { it.id }) { template ->
                FilledTonalButton(
                    onClick = { viewModel.applyLetterTemplate(template) },
                    modifier = Modifier.testTag("letter_template_${template.id}"),
                    shape = RoundedCornerShape(50),
                ) {
                    Text(template.label)
                }
            }
        }

        OutlinedTextField(
            value = state.letterSalutation,
            onValueChange = viewModel::updateLetterSalutation,
            label = { Text("Dear / Salutation") },
            placeholder = { Text("e.g., Mere sabse khaas insaan,") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("letter_salutation_input"),
        )

        OutlinedTextField(
            value = state.letterBody,
            onValueChange = viewModel::updateLetterBody,
            label = { Text("Your Birthday Letter") },
            placeholder = { Text("Dil ki baat likhein — favorite memories, gratitude, aur aane wale saal ki duayein...") },
            minLines = 7,
            maxLines = 14,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("letter_body_input"),
        )

        OutlinedTextField(
            value = state.letterClosing,
            onValueChange = viewModel::updateLetterClosing,
            label = { Text("Warm Sign-Off") },
            placeholder = { Text("e.g., Hamesha tumhare saath,") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("letter_closing_input"),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GiftSurpriseSection(
    state: CardStudioUiState,
    viewModel: CardStudioViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Secret Gift / Treat Voucher",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Text(
                    text = "Hide a real-life treat or gift clue! Your loved one taps a shimmering golden surprise card to uncover it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Text(
            text = "Quick Gift Ideas",
            style = MaterialTheme.typography.labelLarge,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            LetterTemplates.quickGiftIdeas.forEach { idea ->
                SuggestionChip(
                    onClick = { viewModel.updateGiftSurprise(idea) },
                    label = { Text(idea) },
                )
            }
        }

        OutlinedTextField(
            value = state.giftSurprise,
            onValueChange = viewModel::updateGiftSurprise,
            label = { Text("Your Secret Gift or Treat") },
            placeholder = { Text("e.g., Look inside the top drawer / Dinner treat at your favorite spot!") },
            minLines = 3,
            maxLines = 5,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("gift_surprise_input"),
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}
