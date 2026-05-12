package com.yasashny.fortera.core.common

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InteractorTest {

    private class TestInteractor(dispatcher: CoroutineDispatcher) : Interactor(dispatcher) {
        suspend fun <T> runExecute(block: suspend () -> T): Result<T> = execute(block)
        fun <T> runExecuteFlow(block: () -> Flow<T>): Flow<T> = executeFlow(block)
    }

    @Test
    fun `execute returns success on normal completion`() = runTest {
        val interactor = TestInteractor(UnconfinedTestDispatcher(testScheduler))

        val result = interactor.runExecute { 42 }

        assertEquals(42, result.getOrThrow())
    }

    @Test
    fun `execute wraps thrown exception into Result_failure`() = runTest {
        val interactor = TestInteractor(UnconfinedTestDispatcher(testScheduler))
        val error = IllegalStateException("boom")

        val result = interactor.runExecute { throw error }

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }

    @Test
    fun `execute rethrows CancellationException`() = runTest {
        val interactor = TestInteractor(UnconfinedTestDispatcher(testScheduler))

        try {
            interactor.runExecute<Nothing> { throw CancellationException("stop") }
            fail("Expected CancellationException to propagate")
        } catch (ce: CancellationException) {
            assertEquals("stop", ce.message)
        }
    }

    @Test
    fun `executeFlow emits all values from the source flow`() = runTest {
        val interactor = TestInteractor(UnconfinedTestDispatcher(testScheduler))

        val collected = interactor.runExecuteFlow { flowOf(1, 2, 3) }.toList()

        assertEquals(listOf(1, 2, 3), collected)
    }

    @Test
    fun `execute completes successfully on a StandardTestDispatcher`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val interactor = TestInteractor(dispatcher)

        val result = interactor.runExecute { "ok" }

        assertEquals("ok", result.getOrThrow())
    }
}
