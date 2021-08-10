package com.anytypeio.anytype.ui.editor.layout

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.editor.layout.ObjectLayoutViewModel
import kotlinx.android.synthetic.main.fragment_object_layout.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class ObjectLayoutFragment : BaseBottomSheetFragment() {

    private val ctx: String get() = argString(CONTEXT_ID_KEY)

    @Inject
    lateinit var factory: ObjectLayoutViewModel.Factory
    private val vm by viewModels<ObjectLayoutViewModel> { factory }

    var onDismissListener: (() -> Unit)? = null

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
                    ObjectType.Layout.BASIC -> {
                        basic.isSelected = true
                        profile.isSelected = false
                        todo.isSelected = false
                    }
                    ObjectType.Layout.PROFILE -> {
                        basic.isSelected = false
                        profile.isSelected = true
                        todo.isSelected = false
                    }
                    ObjectType.Layout.TODO -> {
                        basic.isSelected = false
                        profile.isSelected = false
                        todo.isSelected = true
                    }
                    else -> toast("Unexpected layout: $layout")
                }
            }
            subscribe(profile.clicks()) {
                vm.onLayoutClicked(
                    ctx = ctx,
                    layout = ObjectType.Layout.PROFILE
                )
            }
            subscribe(basic.clicks()) {
                vm.onLayoutClicked(
                    ctx = ctx,
                    layout = ObjectType.Layout.BASIC
                )
            }
            subscribe(todo.clicks()) {
                vm.onLayoutClicked(
                    ctx = ctx,
                    layout = ObjectType.Layout.TODO
                )
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke()
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