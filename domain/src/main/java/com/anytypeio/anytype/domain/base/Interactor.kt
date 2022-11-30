package com.anytypeio.anytype.domain.base

import com.anytypeio.anytype.domain.base.Interactor.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
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

abstract class ResultInteractor<in P, R>(
    private val context: CoroutineContext = Dispatchers.IO
) {
    fun asFlow(params: P): Flow<R> = flow { emit(doWork(params)) }.flowOn(context)

    fun stream(params: P): Flow<Resultat<R>> {
        return asFlow(params)
            .map {
                @Suppress("USELESS_CAST")
                Resultat.Success(run(params)) as Resultat<R>
            }
            .onStart { emit(Resultat.Loading()) }
            .catch { e -> emit(Resultat.Failure(e)) }
            .flowOn(context)
    }

    suspend fun run(params: P) = doWork(params)
    suspend fun execute(params: P): Resultat<R> = runCatchingL { doWork(params) }

    protected abstract suspend fun doWork(params: P): R
}