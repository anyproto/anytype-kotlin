package com.anytypeio.anytype.core_utils.ui

import android.view.View
import com.anytypeio.anytype.core_utils.ext.focusAndShowKeyboard
import com.anytypeio.anytype.core_utils.ext.hideKeyboard
import com.google.android.material.bottomsheet.BottomSheetBehavior

class TextInputDialogBottomBehaviorApplier(
    private val attachView: View,
    private val textInput: View,
    private val dialogCancelListener: OnDialogCancelListener
) {
    interface OnDialogCancelListener {
        fun onDialogCancelled()
    }

    private var isOpenedKeyboard = true

    fun apply() {
        BottomSheetBehavior.from(attachView).apply {
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_EXPANDED
            isHideable = true
            setupCallback()
        }
    }

    private fun BottomSheetBehavior<View>.setupCallback() {
        addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                onSlide(slideOffset)
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                onStateChanged(newState)
            }
        })
    }

    private fun onSlide(slideOffset: Float) {
        if (slideOffset <= MIDDLE_POSITION) {
            if (isOpenedKeyboard) {
                attachView.hideKeyboard()
                isOpenedKeyboard = false
            }
        } else {
            if (!isOpenedKeyboard) {
                textInput.focusAndShowKeyboard()
                isOpenedKeyboard = true
            }
        }
    }

    private fun onStateChanged(newState: Int) {
        if (newState == BottomSheetBehavior.STATE_HIDDEN) {
            dialogCancelListener.onDialogCancelled()
        }
    }
}

private const val MIDDLE_POSITION = 0f