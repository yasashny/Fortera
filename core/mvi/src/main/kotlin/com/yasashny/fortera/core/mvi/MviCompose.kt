package com.yasashny.fortera.core.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

/**
 * Collect UI state with lifecycle awareness.
 *
 * Backed by [collectAsStateWithLifecycle] — pauses collection when the host lifecycle is stopped.
 *
 * ```
 * val state by viewModel.collectState()
 * ```
 */
@Composable
fun <S : UiState, I : UiIntent, E : UiEffect> MviViewModel<S, I, E>.collectState() =
    state.collectAsStateWithLifecycle()

/**
 * Collect and handle one-shot effects, with lifecycle awareness.
 *
 * Collection is gated by [lifecycleState] (default [Lifecycle.State.STARTED]) so effects
 * won't fire on a backgrounded screen — events queue in the underlying unlimited channel
 * and flush when the lifecycle resumes.
 *
 * Uses `collect`, not `collectLatest`: one-shot handlers must run to completion — losing a
 * navigation call because another effect arrived would be a silent bug.
 *
 * ```
 * viewModel.collectEffect { effect ->
 *     when (effect) {
 *         is HomeEffect.NavigateToDetail -> navigator.navigate(Detail(effect.id))
 *     }
 * }
 * ```
 */
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

/**
 * Collect effects from an arbitrary [Flow] with the same lifecycle semantics as [collectEffect].
 *
 * Useful when effects come from a non-ViewModel source (e.g., a shared coordinator flow).
 */
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

/**
 * Screen-level container for an MVI view model.
 *
 * Wires state collection, effect handling, and intent dispatch in one place so the screen
 * Composable is a tiny adapter between the ViewModel and the Layout.
 *
 * ```
 * @Composable
 * fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
 *     MviContainer(
 *         viewModel = viewModel,
 *         onEffect = { effect ->
 *             when (effect) {
 *                 is HomeEffect.NavigateToDetail -> navigator.navigate(Detail(effect.id))
 *             }
 *         },
 *     ) { state, onIntent -> HomeLayout(state = state, onIntent = onIntent) }
 * }
 * ```
 */
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
