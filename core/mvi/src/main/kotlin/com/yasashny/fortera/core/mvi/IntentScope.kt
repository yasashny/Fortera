package com.yasashny.fortera.core.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * DSL receiver used inside [MviViewModel.intent] blocks.
 *
 * Provides state / effect operations on the owning ViewModel and a [launch] helper that
 * starts child coroutines on the underlying [viewModelScope][androidx.lifecycle.viewModelScope].
 *
 * Example:
 * ```
 * intent {
 *     updateState { it.copy(isLoading = true) }
 *     val result = interactor.load()
 *     launch { interactor.prefetch() }       // parallel side-task
 *     reduce(State.Loaded(result))
 *     sendEffect(Effect.ShowToast("done"))
 * }
 * ```
 */
class IntentScope<S : UiState, E : UiEffect> internal constructor(
    private val stateProvider: () -> S,
    private val stateUpdater: ((S) -> S) -> Unit,
    private val stateReducer: (S) -> Unit,
    private val effectDispatcher: (E) -> Unit,
    private val coroutineScope: CoroutineScope,
) {

    /** Snapshot of current state. Always fresh — reads from the underlying [kotlinx.coroutines.flow.StateFlow]. */
    val state: S get() = stateProvider()

    /** Apply a transform to the current state. Preferred for partial updates. */
    fun updateState(transform: (S) -> S) {
        stateUpdater(transform)
    }

    /** Replace the state wholesale. Prefer [updateState] unless you truly have a fully formed new state. */
    fun reduce(newState: S) {
        stateReducer(newState)
    }

    /** Enqueue a one-shot side effect. In-order, non-suspending. */
    fun sendEffect(effect: E) {
        effectDispatcher(effect)
    }

    /**
     * Launch a child coroutine on the owning [viewModelScope][androidx.lifecycle.viewModelScope].
     * Returns the [Job] so callers can cancel specific branches of work.
     */
    fun launch(block: suspend CoroutineScope.() -> Unit): Job =
        coroutineScope.launch(block = block)

    /** Run [block] only if current state is of type [T]. Supports suspend work inside. */
    suspend inline fun <reified T : S> withState(crossinline block: suspend (T) -> Unit) {
        (state as? T)?.let { block(it) }
    }

    /** Narrow the current state to [T], or null if the type doesn't match. */
    inline fun <reified T : S> stateAs(): T? = state as? T
}
