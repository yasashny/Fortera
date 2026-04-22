package com.yasashny.fortera.core.common

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Base class for domain interactors that offload work to a specific [dispatcher].
 *
 * [execute] rethrows [CancellationException] so the coroutine cancellation machinery
 * still works — otherwise a cancelled interactor would surface as `Result.failure(...)`
 * and the caller could keep running on a dead scope.
 */
abstract class Interactor(
    protected val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    protected suspend fun <T> execute(block: suspend () -> T): Result<T> =
        withContext(dispatcher) {
            try {
                Result.success(block())
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                Result.failure(t)
            }
        }

    protected fun <T> executeFlow(block: () -> Flow<T>): Flow<T> =
        block().flowOn(dispatcher)
}
