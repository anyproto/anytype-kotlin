package com.anytypeio.anytype.presentation.editor.editor.pattern

/**
 * Matcher used to find patterns (hot keys, regex, etc) in editor text.
 */
class DefaultPatternMatcher : Matcher<Pattern> {

    private val numbered = Regex(REGEX_NUMBERED_LIST_PATTERN)
    private val line = Regex(REGEX_DIVIDER_LINE_PATTERN)
    private val dots = Regex(REGEX_DIVIDER_DOTS_PATTERN)
    private val checkbox = Regex(REGEX_CHECKBOX_PATTERN)
    private val h1 = Regex(REGEX_HEADER_ONE_PATTERN)
    private val h2 = Regex(REGEX_HEADER_TWO_PATTERN)
    private val h3 = Regex(REGEX_HEADER_THREE_PATTERN)
    private val quote = Regex(REGEX_QUOTE_PATTERN)
    private val toggle = Regex(REGEX_TOGGLE_PATTERN)
    private val bullet = Regex(REGEX_BULLET_PATTERN)
    private val snippet = Regex(REGEX_SNIPPET_PATTERN)

    override fun match(text: String): List<Pattern> {
        val result = mutableListOf<Pattern>()
        when {
            text.matches(h1) -> result.add(Pattern.H1)
            text.matches(h2) -> result.add(Pattern.H2)
            text.matches(h3) -> result.add(Pattern.H3)
            text.matches(bullet) -> result.add(Pattern.BULLET)
            text.matches(numbered) -> result.add(Pattern.NUMBERED)
            text.matches(checkbox) -> result.add(Pattern.CHECKBOX)
            text.matches(quote) -> result.add(Pattern.QUOTE)
            text.matches(toggle) -> result.add(Pattern.TOGGLE)
            text.matches(line) -> result.add(Pattern.DIVIDER_LINE)
            text.matches(dots) -> result.add(Pattern.DIVIDER_DOTS)
            text.matches(snippet) -> result.add(Pattern.SNIPPET)
        }
        return result
    }

    companion object {
        const val REGEX_NUMBERED_LIST_PATTERN = "^1\\. "
        const val REGEX_DIVIDER_LINE_PATTERN = "^--- "
        const val REGEX_DIVIDER_DOTS_PATTERN = "^\\*\\*\\* "
        const val REGEX_CHECKBOX_PATTERN = "^\\[] "
        const val REGEX_HEADER_ONE_PATTERN = "^# "
        const val REGEX_HEADER_TWO_PATTERN = "^## "
        const val REGEX_HEADER_THREE_PATTERN = "^### "
        const val REGEX_QUOTE_PATTERN = "^\" |« |“ |' |‘ "
        const val REGEX_TOGGLE_PATTERN = "^> "
        const val REGEX_BULLET_PATTERN = "^\\* |\\+ |- "
        const val REGEX_SNIPPET_PATTERN = "^``` "
    }
}