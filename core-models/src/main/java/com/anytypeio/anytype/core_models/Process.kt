package com.anytypeio.anytype.core_models

data class Process(
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
    }

    enum class State {
        None,
        Running,
        Done,
        Canceled,
        Error;
    }

    data class Progress(
        val total: Long,
        val done: Long,
        val message: String
    )
}
