package com.anytypeio.anytype.ui.sets.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.menu.DataViewEditViewPopupMenu
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_ui.reactive.textChanges
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.hideKeyboard
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.EditDataViewViewerViewModel
import kotlinx.android.synthetic.main.fragment_edit_data_view_viewer.*
import javax.inject.Inject

class EditDataViewViewerFragment : BaseBottomSheetFragment() {

    private val ctx: Id get() = arg(CTX_KEY)
    private val viewer: Id get() = arg(VIEWER_KEY)
    private val name: Id get() = arg(NAME_KEY)

    @Inject
    lateinit var factory: EditDataViewViewerViewModel.Factory
    private val vm: EditDataViewViewerViewModel by viewModels { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_edit_data_view_viewer, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewerNameInput.setText(name)
        with(lifecycleScope) {
            subscribe(viewerNameInput.textChanges()) { name ->
                vm.onViewerNameChanged(
                    ctx = ctx,
                    viewer = viewer,
                    name = name.toString()
                )
            }
            subscribe(btnDone.clicks()) { vm.onDoneClicked() }
            subscribe(threeDotsButton.clicks()) { vm.onMenuClicked() }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(lifecycleScope) {
            subscribe(vm.isDismissed) { isDismissed ->
                if (isDismissed) {
                    viewerNameInput.apply {
                        clearFocus()
                        hideKeyboard()
                    }
                    dismiss()
                }
            }
            subscribe(vm.toasts) { toast(it) }
            subscribe(vm.popupCommands) { cmd ->
                DataViewEditViewPopupMenu(
                    requireContext(),
                    threeDotsButton,
                    cmd.isDeletionAllowed
                ).show()
            }
        }
    }

    override fun injectDependencies() {
        componentManager().editDataViewViewerComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().editDataViewViewerComponent.release(ctx)
    }

    companion object {
        const val CTX_KEY = "arg.edit-data-view-viewer.ctx"
        const val VIEWER_KEY = "arg.edit-data-view-viewer.viewer"
        const val NAME_KEY = "arg.edit-data-view-viewer.name"

        fun new(
            ctx: Id,
            viewer: Id,
            name: String,
        ): EditDataViewViewerFragment = EditDataViewViewerFragment().apply {
            arguments = bundleOf(
                CTX_KEY to ctx,
                VIEWER_KEY to viewer,
                NAME_KEY to name
            )
        }
    }
}