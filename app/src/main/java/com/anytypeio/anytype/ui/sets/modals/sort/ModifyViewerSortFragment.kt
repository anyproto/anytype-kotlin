package com.anytypeio.anytype.ui.sets.modals.sort

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.extensions.text
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.sort.ModifyViewerSortViewModel
import kotlinx.android.synthetic.main.fragment_modify_viewer_sort.*
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

class ModifyViewerSortFragment : BaseBottomSheetFragment() {

    private val ctx: String get() = arg(CTX_KEY)
    private val relation: String get() = arg(RELATION_KEY)

    @Inject
    lateinit var factory: ModifyViewerSortViewModel.Factory

    private val vm: ModifyViewerSortViewModel by viewModels { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_modify_viewer_sort, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(lifecycleScope) {
            subscribe(tvSortAsc.clicks()) { vm.onSortAscSelected(ctx, relation) }
            subscribe(tvSortDesc.clicks()) { vm.onSortDescSelected(ctx, relation) }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(lifecycleScope) {
            subscribe(vm.isDismissed) { isDismissed -> if (isDismissed) dismiss() }
            subscribe(vm.viewState.filterNotNull()) { state ->
                tvSortAsc.setText(DVSortType.ASC.text(state.format))
                tvSortDesc.setText(DVSortType.DESC.text(state.format))
                txtName.text = state.name
                when (state.type) {
                    Block.Content.DataView.Sort.Type.ASC -> {
                        ivAscSelected.visible()
                        ivDescSelected.invisible()
                    }
                    Block.Content.DataView.Sort.Type.DESC -> {
                        ivAscSelected.invisible()
                        ivDescSelected.visible()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        vm.onStart(relation)
    }

    override fun onStop() {
        super.onStop()
        vm.onStop()
    }

    override fun injectDependencies() {
        componentManager().modifyViewerSortComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().modifyViewerSortComponent.release(ctx)
    }

    companion object {
        fun new(ctx: Id, relation: Id): ModifyViewerSortFragment = ModifyViewerSortFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx, RELATION_KEY to relation)
        }

        private const val CTX_KEY = "arg.modify-viewer-sort.ctx"
        private const val RELATION_KEY = "arg.modify-viewer-sort.relation"
    }
}