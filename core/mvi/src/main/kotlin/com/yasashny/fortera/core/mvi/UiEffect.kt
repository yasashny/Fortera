package com.yasashny.fortera.core.mvi

/**
 * Marker for one-shot side effects.
 *
 * Use effects only for things that **cannot be modeled as state**: navigation, system calls,
 * showing a snackbar, vibration. Anything that should survive configuration changes — banners,
 * dialogs, loading flags — belongs in [UiState].
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
