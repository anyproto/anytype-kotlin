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
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.EditDataViewViewerViewModel
import kotlinx.android.synthetic.main.fragment_edit_data_view_viewer.*
import javax.inject.Inject

class EditDataViewViewerFragment : BaseBottomSheetFragment() {

    private val ctx: Id get() = arg(CTX_KEY)
    private val viewer: Id get() = arg(VIEWER_KEY)

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
        with(lifecycleScope) {
            subscribe(viewerNameInput.textChanges()) { name ->
                vm.onViewerNameChanged(name = name.toString())
            }
            subscribe(btnDone.clicks()) {
                vm.onDoneClicked(ctx, viewer)
            }
            subscribe(threeDotsButton.clicks()) { vm.onMenuClicked() }
            subscribe(gridContainer.clicks()) { vm.onGridClicked() }
            subscribe(galleryContainer.clicks()) { vm.onGalleryClicked() }
            subscribe(listContainer.clicks()) { vm.onListClicked() }
        }
    }

    override fun onStart() {
        with(lifecycleScope) {
            jobs += subscribe(vm.viewState) { render(it) }
            jobs += subscribe(vm.isDismissed) { isDismissed ->
                if (isDismissed) {
                    viewerNameInput.apply {
                        clearFocus()
                        hideKeyboard()
                    }
                    dismiss()
                }
            }
            jobs += subscribe(vm.isLoading) { isLoading ->
                if (isLoading) progressBar.visible() else progressBar.gone()
            }
            jobs += subscribe(vm.toasts) { toast(it) }
            jobs += subscribe(vm.popupCommands) { cmd ->
                DataViewEditViewPopupMenu(
                    requireContext(),
                    threeDotsButton,
                    cmd.isDeletionAllowed
                ).apply {
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.duplicate -> vm.onDuplicateClicked(ctx = ctx, viewer = viewer)
                            R.id.delete -> vm.onDeleteClicked(ctx = ctx, viewer = viewer)
                        }
                        true
                    }
                }.show()
            }
        }
        super.onStart()
        vm.onStart(viewer)
    }

    private fun render(state: EditDataViewViewerViewModel.ViewState) {
        when (state) {
            EditDataViewViewerViewModel.ViewState.Init -> {
                viewerNameInput.text = null
                isListChosen.invisible()
                isTableChosen.invisible()
                isGalleryChosen.invisible()
            }
            is EditDataViewViewerViewModel.ViewState.Name -> {
                viewerNameInput.setText(state.name)
            }
            EditDataViewViewerViewModel.ViewState.Completed -> {
                dismiss()
            }
            is EditDataViewViewerViewModel.ViewState.Error -> {
                toast(state.msg)
            }
            EditDataViewViewerViewModel.ViewState.Gallery -> {
                isListChosen.invisible()
                isTableChosen.invisible()
                isGalleryChosen.visible()
            }
            EditDataViewViewerViewModel.ViewState.Grid -> {
                isListChosen.invisible()
                isTableChosen.visible()
                isGalleryChosen.invisible()
            }

            EditDataViewViewerViewModel.ViewState.List -> {
                isListChosen.visible()
                isTableChosen.invisible()
                isGalleryChosen.invisible()
            }
            EditDataViewViewerViewModel.ViewState.Kanban -> {}
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

        fun new(
            ctx: Id,
            viewer: Id
        ): EditDataViewViewerFragment = EditDataViewViewerFragment().apply {
            arguments = bundleOf(
                CTX_KEY to ctx,
                VIEWER_KEY to viewer
            )
        }
    }
}