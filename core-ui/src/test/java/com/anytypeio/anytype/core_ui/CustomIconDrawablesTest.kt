package com.anytypeio.anytype.core_ui

import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons.CustomIconDrawables
import com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons.CustomIcons
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.Test

/**
 * Guards the custom icon set of the View based screens.
 *
 * The release build runs `shrinkResources true`. R8 deletes a drawable that no code names
 * statically, so [CustomIconDrawables] must hold a static reference for every icon. A run time
 * lookup by name resolves to 0 in the production APK, and the icon then disappears.
 */
class CustomIconDrawablesTest {

    @Test
    fun `every compose icon has a drawable`() {
        val missing = CustomIcons.iconsMap.keys - CustomIconDrawables.drawableIds.keys
        assertTrue(
            missing.isEmpty(),
            "Add these icons to CustomIconDrawables: $missing"
        )
    }

    @Test
    fun `every drawable has a compose icon`() {
        val missing = CustomIconDrawables.drawableIds.keys - CustomIcons.iconsMap.keys
        assertTrue(
            missing.isEmpty(),
            "Add these icons to CustomIcons: $missing"
        )
    }

    @Test
    fun `every drawable id is valid`() {
        val invalid = CustomIconDrawables.drawableIds.filterValues { it == 0 }.keys
        assertTrue(invalid.isEmpty(), "These icons resolve to 0: $invalid")
    }

    @Test
    fun `the default fallback icon has a drawable`() {
        assertNotNull(
            CustomIconDrawables.getDrawableRes(
                ObjectIcon.TypeIcon.Fallback.DEFAULT_FALLBACK_ICON
            )
        )
    }

    @Test
    fun `an unknown icon name returns null`() {
        assertEquals(null, CustomIconDrawables.getDrawableRes("no-such-icon"))
    }
}
