package com.yasashny.fortera.core.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

@Composable
fun <S : UiState, I : UiIntent, E : UiEffect> MviViewModel<S, I, E>.collectState() =
    state.collectAsStateWithLifecycle()

@Composable
fun <S : UiState, I : UiIntent, E : UiEffect> MviViewModel<S, I, E>.collectEffect(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    onEffect: suspend (E) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(this, lifecycleOwner, lifecycleState) {
        lifecycleOwner.repeatOnLifecycle(lifecycleState) {
            effect.collect(onEffect)
        }
    }
}

@Composable
fun <E : UiEffect> CollectEffect(
    effectFlow: Flow<E>,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    onEffect: suspend (E) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(effectFlow, lifecycleOwner, lifecycleState) {
        lifecycleOwner.repeatOnLifecycle(lifecycleState) {
            effectFlow.collect(onEffect)
        }
    }
}

@Composable
fun <S : UiState, I : UiIntent, E : UiEffect> MviContainer(
    viewModel: MviViewModel<S, I, E>,
    onEffect: suspend (E) -> Unit = {},
    content: @Composable (state: S, onIntent: (I) -> Unit) -> Unit,
) {
    val state by viewModel.collectState()
    viewModel.collectEffect(onEffect = onEffect)
    content(state, viewModel::onIntent)
}
