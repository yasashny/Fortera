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

abstract class MviViewModel<S : UiState, I : UiIntent, E : UiEffect>(
    initialState: S,
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effect = Channel<E>(capacity = Channel.UNLIMITED)
    val effect: Flow<E> = _effect.receiveAsFlow()

    val currentState: S get() = _state.value

    protected abstract fun handleIntent(intent: I)

    fun onIntent(intent: I) {
        handleIntent(intent)
    }

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

    protected fun updateState(transform: (S) -> S) {
        _state.update(transform)
    }

    protected fun reduce(newState: S) {
        _state.value = newState
    }

    protected fun sendEffect(effect: E) {
        _effect.trySend(effect)
    }

    protected open fun handleError(throwable: Throwable) {
        Log.e(this::class.java.simpleName, "Uncaught error in intent", throwable)
    }

    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        handleError(throwable)
    }
}
