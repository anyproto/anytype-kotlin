package com.agileburo.anytype.presentation.databaseview.models

sealed class Cell {
    data class Title(val title: String) : Cell()
    data class Text(val text: String) : Cell()
    data class Number(val number: String) : Cell()
    data class Email(val email: String) : Cell()

    //todo move Int -> Long
    data class Date(val date: Int) : Cell()
    data class Select(val select: String) : Cell()
    data class Multiple(val multiple:Array<String>) : Cell()
    data class Account(val accounts: HashMap<String, String>) : Cell()
    data class File(val file: String) : Cell()
    data class Checked(val isChecked: Boolean) : Cell()
    data class Link(val link: String) : Cell()
    data class Phone(val phone: String) : Cell()
}

