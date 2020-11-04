package com.anytypeio.anytype.presentation.page.editor.pattern

interface Matcher<out T> {
    fun match(text: String): List<T>
}