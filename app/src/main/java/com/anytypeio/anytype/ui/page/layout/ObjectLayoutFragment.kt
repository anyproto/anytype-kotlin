package com.anytypeio.anytype.ui.page.layout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Layout
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.page.layout.ObjectLayoutViewModel
import kotlinx.android.synthetic.main.fragment_object_layout.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class ObjectLayoutFragment : BaseBottomSheetFragment() {

    private val ctx: String get() = argString(CONTEXT_ID_KEY)

    @Inject
    lateinit var factory: ObjectLayoutViewModel.Factory
    private val vm by viewModels<ObjectLayoutViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_object_layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            subscribe(vm.layout) { layout ->
                when(layout) {
                    Layout.BASIC -> {
                        basic.isSelected = true
                        profile.isSelected = false
                        todo.isSelected = false
                    }
                    Layout.PROFILE -> {
                        basic.isSelected = false
                        profile.isSelected = true
                        todo.isSelected = false
                    }
                    Layout.TODO -> {
                        basic.isSelected = false
                        profile.isSelected = false
                        todo.isSelected = true
                    }
                }
            }
            subscribe(profile.clicks()) {
                vm.onLayouClicked(
                    ctx = ctx,
                    layout = Layout.PROFILE
                )
            }
            subscribe(basic.clicks()) {
                vm.onLayouClicked(
                    ctx = ctx,
                    layout = Layout.BASIC
                )
            }
            subscribe(todo.clicks()) {
                vm.onLayouClicked(
                    ctx = ctx,
                    layout = Layout.TODO
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        vm.onStart(ctx)
    }

    override fun onStop() {
        super.onStop()
        vm.onStop()
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