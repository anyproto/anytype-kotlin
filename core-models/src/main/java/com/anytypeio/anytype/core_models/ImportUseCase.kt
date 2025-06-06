package com.anytypeio.anytype.core_models

/**
 * Space creation use case enum that corresponds to Rpc.Object.ImportUseCase.Request.UseCase
 */
enum class SpaceCreationUseCase(val value: Int) {
    NONE(0),
    GET_STARTED(1),
    EMPTY(2),
    GUIDE_ONLY(3),
    GET_STARTED_MOBILE(4),
    EMPTY_MOBILE(5)
} 