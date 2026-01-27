package com.yasashny.fortera.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

abstract class Interactor(
    protected val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    protected suspend fun <T> execute(block: suspend () -> T): Result<T> =
        withContext(dispatcher) { runCatching { block() } }

    protected fun <T> executeFlow(block: () -> Flow<T>): Flow<T> =
        block().flowOn(dispatcher)
}
