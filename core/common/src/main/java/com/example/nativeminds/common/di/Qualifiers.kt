package com.example.nativeminds.common.di

import javax.inject.Qualifier

/**
 * `CoroutineDispatcher` and `CoroutineScope` are unqualified types that several bindings would
 * otherwise collide on, so every one of them is addressed through a qualifier. Injecting the
 * dispatcher instead of calling `Dispatchers.IO` inline is also what makes the code testable —
 * a test can bind a `TestDispatcher` and control the clock.
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IoDispatcher

/** CPU-bound work: mapping, parsing, sorting. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class DefaultDispatcher

/**
 * A `CoroutineScope` that lives as long as the process — for work that must survive the screen that
 * started it (sync, analytics flushes), which `viewModelScope` cannot do.
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope
