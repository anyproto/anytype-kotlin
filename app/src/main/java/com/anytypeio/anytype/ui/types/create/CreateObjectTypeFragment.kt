package com.anytypeio.anytype.ui.types.create

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
import com.anytypeio.anytype.core_ui.common.ComposeDialogView
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.types.CreateObjectTypeViewModel
import com.anytypeio.anytype.ui.settings.typography
import com.anytypeio.anytype.ui.types.picker.REQUEST_KEY_PICK_EMOJI
import com.anytypeio.anytype.ui.types.picker.REQUEST_KEY_REMOVE_EMOJI
import com.anytypeio.anytype.ui.types.picker.RESULT_EMOJI_UNICODE
import javax.inject.Inject
import timber.log.Timber

class CreateObjectTypeFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: CreateObjectTypeViewModel.Factory

    private val vm by viewModels<CreateObjectTypeViewModel> { factory }

    private val preparedName get() = argString(ARG_TYPE_NAME)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(REQUEST_KEY_PICK_EMOJI) { _, bundle ->
            val res = requireNotNull(bundle.getString(RESULT_EMOJI_UNICODE))
            vm.setEmoji(res)
        }
        setFragmentResultListener(REQUEST_KEY_REMOVE_EMOJI) { _, bundle ->
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
                TypeCreationScreen(vm, preparedName)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.onPreparedString(preparedName)
        subscribe(vm.navigation) {
            when (it) {
                is CreateObjectTypeViewModel.Navigation.BackWithCreatedType -> {
                    setFragmentResult(REQUEST_CREATE_OBJECT, bundleOf())
                    findNavController().popBackStack()
                }
                is CreateObjectTypeViewModel.Navigation.SelectEmoji -> {
                    runCatching {
                        findNavController().navigate(
                            R.id.openEmojiPicker, bundleOf(ARG_SHOW_REMOVE_BUTTON to it.showRemove)
                        )
                    }.onFailure {
                        Timber.w("Error while opening emoji picker")
                    }
                }
            }
        }
        subscribe(vm.toasts) { toast(it) }
    }

    override fun injectDependencies() {
        componentManager().createObjectTypeComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().createObjectTypeComponent.release()
    }

    companion object {
        fun args(typeName: String) = bundleOf(ARG_TYPE_NAME to typeName)
    }

}

private const val ARG_TYPE_NAME = "arg.type_name"
private const val ARG_SHOW_REMOVE_BUTTON = "arg.type_show_remove"
const val REQUEST_CREATE_OBJECT = "request.create_type"
