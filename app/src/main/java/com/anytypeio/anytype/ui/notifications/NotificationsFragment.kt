package com.anytypeio.anytype.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.core_ui.notifications.NotificationsScreen
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.notifications.NotificationsViewModel
import com.anytypeio.anytype.presentation.notifications.NotificationsViewModelFactory
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject
import timber.log.Timber

class NotificationsFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: NotificationsViewModelFactory

    private val vm by viewModels<NotificationsViewModel> { factory }

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
                NotificationsScreen(
                    state = vm.state.collectAsStateWithLifecycle().value,
                    onActionButtonClick = vm::onSpaceClick,
                    onErrorButtonClick = vm::onErrorButtonClick
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        jobs += subscribe(vm.command) { command ->
            Timber.d("GalleryInstallationFragment command: $command")
            when (command) {
                NotificationsViewModel.Command.Dismiss -> {
                    dismiss()
                }
                is NotificationsViewModel.Command.NavigateToSpace -> {
                    //TODO: implement navigation
                }
                else -> {
                    // do nothing
                }
            }
        }
    }

    override fun injectDependencies() {
        componentManager().notificationsComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().notificationsComponent.release()
    }
}