package com.agileburo.anytype.presentation.databaseview.models

import com.agileburo.anytype.domain.database.model.Value

sealed class ColumnView {

    data class Title(val id: String, val name: String) : ColumnView()

    data class Text(val id: String, val name: String) : ColumnView()

    data class Number(val id: String, val name: String) : ColumnView()

    data class Date(val id: String, val name: String) : ColumnView()

    data class Select(val id: String, val name: String, val select: Set<String> = emptySet()) :
        ColumnView()

    data class Multiple(
        val id: String,
        val name: String,
        val multiSelect: Set<String> = emptySet()
    ) : ColumnView()

    data class Account(val id: String, val name: String, val accounts: Set<Value>) : ColumnView()

    data class File(val id: String, val name: String) : ColumnView()

    data class Bool(val id: String, val name: String) : ColumnView()

    data class Link(val id: String, val name: String) : ColumnView()

    data class Email(val id: String, val name: String) : ColumnView()

    data class Phone(val id: String, val name: String) : ColumnView()
}

