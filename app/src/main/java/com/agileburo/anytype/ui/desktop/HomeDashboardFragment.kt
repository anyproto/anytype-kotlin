package com.agileburo.anytype.ui.desktop

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.extensions.avatarColor
import com.agileburo.anytype.core_ui.tools.DefaultDragAndDropBehavior
import com.agileburo.anytype.core_utils.ext.*
import com.agileburo.anytype.core_utils.ui.EqualSpacingItemDecoration
import com.agileburo.anytype.core_utils.ui.EqualSpacingItemDecoration.Companion.GRID
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.presentation.desktop.HomeDashboardStateMachine.State
import com.agileburo.anytype.presentation.desktop.HomeDashboardViewModel
import com.agileburo.anytype.presentation.desktop.HomeDashboardViewModelFactory
import com.agileburo.anytype.presentation.mapper.toView
import com.agileburo.anytype.presentation.profile.ProfileView
import com.agileburo.anytype.ui.base.ViewStateFragment
import com.agileburo.anytype.ui.page.PageFragment
import kotlinx.android.synthetic.main.fragment_desktop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class HomeDashboardFragment : ViewStateFragment<State>(R.layout.fragment_desktop) {

    private val profileObserver = Observer<ProfileView> { profile ->
        greeting.text = getString(R.string.greet, profile.name)
        val pos = profile.name.firstDigitByHash()
        avatar.bind(
            name = profile.name,
            color = requireContext().avatarColor(pos)
        )
        profile.avatar?.let { avatar.icon(it) }
    }

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
    lateinit var builder: UrlBuilder

    private val dashboardAdapter by lazy {
        DashboardAdapter(
            data = mutableListOf(),
            onDocumentClicked = { vm.onDocumentClicked(it.target) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()

        with(vm) {
            state.observe(viewLifecycleOwner, this@HomeDashboardFragment)
            navigation.observe(viewLifecycleOwner, navObserver)
            profile.observe(viewLifecycleOwner, profileObserver)
        }
        
        parseIntent()
    }

    private fun parseIntent() {
        val deepLinkPage = arguments?.getString(PageFragment.ID_KEY, null)
        if (deepLinkPage != null) {
            arguments?.remove(PageFragment.ID_KEY)

            vm.onNavigationDeepLink(deepLinkPage)
        } else {
            vm.onViewCreated()
        }
    }

    override fun render(state: State) {
        Timber.d("Rendering state: $state")
        when {
            state.isLoading -> {
                bottomToolbar.invisible()
            }
            state.error != null -> {
                requireActivity().toast("Error: ${state.error}")
            }
            state.dashboard != null -> {
                bottomToolbar.visible()
                state.dashboard?.let { dashboard ->
                    lifecycleScope.launch {
                        val result = withContext(Dispatchers.IO) {
                            dashboard.toView(builder)
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

        avatar.setOnClickListener { vm.onProfileClicked() }

        bottomToolbar
            .navigationClicks()
            .onEach { vm.onPageNavigationClicked() }
            .launchIn(lifecycleScope)

        bottomToolbar
            .addPageClick()
            .onEach { vm.onAddNewDocumentClicked() }
            .launchIn(lifecycleScope)

        bottomToolbar
            .searchClicks()
            .onEach { vm.onPageSearchClicked() }
            .launchIn(lifecycleScope)
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
