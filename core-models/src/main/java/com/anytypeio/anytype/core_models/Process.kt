package com.anytypeio.anytype.core_models

data class Process(
    val id: String,
    val spaceId: Id,
    val type: Type,
    val state: State,
    val progress: Progress?
) {
    enum class Type {
        DROP_FILES,
        IMPORT,
        EXPORT,
        SAVE_FILE,
        MIGRATION,
        UNKNOWN
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

        sealed class Migration : Event() {
            data class New(
                val process: Process
            ) : Migration()

            data class Update(
                val process: Process
            ) : Migration()

            data class Done(
                val process: Process
            ) : Migration()
        }

        sealed class DropFiles : Event() {
            data class New(
                val process: Process
            ) : DropFiles()

            data class Update(
                val process: Process
            ) : DropFiles()

            data class Done(
                val process: Process
            ) : DropFiles()
        }

        sealed class Import : Event() {
            data class New(
                val process: Process
            ) : Import()

            data class Update(
                val process: Process
            ) : Import()

            data class Done(
                val process: Process
            ) : Import()
        }
    }
}
