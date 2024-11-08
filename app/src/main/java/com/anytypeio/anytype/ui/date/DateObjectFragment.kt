package com.anytypeio.anytype.ui.date

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_date.presentation.DateObjectViewModel
import com.anytypeio.anytype.feature_date.presentation.DateObjectViewModelFactory
import com.anytypeio.anytype.feature_date.ui.DateObjectScreen
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject

class DateObjectFragment : BaseComposeFragment() {
    @Inject
    lateinit var factory: DateObjectViewModelFactory

    private val vm by viewModels<DateObjectViewModel> { factory }

    private val space get() = argString(ARG_SPACE)
    private val objectId get() = argString(ARG_OBJECT_ID)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        MaterialTheme(
            typography = typography
        ) {
            DateLayoutScreenWrapper()
        }
    }

    override fun onStart() {
        vm.onStart()
        super.onStart()
    }

    @Composable
    fun DateLayoutScreenWrapper() {
        NavHost(
            navController = rememberNavController(),
            startDestination = DATE_MAIN
        ) {
            composable(route = DATE_MAIN) {
                DateObjectScreen(
                    uiTopToolbarState = vm.uiTopToolbarState.collectAsStateWithLifecycle().value,
                    uiHeaderState = vm.uiHeaderState.collectAsStateWithLifecycle().value,
                    uiHorizontalListState = vm.uiHorizontalListState.collectAsStateWithLifecycle().value,
                    uiVerticalListState = vm.uiVerticalListState.collectAsStateWithLifecycle().value,
                    uiDateObjectBottomMenu = vm.uiBottomMenu.collectAsStateWithLifecycle().value,
                    uiSheetState = vm.uiSheetState.collectAsStateWithLifecycle().value,
                    uiHeaderActions = {},
                    uiBottomMenuActions = {},
                    uiTopToolbarActions = {},
                    uiVerticalListActions = {},
                    uiHorizontalListActions = {}
                )
            }
        }
    }

    override fun injectDependencies() {
        val params = DateObjectViewModel.VmParams(
            spaceId = SpaceId(space),
            objectId = objectId
        )
        componentManager().dateObjectComponent.get(params).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().dateObjectComponent.release()
    }

    override fun onApplyWindowRootInsets(view: View) {
        if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK) {
            // Do nothing.
        } else {
            super.onApplyWindowRootInsets(view)
        }
    }

    companion object DateLayoutNavigation {
        private const val DATE_MAIN = "date_main"
        private const val DATE_CALENDAR = "date_calendar"
        private const val DATE_ALL_RELATIONS = "date_all_relations"
        const val ARG_SPACE = "arg.date.object.space"
        const val ARG_OBJECT_ID = "arg.date.object.object_id"

        fun args(space: Id, objectId: Id) = bundleOf(
            ARG_SPACE to space,
            ARG_OBJECT_ID to objectId
        )
    }
}