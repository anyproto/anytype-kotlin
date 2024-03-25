package com.anytypeio.anytype.ui.sets.modals

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.extensions.drawable
import com.anytypeio.anytype.core_ui.features.sets.PickFilterConditionAdapter
import com.anytypeio.anytype.core_ui.layout.DividerVerticalItemDecoration
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.argInt
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentSelectFilterConditionBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.filter.PickFilterConditionViewModel
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.ui.sets.modals.filter.UpdateConditionActionReceiver
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class PickFilterConditionFragment :
    BaseBottomSheetFragment<FragmentSelectFilterConditionBinding>() {

    private val ctx: String get() = arg(CTX_KEY)
    private val mode: Int get() = argInt(ARG_MODE)
    private val type: Viewer.Filter.Type get() = requireArguments().getParcelable(TYPE_KEY)!!
    private val index: Int get() = argInt(INDEX_KEY)

    @Inject
    lateinit var factory: PickFilterConditionViewModel.Factory
    private val vm: PickFilterConditionViewModel by viewModels { factory }

    val isDismissed = MutableSharedFlow<Boolean>(replay = 0)

    override fun onStart() {
        super.onStart()
        with(lifecycleScope) {
            jobs += subscribe(isDismissed) { isDismissed -> if (isDismissed) dismiss() }
            jobs += subscribe(vm.views) { screenState ->
                binding.recycler.adapter = PickFilterConditionAdapter(
                    picked = screenState.picked,
                    conditions = screenState.conditions,
                    click = this@PickFilterConditionFragment::click
                )
                binding.recycler.addItemDecoration(
                    DividerVerticalItemDecoration(
                        divider = requireContext().drawable(R.drawable.divider_relations),
                        isShowInLastItem = false
                    )
                )
            }
        }
        vm.onStart(type, index)
    }

    private fun click(condition: Viewer.Filter.Condition) {
        withParent<UpdateConditionActionReceiver> { update(condition) }
        lifecycleScope.launch { isDismissed.emit(true) }
    }

    override fun injectDependencies() {
        when (mode) {
            MODE_CREATE -> componentManager().pickFilterConditionComponentCreate.get(ctx)
                .inject(this)
            MODE_MODIFY -> componentManager().pickFilterConditionComponentModify.get(ctx)
                .inject(this)
            else -> throw RuntimeException("Wrong mode")
        }
    }

    override fun releaseDependencies() {
        when (mode) {
            MODE_CREATE -> componentManager().pickFilterConditionComponentCreate.release()
            MODE_MODIFY -> componentManager().pickFilterConditionComponentModify.release()
            else -> throw RuntimeException("Wrong mode")
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSelectFilterConditionBinding = FragmentSelectFilterConditionBinding.inflate(
        inflater, container, false
    )

    companion object {

        const val MODE_CREATE = 1
        const val MODE_MODIFY = 2

        private const val CTX_KEY = "arg.create-filter-relation.ctx"
        private const val ARG_MODE = "arg.create-filter-relation.mode"
        private const val TYPE_KEY = "arg.create-filter-relation.type"
        private const val INDEX_KEY = "arg.create-filter-relation.index"

        fun new(
            ctx: Id,
            mode: Int,
            type: Viewer.Filter.Type,
            index: Int
        ) = PickFilterConditionFragment().apply {
            arguments = bundleOf(
                CTX_KEY to ctx,
                ARG_MODE to mode,
                TYPE_KEY to type,
                INDEX_KEY to index
            )
        }
    }
}