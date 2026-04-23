package com.anytypeio.anytype.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.anytypeio.anytype.core_models.FileDownloadLimit
import com.anytypeio.anytype.core_ui.common.ComposeDialogView
import com.anytypeio.anytype.core_utils.ext.setupBottomSheetBehavior
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.core_utils.ui.proceed
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.settings.FilesStorageViewModel
import com.anytypeio.anytype.presentation.settings.FilesStorageViewModel.Command
import com.anytypeio.anytype.presentation.settings.FilesStorageViewModel.Event
import com.anytypeio.anytype.ui.auth.account.DeleteAccountWarning
import com.anytypeio.anytype.ui.dashboard.ClearCacheAlertFragment
import com.anytypeio.anytype.ui_settings.fstorage.LocalStorageScreen
import com.anytypeio.anytype.ui_settings.fstorage.OfflineDownloadsSelectorSheet
import javax.inject.Inject
import kotlinx.coroutines.launch

class FilesStorageFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: FilesStorageViewModel.Factory

    private val vm by viewModels<FilesStorageViewModel> { factory }

    // Compose-observable state for the selector sheet visibility.
    // Set by processCommands when the ViewModel emits ShowOfflineDownloadsSelector;
    // cleared by the sheet itself on dismiss.
    private val selectorCurrent = mutableStateOf<FileDownloadLimit?>(null)

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
                    val downloadLimit by vm.downloadLimit.collectAsStateWithLifecycle()
                    val useCellular by vm.useCellular.collectAsStateWithLifecycle()

                    LocalStorageScreen(
                        data = vm.state.collectAsStateWithLifecycle().value,
                        downloadLimit = downloadLimit,
                        useCellular = useCellular,
                        onOffloadFilesClicked = { throttle { vm.event(Event.OnOffloadFilesClicked) } },
                        onDeleteAccountClicked = { proceedWithAccountDeletion() },
                        onOfflineDownloadsClicked = {
                            throttle { vm.event(Event.OnOfflineDownloadsClicked) }
                        },
                        onUseCellularToggled = { checked ->
                            vm.onUseCellularToggled(checked)
                        }
                    )

                    selectorCurrent.value?.let { current ->
                        OfflineDownloadsSelectorSheet(
                            current = current,
                            onValueSelected = { vm.onOfflineDownloadsValueSelected(it) },
                            onDismiss = { selectorCurrent.value = null }
                        )
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheetBehavior(PADDING_TOP)
        collectCommands()
    }

    override fun onStart() {
        super.onStart()
        proceed(vm.toasts) { toast(it) }
        vm.onStart()
    }

    override fun onStop() {
        vm.onStop()
        super.onStop()
    }

    private fun collectCommands() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.commands.collect { command -> processCommands(command) }
            }
        }
    }

    private fun processCommands(command: FilesStorageViewModel.Command) {
        when (command) {
            FilesStorageViewModel.Command.OpenOffloadFilesScreen -> showClearCacheDialog()
            is Command.ShowOfflineDownloadsSelector -> {
                selectorCurrent.value = command.current
            }
        }
    }

    private fun showClearCacheDialog() {
        val dialog = ClearCacheAlertFragment.new()
        dialog.onClearAccepted = { vm.onClearFileCacheAccepted() }
        dialog.show(childFragmentManager, null)
    }

    private fun proceedWithAccountDeletion() {
        vm.proceedWithAccountDeletion()
        val dialog = DeleteAccountWarning()
        dialog.onDeletionAccepted = {
            dialog.dismiss()
            vm.onDeleteAccountClicked()
        }
        dialog.show(childFragmentManager, null)
    }

    override fun injectDependencies() {
        componentManager().filesStorageComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().filesStorageComponent.release()
    }

    companion object {
        fun args(isRemote: Boolean) : Bundle = Bundle(
            bundleOf(ARG_STORAGE_TYPE to isRemote)
        )
        private const val ARG_STORAGE_TYPE = "arg.storage.type.is-remote"
    }
}

private const val PADDING_TOP = 54
