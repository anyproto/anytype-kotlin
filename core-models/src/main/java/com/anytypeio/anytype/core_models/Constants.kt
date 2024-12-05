package com.anytypeio.anytype.core_models

import java.text.DateFormat

const val NO_VALUE = ""
const val EMPTY_QUERY = ""

/**
 * The default date format pattern to use if retrieval fails.
 */
const val FALLBACK_DATE_PATTERN = "dd/MM/yyyy"

/**
 * The default value for relative dates.
 */
const val DEFAULT_RELATIVE_DATES = true

/**
 * The default value for showing the introduce vault.
 */
const val DEFAULT_SHOW_INTRODUCE_VAULT = true

/**
 * The default value for showing the time
 */
const val DEFAULT_DATE_FORMAT_STYLE = DateFormat.DEFAULT

/**
 * The date range for the date picker.
 */
val DATE_PICKER_YEAR_RANGE = IntRange(0, 3000)

/**
 * The maximum size of a snippet.
 */
const val MAX_SNIPPET_SIZE = 30