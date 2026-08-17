package com.example.nativeminds.feature.quiz.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.example.nativeminds.designsystem.icons.NativeMindsIcons
import com.example.nativeminds.designsystem.preview.PreviewSurface
import com.example.nativeminds.designsystem.preview.ScreenThemePreviews
import com.example.nativeminds.designsystem.preview.ThemePreviews
import com.example.nativeminds.designsystem.theme.NativeMindsTheme
import com.example.nativeminds.designsystem.theme.Pill
import com.example.nativeminds.feature.quiz.R
import com.example.nativeminds.feature.quiz.ui.preview.QuizPreviewCase
import com.example.nativeminds.feature.quiz.ui.preview.QuizPreviewCases

@Composable
fun QuizScreen(
    onBack: () -> Unit,
    onPaywallRequested: (lessonId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuizViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(viewModel, lifecycle) {
        viewModel.effects.flowWithLifecycle(lifecycle).collect { effect ->
            when (effect) {
                QuizUiEffect.NavigateToPaywall -> onPaywallRequested(state.lessonId)
            }
        }
    }

    QuizScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        modifier = modifier,
    )
}

/** Stateless so it is directly usable from previews and Compose tests. */
@Composable
fun QuizScreenContent(
    state: QuizUiState,
    onIntent: (QuizIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            QuizTopBar(onClose = onBack)

            when (val content = state.content) {
                QuizContentUiState.Loading, QuizContentUiState.Locked -> QuizLoadingBody()

                QuizContentUiState.Error -> QuizErrorBody(
                    onRetry = { onIntent(QuizIntent.RetryRequested) },
                    modifier = Modifier.padding(top = NativeMindsTheme.spacing.xl),
                )

                is QuizContentUiState.Ready -> QuizQuestionBody(
                    question = content.question,
                    onOptionSelected = { onIntent(QuizIntent.OptionSelected(it)) },
                    onContinueReading = onBack,
                )
            }
        }
    }
}

@Composable
private fun QuizTopBar(onClose: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(NativeMindsTheme.sizes.readerTopBar)
            .padding(horizontal = NativeMindsTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val closeDescription = stringResource(R.string.quiz_close)
        Box(
            modifier = Modifier
                .size(NativeMindsTheme.sizes.iconButton)
                .clip(CircleShape)
                .clickable(onClick = onClose)
                .semantics { contentDescription = closeDescription },
            contentAlignment = Alignment.Center,
        ) {
            NativeMindsIcons.Close(tint = MaterialTheme.colorScheme.onBackground)
        }

        Text(
            text = stringResource(R.string.quiz_top_bar_title),
            style = NativeMindsTheme.typography.readerTopBarTitle,
            color = NativeMindsTheme.colors.textMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = NativeMindsTheme.spacing.sm),
        )

        Spacer(modifier = Modifier.size(NativeMindsTheme.sizes.iconButton))
    }
}

@Composable
private fun QuizLoadingBody() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun QuizErrorBody(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(NativeMindsTheme.spacing.screen),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.md),
    ) {
        Text(
            text = stringResource(R.string.quiz_error_title),
            style = NativeMindsTheme.typography.emptyTitle,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.quiz_error_body),
            style = NativeMindsTheme.typography.emptyBody,
            color = NativeMindsTheme.colors.textMuted,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.quiz_retry),
            style = NativeMindsTheme.typography.actionLabel,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = NativeMindsTheme.spacing.sm)
                .clip(Pill)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onRetry)
                .padding(horizontal = NativeMindsTheme.spacing.xl, vertical = NativeMindsTheme.spacing.md),
        )
    }
}

@Composable
private fun QuizQuestionBody(
    question: QuizQuestionUiModel,
    onOptionSelected: (String) -> Unit,
    onContinueReading: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = NativeMindsTheme.spacing.screen),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.xs),
            modifier = Modifier
                .padding(top = NativeMindsTheme.spacing.xs)
                .clip(Pill)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = NativeMindsTheme.spacing.md, vertical = NativeMindsTheme.spacing.xs),
        ) {
            NativeMindsIcons.Sparkle(
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                size = NativeMindsTheme.sizes.iconButton / 3,
            )
            Text(
                text = stringResource(R.string.quiz_ai_badge),
                style = NativeMindsTheme.typography.chip,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }

        Text(
            text = stringResource(R.string.quiz_progress_label),
            style = NativeMindsTheme.typography.sectionHeading,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = NativeMindsTheme.spacing.lg),
        )

        Text(
            text = question.questionText,
            style = NativeMindsTheme.typography.readerTitle.copy(fontSize = 25.sp, lineHeight = 30.sp),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = NativeMindsTheme.spacing.sm),
        )

        Text(
            text = question.storyTitle,
            style = NativeMindsTheme.typography.readerAuthor,
            color = NativeMindsTheme.colors.textMuted,
            modifier = Modifier.padding(top = NativeMindsTheme.spacing.xs, bottom = NativeMindsTheme.spacing.lg),
        )

        Column(verticalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.md)) {
            question.options.forEach { option ->
                QuizOptionRow(
                    option = option,
                    enabled = !question.revealed,
                    onClick = { onOptionSelected(option.id) },
                )
            }
        }

        if (question.revealed && question.isCorrect != null) {
            QuizExplanationCard(
                isCorrect = question.isCorrect,
                correctLetter = question.correctOptionId,
                explanation = question.explanation,
                onContinueReading = onContinueReading,
                modifier = Modifier.padding(top = NativeMindsTheme.spacing.lg),
            )
        }
    }
}

@Composable
private fun QuizOptionRow(
    option: QuizOptionUiModel,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val (background, border) = when (option.visualState) {
        QuizOptionVisualState.UNSELECTED -> MaterialTheme.colorScheme.surfaceContainerLowest to NativeMindsTheme.colors.cardBorder
        QuizOptionVisualState.CORRECT_REVEALED -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.secondary
        QuizOptionVisualState.INCORRECT_REVEALED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.error
    }
    val badgeBackground = when (option.visualState) {
        QuizOptionVisualState.UNSELECTED -> MaterialTheme.colorScheme.surfaceContainerHigh
        QuizOptionVisualState.CORRECT_REVEALED -> MaterialTheme.colorScheme.secondary
        QuizOptionVisualState.INCORRECT_REVEALED -> MaterialTheme.colorScheme.error
    }
    val badgeContent = when (option.visualState) {
        QuizOptionVisualState.UNSELECTED -> MaterialTheme.colorScheme.onSurface
        QuizOptionVisualState.CORRECT_REVEALED -> MaterialTheme.colorScheme.onSecondary
        QuizOptionVisualState.INCORRECT_REVEALED -> MaterialTheme.colorScheme.onError
    }

    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.md),
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(background)
            .border(width = 1.5.dp, color = border, shape = MaterialTheme.shapes.medium)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(NativeMindsTheme.spacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(badgeBackground),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = option.letter,
                style = NativeMindsTheme.typography.lessonMeta,
                color = badgeContent,
            )
        }
        Text(
            text = option.text,
            style = NativeMindsTheme.typography.lessonTitle,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun QuizExplanationCard(
    isCorrect: Boolean,
    correctLetter: String,
    explanation: String,
    onContinueReading: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val labelColor = if (isCorrect) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(width = 1.dp, color = NativeMindsTheme.colors.cardBorder, shape = MaterialTheme.shapes.medium)
            .padding(NativeMindsTheme.spacing.md),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.xs),
        ) {
            if (isCorrect) {
                NativeMindsIcons.Check(tint = labelColor)
            }
            Text(
                text = if (isCorrect) {
                    stringResource(R.string.quiz_correct_label)
                } else {
                    stringResource(R.string.quiz_incorrect_label, correctLetter)
                },
                style = NativeMindsTheme.typography.lessonMeta,
                color = labelColor,
            )
        }
        Text(
            text = explanation,
            style = NativeMindsTheme.typography.unlockBody,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = NativeMindsTheme.spacing.xs),
        )
        Text(
            text = stringResource(R.string.quiz_continue_reading),
            style = NativeMindsTheme.typography.actionLabel,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = NativeMindsTheme.spacing.md)
                .fillMaxWidth()
                .clip(Pill)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onContinueReading)
                .padding(vertical = NativeMindsTheme.spacing.md),
        )
    }
}

@ScreenThemePreviews
@Composable
private fun QuizScreenContentPreview(
    @PreviewParameter(QuizPreviewCases::class) case: QuizPreviewCase,
) {
    NativeMindsTheme {
        QuizScreenContent(
            state = case.state,
            onIntent = {},
            onBack = {},
        )
    }
}

@ThemePreviews
@Composable
private fun QuizTopBarPreview() {
    PreviewSurface(padding = 0.dp) {
        QuizTopBar(onClose = {})
    }
}
