package com.yasashny.fortera.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class MviViewModel<S : UiState, I : UiIntent, E : UiEffect>(
    initialState: S
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    val currentState: S get() = _state.value

    abstract fun handleIntent(intent: I)

    fun onIntent(intent: I) {
        handleIntent(intent)
    }

    /**
     * DSL builder for intent handling with coroutine scope.
     * Runs on viewModelScope (Main dispatcher).
     * IO work is handled by Interactors/Repositories via their own dispatchers.
     */
    protected fun intent(block: suspend IntentScope<S, E>.() -> Unit) {
        viewModelScope.launch {
            val scope = IntentScope<S, E>(
                stateUpdater = { transform: (S) -> S -> _state.update(transform) },
                effectSender = { eff: E -> _effect.send(eff) },
                stateProvider = { _state.value },
                coroutineScope = this
            )
            scope.block()
        }
    }

    protected fun updateState(transform: (S) -> S) {
        _state.update(transform)
    }

    protected fun setState(newState: S) {
        _state.value = newState
    }

    protected fun sendEffect(effect: E) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}

class IntentScope<S : UiState, E : UiEffect>(
    private val stateUpdater: ((S) -> S) -> Unit,
    private val effectSender: suspend (E) -> Unit,
    private val stateProvider: () -> S,
    private val coroutineScope: CoroutineScope
) {
    val state: S get() = stateProvider()

    fun updateState(transform: (S) -> S) {
        stateUpdater(transform)
    }

    fun reduce(newState: S) {
        stateUpdater { newState }
    }

    suspend fun sendEffect(effect: E) {
        effectSender(effect)
    }

    /**
     * Executes [block] only if the current state is of type [T].
     * Accepts both regular and suspend lambdas.
     * Replaces the old withState (non-suspend) + withStateSuspend (suspend) pair.
     *
     * Example:
     * ```
     * withState<State.Content> { content ->
     *     reduce(content.copy(isLoading = true))
     *     val result = interactor.loadData()      // suspend call — works fine
     *     reduce(content.copy(data = result))
     * }
     * ```
     */
    suspend inline fun <reified T : S> withState(crossinline block: suspend (T) -> Unit) {
        val currentState = state
        if (currentState is T) {
            block(currentState)
        }
    }

    inline fun <reified T : S> stateAs(): T? = state as? T

    fun launch(block: suspend CoroutineScope.() -> Unit) {
        coroutineScope.launch(block = block)
    }
}
