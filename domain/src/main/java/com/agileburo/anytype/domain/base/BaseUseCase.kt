package com.agileburo.anytype.domain.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class BaseUseCase<out Type, in Params>(
    private val context: CoroutineContext = Dispatchers.IO
) where Type : Any {

    abstract suspend fun run(params: Params): Either<Throwable, Type>

    open operator fun invoke(
        scope: CoroutineScope,
        params: Params,
        onResult: (Either<Throwable, Type>) -> Unit = {}
    ) {
        val job = scope.async(context) { run(params) }
        scope.launch { onResult(job.await()) }
    }

    object None
}