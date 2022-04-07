package com.anytypeio.anytype.core_utils.ext

import android.view.View
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE
import androidx.core.view.WindowInsetsCompat
import com.anytypeio.anytype.core_utils.insets.ControlFocusInsetsAnimationCallback
import com.anytypeio.anytype.core_utils.insets.TranslateDeferringInsetsAnimationCallback

fun EditText.syncFocusWithImeVisibility() {
    ViewCompat.setWindowInsetsAnimationCallback(
        this,
        ControlFocusInsetsAnimationCallback(this)
    )
}

fun View.syncTranslationWithImeVisibility(
    dispatchMode: Int = DISPATCH_MODE_CONTINUE_ON_SUBTREE
) {
    ViewCompat.setWindowInsetsAnimationCallback(
        this,
        TranslateDeferringInsetsAnimationCallback(
            view = this,
            persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
            deferredInsetTypes = WindowInsetsCompat.Type.ime(),
            dispatchMode = dispatchMode
        )
    )
}