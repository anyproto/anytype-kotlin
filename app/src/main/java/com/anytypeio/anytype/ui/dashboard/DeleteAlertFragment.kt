package com.anytypeio.anytype.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import kotlinx.android.synthetic.main.fragment_delete_alert.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class DeleteAlertFragment : BaseBottomSheetFragment() {

    private val count get() = arg<Int>(COUNT_KEY)

    var onDeletionAccepted: () -> Unit = {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_delete_alert, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (count > 1) {
            tvTitle.text = getString(R.string.are_you_sure_delete_n_objects, count)
            tvSubtitle.setText(R.string.delete_irrevocably)
        } else {
            tvTitle.setText(R.string.are_you_sure_delete_one_object)
            tvSubtitle.setText(R.string.delete_irrevocably_one_object)
        }

        btnCancel
            .clicks()
            .onEach { dismiss() }
            .launchIn(lifecycleScope)

        btnDelete
            .clicks()
            .onEach {
                onDeletionAccepted()
                dismiss()
            }
            .launchIn(lifecycleScope)
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    companion object {
        const val COUNT_KEY = "arg.delete-alert-dialog.count"
        fun new(count: Int) : DeleteAlertFragment = DeleteAlertFragment().apply {
            arguments = bundleOf(COUNT_KEY to count)
        }
    }
}