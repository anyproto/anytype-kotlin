package com.anytypeio.anytype.ui.objects.appearance

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
import com.anytypeio.anytype.core_ui.features.objects.appearance.ObjectAppearanceSettingAdapter
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.drawable
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentObjectAppearanceSettingBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.objects.appearance.ObjectAppearanceMainSettingsView
import com.anytypeio.anytype.presentation.objects.appearance.ObjectAppearanceSettingViewModel
import com.anytypeio.anytype.ui.objects.appearance.choose.ObjectAppearanceChooseCoverFragment
import com.anytypeio.anytype.ui.objects.appearance.choose.ObjectAppearanceChooseIconFragment
import com.anytypeio.anytype.ui.objects.appearance.choose.ObjectAppearanceChoosePreviewLayoutFragment
import javax.inject.Inject

class ObjectAppearanceSettingFragment :
    BaseBottomSheetFragment<FragmentObjectAppearanceSettingBinding>() {

    @Inject
    lateinit var factory: ObjectAppearanceSettingViewModel.Factory
    private val vm by viewModels<ObjectAppearanceSettingViewModel> { factory }

    private val ctx: String get() = argString(CONTEXT_ID_KEY)
    private val block: String get() = argString(BLOCK_ID_KEY)
    private val adapterAppearance by lazy {
        ObjectAppearanceSettingAdapter(
            onItemClick = vm::onItemClicked,
            onSettingToggleChanged = { item, isChecked ->
                when (item) {
                    is ObjectAppearanceMainSettingsView.Relation.Description -> {
                        vm.onToggleClicked(
                            description = item,
                            blockId = block,
                            ctx = ctx,
                            isChecked = isChecked
                        )
                    }
                }
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.recyclerView) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterAppearance
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                    setDrawable(drawable(R.drawable.divider_relations))
                }
            )
        }
    }

    override fun onStart() {
        jobs += lifecycleScope.subscribe(vm.objectPreviewState) { observeState(it) }
        jobs += lifecycleScope.subscribe(vm.commands) { observeCommands(it) }
        super.onStart()
        vm.onStart(blockId = block)
    }

    private fun observeState(state: ObjectAppearanceSettingViewModel.State) {
        when (state) {
            is ObjectAppearanceSettingViewModel.State.Error -> {
                activity?.toast(state.msg)
            }
            is ObjectAppearanceSettingViewModel.State.Success -> {
                adapterAppearance.submitList(state.data)
            }
        }
    }

    private fun observeCommands(command: ObjectAppearanceSettingViewModel.Command) {
        when (command) {
            ObjectAppearanceSettingViewModel.Command.CoverScreen -> {
                val fr = ObjectAppearanceChooseCoverFragment.new(block = block, ctx = ctx)
                fr.show(parentFragmentManager, null)
            }
            ObjectAppearanceSettingViewModel.Command.IconScreen -> {
                val fr = ObjectAppearanceChooseIconFragment.new(block = block, ctx = ctx)
                fr.show(parentFragmentManager, null)
            }
            ObjectAppearanceSettingViewModel.Command.PreviewLayoutScreen -> {
                val fr = ObjectAppearanceChoosePreviewLayoutFragment.new(block = block, ctx = ctx)
                fr.show(parentFragmentManager, null)
            }
        }
    }

    override fun injectDependencies() {
        componentManager().objectAppearanceSettingComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectAppearanceSettingComponent.release(ctx)
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentObjectAppearanceSettingBinding = FragmentObjectAppearanceSettingBinding.inflate(
        inflater, container, false
    )

    companion object {
        fun new(ctx: Id, block: Id) = ObjectAppearanceSettingFragment().apply {
            arguments = bundleOf(
                CONTEXT_ID_KEY to ctx,
                BLOCK_ID_KEY to block
            )
        }

        const val CONTEXT_ID_KEY = "arg.object-appearance-setting.ctx"
        const val BLOCK_ID_KEY = "arg.object-appearance-setting.block"
    }
}