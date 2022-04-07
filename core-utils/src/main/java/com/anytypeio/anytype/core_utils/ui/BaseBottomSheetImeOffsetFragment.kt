package com.anytypeio.anytype.core_utils.ui

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.anytypeio.anytype.core_utils.BuildConfig
import com.anytypeio.anytype.core_utils.insets.RootViewDeferringInsetsCallback
import com.anytypeio.anytype.core_utils.insets.TranslateDeferringInsetsAnimationCallback
import com.google.android.material.bottomsheet.BottomSheetDialog

abstract class BaseBottomSheetImeOffsetFragment<T : ViewBinding>(
    fragmentScope: Boolean = true
) : BaseBottomSheetFragment<T>(fragmentScope) {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (BuildConfig.USE_NEW_WINDOW_INSET_API && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return object : BottomSheetDialog(requireContext(), theme) {
                override fun onAttachedToWindow() {
                    super.onAttachedToWindow()
                    window?.let {
                        WindowCompat.setDecorFitsSystemWindows(it, false)
                    }
                    findViewById<View>(com.google.android.material.R.id.container)?.apply {
                        fitsSystemWindows = false
                        val deferringInsetsListener = RootViewDeferringInsetsCallback(
                            persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
                            deferredInsetTypes = WindowInsetsCompat.Type.ime()
                        )
                        ViewCompat.setWindowInsetsAnimationCallback(this, deferringInsetsListener)
                        ViewCompat.setOnApplyWindowInsetsListener(this, deferringInsetsListener)
                    }
                    findViewById<View>(com.google.android.material.R.id.coordinator)?.apply {
                        fitsSystemWindows = false
                        ViewCompat.setWindowInsetsAnimationCallback(
                            this,
                            TranslateDeferringInsetsAnimationCallback(
                                view = this,
                                persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
                                deferredInsetTypes = WindowInsetsCompat.Type.ime(),
                                dispatchMode = WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE
                            )
                        )
                    }
                }
            }
        } else {
            return super.onCreateDialog(savedInstanceState)
        }
    }
}