package com.anytypeio.anytype.ui.sets.modals

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseDialogFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.DataViewViewerActionViewModel
import kotlinx.android.synthetic.main.fragment_data_view_viewer_actions.*
import javax.inject.Inject

@Deprecated("Legacy")
class DataViewViewerActionFragment : BaseDialogFragment() {

    private val ctx: String get() = arg(CTX_KEY)
    private val viewer: String get() = arg(VIEWER_KEY)
    private val title: String get() = arg(TITLE_KEY)

    @Inject
    lateinit var factory: DataViewViewerActionViewModel.Factory

    private val vm: DataViewViewerActionViewModel by viewModels { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_data_view_viewer_actions, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvTitle.text = title
        with(lifecycleScope) {
            subscribe(duplicateViewContainer.clicks()) {
                vm.onDuplicateClicked(ctx = ctx, viewer = viewer)
            }
            subscribe(deleteViewContainer.clicks()) {
                vm.onDeleteClicked(ctx = ctx, viewer = viewer)
            }
            subscribe(editViewContainer.clicks()) {
                val fr = EditDataViewViewerFragment.new(
                    ctx = ctx,
                    viewer = viewer
                )
                fr.show(parentFragmentManager, null)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(lifecycleScope) {
            subscribe(vm.isDismissed) { isDismissed -> if (isDismissed) dismiss() }
        }
    }

    override fun onStart() {
        super.onStart()
        setupAppearance()
    }

    override fun injectDependencies() {
        componentManager().dataviewViewerActionComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().dataviewViewerActionComponent.release(ctx)
    }

    private fun setupAppearance() {
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.BOTTOM)
            setBackgroundDrawableResource(android.R.color.transparent)
            setWindowAnimations(R.style.DefaultBottomDialogAnimation)
        }
    }

    companion object {
        fun new(ctx: Id, title: String, viewer: Id) = DataViewViewerActionFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx, TITLE_KEY to title, VIEWER_KEY to viewer)
        }

        const val CTX_KEY = "arg.dialog.viewer-action.ctx"
        const val VIEWER_KEY = "arg.dialog.viewer-action.viewer"
        const val TITLE_KEY = "arg.dialog.viewer-action.title"
    }
}