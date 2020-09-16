package com.agileburo.anytype.domain.error

sealed class Error {
    object BackwardCompatibility : Error()
}