package com.anytypeio.anytype.ui.desktop

import android.os.Bundle
import android.view.View
import android.view.View.OVER_SCROLL_NEVER
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.EqualSpacingItemDecoration
import com.anytypeio.anytype.core_utils.ui.EqualSpacingItemDecoration.Companion.GRID
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.desktop.DashboardView
import com.anytypeio.anytype.presentation.desktop.HomeDashboardStateMachine.State
import com.anytypeio.anytype.presentation.desktop.HomeDashboardViewModel
import com.anytypeio.anytype.presentation.desktop.HomeDashboardViewModelFactory
import com.anytypeio.anytype.presentation.extension.filterByNotArchivedPages
import com.anytypeio.anytype.ui.base.ViewStateFragment
import com.anytypeio.anytype.ui.page.PageFragment
import kotlinx.android.synthetic.main.fragment_desktop.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class HomeDashboardFragment : ViewStateFragment<State>(R.layout.fragment_desktop) {

    private val vm by viewModels<HomeDashboardViewModel> { factory }

    private val dndBehavior by lazy {
        DashboardDragAndDropBehavior(
            onItemMoved = { from, to ->
                dashboardAdapter
                    .onItemMove(from, to)
                    .also {
                        vm.onItemMoved(
                            views = dashboardAdapter.provideAdapterData(),
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
            onDocumentClicked = { target, isLoading -> vm.onDocumentClicked(target, isLoading) },
            onArchiveClicked = { vm.onArchivedClicked(it) }
        )
    }

    private val profileAdapter by lazy {
        DashboardProfileAdapter(
            data = mutableListOf(),
            onProfileClicked = { vm.onProfileClicked() }
        )
    }

    private val concatAdapter by lazy {
        ConcatAdapter(ProfileContainerAdapter(profileAdapter), dashboardAdapter)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()

        with(vm) {
            state.observe(viewLifecycleOwner, this@HomeDashboardFragment)
            navigation.observe(viewLifecycleOwner, navObserver)
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
        when {
            state.error != null -> {
                requireActivity().toast("Error: ${state.error}")
            }
            state.isInitialzed -> {
                bottomToolbar.visible()
                state.blocks.let { views ->
                    val profile = views.filterIsInstance<DashboardView.Profile>()
                    val links = views.filterByNotArchivedPages()
                    if (profile.isNotEmpty()) {
                        profileAdapter.update(profile)
                    }
                    dashboardAdapter.update(links)
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
            overScrollMode = OVER_SCROLL_NEVER
            layoutManager = GridLayoutManager(context, 2).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (position == 0) {
                            2
                        } else {
                            1
                        }
                    }
                }
            }

            adapter = concatAdapter
            ItemTouchHelper(dndBehavior).attachToRecyclerView(this)
            addItemDecoration(decoration)
            setHasFixedSize(true)
        }

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
