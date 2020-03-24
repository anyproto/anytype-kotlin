package com.agileburo.anytype.sample

import android.os.Bundle
import android.view.View
import com.agileburo.anytype.core_ui.widgets.dialog.AboveDialog
import kotlinx.android.synthetic.main.above_dialog_fragment.view.*

class AboveDialogFragment : AboveDialog() {

    override fun layout(): Int = R.layout.above_dialog_fragment
    override fun title(): String? = "Тестовый заголовок"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.button4.setOnClickListener {
            (requireActivity() as UpdateValues).update1()
            dismiss()
        }
        view.button5.setOnClickListener { (requireActivity() as UpdateValues).update2() }
        view.button6.setOnClickListener { (requireActivity() as UpdateValues).update3() }
    }
}
