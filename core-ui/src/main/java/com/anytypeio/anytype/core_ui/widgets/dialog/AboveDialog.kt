package com.anytypeio.anytype.core_ui.widgets.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

/**
 * This implementation of dialog fragment can be shown above keyboard
 * There is a bug on versions of Fragments prior 1.2.3: https://issuetracker.google.com/issues/117894767
 */
abstract class AboveDialog : DialogFragment() {

    lateinit var dialogView: View

    abstract fun layout(): Int
    abstract fun title(): String?

    //Always called before Fragment onCreateView method
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = layoutInflater
        dialogView = onCreateDialogView(inflater)
        val builder = AlertDialog.Builder(requireContext())
            .setTitle(title())
            .setView(dialogView)
        return builder.create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return dialogView
    }

    private fun onCreateDialogView(
        inflater: LayoutInflater
    ): View = inflater.inflate(layout(), null, false)
}