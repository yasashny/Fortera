package com.yasashny.fortera.core.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class IntentScope<S : UiState, E : UiEffect> internal constructor(
    private val stateProvider: () -> S,
    private val stateUpdater: ((S) -> S) -> Unit,
    private val stateReducer: (S) -> Unit,
    private val effectDispatcher: (E) -> Unit,
    private val coroutineScope: CoroutineScope,
) {

    val state: S get() = stateProvider()

    fun updateState(transform: (S) -> S) {
        stateUpdater(transform)
    }

    fun reduce(newState: S) {
        stateReducer(newState)
    }

    fun sendEffect(effect: E) {
        effectDispatcher(effect)
    }

    fun launch(block: suspend CoroutineScope.() -> Unit): Job =
        coroutineScope.launch(block = block)

    suspend inline fun <reified T : S> withState(crossinline block: suspend (T) -> Unit) {
        (state as? T)?.let { block(it) }
    }

    inline fun <reified T : S> stateAs(): T? = state as? T
}
