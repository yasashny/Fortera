package com.yasashny.fortera.core.mvi

import androidx.compose.runtime.Immutable

/**
 * Marker for UI state.
 *
 * All implementors **must be immutable** — states are compared structurally and passed
 * directly to Compose, so mutation after construction breaks recomposition correctness.
 * The [Immutable] annotation lets Compose treat them as skippable.
 *
 * Example:
 * ```
 * sealed interface HomeUiState : UiState {
 *     data object Loading : HomeUiState
 *     data class Success(val items: List<Item>) : HomeUiState
 *     data class Error(val message: String) : HomeUiState
 * }
 * ```
 */
@Immutable
interface UiState
