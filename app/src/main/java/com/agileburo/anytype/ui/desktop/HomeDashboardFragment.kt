package com.agileburo.anytype.ui.desktop

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.tools.DefaultDragAndDropBehavior
import com.agileburo.anytype.core_utils.ext.dimen
import com.agileburo.anytype.core_utils.ext.invisible
import com.agileburo.anytype.core_utils.ext.toast
import com.agileburo.anytype.core_utils.ext.visible
import com.agileburo.anytype.core_utils.ui.EqualSpacingItemDecoration
import com.agileburo.anytype.core_utils.ui.EqualSpacingItemDecoration.Companion.GRID
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.domain.emoji.Emojifier
import com.agileburo.anytype.presentation.desktop.HomeDashboardStateMachine.State
import com.agileburo.anytype.presentation.desktop.HomeDashboardViewModel
import com.agileburo.anytype.presentation.desktop.HomeDashboardViewModelFactory
import com.agileburo.anytype.presentation.mapper.toView
import com.agileburo.anytype.presentation.profile.ProfileView
import com.agileburo.anytype.ui.base.ViewStateFragment
import kotlinx.android.synthetic.main.fragment_desktop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    @Inject
    lateinit var emojifier: Emojifier

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
            state.dashboard != null -> {
                progress.invisible()
                fab.visible()
                state.dashboard?.let { dashboard ->
                    lifecycleScope.launch {
                        val result = withContext(Dispatchers.IO) {
                            dashboard.toView()
                        }
                        dashboardAdapter.update(result)
                    }
                }
            }
        }
    }

    private fun setup() {

        val spacing = requireContext().dimen(R.dimen.default_dashboard_item_spacing).toInt()
        val decoration = EqualSpacingItemDecoration(
            topSpacing = spacing,
            leftSpacing = spacing,
            rightSpacing = spacing,
            bottomSpacing = 0,
            displayMode = GRID
        )

        desktopRecycler.apply {
            layoutManager = GridLayoutManager(context, COLUMN_COUNT)
            adapter = dashboardAdapter
            ItemTouchHelper(dndBehavior).attachToRecyclerView(this)
            addItemDecoration(decoration)
            setHasFixedSize(true)
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