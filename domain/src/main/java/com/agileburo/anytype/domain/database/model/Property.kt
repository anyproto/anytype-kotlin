package com.agileburo.anytype.domain.database.model

sealed class Property

data class Title(val id: String, val name: String) : Property()

data class Text(val id: String, val name: String, val text: String) : Property()

data class Number(val id: String, val name: String, val number: Int) : Property()

data class Date(val id: String, val name: String, val date: String) : Property()

data class Select(val id: String, val name: String, val select: Set<String>) : Property()

data class Multiple(val id: String, val name: String, val multiSelect: Set<String>) : Property()

data class Account(val id: String, val name: String) : Property()

data class File(val id: String, val name: String, val files: List<String>) : Property()

data class Bool(val id: String, val name: String, val checked: Boolean) : Property()

data class Link(val id: String, val name: String, val bookmark: String) : Property()

data class Email(val id: String, val name: String, val email: String) : Property()

data class Phone(val id: String, val name: String, val phone: String) : Property()