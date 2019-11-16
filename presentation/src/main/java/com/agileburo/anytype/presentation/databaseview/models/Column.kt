package com.agileburo.anytype.presentation.databaseview.models

import com.agileburo.anytype.domain.database.model.Value

sealed class Column {

    data class Title(val id: String, val name: String) : Column()

    data class Text(val id: String, val name: String) : Column()

    data class Number(val id: String, val name: String) : Column()

    data class Date(val id: String, val name: String) : Column()

    data class Select(val id: String, val name: String, val select: Set<String> = emptySet()) :
        Column()

    data class Multiple(
        val id: String,
        val name: String,
        val multiSelect: Set<String> = emptySet()
    ) : Column()

    data class Account(val id: String, val name: String, val accounts: Set<Value>) : Column()

    data class File(val id: String, val name: String) : Column()

    data class Bool(val id: String, val name: String) : Column()

    data class Link(val id: String, val name: String) : Column()

    data class Email(val id: String, val name: String) : Column()

    data class Phone(val id: String, val name: String) : Column()
}

