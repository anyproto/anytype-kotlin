package com.anytypeio.anytype.ui.sets.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.CreateDataViewViewerViewModel
import com.anytypeio.anytype.presentation.sets.CreateDataViewViewerViewModel.Companion.STATE_COMPLETED
import kotlinx.android.synthetic.main.fragment_create_data_view_viewer.*
import javax.inject.Inject

class CreateDataViewViewerFragment : BaseBottomSheetFragment() {

    val ctx get() = arg<String>(CTX_KEY)
    val target get() = arg<String>(TARGET_KEY)

    @Inject
    lateinit var factory: CreateDataViewViewerViewModel.Factory
    private val vm: CreateDataViewViewerViewModel by viewModels { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_create_data_view_viewer, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(lifecycleScope) {
            subscribe(btnCreateViewer.clicks()) {
                vm.onAddViewer(
                    name = viewerNameInput.text.toString(),
                    ctx = ctx,
                    target = target
                )
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        lifecycleScope.subscribe(vm.state) { state ->
            if (state == STATE_COMPLETED) dismiss()
        }
    }

    override fun injectDependencies() {
        componentManager().createDataViewViewerComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().createDataViewViewerComponent.release(ctx)
    }

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