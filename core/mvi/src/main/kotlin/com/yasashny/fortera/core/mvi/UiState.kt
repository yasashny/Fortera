package com.yasashny.fortera.core.mvi

/**
 * Marker interface for UI state.
 * All screen states should implement this interface.
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
interface UiState
