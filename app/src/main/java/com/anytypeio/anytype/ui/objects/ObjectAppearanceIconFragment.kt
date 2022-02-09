package com.anytypeio.anytype.ui.objects

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.objects.ObjectAppearanceSettingAdapter
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentObjAppearanceBaseBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.objects.appearance.ObjectAppearanceCoverViewModel
import com.anytypeio.anytype.presentation.objects.appearance.ObjectAppearanceIconViewModel
import com.anytypeio.anytype.presentation.objects.appearance.ObjectAppearancePreviewLayoutViewModel
import javax.inject.Inject

//region ICON
class ObjectAppearanceIconFragment : BaseBottomSheetFragment() {

    @Inject
    lateinit var factory: ObjectAppearanceIconViewModel.Factory
    private val vm by viewModels<ObjectAppearanceIconViewModel> { factory }

    private val adapterAppearance by lazy {
        ObjectAppearanceSettingAdapter(
            onItemClick = { item ->
                vm.onItemClicked(item = item, blockId = block, ctx = ctx)
            },
            onSettingToggleChanged = { _, _ -> }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentObjAppearanceBaseBinding.inflate(inflater, container, false)

        binding.tvScreenTitle.text = getString(R.string.icon)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterAppearance
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                    setDrawable(drawable(R.drawable.divider_object_appearance))
                }
            )
        }

        return binding.root
    }

    override fun onStart() {
        jobs += lifecycleScope.subscribe(vm.state) { state ->
            when (state) {
                ObjectAppearanceIconViewModel.State.Dismiss -> {
                    dismiss()
                }
                is ObjectAppearanceIconViewModel.State.Success -> {
                    adapterAppearance.submitList(state.items)
                }
                is ObjectAppearanceIconViewModel.State.Error -> {
                    toast(state.msg)
                    dismiss()
                }
            }
        }
        super.onStart()
        vm.onStart(block)
    }

    override fun onStop() {
        vm.onStop()
        super.onStop()
    }

    override fun injectDependencies() {
        componentManager().objectAppearanceIconComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectAppearanceIconComponent.release(ctx)
    }

    private val ctx: String get() = argString(CONTEXT_ID_KEY)
    private val block: String get() = argString(BLOCK_ID_KEY)

    companion object {
        fun new(ctx: Id, block: Id) = ObjectAppearanceIconFragment().apply {
            arguments = bundleOf(CONTEXT_ID_KEY to ctx, BLOCK_ID_KEY to block)
        }

        const val CONTEXT_ID_KEY = "arg.object-appearance-icon.ctx"
        const val BLOCK_ID_KEY = "arg.object-appearance-cover.block"
    }
}
//endregion

//region PREVIEW LAYOUT
class ObjectAppearancePreviewLayoutFragment : BaseBottomSheetFragment() {

    @Inject
    lateinit var factory: ObjectAppearancePreviewLayoutViewModel.Factory
    private val vm by viewModels<ObjectAppearancePreviewLayoutViewModel> { factory }

    private val adapterAppearance by lazy {
        ObjectAppearanceSettingAdapter(
            onItemClick = { item ->
                vm.onItemClicked(item = item, blockId = block, ctx = ctx)
            },
            onSettingToggleChanged = { _, _ -> }
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentObjAppearanceBaseBinding.inflate(inflater, container, false)

        binding.tvScreenTitle.text = getString(R.string.preview_layout)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterAppearance
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                    setDrawable(drawable(R.drawable.divider_obj_appearance_preview_layout))
                }
            )
        }

        return binding.root
    }

    override fun onStart() {
        jobs += lifecycleScope.subscribe(vm.state) { state ->
            when (state) {
                ObjectAppearancePreviewLayoutViewModel.State.Dismiss -> dismiss()
                is ObjectAppearancePreviewLayoutViewModel.State.Error -> {
                    toast(state.msg)
                    dismiss()
                }
                is ObjectAppearancePreviewLayoutViewModel.State.Success -> {
                    adapterAppearance.submitList(state.items)
                }
            }
        }
        super.onStart()
        vm.onStart(block)
    }

    override fun onStop() {
        vm.onStop()
        super.onStop()
    }

    override fun injectDependencies() {
        componentManager().objectAppearancePreviewLayoutComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectAppearancePreviewLayoutComponent.release(ctx)
    }

    private val ctx: String get() = argString(CONTEXT_ID_KEY)
    private val block: String get() = argString(BLOCK_ID_KEY)

    companion object {
        fun new(ctx: Id, block: Id) = ObjectAppearancePreviewLayoutFragment().apply {
            arguments = bundleOf(CONTEXT_ID_KEY to ctx, BLOCK_ID_KEY to block)
        }

        const val CONTEXT_ID_KEY = "arg.object-appearance-preview-layout.ctx"
        const val BLOCK_ID_KEY = "arg.object-appearance-preview-layout.block"
    }
}
//endregion

//region COVER
class ObjectAppearanceCoverFragment : BaseBottomSheetFragment() {

    @Inject
    lateinit var factory: ObjectAppearanceCoverViewModel.Factory
    private val vm by viewModels<ObjectAppearanceCoverViewModel> { factory }

    private val adapterAppearance by lazy {
        ObjectAppearanceSettingAdapter(
            onItemClick = { item ->
                vm.onItemClicked(item = item, blockId = block, ctx = ctx)
            },
            onSettingToggleChanged = { _, _ -> }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentObjAppearanceBaseBinding.inflate(inflater, container, false)

        binding.tvScreenTitle.text = getString(R.string.cover)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterAppearance
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                    setDrawable(drawable(R.drawable.divider_object_appearance))
                }
            )
        }

        return binding.root
    }


    override fun onStart() {
        jobs += lifecycleScope.subscribe(vm.state) { state ->
            when (state) {
                ObjectAppearanceCoverViewModel.State.Dismiss -> {
                    dismiss()
                }
                is ObjectAppearanceCoverViewModel.State.Success -> {
                    adapterAppearance.submitList(state.items)
                }
                is ObjectAppearanceCoverViewModel.State.Error -> {
                    toast(state.msg)
                    dismiss()
                }
            }
        }
        super.onStart()
        vm.onStart(block)
    }

    override fun onStop() {
        vm.onStop()
        super.onStop()
    }

    override fun injectDependencies() {
        componentManager().objectAppearanceCoverComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectAppearanceCoverComponent.release(ctx)
    }

    private val ctx: String get() = argString(CONTEXT_ID_KEY)
    private val block: String get() = argString(BLOCK_ID_KEY)

    companion object {
        fun new(ctx: Id, block: Id) = ObjectAppearanceCoverFragment().apply {
            arguments = bundleOf(CONTEXT_ID_KEY to ctx, BLOCK_ID_KEY to block)
        }

        const val CONTEXT_ID_KEY = "arg.object-appearance-cover.ctx"
        const val BLOCK_ID_KEY = "arg.object-appearance-cover.block"
    }
}
//endregion