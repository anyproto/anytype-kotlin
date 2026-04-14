package com.anytypeio.anytype.ui.home

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.home.HomeScreenViewModel
import com.anytypeio.anytype.presentation.home.HomeScreenVmParams
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject
import kotlinx.coroutines.launch
import timber.log.Timber

class WidgetOverlayFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var factory: HomeScreenViewModel.Factory

    private val vm: HomeScreenViewModel by viewModels { factory }

    private val space: String get() = argString(ARG_SPACE_ID)

    override fun getTheme(): Int = R.style.WidgetOverlayBottomSheetTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        val vmParams = HomeScreenVmParams(
            spaceId = SpaceId(space),
            showHomepagePicker = false
        )
        componentManager().widgetOverlayComponent.get(vmParams).inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.skipCollapsed = true
        dialog.behavior.isFitToContents = false
        dialog.behavior.isDraggable = true
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            MaterialTheme {
                WidgetOverlayContent(
                    vm = vm,
                    onBackClicked = { dismiss() },
                    onSpaceSettingsClicked = {
                        vm.onSpaceSettingsClicked(space = SpaceId(space))
                    }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.commands.collect {
                        // TODO(DROID-4318 Task 12): forward to parent navigator
                        Timber.d("WidgetOverlay vm command: $it")
                    }
                }
                launch {
                    vm.navigation.collect {
                        // TODO(DROID-4318 Task 12): forward to parent navigator
                        Timber.d("WidgetOverlay vm navigation: $it")
                    }
                }
                launch { vm.toasts.collect { toast(it) } }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        vm.onStart()
    }

    override fun onStop() {
        vm.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        componentManager().widgetOverlayComponent.release()
        super.onDestroy()
    }

    companion object {
        const val REQUEST_KEY = "widget_overlay_result"
        const val RESULT_OBJECT_ID = "object_id"
        const val RESULT_OBJECT_TYPE = "object_type"
        private const val ARG_SPACE_ID = "space_id"
        private const val TAG = "WidgetOverlayFragment"

        fun show(fm: FragmentManager, spaceId: String) {
            WidgetOverlayFragment().apply {
                arguments = bundleOf(ARG_SPACE_ID to spaceId)
            }.show(fm, TAG)
        }
    }
}

@Composable
private fun WidgetOverlayContent(
    vm: HomeScreenViewModel,
    onBackClicked: () -> Unit,
    onSpaceSettingsClicked: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(rememberNestedScrollInteropConnection())
    ) {
        // TODO (Task 12): thread an onObjectClicked callback so the overlay can
        //  intercept object taps and deliver them via setFragmentResult(REQUEST_KEY, ...)
        //  keyed on RESULT_OBJECT_ID / RESULT_OBJECT_TYPE.
        WidgetsScreen(
            viewModel = vm
        )
        HomeScreenToolbar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding(),
            onBackButtonClicked = onBackClicked,
            onSpaceSettingsClicked = onSpaceSettingsClicked,
        )
    }
}
