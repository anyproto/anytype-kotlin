package com.anytypeio.anytype.ui.objects

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.objects.ObjectAppearanceSettingAdapter
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.objects.ObjectAppearanceSettingViewModel
import kotlinx.android.synthetic.main.fragment_object_appearance_setting.*
import javax.inject.Inject

class ObjectAppearanceSettingFragment : BaseBottomSheetFragment() {

    @Inject
    lateinit var factory: ObjectAppearanceSettingViewModel.Factory
    private val vm by viewModels<ObjectAppearanceSettingViewModel> { factory }

    private val ctx: String get() = argString(CONTEXT_ID_KEY)
    private val block: String get() = argString(BLOCK_ID_KEY)
    private val target: String get() = argString(TARGET_ID_KEY)
    private val adapterAppearance by lazy {
        ObjectAppearanceSettingAdapter(onItemClick = vm::onItemClicked)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_object_appearance_setting, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(recyclerView) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterAppearance
        }
    }

    override fun onStart() {
        jobs += lifecycleScope.subscribe(vm.objectPreviewState) { observeState(it) }
        jobs += lifecycleScope.subscribe(vm.commands) { observeCommands(it) }
        super.onStart()
        vm.onStart(
            targetId = target,
            blockId = block
        )
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
                val fr = ObjectAppearanceCoverFragment.new(block = block, ctx = ctx)
                fr.show(parentFragmentManager, null)
            }
            ObjectAppearanceSettingViewModel.Command.IconScreen -> {
                val fr = ObjectAppearanceIconFragment.new(block = block, ctx = ctx)
                fr.show(parentFragmentManager, null)
            }
            ObjectAppearanceSettingViewModel.Command.PreviewLayoutScreen -> {
                val fr = ObjectAppearancePreviewLayoutFragment.new(block = block, ctx = ctx)
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

    companion object {
        fun new(ctx: Id, block: Id, target: Id) = ObjectAppearanceSettingFragment().apply {
            arguments = bundleOf(
                CONTEXT_ID_KEY to ctx,
                BLOCK_ID_KEY to block,
                TARGET_ID_KEY to target
            )
        }

        const val CONTEXT_ID_KEY = "arg.object-appearance-setting.ctx"
        const val BLOCK_ID_KEY = "arg.object-appearance-setting.block"
        const val TARGET_ID_KEY = "arg.object-appearance-setting.target"
    }
}