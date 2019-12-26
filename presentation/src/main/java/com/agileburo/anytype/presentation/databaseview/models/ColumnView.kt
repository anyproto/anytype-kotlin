package com.agileburo.anytype.presentation.databaseview.models

import com.agileburo.anytype.domain.database.model.Value

sealed class ColumnView {

    data class Title(val id: String, val name: String, val show: Boolean) : ColumnView()

    data class Text(val id: String, val name: String, val show: Boolean) : ColumnView()

    data class Number(val id: String, val name: String, val show: Boolean) : ColumnView()

    data class Date(val id: String, val name: String, val show: Boolean) : ColumnView()

    data class Select(
        val id: String,
        val name: String,
        val select: Set<String> = emptySet(),
        val show: Boolean
    ) :
        ColumnView()

    data class Multiple(
        val id: String,
        val name: String,
        val multiSelect: Set<String> = emptySet(),
        val show: Boolean
    ) : ColumnView()

    data class Person(
        val id: String,
        val name: String,
        val accounts: Set<Value>,
        val show: Boolean
    ) : ColumnView()

    data class File(val id: String, val name: String, val show: Boolean) : ColumnView()

    data class Checkbox(val id: String, val name: String, val show: Boolean) : ColumnView()

    data class URL(val id: String, val name: String, val show: Boolean) : ColumnView()

    data class Email(val id: String, val name: String, val show: Boolean) : ColumnView()

    data class Phone(val id: String, val name: String, val show: Boolean) : ColumnView()

    object AddNew : ColumnView()
}

