package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.presentation.objects.custom_icon.toCamelCase
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for the [toCamelCase] extension function.
 */
class ToCamelCaseTests {

    @Test
    fun testSimpleConversion() {
        // "icon-one" should become "iconOne"
        assertEquals("iconOne", "icon-one".toCamelCase())
    }

    @Test
    fun testMultipleHyphens() {
        // "icon-two-three" should become "iconTwoThree"
        assertEquals("iconTwoThree", "icon-two-three".toCamelCase())
    }

    @Test
    fun testNoHyphen() {
        // When there's no hyphen, the string remains the same (with first letter lowercased)
        assertEquals("icon", "icon".toCamelCase())
    }

    @Test
    fun testEmptyString() {
        // An empty string should remain empty.
        assertEquals("", "".toCamelCase())
    }

    @Test
    fun testConsecutiveHyphens() {
        // Consecutive hyphens are treated as delimiters resulting in an empty segment.
        // "icon--one" should become "iconOne"
        assertEquals("iconOne", "icon--one".toCamelCase())
    }

    @Test
    fun testLeadingHyphen() {
        // A leading hyphen will produce an empty first segment.
        // "-icon" becomes "" (first segment) + "Icon" = "Icon"
        assertEquals("Icon", "-icon".toCamelCase())
    }

    @Test
    fun testTrailingHyphen() {
        // A trailing hyphen produces an empty segment at the end.
        // "icon-" becomes "icon" + "" = "icon"
        assertEquals("icon", "icon-".toCamelCase())
    }

    @Test
    fun testAllCapsConversion() {
        // For an input like "ICON-ONE", the first segment is lowercased.
        // "ICON-ONE" becomes "icon" + "ONE" = "iconONE"
        assertEquals("iconONE", "ICON-ONE".toCamelCase())
    }

    @Test
    fun testAlreadyCamelCase() {
        // "addCircle" is already camelCase and should remain unchanged.
        assertEquals("addCircle", "addCircle".toCamelCase())
    }
}