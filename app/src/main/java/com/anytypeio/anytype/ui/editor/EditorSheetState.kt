package com.anytypeio.anytype.ui.editor

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior

/**
 * Drops the sheet state that the view hierarchy restored after a configuration change (DROID-4592).
 *
 * The editor holds the state of every toolbar sheet in `ControlPanelState`, which the view model
 * owns and a rotation preserves. [BottomSheetBehavior] saves a second copy into the view hierarchy.
 * It restores that copy with a direct field write, so no state callback runs, and the restore runs
 * after `onViewCreated`. The freshly inflated sheet is therefore invisible while its behavior
 * reports `STATE_EXPANDED`, and every guard in `EditorFragment.render` reads the behavior and skips
 * the setup of a sheet that the user cannot see.
 *
 * Call this after the hierarchy restore and before the first render. The control panel state is
 * then the only source of truth for the chrome.
 */
fun Iterable<View>.resetRestoredSheetState() {
    forEach { sheet ->
        BottomSheetBehavior.from(sheet).state = BottomSheetBehavior.STATE_HIDDEN
    }
}
