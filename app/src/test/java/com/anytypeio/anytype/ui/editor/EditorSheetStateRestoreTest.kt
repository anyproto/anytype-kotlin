package com.anytypeio.anytype.ui.editor

import android.content.Context
import android.os.Build
import android.os.Parcelable
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.test.core.app.ApplicationProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * DROID-4592. The editor holds the state of every toolbar sheet in `ControlPanelState`, which the
 * view model owns and a rotation preserves. [BottomSheetBehavior] also saves its own state into the
 * view hierarchy. It restores that state with a direct field write, so no state callback runs, and
 * the restore runs after `onViewCreated`.
 *
 * A rotation therefore gives `render()` a fresh, invisible sheet whose behavior reports
 * `STATE_EXPANDED`. Each guard in `render()` reads the behavior, concludes that the sheet is open,
 * and skips the setup. The block action toolbar stays invisible, and `showSelectButton()` never
 * moves the select toolbar back on screen.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class EditorSheetStateRestoreTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private companion object {
        const val PANELS_ID = 1001
        const val SHEET_ID = 1002
    }

    private class Panels(
        val root: CoordinatorLayout,
        val sheet: View,
        val behavior: BottomSheetBehavior<View>
    )

    /** Builds the `panels` container with one hideable sheet, as `fragment_editor.xml` declares it. */
    private fun buildPanels(): Panels {
        val sheetBehavior = BottomSheetBehavior<View>().apply { isHideable = true }
        val sheet = View(context).apply {
            id = SHEET_ID
            visibility = View.INVISIBLE
            layoutParams = CoordinatorLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { behavior = sheetBehavior }
        }
        val root = CoordinatorLayout(context).apply {
            id = PANELS_ID
            addView(sheet)
        }
        return Panels(root, sheet, sheetBehavior)
    }

    /** Saves the hierarchy of an open sheet, then restores it onto a fresh hierarchy. */
    private fun rotate(): Panels {
        val before = buildPanels()
        before.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        val container = SparseArray<Parcelable>()
        before.root.saveHierarchyState(container)

        val after = buildPanels()
        // `onViewCreated` hides every sheet, and it runs before the hierarchy restore.
        after.behavior.state = BottomSheetBehavior.STATE_HIDDEN
        after.root.restoreHierarchyState(container)
        return after
    }

    @Test
    fun `a rotation restores the expanded state onto a fresh invisible sheet`() {
        val after = rotate()
        // This is the trap the fix exists for: the two sources of truth disagree.
        assertEquals(BottomSheetBehavior.STATE_EXPANDED, after.behavior.state)
        assertEquals(View.INVISIBLE, after.sheet.visibility)
    }

    @Test
    fun `resetRestoredSheetState drops the stale state, so render can drive the sheet`() {
        val after = rotate()

        listOf(after.sheet).resetRestoredSheetState()

        assertEquals(BottomSheetBehavior.STATE_HIDDEN, after.behavior.state)
    }
}
