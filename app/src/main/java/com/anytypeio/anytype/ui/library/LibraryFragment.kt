package com.anytypeio.anytype.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.safeNavigate
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.library.LibraryViewModel
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.ui.relations.RelationCreateFromScratchForObjectFragment
import com.anytypeio.anytype.ui.settings.typography
import com.anytypeio.anytype.ui.types.create.REQUEST_CREATE_TYPE
import com.anytypeio.anytype.ui.types.create.TypeCreationFragment
import com.anytypeio.anytype.ui.types.edit.REQUEST_KEY_MODIFY
import com.anytypeio.anytype.ui.types.edit.REQUEST_UNINSTALL_ARG_ID
import com.anytypeio.anytype.ui.types.edit.REQUEST_UNINSTALL_ARG_NAME
import com.anytypeio.anytype.ui.types.edit.REQUEST_KEY_UNINSTALL
import com.anytypeio.anytype.ui.types.edit.REQUEST_UNINSTALL_ARG_ICON
import com.anytypeio.anytype.ui.types.edit.TypeEditFragment
import com.google.accompanist.pager.ExperimentalPagerApi
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview

class LibraryFragment : BaseComposeFragment() {

    @Inject
    lateinit var factory: LibraryViewModel.Factory

    private val vm by viewModels<LibraryViewModel> { factory }

    @FlowPreview
    @ExperimentalPagerApi
    @ExperimentalMaterialApi
    @ExperimentalComposeUiApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    LibraryScreen(LibraryConfiguration(), vm)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribe(vm.toasts) { toast(it) }
        subscribe(vm.navigation) {
            when (it) {
                is LibraryViewModel.Navigation.OpenTypeCreation -> {
                    findNavController().safeNavigate(
                        R.id.libraryFragment,
                        R.id.openTypeCreationScreen,
                        TypeCreationFragment.args(it.name)
                    )
                }
                is LibraryViewModel.Navigation.OpenTypeEditing -> {
                    findNavController().safeNavigate(
                        R.id.libraryFragment,
                        R.id.openTypeEditingScreen,
                        TypeEditFragment.args(
                            typeName = it.view.name,
                            id = it.view.id,
                            iconUnicode = (it.view.icon as? ObjectIcon.Basic.Emoji)?.unicode ?: ""
                        )
                    )
                }
                is LibraryViewModel.Navigation.OpenRelationCreation -> {
                    findNavController().safeNavigate(
                        R.id.libraryFragment,
                        R.id.openRelationCreationScreen,
                        RelationCreateFromScratchForObjectFragment.args(
                            ctx = "",
                            query = it.name
                        )
                    )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(REQUEST_KEY_UNINSTALL) { _, bundle ->
            val id = requireNotNull(bundle.getString(REQUEST_UNINSTALL_ARG_ID))
            val name = requireNotNull(bundle.getString(REQUEST_UNINSTALL_ARG_NAME))
            vm.uninstallType(id, name)
        }
        setFragmentResultListener(REQUEST_KEY_MODIFY) { _, bundle ->
            val id = requireNotNull(bundle.getString(REQUEST_UNINSTALL_ARG_ID))
            val name = requireNotNull(bundle.getString(REQUEST_UNINSTALL_ARG_NAME))
            val icon = requireNotNull(bundle.getString(REQUEST_UNINSTALL_ARG_ICON))
            vm.updateType(id, name, icon)
        }
        setFragmentResultListener(REQUEST_CREATE_TYPE) { _, bundle ->
            vm.onTypeCreated()
        }
    }

    override fun injectDependencies() {
        componentManager().libraryComponent.get(requireContext()).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().libraryComponent.release()
    }

}