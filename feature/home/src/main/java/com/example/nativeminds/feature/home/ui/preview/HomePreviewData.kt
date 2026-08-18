package com.example.nativeminds.feature.home.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.nativeminds.feature.home.ui.HomeUiState
import com.example.nativeminds.feature.home.ui.model.ChipUiModel
import com.example.nativeminds.feature.home.ui.model.GreetingPeriod
import com.example.nativeminds.feature.home.ui.model.LessonUiModel

/**
 * Fixtures shared by every `@Preview` in this feature.
 *
 * Kept in one place so a change to a UI model breaks one file instead of six, and so previews stay
 * static — they never reach the database or a ViewModel.
 */

internal val PreviewSubjects = listOf("Biyoloji", "Tarih", "Coğrafya", "Kimya")

internal val PreviewChips = (listOf(null) + PreviewSubjects)
    .mapIndexed { index, subject -> ChipUiModel(subject = subject, isSelected = index == 0) }

internal val PreviewSuggestions = PreviewSubjects.take(3).map { ChipUiModel(it) }

internal val PreviewLesson = LessonUiModel(
    id = 1,
    subject = "Biyoloji",
    title = "Hücre Yapısı ve Organeller",
    teaser = "Zarın içindeki küçük fabrika.",
    minutes = 6,
    hasAudio = true,
    isLocked = false,
    image = "subject_biology",
)

internal val PreviewLockedLesson = LessonUiModel(
    id = 3,
    subject = "Tarih",
    title = "İstanbul'un Fethi ve Sonuçları",
    teaser = "Bir çağın kapanıp diğerinin açılması.",
    minutes = 8,
    hasAudio = false,
    isLocked = true,
    image = "subject_history",
)

internal val PreviewLessons = listOf(
    PreviewLesson,
    LessonUiModel(2, "Kimya", "Maddenin Yapısı ve Atom Modelleri", "Her şeyin en küçük yapı taşı.", minutes = 4, hasAudio = true, isLocked = false, image = "subject_chemistry"),
    PreviewLockedLesson,
    LessonUiModel(4, "Coğrafya", "Türkiye'nin Coğrafi Konumu", "Üç kıtanın kesiştiği noktada bir ülke.", minutes = 5, hasAudio = true, isLocked = false, image = "subject_geography"),
)

/** One row of the home screen's preview matrix: a state and the lessons the pager returns for it. */
internal data class HomePreviewCase(
    val state: HomeUiState,
    val lessons: List<LessonUiModel>,
)

/** Content states of the home screen. The theme axis comes from `@ScreenThemePreviews`. */
internal class HomePreviewCases : PreviewParameterProvider<HomePreviewCase> {
    override val values = sequenceOf(
        HomePreviewCase(
            state = HomeUiState(
                greeting = GreetingPeriod.MORNING,
                userName = "Ozan",
                subjects = PreviewSubjects,
            ),
            lessons = PreviewLessons,
        ),
        HomePreviewCase(
            state = HomeUiState(
                greeting = GreetingPeriod.MORNING,
                userName = "Ozan",
                query = "quantum lullabies",
                subjects = PreviewSubjects,
            ),
            lessons = emptyList(),
        ),
        HomePreviewCase(
            state = HomeUiState(greeting = GreetingPeriod.MORNING, userName = "Ozan"),
            lessons = emptyList(),
        ),
    )
}
