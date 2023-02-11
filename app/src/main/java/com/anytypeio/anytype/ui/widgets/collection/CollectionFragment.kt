package com.anytypeio.anytype.ui.widgets.collection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.search.Subscriptions
import com.anytypeio.anytype.ui.settings.typography
import com.anytypeio.anytype.ui.widgets.collection.CollectionViewModel.Command
import javax.inject.Inject
import kotlinx.coroutines.launch

class CollectionFragment : BaseComposeFragment() {

    @Inject
    lateinit var factory: CollectionViewModel.Factory

    private val vm by viewModels<CollectionViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(typography = typography) {
                CollectionScreen(vm)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            jobs += subscribe(vm.commands) { execute(it) }
        }
        //vm.onStart(Subscriptions.SUBSCRIPTION_RECENT)
        vm.onStart(Subscriptions.SUBSCRIPTION_SETS)
    }

    private fun execute(command: Command) {
        when (command) {
            is Command.LaunchDocument -> launchDocument(command.id)
            is Command.LaunchObjectSet -> launchObjectSet(command.target)
        }
    }

    private fun launchObjectSet(target: Id) {
        TODO("Not yet implemented")
    }

    private fun launchDocument(id: Id) {
        TODO("Not yet implemented")
    }


    override fun onStop() {
        //vm.onStop(Subscriptions.SUBSCRIPTION_RECENT)
        vm.onStop(Subscriptions.SUBSCRIPTION_SETS)
        super.onStop()
    }

    override fun injectDependencies() {
        componentManager().collectionComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().collectionComponent.release()
    }
}