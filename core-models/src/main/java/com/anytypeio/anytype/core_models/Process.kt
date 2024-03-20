package com.anytypeio.anytype.core_models

data class Process(
    val id: String,
    val type: Type,
    val state: State,
    val progress: Progress?
) {
    enum class Type {
        DROP_FILES,
        IMPORT,
        EXPORT,
        SAVE_FILE,
        RECOVER_ACCOUNT,
        MIGRATION
    }

    enum class State {
        NONE,
        RUNNING,
        DONE,
        CANCELED,
        ERROR
    }

    data class Progress(
        val total: Long,
        val done: Long,
        val message: String
    )

    sealed class Event {
        data class New(
            val process: Process?
        ) : Event()

        data class Update(
            val process: Process?
        ) : Event()

        data class Done(
            val process: Process?
        ) : Event()
    }
}
