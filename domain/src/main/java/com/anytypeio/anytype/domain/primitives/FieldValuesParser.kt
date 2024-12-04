package com.anytypeio.anytype.domain.primitives

import com.anytypeio.anytype.core_models.primitives.Value
import com.anytypeio.anytype.core_models.primitives.Value.Single

/**
 * Object responsible for parsing various input types into a [Value.Single] containing a [Long] timestamp.
 *
 * The `FieldDateParser` provides a flexible way to handle different types of inputs that represent dates or timestamps.
 * It attempts to parse the input into a `Long` value, which can then be used for further date calculations or formatting.
 *
 * **Supported Input Types:**
 * - **String**: Parses numeric strings into `Long`.
 * - **Number**: Converts numeric types directly into `Long`.
 * - **List**: Iterates over a list to find and return the **first** valid `Long` value.
 *
 * If the input cannot be parsed into a `Long`, the parser returns `null`.
 */
object FieldDateParser {

    /**
     * Parses the input [value] into a [Value.Single] containing a [Long], if possible.
     *
     * ### Supported Input Types:
     * - **String**: Attempts to parse the string into a `Long`. Returns a [Value.Single] if successful.
     * - **Number**: Converts the number to a `Long` and wraps it in a [Value.Single].
     * - **List<\*>**: Iterates over the list to find the **first** valid `Long` value.
     *   - The list can contain elements of type `String`, `Number`, or others.
     *   - Invalid entries are skipped.
     *   - If a valid `Long` is found, it is returned as a [Value.Single].
     *
     * ### Behavior:
     * - Returns `null` if the input is `null` or cannot be parsed into a `Long`.
     * - For floating-point numbers, the decimal part is truncated when converting to `Long`.
     *
     * @param value The input value to parse, which can be of any type (`Any?`).
     * @return A [Value.Single] containing the parsed `Long` value, or `null` if parsing fails.
     *
     * ### Examples:
     * ```kotlin
     * FieldDateParser.parse("1627814400")                      // Returns Value.Single(1627814400L)
     * FieldDateParser.parse(1627814400L)                       // Returns Value.Single(1627814400L)
     * FieldDateParser.parse(listOf("invalid", "1627814400"))   // Returns Value.Single(1627814400L)
     * FieldDateParser.parse(null)                              // Returns null
     * FieldDateParser.parse("invalid_number")                  // Returns null
     * ```
     */
    fun parse(value: Any?): Single<Long>? = when (value) {
        is String -> value.toLongOrNull()?.let(Value<Long>::Single)
        is Number -> Single(value.toLong())
        is List<*> -> value
            .asSequence()
            .mapNotNull {
                when (it) {
                    is String -> it.toLongOrNull()
                    is Number -> it.toLong()
                    else -> null
                }
            }
            .firstOrNull()
            ?.let(Value<Long>::Single)
        else -> null
    }
}