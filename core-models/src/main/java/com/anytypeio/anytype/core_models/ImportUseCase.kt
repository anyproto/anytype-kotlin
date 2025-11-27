package com.anytypeio.anytype.core_models

/**
 * Space creation use case enum that corresponds to Rpc.Object.ImportUseCase.Request.UseCase
 */
enum class SpaceCreationUseCase(val value: Int) {
    NONE(0),
    GET_STARTED(1),
    DATA_SPACE(2),
    GUIDE_ONLY(3),
    GET_STARTED_MOBILE(4),
    CHAT_SPACE(5),
    DATA_SPACE_MOBILE(6),
    ONE_TO_ONE_SPACE(7),
} 