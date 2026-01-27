package com.yasashny.fortera.core.mvi

/**
 * Marker interface for side effects.
 * One-time events like navigation, showing snackbar, etc.
 *
 * Example:
 * ```
 * sealed interface HomeEffect : UiEffect {
 *     data class NavigateToDetail(val id: String) : HomeEffect
 *     data class ShowSnackbar(val message: String) : HomeEffect
 *     data object NavigateBack : HomeEffect
 * }
 * ```
 */
interface UiEffect
