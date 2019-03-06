package com.agileburo.anytype.editor

import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.agileburo.anytype.R

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 06.03.2019.
 */
class EditorFragment: BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return inflater.inflate(R.layout.fragment_editor, container, false)
    }
}