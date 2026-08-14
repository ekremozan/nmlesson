package com.example.nativeminds.data.local

/**
 * The full seed catalog: four subjects, ten topics each, in a fixed order — Biyoloji (1-10), Tarih
 * (11-20), Coğrafya (21-30), Kimya (31-40). [DummyLessonSeed] and [DummyLessonContentSeed] both fan
 * this list out by its position, so a topic's id is entirely determined by where it sits here.
 */
internal object LessonSeedCatalog {
    val all: List<LessonSeedTopic> =
        BiologyLessons.all + HistoryLessons.all + GeographyLessons.all + ChemistryLessons.all
}
