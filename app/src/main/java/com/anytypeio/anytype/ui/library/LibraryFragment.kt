package com.anytypeio.anytype.ui.library

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.library.LibraryViewModel
import com.anytypeio.anytype.ui.relations.REQUEST_KEY_MODIFY_RELATION
import com.anytypeio.anytype.ui.relations.REQUEST_KEY_UNINSTALL_RELATION
import com.anytypeio.anytype.ui.relations.REQUEST_UNINSTALL_RELATION_ARG_ID
import com.anytypeio.anytype.ui.relations.REQUEST_UNINSTALL_RELATION_ARG_NAME
import com.anytypeio.anytype.ui.settings.typography
import com.anytypeio.anytype.ui.types.create.REQUEST_CREATE_OBJECT
import com.anytypeio.anytype.ui.types.edit.REQUEST_KEY_MODIFY_TYPE
import com.anytypeio.anytype.ui.types.edit.REQUEST_KEY_UNINSTALL_TYPE
import com.anytypeio.anytype.ui.types.edit.REQUEST_UNINSTALL_TYPE_ARG_ICON
import com.anytypeio.anytype.ui.types.edit.REQUEST_UNINSTALL_TYPE_ARG_ID
import com.anytypeio.anytype.ui.types.edit.REQUEST_UNINSTALL_TYPE_ARG_NAME
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import timber.log.Timber

@Deprecated("legacy")
class LibraryFragment : BaseComposeFragment() {

    @Inject
    lateinit var factory: LibraryViewModel.Factory

    private val vm by viewModels<LibraryViewModel> { factory }

    private val space get() = arg<Id>(ARG_SPACE_ID_KEY)

    @OptIn(ExperimentalAnimationApi::class)
    @FlowPreview
    @ExperimentalMaterialApi
    @ExperimentalComposeUiApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    LibraryScreen(
                        configuration = LibraryConfiguration(),
                        viewModel = vm,
                        onBackPressed = {
                            findNavController().popBackStack()
                        },
                        onCreateObjectLongClicked = {},
                        onBackLongPressed = {
                            runCatching {
                                findNavController().navigate(R.id.actionExitToSpaceWidgets)
                            }.onFailure {
                                Timber.e(it, "Error while opening space switcher from library")
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribe(vm.toasts) { toast(it) }
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
        componentManager()
            .libraryComponent
            .get(
                Pair(
                    requireContext(),
                    LibraryViewModel.Params(
                        space = SpaceId(space)
                    )
                )
            )
            .inject(this)
    }

    override fun releaseDependencies() {
        componentManager().libraryComponent.release()
    }

    override fun onApplyWindowRootInsets(view: View) {
        if (BuildConfig.USE_EDGE_TO_EDGE && Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK) {
            // Do nothing.
        } else {
            super.onApplyWindowRootInsets(view)
        }
    }

    companion object {
        const val ARG_SPACE_ID_KEY = "arg.library.space-id"
        fun args(space: Id) = bundleOf(ARG_SPACE_ID_KEY to space)
    }
}