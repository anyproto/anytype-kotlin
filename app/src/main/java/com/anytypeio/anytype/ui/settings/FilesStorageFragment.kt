package com.anytypeio.anytype.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.anytypeio.anytype.core_ui.common.ComposeDialogView
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.settings.FilesStorageViewModel
import com.anytypeio.anytype.presentation.settings.FilesStorageViewModel.Event
import com.anytypeio.anytype.ui.dashboard.ClearCacheAlertFragment
import com.anytypeio.anytype.ui_settings.fstorage.FilesStorageScreen
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import javax.inject.Inject
import kotlinx.coroutines.launch

class FilesStorageFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: FilesStorageViewModel.Factory

    private val vm by viewModels<FilesStorageViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeDialogView(
            context = requireContext(),
            dialog = requireDialog()
        ).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    FilesStorageScreen(
                        data = vm.state.collectAsStateWithLifecycle().value,
                        onOffloadFilesClicked = { throttle { vm.event(Event.OnOffloadFilesClicked) } }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val offsetFromTop = PADDING_TOP
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            isFitToContents = false
            expandedOffset = offsetFromTop
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.commands.collect { command -> processCommands(command) }
            }
        }
    }

    private fun processCommands(command: FilesStorageViewModel.Command) {
        when (command) {
            FilesStorageViewModel.Command.OpenOffloadFilesScreen -> {
                val dialog = ClearCacheAlertFragment.new()
                dialog.onClearAccepted = { vm.onClearFileCacheAccepted() }
                dialog.show(childFragmentManager, null)
            }
            FilesStorageViewModel.Command.OpenManageFilesScreen -> {
                //todo
            }
        }
    }

    override fun injectDependencies() {
        componentManager().filesStorageComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().filesStorageComponent.release()
    }
}

private const val PADDING_TOP = 54
