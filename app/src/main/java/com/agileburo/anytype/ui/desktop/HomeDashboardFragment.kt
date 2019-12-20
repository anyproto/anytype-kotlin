package com.agileburo.anytype.ui.desktop

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.tools.DefaultDragAndDropBehavior
import com.agileburo.anytype.core_utils.ext.invisible
import com.agileburo.anytype.core_utils.ext.toast
import com.agileburo.anytype.core_utils.ext.visible
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.presentation.desktop.HomeDashboardViewModel
import com.agileburo.anytype.presentation.desktop.HomeDashboardViewModel.Machine.State
import com.agileburo.anytype.presentation.desktop.HomeDashboardViewModelFactory
import com.agileburo.anytype.presentation.mapper.toView
import com.agileburo.anytype.presentation.profile.ProfileView
import com.agileburo.anytype.ui.base.ViewStateFragment
import kotlinx.android.synthetic.main.fragment_desktop.*
import timber.log.Timber
import javax.inject.Inject


class HomeDashboardFragment : ViewStateFragment<State>(R.layout.fragment_desktop) {

    private val profileObserver = Observer<ProfileView> { profile ->
        greeting.text = getString(R.string.greet, profile.name)
        avatar.bind(name = profile.name)
    }

    private val imageObserver = Observer<ByteArray> { blob -> avatar.bind(blob) }

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(HomeDashboardViewModel::class.java)
    }

    private val dndBehavior by lazy {
        DefaultDragAndDropBehavior(
            onItemMoved = { from, to ->
                dashboardAdapter
                    .onItemMove(from, to)
                    .also {
                        vm.onItemMoved(
                            alteredViews = dashboardAdapter.provideAdapterData(),
                            from = from,
                            to = to
                        )
                    }
            },
            onItemDropped = { index ->
                vm.onItemDropped(dashboardAdapter.provideAdapterData()[index])
            }
        )
    }

    @Inject
    lateinit var factory: HomeDashboardViewModelFactory

    private val dashboardAdapter by lazy {
        DashboardAdapter(
            data = mutableListOf(),
            onDocumentClicked = { vm.onDocumentClicked(it.id) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()

        with(vm) {
            state.observe(viewLifecycleOwner, this@HomeDashboardFragment)
            navigation.observe(viewLifecycleOwner, navObserver)
            profile.observe(viewLifecycleOwner, profileObserver)
            image.observe(viewLifecycleOwner, imageObserver)
        }

        vm.onViewCreated()
    }

    override fun render(state: State) {
        Timber.d("Rendering state: $state")
        when {
            state.isLoading -> {
                fab.invisible()
                progress.visible()
            }
            state.error != null -> {
                progress.invisible()
                requireActivity().toast("Error: ${state.error}")
            }
            state.homeDashboard != null -> {
                progress.invisible()
                fab.visible()
                dashboardAdapter.update(state.homeDashboard!!.toView())
            }
        }
    }

    private fun setup() {
        desktopRecycler.apply {
            layoutManager = GridLayoutManager(context, COLUMN_COUNT)
            adapter = dashboardAdapter
            ItemTouchHelper(dndBehavior).attachToRecyclerView(this)
        }
        fab.setOnClickListener { vm.onAddNewDocumentClicked() }
        avatar.setOnClickListener { vm.onProfileClicked() }
    }

    override fun injectDependencies() {
        componentManager().desktopComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().desktopComponent.release()
    }

    companion object {
        const val COLUMN_COUNT = 2
    }
}