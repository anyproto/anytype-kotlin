package com.anytypeio.anytype.domain.base

import com.anytypeio.anytype.domain.error.Error

@Deprecated(
    message = "Result is deprecated",
    replaceWith = ReplaceWith("Resultat", "com.anytypeio.anytype.domain.base")
)
sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Failure<T>(val error: Error) : Result<T>()
}