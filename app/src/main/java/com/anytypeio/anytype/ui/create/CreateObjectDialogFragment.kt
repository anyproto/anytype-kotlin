package com.anytypeio.anytype.ui.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_create_object.presentation.CreateObjectAction
import com.anytypeio.anytype.feature_create_object.presentation.CreateObjectNavigation
import com.anytypeio.anytype.feature_create_object.presentation.NewCreateObjectViewModel
import com.anytypeio.anytype.feature_create_object.ui.CreateObjectContent
import javax.inject.Inject

class CreateObjectDialogFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private val vm by viewModels<NewCreateObjectViewModel> { factory }

    private val space: String get() = requireArguments().getString(ARG_SPACE, "")
    private val typeKey: String? get() = requireArguments().getString(ARG_TYPE_KEY)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            val state by vm.state.collectAsStateWithLifecycle()
            CreateObjectContent(
                state = state,
                onAction = { action ->
                    when (action) {
                        is CreateObjectAction.SelectPhotos,
                        is CreateObjectAction.TakePhoto,
                        is CreateObjectAction.SelectFiles,
                        is CreateObjectAction.AttachExistingObject -> {
                            setFragmentResult(
                                RESULT_KEY_ACTION,
                                bundleOf(RESULT_ACTION_TYPE to action::class.java.simpleName)
                            )
                            dismiss()
                        }
                        else -> vm.onAction(action)
                    }
                }
            )
        }
    }

    override fun onStart() {
        super.onStart()
        jobs += lifecycleScope.subscribe(vm.navigation) { nav ->
            setFragmentResult(
                RESULT_KEY_NAVIGATION,
                bundleOf(
                    RESULT_NAV_TYPE to nav::class.java.simpleName,
                    RESULT_NAV_ID to when (nav) {
                        is CreateObjectNavigation.OpenEditor -> nav.id
                        is CreateObjectNavigation.OpenSet -> nav.id
                        is CreateObjectNavigation.OpenChat -> nav.id
                    },
                    RESULT_NAV_SPACE to when (nav) {
                        is CreateObjectNavigation.OpenEditor -> nav.space.id
                        is CreateObjectNavigation.OpenSet -> nav.space.id
                        is CreateObjectNavigation.OpenChat -> nav.space.id
                    }
                )
            )
            dismiss()
        }
    }

    override fun injectDependencies() {
        val typeKeyParam = typeKey?.let { TypeKey(it) }
        componentManager().createObjectFeatureComponent.get(
            NewCreateObjectViewModel.VmParams(
                spaceId = SpaceId(space),
                typeKey = typeKeyParam
            )
        ).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().createObjectFeatureComponent.release()
    }

    companion object {
        const val TAG = "CreateObjectDialogFragment"
        private const val ARG_SPACE = "arg.create-object.space"
        private const val ARG_TYPE_KEY = "arg.create-object.type-key"

        const val RESULT_KEY_NAVIGATION = "result.create-object.navigation"
        const val RESULT_KEY_ACTION = "result.create-object.action"
        const val RESULT_NAV_TYPE = "result.nav.type"
        const val RESULT_NAV_ID = "result.nav.id"
        const val RESULT_NAV_SPACE = "result.nav.space"
        const val RESULT_ACTION_TYPE = "result.action.type"

        fun new(space: Id, typeKey: String? = null) = CreateObjectDialogFragment().apply {
            arguments = bundleOf(
                ARG_SPACE to space,
                ARG_TYPE_KEY to typeKey
            )
        }
    }
}
