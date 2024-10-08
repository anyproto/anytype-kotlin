package com.anytypeio.anytype.ui.types.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.common.ComposeDialogView
import com.anytypeio.anytype.core_utils.ext.argBoolean
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.types.TypeEditViewModel
import com.anytypeio.anytype.ui.settings.typography
import com.anytypeio.anytype.ui.types.picker.REQUEST_KEY_PICK_EMOJI
import com.anytypeio.anytype.ui.types.picker.REQUEST_KEY_REMOVE_EMOJI
import com.anytypeio.anytype.ui.types.picker.RESULT_EMOJI_UNICODE
import javax.inject.Inject
import timber.log.Timber

class TypeEditFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: TypeEditViewModel.Factory

    private val vm by viewModels<TypeEditViewModel> { factory }

    private val id get() = argString(ARG_TYPE_EDIT_ID)
    private val name get() = argString(ARG_TYPE_EDIT_NAME)
    private val icon get() = argString(ARG_TYPE_EDIT_UNICODE)
    private val readOnly get() = argBoolean(ARG_TYPE_EDIT_READONLY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(REQUEST_KEY_PICK_EMOJI) { _, bundle ->
            val res = requireNotNull(bundle.getString(RESULT_EMOJI_UNICODE))
            vm.setEmoji(res)
        }
        setFragmentResultListener(REQUEST_KEY_REMOVE_EMOJI) { _, _ ->
            vm.removeEmoji()
        }
    }

    @ExperimentalMaterialApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeDialogView(
        context = requireContext(),
        dialog = requireDialog()
    ).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(typography = typography) {
                TypeEditScreen(
                    vm = vm,
                    preparedName = name,
                    readOnly = readOnly
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribe(vm.navigation) {
            when (it) {
                is TypeEditViewModel.Navigation.Back -> {
                    findNavController().popBackStack()
                }
                is TypeEditViewModel.Navigation.SelectEmoji -> {
                    runCatching {
                        findNavController().navigate(R.id.openEmojiPicker)
                    }.onFailure {
                        Timber.w("Error while opening emoji picker")
                    }
                }
                is TypeEditViewModel.Navigation.BackWithUninstall -> {
                    setFragmentResult(
                        REQUEST_KEY_UNINSTALL_TYPE, bundleOf(
                            REQUEST_UNINSTALL_TYPE_ARG_ID to id,
                            REQUEST_UNINSTALL_TYPE_ARG_NAME to name,
                        )
                    )
                    findNavController().popBackStack()
                }
                is TypeEditViewModel.Navigation.BackWithModify -> {
                    setFragmentResult(
                        REQUEST_KEY_MODIFY_TYPE, bundleOf(
                            REQUEST_UNINSTALL_TYPE_ARG_ID to id,
                            REQUEST_UNINSTALL_TYPE_ARG_NAME to it.typeName,
                            REQUEST_UNINSTALL_TYPE_ARG_ICON to it.typeIcon
                        )
                    )
                    findNavController().popBackStack()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        vm.onStart(isReadOnly = readOnly)
    }

    override fun injectDependencies() {
        componentManager().typeEditComponent.get(
            TypeEditParameters(
                id = id,
                name = name,
                icon = icon
            )
        ).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().typeEditComponent.release()
    }

    companion object {
        fun args(
            typeName: String,
            id: Id,
            iconUnicode: String,
            readOnly: Boolean
        ) = bundleOf(
            ARG_TYPE_EDIT_NAME to typeName,
            ARG_TYPE_EDIT_ID to id,
            ARG_TYPE_EDIT_UNICODE to iconUnicode,
            ARG_TYPE_EDIT_READONLY to readOnly
        )
    }

}

data class TypeEditParameters(
    val id: Id,
    val name: String,
    val icon: String
)

const val REQUEST_KEY_UNINSTALL_TYPE = "request.type_edit_uninstall"
const val REQUEST_KEY_MODIFY_TYPE = "request.type_edit_modify"
const val REQUEST_UNINSTALL_TYPE_ARG_ID = "request.type_edit_uninstall_id"
const val REQUEST_UNINSTALL_TYPE_ARG_NAME = "request.type_edit_uninstall_name"
const val REQUEST_UNINSTALL_TYPE_ARG_ICON = "request.type_edit_uninstall_icon"

private const val ARG_TYPE_EDIT_NAME = "arg.type_edit_name"
private const val ARG_TYPE_EDIT_ID = "arg.type_edit_id"
private const val ARG_TYPE_EDIT_UNICODE = "arg.type_edit_unicode"
private const val ARG_TYPE_EDIT_READONLY = "arg.type_edit_readonly"