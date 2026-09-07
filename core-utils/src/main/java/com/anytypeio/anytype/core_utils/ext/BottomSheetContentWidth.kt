package com.anytypeio.anytype.core_utils.ext

import com.anytypeio.anytype.core_utils.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Caps the sheet at the width of the content column.
 *
 * A [BottomSheetDialog] owns its own window, so the activity layout cannot reach it. Without the
 * cap the sheet spans the whole tablet while the content behind it stops at the column.
 *
 * Material Components 1.12.0 declares no `behavior_maxWidth` style attribute, so the value goes
 * through the behavior instead. On a phone in portrait the dimension is large enough to never
 * bind, and the sheet keeps the full window.
 *
 * Call this after the dialog exists, for example from `onStart`.
 */
fun BottomSheetDialogFragment.applyContentWidthCap() {
    (dialog as? BottomSheetDialog)?.behavior?.maxWidth =
        resources.getDimensionPixelSize(R.dimen.max_content_width)
}
