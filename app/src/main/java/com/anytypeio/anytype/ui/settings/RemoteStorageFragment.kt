package com.anytypeio.anytype.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.core_utils.ui.proceed
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.widgets.collection.CollectionViewModel
import com.anytypeio.anytype.presentation.widgets.collection.Subscription
import com.anytypeio.anytype.presentation.widgets.collection.SubscriptionMapper
import com.anytypeio.anytype.ui.base.navigation
import com.anytypeio.anytype.ui.dashboard.DeleteAlertFragment
import com.anytypeio.anytype.ui.settings.remote.RemoteStorageScreen
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import javax.inject.Inject

class RemoteStorageFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: CollectionViewModel.Factory

    private val navigation get() = navigation()

    private val subscription: Subscription by lazy {
        SubscriptionMapper().map(argString(SUBSCRIPTION_KEY))
    }

    private val vm by viewModels<CollectionViewModel> { factory }

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    RemoteStorageScreen(vm = vm)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val offsetFromTop = PADDING_TOP
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            isFitToContents = false
            expandedOffset = offsetFromTop
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
    }

    override fun onStart() {
        super.onStart()
        proceed(vm.commands) { execute(it) }
        proceed(vm.toasts) { toast(it) }
        vm.onStart(subscription)
    }

    private fun execute(command: CollectionViewModel.Command) {
        when (command) {
            is CollectionViewModel.Command.LaunchDocument -> launchDocument(command.id)
            is CollectionViewModel.Command.LaunchObjectSet -> launchObjectSet(command.target)
            is CollectionViewModel.Command.ConfirmRemoveFromBin -> confirmRemoveFromBin(command)
            is CollectionViewModel.Command.OpenCollection -> navigation.launchCollections(command.subscription)
            is CollectionViewModel.Command.ToDesktop -> navigation.exitToDesktop()
            is CollectionViewModel.Command.ToSearch -> navigation.openPageSearch()
            is CollectionViewModel.Command.Exit -> exit()
        }
    }

    private fun confirmRemoveFromBin(command: CollectionViewModel.Command.ConfirmRemoveFromBin) {
        val dialog = DeleteAlertFragment.new(command.count)
        dialog.onDeletionAccepted = { vm.onDeletionAccepted() }
        dialog.showChildFragment()
    }

    private fun exit() {
        navigation.exit()
    }

    private fun launchObjectSet(target: Id) {
        navigation.launchObjectSet(target)
    }

    private fun launchDocument(id: Id) {
        navigation.launchDocument(id)
    }

    override fun onStop() {
        vm.onStop()
        jobs.forEach { it.cancel() }
        jobs.clear()
        super.onStop()
    }

    override fun injectDependencies() {
        componentManager().collectionComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().collectionComponent.release()
    }

    companion object {
        const val SUBSCRIPTION_KEY: String = "arg.space.files.subscription.key"
    }
}

private const val PADDING_TOP = 74