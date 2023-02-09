package com.anytypeio.anytype.ui.widgets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.widgets.SelectWidgetTypeViewModel
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject
import kotlinx.coroutines.launch

class SelectWidgetTypeFragment : BaseBottomSheetComposeFragment() {

    private val ctx: Id get() = argString(CTX_KEY)
    private val widget: Id get() = argString(WIDGET_ID_KEY)
    private val source: Id get() = argString(WIDGET_SOURCE_KEY)
    private val currentType: Int get() = arg(WIDGET_TYPE_KEY)

    private val vm by viewModels<SelectWidgetTypeViewModel> { factory }

    @Inject
    lateinit var factory: SelectWidgetTypeViewModel.Factory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(typography = typography) {
                SelectWidgetTypeScreen(
                    views = vm.views.collectAsState().value,
                    onViewClicked = { view ->
                        vm.onWidgetTypeClicked(
                            ctx = ctx,
                            view = view,
                            widget = widget,
                            source = source
                        )
                    }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.isDismissed.collect { isDismissed ->
                    if (isDismissed) dismiss()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        vm.onStart(currentType = currentType)
    }

    override fun injectDependencies() {
        componentManager().selectWidgetTypeSubcomponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().selectWidgetTypeSubcomponent.release()
    }

    companion object {
        private const val CTX_KEY = "arg.select-widget-type.ctx"
        private const val WIDGET_ID_KEY = "arg.select-widget-type.widget-id"
        private const val WIDGET_TYPE_KEY = "arg.select-widget-type.widget-type"
        private const val WIDGET_SOURCE_KEY = "arg.select-widget-type.widget-source"

        fun args(
            ctx: Id,
            widget: Id,
            source: Id,
            type: Int
        ) = bundleOf(
            CTX_KEY to ctx,
            WIDGET_ID_KEY to widget,
            WIDGET_SOURCE_KEY to source,
            WIDGET_TYPE_KEY to type
        )
    }
}