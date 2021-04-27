package com.anytypeio.anytype.domain.base

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * Use-case caching its first successful result.
 */
abstract class CacheUseCase<out Type, in Params>(
    context: CoroutineContext = Dispatchers.IO
) : BaseUseCase<Type, Params>(context) where Type : Any {

    private var cache: Type? = null

    override suspend fun invoke(
        params: Params
    ): Either<Throwable, Type> = cache?.let {
        Either.Right(it)
    } ?: super.invoke(params).also { result ->
        if (result is Either.Right) {
            cache = result.b
        }
    }
}