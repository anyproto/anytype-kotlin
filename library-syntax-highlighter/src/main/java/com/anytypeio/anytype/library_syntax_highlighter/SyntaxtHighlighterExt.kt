package com.anytypeio.anytype.library_syntax_highlighter

import android.content.Context
import android.graphics.Color
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.IOException

fun Context.obtainSyntaxRules(language: String): List<Syntax> {
    val path = "syntax/${language}.json"
    val json = obtainJsonDataFromAsset(path)
    return if (json != null) {
        val descriptor = Json.decodeFromString<SyntaxDescriptor>(json)
        val rules = descriptor.let { it.keywords + it.operators + it.other }
        rules.map { s ->
            Syntax(
                regex = s.pattern,
                color = Color.parseColor(s.color)
            )
        }
    } else {
        emptyList()
    }
}

fun Context.obtainGenericSyntaxRules(): List<Syntax> {
    return obtainSyntaxRules(Syntaxes.GENERIC)
}

fun Context.obtainLanguages(): List<Pair<String, String>> {
    val json = obtainJsonDataFromAsset("syntax/languages.json")
    checkNotNull(json) { "Json data for languages is missing" }
    return Json.parseToJsonElement(json).jsonObject.map { (key, element) ->
        key to element.jsonPrimitive.content
    }
}

fun Context.obtainJsonDataFromAsset(path: String): String? = try {
    assets.open(path).bufferedReader().use { stream -> stream.readText() }
} catch (e: IOException) {
    e.printStackTrace()
    null
}