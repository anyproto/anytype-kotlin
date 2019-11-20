package com.agileburo.anytype.domain.database.model

sealed class Property(open val id: String) {

    data class Title(
        override val id: String,
        val name: String
    ) : Property(id)

    data class Text(
        override val id: String,
        val name: String
    ) : Property(id)

    data class Number(
        override val id: String,
        val name: String
    ) :
        Property(id)

    data class Date(
        override val id: String,
        val name: String
    ) : Property(id)

    data class Select(
        override val id: String,
        val name: String,
        val select: Set<String> = emptySet()
    ) :
        Property(id)

    data class Multiple(
        override val id: String,
        val name: String,
        val multiSelect: Set<String> = emptySet()
    ) : Property(id)

    data class Account(
        override val id: String,
        val name: String,
        val accounts: Set<Value>
    ) :
        Property(id)

    data class File(
        override val id: String,
        val name: String
    ) :
        Property(id)

    data class Bool(
        override val id: String,
        val name: String
    ) :
        Property(id)

    data class Link(
        override val id: String,
        val name: String
    ) :
        Property(id)

    data class Email(
        override val id: String,
        val name: String
    ) :
        Property(id)

    data class Phone
        (
        override val id: String,
        val name: String
    ) :
        Property(id)
}

data class Value(val name: String)