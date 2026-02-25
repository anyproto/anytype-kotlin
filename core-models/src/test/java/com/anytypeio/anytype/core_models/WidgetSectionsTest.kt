package com.anytypeio.anytype.core_models

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Test

class WidgetSectionsTest {

    // ========================================
    // Tests for WidgetSections.default()
    // ========================================

    @Test
    fun `default creates all sections visible in default order`() {
        val sections = WidgetSections.default()

        assertEquals(WidgetSectionType.DEFAULT_ORDER.size, sections.sections.size)
        sections.sections.forEachIndexed { index, config ->
            assertEquals(WidgetSectionType.DEFAULT_ORDER[index], config.id)
            assertTrue(config.isVisible)
            assertEquals(index, config.order)
        }
    }

    // ========================================
    // Tests for getVisibleSections()
    // ========================================

    @Test
    fun `getVisibleSections returns all sections in default order when all visible`() {
        val sections = WidgetSections.default()

        val visible = sections.getVisibleSections()

        assertEquals(WidgetSectionType.DEFAULT_ORDER, visible)
    }

    @Test
    fun `getVisibleSections excludes hidden sections`() {
        val sections = WidgetSections(
            sections = listOf(
                WidgetSectionConfig(id = WidgetSectionType.UNREAD, isVisible = true, order = 0),
                WidgetSectionConfig(id = WidgetSectionType.PINNED, isVisible = false, order = 1),
                WidgetSectionConfig(id = WidgetSectionType.OBJECTS, isVisible = true, order = 2),
                WidgetSectionConfig(id = WidgetSectionType.RECENTLY_EDITED, isVisible = false, order = 3),
                WidgetSectionConfig(id = WidgetSectionType.BIN, isVisible = true, order = 4)
            )
        )

        val visible = sections.getVisibleSections()

        assertEquals(
            listOf(WidgetSectionType.UNREAD, WidgetSectionType.OBJECTS, WidgetSectionType.BIN),
            visible
        )
    }

    @Test
    fun `getVisibleSections respects custom order`() {
        val sections = WidgetSections(
            sections = listOf(
                WidgetSectionConfig(id = WidgetSectionType.BIN, isVisible = true, order = 0),
                WidgetSectionConfig(id = WidgetSectionType.OBJECTS, isVisible = true, order = 1),
                WidgetSectionConfig(id = WidgetSectionType.PINNED, isVisible = true, order = 2),
                WidgetSectionConfig(id = WidgetSectionType.UNREAD, isVisible = true, order = 3),
                WidgetSectionConfig(id = WidgetSectionType.RECENTLY_EDITED, isVisible = true, order = 4)
            )
        )

        val visible = sections.getVisibleSections()

        assertEquals(
            listOf(
                WidgetSectionType.BIN,
                WidgetSectionType.OBJECTS,
                WidgetSectionType.PINNED,
                WidgetSectionType.UNREAD,
                WidgetSectionType.RECENTLY_EDITED
            ),
            visible
        )
    }

    @Test
    fun `getVisibleSections returns empty when all hidden`() {
        val sections = WidgetSections(
            sections = WidgetSectionType.DEFAULT_ORDER.mapIndexed { index, type ->
                WidgetSectionConfig(id = type, isVisible = false, order = index)
            }
        )

        assertTrue(sections.getVisibleSections().isEmpty())
    }

    @Test
    fun `getVisibleSections respects reordered sections after drag and drop`() {
        // Simulate: user dragged OBJECTS to position 0, pushing everything else down
        val sections = WidgetSections(
            sections = listOf(
                WidgetSectionConfig(id = WidgetSectionType.OBJECTS, isVisible = true, order = 0),
                WidgetSectionConfig(id = WidgetSectionType.UNREAD, isVisible = true, order = 1),
                WidgetSectionConfig(id = WidgetSectionType.PINNED, isVisible = true, order = 2),
                WidgetSectionConfig(id = WidgetSectionType.RECENTLY_EDITED, isVisible = true, order = 3),
                WidgetSectionConfig(id = WidgetSectionType.BIN, isVisible = true, order = 4)
            )
        )

        val visible = sections.getVisibleSections()

        assertEquals(WidgetSectionType.OBJECTS, visible.first())
        assertEquals(WidgetSectionType.BIN, visible.last())
    }

    // ========================================
    // Tests for isSectionVisible()
    // ========================================

    @Test
    fun `isSectionVisible returns true for visible section`() {
        val sections = WidgetSections.default()

        assertTrue(sections.isSectionVisible(WidgetSectionType.PINNED))
    }

    @Test
    fun `isSectionVisible returns false for hidden section`() {
        val sections = WidgetSections(
            sections = listOf(
                WidgetSectionConfig(id = WidgetSectionType.PINNED, isVisible = false, order = 0)
            )
        )

        assertFalse(sections.isSectionVisible(WidgetSectionType.PINNED))
    }

    @Test
    fun `isSectionVisible returns true for missing section (defaults to visible)`() {
        val sections = WidgetSections(sections = emptyList())

        assertTrue(sections.isSectionVisible(WidgetSectionType.UNREAD))
    }

    // ========================================
    // Tests for withDefaults()
    // ========================================

    @Test
    fun `withDefaults adds missing sections`() {
        val sections = WidgetSections(
            sections = listOf(
                WidgetSectionConfig(id = WidgetSectionType.PINNED, isVisible = true, order = 0)
            )
        )

        val result = sections.withDefaults()

        assertEquals(WidgetSectionType.DEFAULT_ORDER.size, result.sections.size)
        val types = result.sections.map { it.id }
        WidgetSectionType.DEFAULT_ORDER.forEach { type ->
            assertTrue(type in types, "Missing type: $type")
        }
    }

    @Test
    fun `withDefaults returns same instance when all types present`() {
        val sections = WidgetSections.default()

        val result = sections.withDefaults()

        assertTrue(result === sections)
    }

    @Test
    fun `withDefaults preserves existing config for present sections`() {
        val sections = WidgetSections(
            sections = listOf(
                WidgetSectionConfig(id = WidgetSectionType.PINNED, isVisible = false, order = 0),
                WidgetSectionConfig(id = WidgetSectionType.OBJECTS, isVisible = false, order = 1)
            )
        )

        val result = sections.withDefaults()

        val pinnedConfig = result.sections.find { it.id == WidgetSectionType.PINNED }!!
        assertFalse(pinnedConfig.isVisible)

        val objectsConfig = result.sections.find { it.id == WidgetSectionType.OBJECTS }!!
        assertFalse(objectsConfig.isVisible)
    }

    @Test
    fun `withDefaults assigns sequential order values`() {
        val sections = WidgetSections(
            sections = listOf(
                WidgetSectionConfig(id = WidgetSectionType.PINNED, isVisible = true, order = 0)
            )
        )

        val result = sections.withDefaults()

        result.sections.forEachIndexed { index, config ->
            assertEquals(index, config.order)
        }
    }

    // ========================================
    // Tests for WidgetSectionType
    // ========================================

    @Test
    fun `UNREAD is not user configurable`() {
        assertFalse(WidgetSectionType.UNREAD.isUserConfigurable())
    }

    @Test
    fun `all sections except UNREAD are user configurable`() {
        val configurableTypes = WidgetSectionType.entries.filter { it != WidgetSectionType.UNREAD }
        configurableTypes.forEach { type ->
            assertTrue(type.isUserConfigurable(), "$type should be user configurable")
        }
    }
}
