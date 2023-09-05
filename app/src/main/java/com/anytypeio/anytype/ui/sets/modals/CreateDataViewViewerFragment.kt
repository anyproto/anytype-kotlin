package com.anytypeio.anytype.ui.sets.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentCreateDataViewViewerBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.CreateDataViewViewerViewModel
import javax.inject.Inject

class CreateDataViewViewerFragment : BaseBottomSheetFragment<FragmentCreateDataViewViewerBinding>() {

    val ctx get() = arg<String>(CTX_KEY)
    val target get() = arg<String>(TARGET_KEY)

    @Inject
    lateinit var factory: CreateDataViewViewerViewModel.Factory
    private val vm: CreateDataViewViewerViewModel by viewModels { factory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(lifecycleScope) {
            subscribe(binding.btnCreateViewer.clicks()) {
                vm.onAddViewer(
                    name = binding.viewerNameInput.text.toString(),
                    ctx = ctx,
                    target = target
                )
            }
            subscribe(binding.gridContainer.clicks()) { vm.onGridClicked() }
            subscribe(binding.galleryContainer.clicks()) { vm.onGalleryClicked() }
            subscribe(binding.listContainer.clicks()) { vm.onListClicked() }
        }
    }

    override fun onStart() {
        with(lifecycleScope) {
            jobs += subscribe(vm.state) { render(it) }
        }
        super.onStart()
    }

    private fun render(state: CreateDataViewViewerViewModel.ViewState) {
        when (state) {
            CreateDataViewViewerViewModel.ViewState.Init -> {
                binding.isListChosen.invisible()
                binding.isTableChosen.visible()
                binding.isGalleryChosen.invisible()
            }
            CreateDataViewViewerViewModel.ViewState.Completed -> {
                dismiss()
            }
            is CreateDataViewViewerViewModel.ViewState.Error -> {
                toast(state.msg)
            }
            CreateDataViewViewerViewModel.ViewState.Gallery -> {
                binding.isListChosen.invisible()
                binding.isTableChosen.invisible()
                binding.isGalleryChosen.visible()
            }
            CreateDataViewViewerViewModel.ViewState.Grid -> {
                binding.isListChosen.invisible()
                binding.isTableChosen.visible()
                binding.isGalleryChosen.invisible()
            }

            CreateDataViewViewerViewModel.ViewState.List -> {
                binding.isListChosen.visible()
                binding.isTableChosen.invisible()
                binding.isGalleryChosen.invisible()
            }
        }
    }

    override fun injectDependencies() {
        componentManager().createDataViewViewerComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().createDataViewViewerComponent.release(ctx)
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCreateDataViewViewerBinding = FragmentCreateDataViewViewerBinding.inflate(
        inflater, container, false
    )

    companion object {
        fun new(ctx: String, target: String) = CreateDataViewViewerFragment().apply {
            arguments = bundleOf(
                CTX_KEY to ctx, TARGET_KEY to target
            )
        }

        private const val CTX_KEY = "arg.create-data-view-viewer.context"
        private const val TARGET_KEY = "arg.create-data-view-viewer.target"
    }
}