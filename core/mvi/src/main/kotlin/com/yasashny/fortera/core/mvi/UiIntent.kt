package com.yasashny.fortera.core.mvi

/**
 * Marker interface for user intents/events.
 * All user actions should be represented as intents.
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
