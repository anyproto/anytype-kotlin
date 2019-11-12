package com.agileburo.anytype.domain.database.model

sealed class Property
data class Name(val title: String, val name: String) : Property()
data class Text(val title: String, val text: String) : Property()
data class Number(val title: String, val number: Int) : Property()
data class Select(val title: String, val select: Set<String>) : Property()
data class MultiSelect(val title: String, val multiSelect: Set<String>) : Property()
data class Date(val title: String, val date: String) : Property()
data class Person(val title: String, val name: String) : Property()
data class Files(val title: String, val files: List<String>) : Property()
data class Bookmark(val title: String, val bookmark: String) : Property()
data class Email(val title: String, val email: String) : Property()
data class Phone(val title: String, val phone: String) : Property()
data class Checkbox(val title: String, val checked: Boolean) : Property()
data class Hash(val hash: String) : Property()