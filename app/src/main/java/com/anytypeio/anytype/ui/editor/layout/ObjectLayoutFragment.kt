package com.anytypeio.anytype.ui.editor.layout

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.common.FooterAdapter
import com.anytypeio.anytype.core_ui.features.objects.ObjectLayoutAdapter
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.drawable
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.editor.layout.ObjectLayoutViewModel
import com.anytypeio.anytype.presentation.objects.ObjectLayoutView
import kotlinx.android.synthetic.main.fragment_object_layout.*
import javax.inject.Inject

class ObjectLayoutFragment : BaseBottomSheetFragment() {

    private val ctx: String get() = argString(CONTEXT_ID_KEY)

    @Inject
    lateinit var factory: ObjectLayoutViewModel.Factory
    private val vm by viewModels<ObjectLayoutViewModel> { factory }

    var onDismissListener: (() -> Unit)? = null

    private val adapterLayouts by lazy {
        ObjectLayoutAdapter(
            onItemClick = { view -> vm.onLayoutClicked(ctx, view) }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_object_layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(rvLayouts) {
            layoutManager = LinearLayoutManager(context)
            adapter = adapterLayouts
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                    setDrawable(drawable(R.drawable.divider_layouts))
                }
            )
        }
        vm.onStart(ctx)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke()
    }

    override fun onStart() {
        with(lifecycleScope) {
            jobs += subscribe(vm.views) { observeState(it) }
        }
        super.onStart()
    }

    private fun observeState(views: List<ObjectLayoutView>) {
        adapterLayouts.update(views)
    }

    override fun injectDependencies() {
        componentManager().objectLayoutComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectLayoutComponent.release(ctx)
    }

    companion object {
        fun new(ctx: Id) : ObjectLayoutFragment = ObjectLayoutFragment().apply {
            arguments = bundleOf(
                CONTEXT_ID_KEY to ctx
            )
        }
        const val CONTEXT_ID_KEY = "arg.object-layout.ctx"
    }
}