package com.anytypeio.anytype.core_models

sealed class ProcessEvent {

    data class New(
        val process: Process?
    ) : ProcessEvent()

    data class Update(
        val process: Process?
    ) : ProcessEvent()

    data class Done(
        val process: Process?
    ) : ProcessEvent()
}