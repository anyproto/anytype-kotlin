package com.anytypeio.anytype.core_models

data class EventProcess(
    val id: String,
    val type: Type,
    val state: State,
    val progress: Progress?
) {
    enum class Type {
        DropFiles,
        Import,
        Export,
        SaveFile,
        RecoverAccount,
        Migration;

        companion object {
            fun fromInt(value: Int) = values().getOrElse(value) { DropFiles }
        }
    }

    enum class State {
        None,
        Running,
        Done,
        Canceled,
        Error;

        companion object {
            fun fromInt(value: Int) = values().getOrElse(value) { None }
        }
    }

    data class Progress(
        val total: Long,
        val done: Long,
        val message: String
    )
}
