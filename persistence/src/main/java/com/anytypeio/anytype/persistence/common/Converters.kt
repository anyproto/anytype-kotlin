package com.anytypeio.anytype.persistence.common

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import timber.log.Timber

typealias JsonString = String

fun Map<String, String>.toJsonString(): JsonString = try {
    JsonObject(
        content = mapValues { entry ->
            JsonPrimitive(entry.value)
        }
    ).toString()
} catch (e: Exception) {
    Timber.e(e, "Error while mapping to json string")
    ""
}

fun JsonString.toStringMap(): Map<String, String> = try {
    if (this.isNotEmpty()) {
        Json.decodeFromString(this)
    } else {
        emptyMap()
    }
} catch (e: Exception) {
    Timber.e(e, "Error while mapping from json string")
    emptyMap()
}