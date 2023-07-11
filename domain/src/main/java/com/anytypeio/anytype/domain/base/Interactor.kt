package com.anytypeio.anytype.domain.base

import com.anytypeio.anytype.domain.base.Interactor.Status
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

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

@Deprecated(
    "Use ResultInteractor instead",
    replaceWith = ReplaceWith("ResultInteractor<P, R>")
)
abstract class ResultatInteractor<in P, out R> {
    operator fun invoke(params: P): Flow<Resultat<R>> {
        return flow {
            emit(Resultat.Loading())
            val r = execute(params)
            emit(Resultat.Success(r))
        }.catch { t ->
            emit(Resultat.Failure(t))
        }
    }

    protected abstract suspend fun execute(params: P): R
}

abstract class ResultInteractor<in P, R>(
    private val context: CoroutineContext
) {
    @Throws(Exception::class)
    fun asFlow(params: P): Flow<R> = flow { emit(doWork(params)) }.flowOn(context)

    fun stream(params: P): Flow<Resultat<R>> {
        return flow {
            emit(Resultat.Loading())
            val r = doWork(params)
            emit(Resultat.Success(r))
        }.catch { t ->
            emit(Resultat.Failure(t))
        }.flowOn(context)
    }

    suspend fun run(params: P) = doWork(params)

    /*
    * N.B. Executes on the caller's thread.
    * */
    suspend fun execute(params: P): Resultat<R> = runCatchingL { doWork(params) }

    protected abstract suspend fun doWork(params: P): R
}