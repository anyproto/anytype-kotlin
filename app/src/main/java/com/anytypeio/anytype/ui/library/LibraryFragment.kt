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
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.extensions.simpleIcon
import com.anytypeio.anytype.core_utils.ext.safeNavigate
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.library.LibraryViewModel
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.relations.REQUEST_KEY_MODIFY_RELATION
import com.anytypeio.anytype.ui.relations.REQUEST_KEY_UNINSTALL_RELATION
import com.anytypeio.anytype.ui.relations.REQUEST_UNINSTALL_RELATION_ARG_ID
import com.anytypeio.anytype.ui.relations.REQUEST_UNINSTALL_RELATION_ARG_NAME
import com.anytypeio.anytype.ui.relations.RelationCreateFromScratchForObjectFragment
import com.anytypeio.anytype.ui.relations.RelationEditFragment
import com.anytypeio.anytype.ui.settings.typography
import com.anytypeio.anytype.ui.types.create.REQUEST_CREATE_OBJECT
import com.anytypeio.anytype.ui.types.create.TypeCreationFragment
import com.anytypeio.anytype.ui.types.edit.REQUEST_KEY_MODIFY_TYPE
import com.anytypeio.anytype.ui.types.edit.REQUEST_UNINSTALL_TYPE_ARG_ID
import com.anytypeio.anytype.ui.types.edit.REQUEST_UNINSTALL_TYPE_ARG_NAME
import com.anytypeio.anytype.ui.types.edit.REQUEST_KEY_UNINSTALL_TYPE
import com.anytypeio.anytype.ui.types.edit.REQUEST_UNINSTALL_TYPE_ARG_ICON
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
                is LibraryViewModel.Navigation.OpenRelationEditing -> {
                    findNavController().safeNavigate(
                        R.id.libraryFragment,
                        R.id.openRelationEditingScreen,
                        RelationEditFragment.args(
                            typeName = it.view.name,
                            id = it.view.id,
                            iconUnicode = it.view.format.simpleIcon() ?: 0
                        )
                    )
                }
                is LibraryViewModel.Navigation.Back -> {
                    findNavController().popBackStack()
                }
                is LibraryViewModel.Navigation.Search -> {
                    findNavController().safeNavigate(
                        R.id.libraryFragment,
                        R.id.pageSearchFragment
                    )
                }
                is LibraryViewModel.Navigation.CreateDoc -> {
                    findNavController().safeNavigate(
                        R.id.libraryFragment,
                        R.id.objectNavigation,
                        bundleOf(
                            EditorFragment.ID_KEY to it.id
                        )
                    )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(REQUEST_KEY_UNINSTALL_TYPE) { _, bundle ->
            val id = requireNotNull(bundle.getString(REQUEST_UNINSTALL_TYPE_ARG_ID))
            val name = requireNotNull(bundle.getString(REQUEST_UNINSTALL_TYPE_ARG_NAME))
            vm.uninstallObject(id, LibraryViewModel.LibraryItem.TYPE, name)
        }
        setFragmentResultListener(REQUEST_KEY_MODIFY_TYPE) { _, bundle ->
            val id = requireNotNull(bundle.getString(REQUEST_UNINSTALL_TYPE_ARG_ID))
            val name = requireNotNull(bundle.getString(REQUEST_UNINSTALL_TYPE_ARG_NAME))
            val icon = requireNotNull(bundle.getString(REQUEST_UNINSTALL_TYPE_ARG_ICON))
            vm.updateObject(id, name, icon)
        }
        setFragmentResultListener(REQUEST_KEY_UNINSTALL_RELATION) { _, bundle ->
            val id = requireNotNull(bundle.getString(REQUEST_UNINSTALL_RELATION_ARG_ID))
            val name = requireNotNull(bundle.getString(REQUEST_UNINSTALL_RELATION_ARG_NAME))
            vm.uninstallObject(id, LibraryViewModel.LibraryItem.RELATION, name)
        }
        setFragmentResultListener(REQUEST_KEY_MODIFY_RELATION) { _, bundle ->
            val id = requireNotNull(bundle.getString(REQUEST_UNINSTALL_RELATION_ARG_ID))
            val name = requireNotNull(bundle.getString(REQUEST_UNINSTALL_RELATION_ARG_NAME))
            vm.updateObject(
                id = id,
                name = name,
                icon = null
            )
        }
        setFragmentResultListener(REQUEST_CREATE_OBJECT) { _, _ ->
            vm.onObjectCreated()
        }
    }

    override fun injectDependencies() {
        componentManager().libraryComponent.get(requireContext()).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().libraryComponent.release()
    }

}