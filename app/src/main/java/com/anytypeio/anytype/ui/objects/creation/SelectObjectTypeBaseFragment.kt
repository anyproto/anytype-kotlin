package com.anytypeio.anytype.ui.objects.creation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.misc.OpenObjectNavigation
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ext.clipboard
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.withParentSafe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.objects.ClipboardToolbarViewState
import com.anytypeio.anytype.presentation.objects.Command
import com.anytypeio.anytype.presentation.objects.SelectObjectTypeViewModel
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.objects.types.pickers.ObjectTypeSelectionListener
import com.anytypeio.anytype.ui.objects.types.pickers.WidgetSourceTypeListener
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * “A fragment that allows you to select an object type as the source in a new widget.”
 */

class WidgetSourceTypeFragment : SelectObjectTypeBaseFragment() {

    private val widget get() = arg<Id>(WIDGET_ID_ARG)

    override fun proceedWithSelectedType(objType: ObjectWrapper.Type) {
        withParentSafe<WidgetSourceTypeListener> {
            onSetNewWidgetSource(objType = objType, widgetId = widget)
        }
    }

    override fun resolveScreenTitle(): String {
        return getString(R.string.create_object)
    }

    companion object {
        private const val WIDGET_ID_ARG = "arg.select-source-type.widget-id"
        fun new(space: Id, widgetId: Id) = WidgetSourceTypeFragment().apply {
            arguments = bundleOf(
                SPACE_ID_KEY to space,
                WIDGET_ID_ARG to widgetId
            )
        }
    }
}

/**
 * “A fragment that allows you to select an object type for a new object.”
 */
class ObjectTypeSelectionFragment : SelectObjectTypeBaseFragment() {

    override fun proceedWithSelectedType(objType: ObjectWrapper.Type) {
        withParentSafe<ObjectTypeSelectionListener> {
            onSelectObjectType(objType = objType)
        }
    }

    override fun resolveScreenTitle(): String {
        return getString(R.string.create_object)
    }

    companion object {
        fun new(space: Id) = ObjectTypeSelectionFragment().apply {
            arguments = bundleOf(SPACE_ID_KEY to space)
        }
    }
}

/**
 * A base class for fragments that allow you to select an object type.
 */
abstract class SelectObjectTypeBaseFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: SelectObjectTypeViewModel.Factory

    private val excludedTypeKeys get() = argOrNull<List<Key>>(EXCLUDED_TYPE_KEYS_ARG_KEY)

    private val space get() = arg<Id>(SPACE_ID_KEY)

    private val vm by viewModels<SelectObjectTypeViewModel> { factory }

    abstract fun proceedWithSelectedType(objType: ObjectWrapper.Type)
    abstract fun resolveScreenTitle(): String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(
                typography = typography,
                shapes = MaterialTheme.shapes.copy(medium = RoundedCornerShape(16.dp)),
                colors = MaterialTheme.colors.copy(
                    surface = colorResource(id = R.color.context_menu_background)
                )
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
                    onMoveLeftClicked = {
                        lifecycleScope.launch {
                            // Workaround to prevent dropdown-menu flickering
                            delay(DROP_DOWN_MENU_ACTION_DELAY)
                            vm.onMoveLeftClicked(it)
                        }
                    },
                    onMoveRightClicked = {
                        lifecycleScope.launch {
                            // Workaround to prevent dropdown-menu flickering
                            delay(DROP_DOWN_MENU_ACTION_DELAY)
                            vm.onMoveRightClicked(it)
                        }
                    },
                    onQueryChanged = vm::onQueryChanged,
                    onFocused = {
                        skipCollapsed()
                        expand()
                    },
                    title = resolveScreenTitle()
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
                                EditorFragment.args(
                                    ctx = nav.target,
                                    space = nav.space
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
        runCatching {
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
        }.onFailure {
            Timber.e(it, "Error while processing clipboard")
        }
        vm.onResume()
    }

    private fun proceedWithCommand(command: Command) {
        when (command) {
            is Command.DispatchObjectType -> {
                proceedWithSelectedType(objType = command.type)
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
                params = SelectObjectTypeViewModel.Params(
                    excludedTypeKeys = excludedTypeKeys?.map { TypeKey(it) } ?: emptyList(),
                    space = SpaceId(space)
                )
            )
            .inject(this)
    }

    override fun releaseDependencies() {
        componentManager().selectObjectTypeComponent.release()
    }

    companion object {
        const val SPACE_ID_KEY = "arg.select-object-type.space-id"
        const val EXCLUDED_TYPE_KEYS_ARG_KEY = "arg.select-object-type.excluded-type-keys"
        private const val DROP_DOWN_MENU_ACTION_DELAY = 100L
    }
}