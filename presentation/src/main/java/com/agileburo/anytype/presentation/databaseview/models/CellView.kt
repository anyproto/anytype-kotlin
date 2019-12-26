package com.agileburo.anytype.presentation.databaseview.models

sealed class CellView {
    data class Title(val title: String) : CellView()
    data class Text(val text: String) : CellView()
    data class Number(val number: String) : CellView()
    data class Email(val email: String) : CellView()

    //todo move Int -> Long
    data class Date(val date: Int) : CellView()

    data class Select(val select: String) : CellView()
    data class Multiple(val multiple: Array<String>) : CellView()
    data class Person(val accounts: HashMap<String, String>) : CellView()
    data class File(val file: String) : CellView()
    data class Checked(val isChecked: Boolean) : CellView()
    data class Link(val link: String) : CellView()
    data class Phone(val phone: String) : CellView()
}

