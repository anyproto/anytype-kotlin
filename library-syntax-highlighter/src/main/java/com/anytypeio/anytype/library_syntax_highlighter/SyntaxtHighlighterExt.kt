package com.anytypeio.anytype.library_syntax_highlighter

import android.content.Context
import android.graphics.Color
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.IOException

fun Context.obtainSyntaxRules(language: String): List<Syntax> {
    val path = "syntax/${language}.json"
    val json = obtainJsonDataFromAsset(path)
    checkNotNull(json) { "Could not deserialize syntax rules from path: $path" }
    val descriptor = Json.decodeFromString<SyntaxDescriptor>(json)
    val rules = descriptor.let { it.keywords + it.operators + it.other }
    return rules.map { s ->
        Syntax(
            regex = s.pattern,
            color = Color.parseColor(s.color)
        )
    }
}

fun Context.obtainJsonDataFromAsset(path: String): String? = try {
    assets.open(path).bufferedReader().use { stream -> stream.readText() }
} catch (e: IOException) {
    e.printStackTrace()
    null
}