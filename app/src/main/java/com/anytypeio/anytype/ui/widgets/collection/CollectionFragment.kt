package com.anytypeio.anytype.ui.widgets.collection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.core_utils.ui.proceed
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.widgets.collection.CollectionViewModel
import com.anytypeio.anytype.presentation.widgets.collection.CollectionViewModel.Command
import com.anytypeio.anytype.presentation.widgets.collection.Subscription
import com.anytypeio.anytype.presentation.widgets.collection.SubscriptionMapper
import com.anytypeio.anytype.ui.base.navigation
import com.anytypeio.anytype.ui.dashboard.DeleteAlertFragment
import javax.inject.Inject

class CollectionFragment : BaseComposeFragment() {

    @Inject
    lateinit var factory: CollectionViewModel.Factory

    private val navigation get() = navigation()

    private val subscription: Subscription by lazy {
        SubscriptionMapper().map(
            argString(
                SUBSCRIPTION_KEY
            )
        )
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
                DefaultTheme {
                    CollectionScreen(vm)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        proceed(vm.commands) { execute(it) }
        vm.onStart(subscription)
    }

    private fun execute(command: Command) {
        when (command) {
            is Command.LaunchDocument -> launchDocument(command.id)
            is Command.LaunchObjectSet -> launchObjectSet(command.target)
            is Command.Exit -> exit()
            is Command.ConfirmRemoveFromBin -> confirmRemoveFromBin(command)
            is Command.OpenCollection -> navigation.launchCollections(command.subscription)
            is Command.ToDesktop -> navigation.exitToDesktop()
            is Command.ToSearch -> navigation.openPageSearch()
        }
    }

    private fun confirmRemoveFromBin(command: Command.ConfirmRemoveFromBin) {
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
        super.onStop()
    }

    override fun injectDependencies() {
        componentManager().collectionComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().collectionComponent.release()
    }

    companion object {
        const val SUBSCRIPTION_KEY: String = "arg.collection.subscription"
    }
}