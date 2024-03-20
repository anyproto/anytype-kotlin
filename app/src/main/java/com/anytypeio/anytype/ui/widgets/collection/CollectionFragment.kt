package com.anytypeio.anytype.ui.widgets.collection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.arg
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
import com.anytypeio.anytype.ui.objects.creation.SelectObjectTypeFragment
import com.anytypeio.anytype.ui.spaces.SelectSpaceFragment
import javax.inject.Inject

class CollectionFragment : BaseComposeFragment() {

    @Inject
    lateinit var factory: CollectionViewModel.Factory

    private val navigation get() = navigation()

    private val space get() = arg<Id>(SPACE_ID_KEY)

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
                    CollectionScreen(
                        vm = vm,
                        onCreateObjectLongClicked = {
                            val dialog = SelectObjectTypeFragment.new(
                                flow = SelectObjectTypeFragment.FLOW_CREATE_OBJECT,
                                space = space
                            ).apply {
                                onTypeSelected = {
                                    vm.onAddClicked(it)
                                }
                            }
                            dialog.show(childFragmentManager, "fullscreen-widget-create-object-of-type-dialog")
                        }
                    )
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
            is Command.LaunchDocument -> launchDocument(
                target = command.target,
                space = command.space
            )
            is Command.LaunchObjectSet -> launchObjectSet(command.target)
            is Command.Exit -> exit()
            is Command.ConfirmRemoveFromBin -> confirmRemoveFromBin(command)
            is Command.OpenCollection -> navigation.launchCollections(
                subscription = command.subscription,
                space = space
            )
            is Command.ToDesktop -> navigation.exitToDesktop()
            is Command.ToSearch -> navigation.openPageSearch()
            is Command.SelectSpace -> {
                findNavController().navigate(
                    R.id.selectSpaceScreen,
                    args = SelectSpaceFragment.args(exitHomeWhenSpaceIsSelected = true)
                )
            }
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

    private fun launchDocument(target: Id, space: Id) {
        navigation.launchDocument(
            target = target,
            space = space
        )
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
        const val SPACE_ID_KEY = "arg.collection.space-id"
        fun args(subscription: Id, space: Id) = bundleOf(
            SUBSCRIPTION_KEY to subscription,
            SPACE_ID_KEY  to space
        )
    }
}