package com.anytypeio.anytype.ui.desktop

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.desktop.DashboardView
import com.anytypeio.anytype.presentation.desktop.HomeDashboardStateMachine.State
import com.anytypeio.anytype.presentation.desktop.HomeDashboardViewModel
import com.anytypeio.anytype.presentation.desktop.HomeDashboardViewModelFactory
import com.anytypeio.anytype.ui.base.ViewStateFragment
import com.anytypeio.anytype.ui.page.PageFragment
import kotlinx.android.synthetic.experimental.fragment_desktop.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

class HomeDashboardFragment : ViewStateFragment<State>(R.layout.fragment_desktop) {

    private val vm by viewModels<HomeDashboardViewModel> { factory }

    private var motionProgress = 0f // 0f being initial state

    private val dndBehavior by lazy {
        DashboardDragAndDropBehavior(
            onItemMoved = { from, to ->
                dashboardDefaultAdapter
                    .onItemMove(from, to)
                    .also {
                        vm.onItemMoved(
                            views = dashboardDefaultAdapter.provideAdapterData(),
                            from = from,
                            to = to
                        )
                    }
            },
            onItemDropped = { index ->
                try {
                    vm.onItemDropped(dashboardDefaultAdapter.provideAdapterData()[index])
                } catch (e: Exception) {
                    Timber.e(e, "Error while dropping item at index: $index")
                }
            }
        )
    }

    @Inject
    lateinit var factory: HomeDashboardViewModelFactory

    @Inject
    lateinit var builder: UrlBuilder

    private val dashboardDefaultAdapter by lazy {
        DashboardAdapter(
            data = mutableListOf(),
            onDocumentClicked = { target, isLoading -> vm.onDocumentClicked(target, isLoading) },
            onArchiveClicked = { vm.onArchivedClicked(it) },
            onObjectSetClicked = { vm.onObjectSetClicked(it) }
        )
    }

    private val dashboardArchiveAdapter by lazy {
        DashboardAdapter(
            data = mutableListOf(),
            onDocumentClicked = { target, isLoading -> vm.onDocumentClicked(target, isLoading) },
            onArchiveClicked = {},
            onObjectSetClicked = {}
        )
    }

    private val dashboardPagerAdapter by lazy {
        DashboardPager(dashboardDefaultAdapter, dashboardArchiveAdapter, dndBehavior)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
        dashboardRoot.progress = motionProgress
        with(vm) {
            state.observe(viewLifecycleOwner, this@HomeDashboardFragment)
            navigation.observe(viewLifecycleOwner, navObserver)
        }
        parseIntent()
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.subscribe(vm.toasts) { toast(it) }
        lifecycleScope.subscribe(vm.archived) { dashboardArchiveAdapter.update(it) }
    }

    override fun onPause() {
        super.onPause()
        motionProgress = dashboardRoot.progress
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
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
                    val links = views.filter { it !is DashboardView.Profile && it !is DashboardView.Archive }.groupBy { it.isArchived }
                    if (profile.isNotEmpty()) {
                        val view = profile.first()
                        avatarContainer.bind(
                            name = view.name,
                            color = context?.getColor(R.color.dashboard_default_avatar_circle_color)
                        )
                        view.avatar?.let { avatar ->
                            avatarContainer.icon(avatar)
                        }
                        if (view.name.isNotEmpty()) {
                            tvGreeting.text = getString(R.string.greet, view.name)
                        } else {
                            tvGreeting.text = getText(R.string.greet_user)
                        }
                    }
                    // TODO refact (no need to filter anything in fragment)
                    dashboardDefaultAdapter.update(links[false] ?: emptyList())
                }
            }
        }
    }

    private fun setup() {

        val callback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabFavourite.isSelected = position == INDEX_FAVORITES
                tabBin.isSelected = position == INDEX_BIN
            }
        }

        tabFavourite.setOnClickListener {
            dashboardPager.setCurrentItem(INDEX_FAVORITES, true)
        }

        tabBin.setOnClickListener {
            dashboardPager.setCurrentItem(INDEX_BIN, true)
        }

        dashboardPager.apply {
            adapter = dashboardPagerAdapter
            registerOnPageChangeCallback(callback)
        }

        bottomToolbar
            .navigationClicks()
            .onEach { vm.onPageNavigationClicked() }
            .launchIn(lifecycleScope)

        btnAddDoc
            .clicks()
            .onEach { vm.onAddNewDocumentClicked() }
            .launchIn(lifecycleScope)

        btnSearch
            .clicks()
            .onEach { vm.onPageSearchClicked() }
            .launchIn(lifecycleScope)

        btnMarketplace
            .clicks()
            .onEach { toast(getString(R.string.coming_soon)) }
            .launchIn(lifecycleScope)

        ivSettings
            .clicks()
            .onEach { vm.onProfileClicked() }
            .launchIn(lifecycleScope)

        avatarContainer
            .clicks()
            .onEach { vm.onAvatarClicked() }
            .launchIn(lifecycleScope)
    }

    override fun injectDependencies() {
        componentManager().desktopComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().desktopComponent.release()
    }

    companion object {
        const val INDEX_FAVORITES = 0
        const val INDEX_BIN = 1
    }
}