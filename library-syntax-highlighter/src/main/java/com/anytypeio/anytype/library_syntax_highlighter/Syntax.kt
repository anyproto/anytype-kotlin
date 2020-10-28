package com.anytypeio.anytype.library_syntax_highlighter

import java.util.regex.Matcher
import java.util.regex.Pattern

class Syntax(val color: Int, val regex: String) {
    private val pattern = Pattern.compile(regex)
    fun matcher(txt: String): Matcher = pattern.matcher(txt)
}