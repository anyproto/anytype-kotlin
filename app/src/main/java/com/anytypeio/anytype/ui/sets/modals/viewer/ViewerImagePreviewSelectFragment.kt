package com.anytypeio.anytype.ui.sets.modals.viewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.features.sets.viewer.ViewerCoverAdapter
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.viewer.ViewerImagePreviewSelectViewModel
import kotlinx.android.synthetic.main.fragment_viewer_image_preview_select.*
import javax.inject.Inject

class ViewerImagePreviewSelectFragment : BaseBottomSheetFragment() {

    @Inject
    lateinit var factory: ViewerImagePreviewSelectViewModel.Factory
    private val vm: ViewerImagePreviewSelectViewModel by viewModels { factory }

    private val ctx get() = arg<String>(CTX_KEY)

    private val viewerCoverAdapter by lazy {
        ViewerCoverAdapter { item ->
            vm.onViewerCoverItemClicked(ctx, item)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_viewer_image_preview_select, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewrCoverRecycler.apply {
            adapter = viewerCoverAdapter
            layoutManager = LinearLayoutManager(context)
        }

        with(lifecycleScope) {
            subscribe(vm.views) { viewerCoverAdapter.update(it) }
            subscribe(vm.isDismissed) { isDismissed ->
                if (isDismissed) dismiss()
            }
        }
    }

    override fun injectDependencies() {
        componentManager().viewerImagePreviewSelectComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().viewerImagePreviewSelectComponent.release(ctx)
    }

    companion object {
        const val CTX_KEY = "arg.viewer-cover-select.ctx"
    }
}