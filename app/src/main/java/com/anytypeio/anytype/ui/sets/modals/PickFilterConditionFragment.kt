package com.anytypeio.anytype.ui.sets.modals

import android.app.Dialog
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
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
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.filter.PickFilterConditionViewModel
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.ui.sets.modals.filter.UpdateConditionActionReceiver
import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class PickFilterConditionFragment : DialogFragment() {

    private val ctx: String get() = arg(CTX_KEY)
    private val mode: Int get() = argInt(ARG_MODE)
    private val type: Viewer.Filter.Type get() = requireArguments().getParcelable(TYPE_KEY)!!
    private val index: Int get() = argInt(INDEX_KEY)

    @Inject
    lateinit var factory: PickFilterConditionViewModel.Factory
    private val vm: PickFilterConditionViewModel by viewModels { factory }

    val isDismissed = MutableSharedFlow<Boolean>(replay = 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(lifecycleScope) {
            subscribe(isDismissed) { isDismissed -> if (isDismissed) dismiss() }
            subscribe(vm.views) { screenState ->
                recycler.adapter = PickFilterConditionAdapter(
                    picked = screenState.picked,
                    conditions = screenState.conditions,
                    click = this@PickFilterConditionFragment::click
                )
                recycler.addItemDecoration(
                    DividerVerticalItemDecoration(
                        divider = requireContext().drawable(R.drawable.divider_relations),
                        isShowInLastItem = false
                    )
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        setDialogWidthAndGravity()
        vm.onStart(type, index)
    }

    private fun click(condition: Viewer.Filter.Condition) {
        withParent<UpdateConditionActionReceiver> { update(condition) }
        lifecycleScope.launch { isDismissed.emit(true) }
    }

    private fun setDialogWidthAndGravity() {
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.BOTTOM)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onDestroyView() {
        releaseDependencies()
        super.onDestroyView()
    }

    fun injectDependencies() {
        when (mode) {
            MODE_CREATE -> componentManager().pickFilterConditionComponentCreate.get(ctx)
                .inject(this)
            MODE_MODIFY -> componentManager().pickFilterConditionComponentModify.get(ctx)
                .inject(this)
            else -> throw RuntimeException("Wrong mode")
        }
    }

    fun releaseDependencies() {
        when (mode) {
            MODE_CREATE -> componentManager().pickFilterConditionComponentCreate.release(ctx)
            MODE_MODIFY -> componentManager().pickFilterConditionComponentModify.release(ctx)
            else -> throw RuntimeException("Wrong mode")
        }
    }


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