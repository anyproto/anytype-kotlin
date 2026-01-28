package com.anytypeio.anytype.ui.objects

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.presentation.objects.ObjectTypeChangeViewModel
import com.anytypeio.anytype.presentation.objects.ObjectTypeChangeViewModel.Command
import com.anytypeio.anytype.presentation.objects.ObjectTypeChangeViewModelFactory
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject

abstract class BaseObjectTypeChangeFragment : BaseBottomSheetComposeFragment() {

    abstract fun resolveTitle(): String
    abstract fun onItemClicked(item: ObjectWrapper.Type)

    @Inject
    lateinit var factory: ObjectTypeChangeViewModelFactory
    protected val vm by viewModels<ObjectTypeChangeViewModel> { factory }

    protected val space: Id
        get() = argString(ARG_SPACE)
    protected val excludeTypes: List<Id>
        get() = argOrNull<List<Id>>(ARG_EXCLUDE_TYPES) ?: emptyList()
    protected val selectedTypes: List<Id>
        get() = argOrNull<List<Id>>(ARG_SELECTED_TYPES) ?: emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        MaterialTheme(
            typography = typography,
            shapes = MaterialTheme.shapes.copy(medium = RoundedCornerShape(16.dp)),
            colors = MaterialTheme.colors.copy(
                surface = colorResource(id = R.color.context_menu_background)
            )
        ) {
            ObjectTypeChangeScreen(
                title = resolveTitle(),
                state = vm.viewState.collectAsStateWithLifecycle().value,
                onTypeClicked = vm::onItemClicked,
                onQueryChanged = vm::onQueryChanged,
                onFocused = {
                    skipCollapsed()
                    expand()
                }
            )
        }
        LaunchedEffect(Unit) {
            vm.commands.collect { command ->
                when (command) {
                    is Command.DispatchType -> {
                        onItemClicked(command.item)
                        dismiss()
                    }
                }
            }
        }
        LaunchedEffect(Unit) {
            vm.toasts.collect { toast ->
                toast(toast)
            }
        }
    }

    companion object {
        const val ARG_SPACE = "arg.object-type-change.space"
        const val ARG_EXCLUDE_TYPES = "arg.object-type-change.exclude-types"
        const val ARG_SELECTED_TYPES = "arg.object-type-change.selected-types"
        const val OBJECT_TYPE_URL_KEY = "object-type-url.key"
        const val OBJECT_TYPE_REQUEST_KEY = "object-type.request"
    }
}
