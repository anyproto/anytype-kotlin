package com.anytypeio.anytype.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.databinding.FragmentDashboardBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.dashboard.DashboardView
import com.anytypeio.anytype.presentation.dashboard.HomeDashboardStateMachine.State
import com.anytypeio.anytype.presentation.dashboard.HomeDashboardViewModel
import com.anytypeio.anytype.presentation.dashboard.HomeDashboardViewModel.TAB
import com.anytypeio.anytype.presentation.dashboard.HomeDashboardViewModelFactory
import com.anytypeio.anytype.ui.base.ViewStateFragment
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class DashboardFragment : ViewStateFragment<State, FragmentDashboardBinding>(R.layout.fragment_dashboard) {

    private val isMnemonicReminderDialogNeeded: Boolean?
        get() = argOrNull(
            SHOW_MNEMONIC_REMINDER_KEY
        )

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
            onDocumentClicked = { target, isLoading ->
                vm.onTabObjectClicked(
                    target,
                    isLoading,
                    TAB.FAVOURITE
                )
            },
            onArchiveClicked = { vm.onArchivedClicked(it) },
            onObjectSetClicked = { vm.onObjectSetClicked(it) }
        )
    }

    private val dashboardRecentAdapter by lazy {
        DashboardAdapter(
            data = mutableListOf(),
            onDocumentClicked = { target, isLoading ->
                vm.onTabObjectClicked(
                    target,
                    isLoading,
                    TAB.RECENT
                )
            },
            onArchiveClicked = {},
            onObjectSetClicked = { vm.onObjectSetClicked(it) }
        )
    }

    private val dashboardSharedAdapter by lazy {
        DashboardAdapter(
            data = mutableListOf(),
            onDocumentClicked = { target, isLoading ->
                vm.onTabObjectClicked(
                    target,
                    isLoading,
                    TAB.SHARED
                )
            },
            onArchiveClicked = {},
            onObjectSetClicked = { vm.onObjectSetClicked(it) }
        )
    }

    private val dashboardSetsAdapter by lazy {
        DashboardAdapter(
            data = mutableListOf(),
            onDocumentClicked = { _, _ -> },
            onArchiveClicked = {},
            onObjectSetClicked = { vm.onObjectSetClicked(it) }
        )
    }

    private val dashboardArchiveAdapter by lazy {
        DashboardAdapter(
            data = mutableListOf(),
            onDocumentClicked = { target, isLoading ->
                vm.onTabObjectClicked(
                    target,
                    isLoading,
                    TAB.BIN
                )
            },
            onArchiveClicked = {},
            onObjectSetClicked = {}
        )
    }

    private val dashboardPagerAdapter by lazy {
        DashboardPager(
            defaultAdapter = dashboardDefaultAdapter,
            recentAdapter = dashboardRecentAdapter,
            setsAdapter = dashboardSetsAdapter,
            archiveAdapter = dashboardArchiveAdapter,
            sharedAdapter = dashboardSharedAdapter,
            dndBehavior = dndBehavior,
            items = listOf()
        )
    }

    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            vm.sendTabEvent(tab?.text)
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {}
        override fun onTabReselected(tab: TabLayout.Tab?) {}
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
        binding.dashboardRoot.progress = motionProgress
        with(vm) {
            state.observe(viewLifecycleOwner, this@DashboardFragment)
            navigation.observe(viewLifecycleOwner, navObserver)
        }
        parseIntent()

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.toasts.collect { toast(it) }
                }
                launch {
                    vm.recent.collect { dashboardRecentAdapter.update(it) }
                }
                launch {
                    vm.shared.collect { dashboardSharedAdapter.update(it) }
                }
                launch {
                    vm.sets.collect { dashboardSetsAdapter.update(it) }
                }
                launch {
                    vm.archived.collect { dashboardArchiveAdapter.update(it) }
                }
                launch {
                    vm.count.collect { binding.tvSelectedCount.text = "$it object selected" }
                }
                launch {
                    vm.alerts.collect { alert ->
                        when (alert) {
                            is HomeDashboardViewModel.Alert.Delete -> {
                                val dialog = DeleteAlertFragment.new(alert.count)
                                dialog.onDeletionAccepted = { vm.onDeletionAccepted() }
                                dialog.show(childFragmentManager, null)
                            }
                        }
                    }
                }
                launch {
                    vm.mode.collect { mode ->
                        when (mode) {
                            HomeDashboardViewModel.Mode.DEFAULT -> {
                                binding.selectionTopToolbar.invisible()
                                binding.tabsLayout.visible()
                                val set = ConstraintSet().apply {
                                    clone(binding.dashboardRoot)
                                    clear(R.id.selectionBottomToolbar, ConstraintSet.BOTTOM)
                                    connect(
                                        R.id.selectionBottomToolbar,
                                        ConstraintSet.TOP,
                                        R.id.dashboardRoot,
                                        ConstraintSet.BOTTOM
                                    )
                                }
                                val transitionSet = TransitionSet().apply {
                                    addTransition(ChangeBounds())
                                    duration = 100
                                }
                                TransitionManager.beginDelayedTransition(
                                    binding.dashboardRoot,
                                    transitionSet
                                )
                                set.applyTo(binding.dashboardRoot)
                            }
                            HomeDashboardViewModel.Mode.SELECTION -> {
                                binding.tabsLayout.invisible()
                                binding.selectionTopToolbar.visible()
                                val set = ConstraintSet().apply {
                                    clone(binding.dashboardRoot)
                                    clear(R.id.selectionBottomToolbar, ConstraintSet.TOP)
                                    connect(
                                        R.id.selectionBottomToolbar,
                                        ConstraintSet.BOTTOM,
                                        R.id.dashboardRoot,
                                        ConstraintSet.BOTTOM
                                    )
                                }
                                val transitionSet = TransitionSet().apply {
                                    addTransition(ChangeBounds())
                                    duration = 100
                                }
                                TransitionManager.beginDelayedTransition(
                                    binding.dashboardRoot,
                                    transitionSet
                                )
                                set.applyTo(binding.dashboardRoot)
                            }
                        }
                    }
                }
                launch {
                    vm.tabs.collect { tabs ->
                        dashboardPagerAdapter.setItems(
                            tabs.map { tab ->
                                when (tab) {
                                    TAB.FAVOURITE -> TabItem(
                                        getString(R.string.favorites),
                                        DashboardPager.TYPE_FAVOURITES
                                    )
                                    TAB.RECENT -> TabItem(
                                        getString(R.string.history),
                                        DashboardPager.TYPE_RECENT
                                    )
                                    TAB.SETS -> TabItem(
                                        getString(R.string.sets),
                                        DashboardPager.TYPE_SETS
                                    )
                                    TAB.SHARED -> TabItem(
                                        getString(R.string.shared),
                                        DashboardPager.TYPE_SHARED
                                    )
                                    TAB.BIN -> TabItem(
                                        getString(R.string.bin),
                                        DashboardPager.TYPE_BIN
                                    )
                                }
                            }
                        )
                    }
                }
                launch {
                    vm.isDeletionInProgress.collect { isDeletionInProgress ->
                        if (isDeletionInProgress) {
                            binding.objectRemovalProgressBar.visible()
                        } else {
                            binding.objectRemovalProgressBar.gone()
                        }
                    }
                }
                launch {
                    vm.profile.collect { profile -> setProfile(profile) }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        vm.onStart()
    }

    private fun setProfile(profile: ViewState<ObjectWrapper.Basic>) {
        when (profile) {
            is ViewState.Success -> {
                val obj = profile.data
                binding.avatarContainer.bind(
                    name = obj.name.orEmpty(),
                    color = context?.getColor(R.color.dashboard_default_avatar_circle_color)
                )
                obj.iconImage?.let { avatar ->
                    binding.avatarContainer.icon(builder.image(avatar))
                }
                if (obj.name.isNullOrEmpty()) {
                    binding.tvGreeting.text = getText(R.string.greet_user)
                } else {
                    binding.tvGreeting.text = getString(R.string.greet, obj.name)
                }
            }
            else -> {
                // TODO reset profile view to zero
            }
        }
    }

    override fun onPause() {
        super.onPause()
        motionProgress = binding.dashboardRoot.progress
        binding.tabsLayout.removeOnTabSelectedListener(onTabSelectedListener)
    }

    override fun onStop() {
        super.onStop()
        vm.onStop()
        jobs.cancel()
    }

    override fun onResume() {
        super.onResume()
        binding.tabsLayout.apply {
            addOnTabSelectedListener(onTabSelectedListener)
        }
        if (isMnemonicReminderDialogNeeded == true) {
            showMnemonicReminderAlert()
        }
    }

    private fun showMnemonicReminderAlert() {
        arguments?.remove(SHOW_MNEMONIC_REMINDER_KEY)
        findNavController().navigate(R.id.dashboardKeychainDialog)
    }

    private fun parseIntent() {
        val deepLinkPage = arguments?.getString(EditorFragment.ID_KEY, null)
        if (deepLinkPage != null) {
            arguments?.remove(EditorFragment.ID_KEY)
            vm.onNavigationDeepLink(deepLinkPage)
        } else {
            vm.onViewCreated()
        }
    }

    override fun render(state: State) {
        when {
            state.error != null -> toast("Error: ${state.error}")
            state.isInitialzed -> {
                val links =
                    state.blocks.filter { it !is DashboardView.Archive }.groupBy { it.isArchived }
                dashboardDefaultAdapter.update(links[false] ?: emptyList())
            }
        }
    }

    private fun setup() {

        binding.tabsLayout.apply {
            tabMode = TabLayout.MODE_SCROLLABLE
        }

        binding.dashboardPager.apply {
            adapter = dashboardPagerAdapter
            TabLayoutMediator(binding.tabsLayout, binding.dashboardPager) { tab, position ->
                tab.text = dashboardPagerAdapter.getTitle(position)
            }.attach()
        }

        binding.btnAddDoc
            .clicks()
            .onEach { vm.onAddNewDocumentClicked() }
            .launchIn(lifecycleScope)

        binding.btnSearch
            .clicks()
            .onEach { vm.onPageSearchClicked() }
            .launchIn(lifecycleScope)

        binding.btnMarketplace
            .clicks()
            .onEach { toast(getString(R.string.coming_soon)) }
            .launchIn(lifecycleScope)

        binding.ivSettings
            .clicks()
            .throttleFirst()
            .onEach { vm.onSettingsClicked() }
            .launchIn(lifecycleScope)

        binding.avatarContainer
            .clicks()
            .onEach { vm.onAvatarClicked() }
            .launchIn(lifecycleScope)

        binding.tvCancel
            .clicks()
            .onEach { vm.onCancelSelectionClicked() }
            .launchIn(lifecycleScope)

        binding.tvSelectAll
            .clicks()
            .onEach { vm.onSelectAllClicked() }
            .launchIn(lifecycleScope)

        binding.tvPutBack
            .clicks()
            .onEach { vm.onPutBackClicked() }
            .launchIn(lifecycleScope)

        binding.tvDelete
            .clicks()
            .onEach { vm.onDeleteObjectsClicked() }
            .launchIn(lifecycleScope)
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDashboardBinding = FragmentDashboardBinding.inflate(
        inflater, container, false
    )

    override fun injectDependencies() {
        componentManager().dashboardComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().dashboardComponent.release()
    }

    companion object {
        const val SHOW_MNEMONIC_REMINDER_KEY = "arg.dashboard.is-mnemonic-reminder-dialog-needed"
    }
}

data class TabItem(val title: String, val type: Int)