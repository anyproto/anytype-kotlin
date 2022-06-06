package com.anytypeio.anytype.domain.base

import com.anytypeio.anytype.domain.base.Interactor.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlin.Result
import kotlin.coroutines.CoroutineContext

/**
 * Interactor, whose invocation [Status] can be observed.
 * It can be used as alternative for [BaseUseCase].
 * @property context coroutine's context for this operation.
 */
abstract class Interactor<in P>(
    private val context: CoroutineContext = Dispatchers.IO
) {

    operator fun invoke(params: P): Flow<Status> {
        return flow {
            emit(Status.Started)
            withContext(context) { run(params) }
            emit(Status.Success)
        }.catch { t ->
            emit(Status.Error(t))
        }
    }

    protected abstract suspend fun run(params: P)

    /**
     * Invocation status for operation.
     */
    sealed class Status {
        /**
         * Operation is running now.
         */
        object Started : Status()

        /**
         * Operation completed successfully.
         */
        object Success : Status()

        /**
         * Operation terminated with error, represented as a [Throwable].
         */
        data class Error(val throwable: Throwable) : Status()
    }
}

abstract class ResultInteractor<in P, R> {
    operator fun invoke(params: P): Flow<R> = flow { emit(doWork(params)) }
    suspend fun run(params: P) = doWork(params)
    suspend fun execute(params: P): Result<R> = try {
        Result.success(doWork(params))
    } catch (e: Exception) {
        Result.failure(e)
    }

    protected abstract suspend fun doWork(params: P): R
}
