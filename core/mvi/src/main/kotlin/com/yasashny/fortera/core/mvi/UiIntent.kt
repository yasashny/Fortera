package com.yasashny.fortera.core.mvi

/**
 * Marker for user intents / events.
 *
 * Every user action is represented as an intent. Intents should be value objects — data classes
 * or `data object`s of a sealed hierarchy — so equality, logging, and testing stay trivial.
 *
 * Example:
 * ```
 * sealed interface HomeIntent : UiIntent {
 *     data object LoadData : HomeIntent
 *     data class ItemClicked(val id: String) : HomeIntent
 *     data object RefreshRequested : HomeIntent
 * }
 * ```
 */
interface UiIntent
