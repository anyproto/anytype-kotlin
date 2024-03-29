package com.anytypeio.anytype.domain.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

@Deprecated(
    "Consider replace with the more useful class",
    replaceWith = ReplaceWith(
        expression = "ResultInteractor<Params, Type>",
        imports = arrayOf("com.anytypeio.anytype.domain.base")
    )
)
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

    open suspend operator fun invoke(params: Params): Either<Throwable, Type> {
        return withContext(context) { run(params) }
    }

    /**
     * Wraps use-case code to [Either]
     * @param block code block to execute
     */
    inline fun <Type> safe(
        block: () -> Type
    ): Either<Throwable, Type> = try {
        Either.Right(block())
    } catch (t: Throwable) {
        Either.Left(t)
    }

    object None
}