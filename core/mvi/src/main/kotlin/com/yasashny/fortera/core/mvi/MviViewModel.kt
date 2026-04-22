package com.yasashny.fortera.core.mvi

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base class for screens following MVI.
 *
 * ## Contract
 * - **State** lives in [state] (a [StateFlow]). The UI observes it; Compose treats it as
 *   [androidx.compose.runtime.Immutable].
 * - **Intents** are dispatched via [onIntent] from the UI; subclasses implement [handleIntent].
 * - **Effects** are one-shot — navigation, snackbars, system calls. Everything that should
 *   persist across configuration changes belongs in state, not effects.
 *
 * ## State mutation
 * Use [updateState] for partial transforms (the common case) and [reduce] for full replacements.
 * From inside an [intent] block, both are available on [IntentScope] with the same semantics.
 *
 * ## Asynchronous work
 * Wrap async logic in [intent]. Inside the block, [IntentScope] is a [kotlinx.coroutines.CoroutineScope],
 * so `launch { }` and `async { }` work directly. Exceptions are routed to [handleError] — override
 * to customize per-screen error handling.
 *
 * ## Effect ordering
 * Effects are delivered in-order via an unlimited channel and non-suspending [Channel.trySend].
 * Two back-to-back [sendEffect] calls are guaranteed to arrive at the collector in order.
 */
abstract class MviViewModel<S : UiState, I : UiIntent, E : UiEffect>(
    initialState: S,
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effect = Channel<E>(capacity = Channel.UNLIMITED)
    val effect: Flow<E> = _effect.receiveAsFlow()

    /** Snapshot of the current state. Cheaper than collecting [state] when you only need a read. */
    val currentState: S get() = _state.value

    /** Subclasses implement this to react to intents. Runs on the Main dispatcher. */
    protected abstract fun handleIntent(intent: I)

    /** Public entry point for UI to dispatch intents. Subclasses override [handleIntent], not this. */
    fun onIntent(intent: I) {
        handleIntent(intent)
    }

    /**
     * Launch a coroutine on [viewModelScope] with an [IntentScope] receiver.
     *
     * Inside the block:
     * - `state` / `currentState` — read the latest state
     * - `updateState { }` / `reduce(...)` — mutate state
     * - `sendEffect(...)` — dispatch a one-shot effect
     * - `launch { }` — start a child coroutine (delegated to [viewModelScope])
     *
     * Uncaught exceptions are routed to [handleError]. Returns the [Job] so callers can cancel.
     */
    protected fun intent(block: suspend IntentScope<S, E>.() -> Unit): Job =
        viewModelScope.launch(errorHandler) {
            IntentScope(
                stateProvider = { currentState },
                stateUpdater = ::updateState,
                stateReducer = ::reduce,
                effectDispatcher = ::sendEffect,
                coroutineScope = this,
            ).block()
        }

    /** Apply a transform to the current state. Preferred for partial updates. */
    protected fun updateState(transform: (S) -> S) {
        _state.update(transform)
    }

    /** Replace the state wholesale. */
    protected fun reduce(newState: S) {
        _state.value = newState
    }

    /**
     * Enqueue a one-shot side effect. Non-suspending and order-preserving.
     *
     * Uses [Channel.trySend] into an unlimited channel, so this never suspends and never drops.
     */
    protected fun sendEffect(effect: E) {
        _effect.trySend(effect)
    }

    /**
     * Hook for handling exceptions thrown inside [intent] blocks (including nested `launch { }` calls).
     * Default implementation logs and swallows. Override to navigate to an error screen, report to
     * crashlytics, or re-throw in debug builds.
     */
    protected open fun handleError(throwable: Throwable) {
        Log.e(this::class.java.simpleName, "Uncaught error in intent", throwable)
    }

    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        handleError(throwable)
    }
}
