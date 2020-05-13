package com.agileburo.anytype.presentation.page.editor

interface Converter<in INPUT, out OUTPUT> {
    fun convert(input: INPUT): OUTPUT
}