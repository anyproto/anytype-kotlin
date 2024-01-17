package com.anytypeio.anytype.ui.objects.creation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ext.clipboard
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.objects.ClipboardToolbarViewState
import com.anytypeio.anytype.presentation.objects.Command
import com.anytypeio.anytype.presentation.objects.SelectObjectTypeViewModel
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SelectObjectTypeFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: SelectObjectTypeViewModel.Factory

    private val excludedTypeKeys get() = argOrNull<List<Key>>(EXCLUDED_TYPE_KEYS_ARG_KEY)

    private val vm by viewModels<SelectObjectTypeViewModel> { factory }

    lateinit var onTypeSelected: (ObjectWrapper.Type) -> Unit

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(
                typography = typography
            ) {
                SelectObjectTypeScreen(
                    state = vm.viewState.collectAsStateWithLifecycle().value,
                    onTypeClicked = vm::onTypeClicked,
                    onPinOnTopClicked = {
                        lifecycleScope.launch {
                            // Workaround to prevent dropdown-menu flickering
                            delay(DROP_DOWN_MENU_ACTION_DELAY)
                            vm.onPinTypeClicked(it)
                        }
                    },
                    onUnpinTypeClicked = {
                        lifecycleScope.launch {
                            // Workaround to prevent dropdown-menu flickering
                            delay(DROP_DOWN_MENU_ACTION_DELAY)
                            vm.onUnpinTypeClicked(it)
                        }
                    },
                    onSetDefaultTypeClicked = {
                        lifecycleScope.launch {
                            // Workaround to prevent dropdown-menu flickering
                            delay(DROP_DOWN_MENU_ACTION_DELAY)
                            vm.onSetDefaultObjectTypeClicked(it)
                        }
                    },
                    onQueryChanged = vm::onQueryChanged,
                    onFocused = {
                        skipCollapsed()
                        expand()
                    }
                )
                when(vm.clipboardToolbarViewState.collectAsStateWithLifecycle().value) {
                    is ClipboardToolbarViewState.CreateBookmark -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            ClipboardBottomToolbar(
                                type = CLIPBOARD_TYPE_BOOKMARK,
                                modifier = Modifier.align(Alignment.BottomCenter),
                                onToolbarClicked = vm::onClipboardToolbarClicked
                            )
                        }
                    }
                    is ClipboardToolbarViewState.CreateObject -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            ClipboardBottomToolbar(
                                type = CLIPBOARD_TYPE_OBJECT,
                                modifier = Modifier.align(Alignment.BottomCenter),
                                onToolbarClicked = vm::onClipboardToolbarClicked
                            )
                        }
                    }
                    ClipboardToolbarViewState.Hidden -> {
                        // Draw nothing.
                    }
                }
            }
            LaunchedEffect(Unit) {
                vm.commands.collect { command ->
                    proceedWithCommand(command)
                }
            }
            LaunchedEffect(Unit) {
                vm.toasts.collect { toast ->
                    toast(toast)
                }
            }
            LaunchedEffect(Unit) {
                vm.navigation.collect { nav ->
                    when(nav) {
                        is OpenObjectNavigation.OpenEditor -> {
                            dismiss()
                            findNavController().navigate(
                                R.id.objectNavigation,
                                bundleOf(
                                    EditorFragment.ID_KEY to nav.target
                                )
                            )
                        }
                        else -> {
                            // Do nothing.
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        with(clipboard()) {
            val clip = primaryClip
            if (hasPrimaryClip() && clip != null) {
                if (clip.itemCount == 1) {
                    val item =clip.getItemAt(0)
                    val text = item.text.toString()
                    if (URLUtil.isValidUrl(text))
                        vm.onClipboardUrlTypeDetected(text)
                    else
                        vm.onClipboardTextTypeDetected(text)
                }
            }
        }
    }

    private fun proceedWithCommand(command: Command) {
        when (command) {
            is Command.DispatchObjectType -> {
                onTypeSelected(command.type)
                dismiss()
            }
            is Command.ShowTypeInstalledToast -> {
                toast(resources.getString(R.string.library_type_added, command.typeName))
            }
        }
    }

    override fun injectDependencies() {
        componentManager()
            .selectObjectTypeComponent.get(
                params = excludedTypeKeys?.map { TypeKey(it) } ?: emptyList()
            )
            .inject(this)
    }

    override fun releaseDependencies() {
        componentManager().selectObjectTypeComponent.release()
    }

    companion object {
        const val EXCLUDED_TYPE_KEYS_ARG_KEY = "arg.create-object-of-type.excluded-type-keys"
        const val DROP_DOWN_MENU_ACTION_DELAY = 100L

        fun newInstance(
            excludedTypeKeys: List<Key>,
            onTypeSelected: (ObjectWrapper.Type) -> Unit
        ): SelectObjectTypeFragment = SelectObjectTypeFragment().apply {
            this.onTypeSelected = onTypeSelected
            arguments =
                bundleOf(
                    EXCLUDED_TYPE_KEYS_ARG_KEY to excludedTypeKeys
                )
        }
    }
}