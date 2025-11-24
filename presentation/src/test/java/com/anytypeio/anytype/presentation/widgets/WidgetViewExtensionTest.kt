package com.anytypeio.anytype.presentation.widgets

import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Test

/**
 * Unit tests for WidgetView extension functions:
 * - [compositeKey]: Generates unique keys for widgets across sections
 * - [extractWidgetId]: Extracts widget IDs from composite keys
 */
class WidgetViewExtensionTest {

    // ========================================
    // Tests for compositeKey()
    // ========================================

    @Test
    fun `compositeKey should generate key with PINNED section`() {
        // Given
        val widget = WidgetView.Tree(
            id = "widget123",
            name = WidgetView.Name.Empty,
            source = Widget.Source.Bundled.Favorites,
            sectionType = SectionType.PINNED
        )

        // When
        val result = widget.compositeKey()

        // Then
        assertEquals("PINNED_widget123", result)
    }

    @Test
    fun `compositeKey should generate key with TYPES section`() {
        // Given
        val widget = WidgetView.Tree(
            id = "typeWidget456",
            name = WidgetView.Name.Empty,
            source = Widget.Source.Bundled.Favorites,
            sectionType = SectionType.TYPES
        )

        // When
        val result = widget.compositeKey()

        // Then
        assertEquals("TYPES_typeWidget456", result)
    }

    @Test
    fun `compositeKey should generate key with NONE section`() {
        // Given
        val widget = WidgetView.Tree(
            id = "noneWidget789",
            name = WidgetView.Name.Empty,
            source = Widget.Source.Bundled.Favorites,
            sectionType = SectionType.NONE
        )

        // When
        val result = widget.compositeKey()

        // Then
        assertEquals("NONE_noneWidget789", result)
    }

    @Test
    fun `compositeKey should generate key with null section`() {
        // Given
        val widget = WidgetView.Tree(
            id = "nullSectionWidget",
            name = WidgetView.Name.Empty,
            source = Widget.Source.Bundled.Favorites,
            sectionType = null
        )

        // When
        val result = widget.compositeKey()

        // Then
        assertEquals("null_nullSectionWidget", result)
    }

    @Test
    fun `compositeKey should handle widget ID with underscores`() {
        // Given
        val widget = WidgetView.Tree(
            id = "widget_id_with_underscores",
            name = WidgetView.Name.Empty,
            source = Widget.Source.Bundled.Favorites,
            sectionType = SectionType.PINNED
        )

        // When
        val result = widget.compositeKey()

        // Then
        assertEquals("PINNED_widget_id_with_underscores", result)
    }

    // ========================================
    // Tests for extractWidgetId()
    // ========================================

    @Test
    fun `extractWidgetId should extract ID from valid PINNED composite key`() {
        // Given
        val compositeKey = "PINNED_abc123"

        // When
        val result = compositeKey.extractWidgetId()

        // Then
        assertEquals("abc123", result)
    }

    @Test
    fun `extractWidgetId should extract ID from valid TYPES composite key`() {
        // Given
        val compositeKey = "TYPES_xyz789"

        // When
        val result = compositeKey.extractWidgetId()

        // Then
        assertEquals("xyz789", result)
    }

    @Test
    fun `extractWidgetId should extract ID from valid NONE composite key`() {
        // Given
        val compositeKey = "NONE_def456"

        // When
        val result = compositeKey.extractWidgetId()

        // Then
        assertEquals("def456", result)
    }

    @Test
    fun `extractWidgetId should handle widget ID with underscores`() {
        // Given
        val compositeKey = "PINNED_widget_id_with_underscores"

        // When
        val result = compositeKey.extractWidgetId()

        // Then
        assertEquals("widget_id_with_underscores", result)
    }

    @Test
    fun `extractWidgetId should extract everything after first underscore when multiple underscores`() {
        // Given
        val compositeKey = "TYPES_multi_underscore_id"

        // When
        val result = compositeKey.extractWidgetId()

        // Then
        assertEquals("multi_underscore_id", result)
    }

    @Test
    fun `extractWidgetId should return null for key without underscore`() {
        // Given
        val invalidKey = "INVALIDKEY"

        // When
        val result = invalidKey.extractWidgetId()

        // Then
        assertNull(result)
    }

    @Test
    fun `extractWidgetId should return null for empty string`() {
        // Given
        val emptyKey = ""

        // When
        val result = emptyKey.extractWidgetId()

        // Then
        assertNull(result)
    }

    @Test
    fun `extractWidgetId should return null for key with underscore but no ID`() {
        // Given
        val invalidKey = "SECTION_"

        // When
        val result = invalidKey.extractWidgetId()

        // Then
        assertNull(result)
    }

    @Test
    fun `extractWidgetId should handle key with only underscore`() {
        // Given
        val invalidKey = "_"

        // When
        val result = invalidKey.extractWidgetId()

        // Then
        assertNull(result)
    }

    // ========================================
    // Roundtrip Tests
    // ========================================

    @Test
    fun `roundtrip should work for PINNED widget`() {
        // Given
        val widget = WidgetView.Tree(
            id = "roundtrip123",
            name = WidgetView.Name.Empty,
            source = Widget.Source.Bundled.Favorites,
            sectionType = SectionType.PINNED
        )

        // When: Generate composite key and extract ID back
        val compositeKey = widget.compositeKey()
        val extractedId = compositeKey.extractWidgetId()

        // Then: Extracted ID should match original ID
        assertEquals(widget.id, extractedId)
    }

    @Test
    fun `roundtrip should work for TYPES widget`() {
        // Given
        val widget = WidgetView.Tree(
            id = "typeRoundtrip456",
            name = WidgetView.Name.Empty,
            source = Widget.Source.Bundled.Favorites,
            sectionType = SectionType.TYPES
        )

        // When: Generate composite key and extract ID back
        val compositeKey = widget.compositeKey()
        val extractedId = compositeKey.extractWidgetId()

        // Then: Extracted ID should match original ID
        assertEquals(widget.id, extractedId)
    }

    @Test
    fun `roundtrip should work for widget with underscores in ID`() {
        // Given
        val widget = WidgetView.Tree(
            id = "widget_with_many_underscores",
            name = WidgetView.Name.Empty,
            source = Widget.Source.Bundled.Favorites,
            sectionType = SectionType.PINNED
        )

        // When: Generate composite key and extract ID back
        val compositeKey = widget.compositeKey()
        val extractedId = compositeKey.extractWidgetId()

        // Then: Extracted ID should match original ID
        assertEquals(widget.id, extractedId)
    }

    @Test
    fun `roundtrip should work for all section types`() {
        val testCases = listOf(
            SectionType.PINNED to "pinned_widget",
            SectionType.TYPES to "types_widget",
            SectionType.NONE to "none_widget"
        )

        testCases.forEach { (sectionType, widgetId) ->
            // Given
            val widget = WidgetView.Tree(
                id = widgetId,
                name = WidgetView.Name.Empty,
                source = Widget.Source.Bundled.Favorites,
                sectionType = sectionType
            )

            // When
            val compositeKey = widget.compositeKey()
            val extractedId = compositeKey.extractWidgetId()

            // Then
            assertEquals(
                widget.id,
                extractedId,
                "Roundtrip failed for section type: $sectionType"
            )
        }
    }
}
