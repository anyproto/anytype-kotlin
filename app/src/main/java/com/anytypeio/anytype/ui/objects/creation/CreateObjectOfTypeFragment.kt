package com.anytypeio.anytype.ui.objects.creation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.objects.Command
import com.anytypeio.anytype.presentation.objects.CreateObjectOfTypeViewModel
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject

class CreateObjectOfTypeFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: CreateObjectOfTypeViewModel.Factory

    private val excludedTypeKeys get() = argOrNull<List<Key>>(EXCLUDED_TYPE_KEYS_ARG_KEY)

    private val vm by viewModels<CreateObjectOfTypeViewModel> { factory }

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
                CreateObjectOfTypeScreen(
                    state = vm.viewState.collectAsStateWithLifecycle().value,
                    onTypeClicked = vm::onTypeClicked,
                    onQueryChanged = vm::onQueryChanged,
                    onFocused = {
                        skipCollapsed()
                        expand()
                    }
                )
            }
            LaunchedEffect(Unit) {
                vm.commands.collect { command ->
                    proceedWithCommand(command)
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
            .createObjectOfTypeComponent.get(
                params = excludedTypeKeys?.map { TypeKey(it) } ?: emptyList()
            )
            .inject(this)
    }

    override fun releaseDependencies() {
        componentManager().createObjectOfTypeComponent.release()
    }

    companion object {
        const val EXCLUDED_TYPE_KEYS_ARG_KEY = "arg.create-object-of-type.excluded-type-keys"

        fun newInstance(
            excludedTypeKeys: List<Key>,
            onTypeSelected: (ObjectWrapper.Type) -> Unit
        ): CreateObjectOfTypeFragment = CreateObjectOfTypeFragment().apply {
            this.onTypeSelected = onTypeSelected
            arguments =
                bundleOf(
                    EXCLUDED_TYPE_KEYS_ARG_KEY to excludedTypeKeys
                )
        }
    }
}