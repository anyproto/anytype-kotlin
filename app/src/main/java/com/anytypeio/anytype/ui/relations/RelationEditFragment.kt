package com.anytypeio.anytype.ui.relations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.argInt
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.RelationEditViewModel
import com.anytypeio.anytype.ui.settings.typography
import com.anytypeio.anytype.ui.types.edit.REQUEST_UNINSTALL_TYPE_ARG_ID
import com.anytypeio.anytype.ui.types.edit.REQUEST_UNINSTALL_TYPE_ARG_NAME
import javax.inject.Inject

class RelationEditFragment: BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: RelationEditViewModel.Factory

    private val vm by viewModels<RelationEditViewModel> { factory }

    private val id get() = argString(ARG_RELATION_EDIT_ID)
    private val name get() = argString(ARG_RELATION_EDIT_NAME)
    private val icon get() = argInt(ARG_RELATION_EDIT_UNICODE)

    @ExperimentalMaterialApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(typography = typography) {
                RelationEditScreen(
                    vm = vm,
                    preparedName = name,
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribe(vm.navigation) {
            when (it) {
                is RelationEditViewModel.Navigation.BackWithModify -> {
                    setFragmentResult(
                        REQUEST_KEY_MODIFY_RELATION, bundleOf(
                            REQUEST_UNINSTALL_TYPE_ARG_ID to id,
                            REQUEST_UNINSTALL_TYPE_ARG_NAME to it.name
                        )
                    )
                    dismiss()
                }
                is RelationEditViewModel.Navigation.BackWithUninstall -> {
                    setFragmentResult(
                        REQUEST_KEY_UNINSTALL_RELATION, bundleOf(
                            REQUEST_UNINSTALL_TYPE_ARG_ID to id,
                            REQUEST_UNINSTALL_TYPE_ARG_NAME to name,
                        )
                    )
                    dismiss()
                }
            }
        }
    }

    override fun injectDependencies() {
        componentManager().relationEditComponent.get(
            RelationEditParameters(id = id, name = name, icon = icon)
        ).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().relationEditComponent.release()
    }

    companion object {
        fun args(
            typeName: String,
            id: Id,
            iconUnicode: Int
        ) = bundleOf(
            ARG_RELATION_EDIT_NAME to typeName,
            ARG_RELATION_EDIT_ID to id,
            ARG_RELATION_EDIT_UNICODE to iconUnicode
        )
    }

}

data class RelationEditParameters(
    val id: Id,
    val name: String,
    val icon: Int
)


const val REQUEST_KEY_UNINSTALL_RELATION = "request.relation_edit_uninstall"
const val REQUEST_KEY_MODIFY_RELATION = "request.relation_edit_modify"
const val REQUEST_UNINSTALL_RELATION_ARG_ID = "request.type_edit_uninstall_id"
const val REQUEST_UNINSTALL_RELATION_ARG_NAME = "request.type_edit_uninstall_name"

private const val ARG_RELATION_EDIT_NAME = "arg.relation_edit_name"
private const val ARG_RELATION_EDIT_ID = "arg.relation_edit_id"
private const val ARG_RELATION_EDIT_UNICODE = "arg.relation_edit_unicode"