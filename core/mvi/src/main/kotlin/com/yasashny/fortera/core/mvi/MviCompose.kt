package com.yasashny.fortera.core.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

/**
 * Collect UI state with lifecycle awareness.
 *
 * Example:
 * ```
 * @Composable
 * fun HomeScreen(viewModel: HomeViewModel) {
 *     val state by viewModel.collectState()
 *
 *     when (state) {
 *         is HomeState.Loading -> LoadingIndicator()
 *         is HomeState.Success -> ContentList(state.items)
 *     }
 * }
 * ```
 */
@Composable
fun <S : UiState, I : UiIntent, E : UiEffect> MviViewModel<S, I, E>.collectState() =
    state.collectAsStateWithLifecycle()

/**
 * Collect and handle side effects.
 *
 * Example:
 * ```
 * @Composable
 * fun HomeScreen(
 *     viewModel: HomeViewModel,
 *     onNavigateToDetail: (String) -> Unit
 * ) {
 *     viewModel.collectEffect { effect ->
 *         when (effect) {
 *             is HomeEffect.NavigateToDetail -> onNavigateToDetail(effect.id)
 *             is HomeEffect.ShowSnackbar -> showSnackbar(effect.message)
 *         }
 *     }
 * }
 * ```
 */
@Composable
fun <S : UiState, I : UiIntent, E : UiEffect> MviViewModel<S, I, E>.collectEffect(
    onEffect: suspend (E) -> Unit
) {
    LaunchedEffect(Unit) {
        effect.collectLatest { effect ->
            onEffect(effect)
        }
    }
}

/**
 * Collect effects from a Flow.
 */
@Composable
fun <E : UiEffect> CollectEffect(
    effectFlow: Flow<E>,
    onEffect: suspend (E) -> Unit
) {
    LaunchedEffect(Unit) {
        effectFlow.collectLatest { effect ->
            onEffect(effect)
        }
    }
}

/**
 * Container composable for MVI screen with state and effect handling.
 *
 * Example:
 * ```
 * @Composable
 * fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
 *     MviContainer(
 *         viewModel = viewModel,
 *         onEffect = { effect ->
 *             when (effect) {
 *                 is HomeEffect.NavigateToDetail -> navigateToDetail(effect.id)
 *             }
 *         }
 *     ) { state, onIntent ->
 *         HomeContent(
 *             state = state,
 *             onItemClick = { id -> onIntent(HomeIntent.ItemClicked(id)) }
 *         )
 *     }
 * }
 * ```
 */
@Composable
fun <S : UiState, I : UiIntent, E : UiEffect> MviContainer(
    viewModel: MviViewModel<S, I, E>,
    onEffect: suspend (E) -> Unit = {},
    content: @Composable (state: S, onIntent: (I) -> Unit) -> Unit
) {
    val state by viewModel.collectState()

    viewModel.collectEffect(onEffect)

    content(state, viewModel::onIntent)
}
