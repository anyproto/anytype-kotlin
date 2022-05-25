package com.anytypeio.anytype.domain.error

sealed class Error {
    object BackwardCompatibility : Error()
    object NotFoundObject : Error()
}