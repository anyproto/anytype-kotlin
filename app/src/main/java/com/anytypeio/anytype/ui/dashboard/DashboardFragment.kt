package com.anytypeio.anytype.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import androidx.viewpager2.widget.ViewPager2
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_ui.reactive.click
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.databinding.FragmentDashboardBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.dashboard.HomeDashboardViewModel
import com.anytypeio.anytype.presentation.dashboard.HomeDashboardViewModel.TAB
import com.anytypeio.anytype.presentation.dashboard.HomeDashboardViewModelFactory
import com.anytypeio.anytype.ui.base.NavigationFragment
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import javax.inject.Inject
import kotlinx.coroutines.launch
import timber.log.Timber

class DashboardFragment :
    NavigationFragment<FragmentDashboardBinding>(R.layout.fragment_dashboard) {

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
                clickAnimationSafely {
                    vm.onTabObjectClicked(
                        target,
                        isLoading,
                        TAB.FAVOURITE
                    )
                }
            },
            onArchiveClicked = { clickAnimationSafely { vm.onArchivedClicked(it) } },
            onObjectSetClicked = { clickAnimationSafely { vm.onObjectSetClicked(it) } }
        )
    }

    private val dashboardRecentAdapter by lazy {
        DashboardAdapter(
            data = mutableListOf(),
            onDocumentClicked = { target, isLoading ->
                clickAnimationSafely {
                    vm.onTabObjectClicked(
                        target,
                        isLoading,
                        TAB.RECENT
                    )
                }
            },
            onArchiveClicked = {},
            onObjectSetClicked = { clickAnimationSafely { vm.onObjectSetClicked(it) } }
        )
    }

    private val dashboardSharedAdapter by lazy {
        DashboardAdapter(
            data = mutableListOf(),
            onDocumentClicked = { target, isLoading ->
                clickAnimationSafely {
                    vm.onTabObjectClicked(
                        target,
                        isLoading,
                        TAB.SHARED
                    )
                }
            },
            onArchiveClicked = {},
            onObjectSetClicked = { clickAnimationSafely { vm.onObjectSetClicked(it) } }
        )
    }

    private val dashboardSetsAdapter by lazy {
        DashboardAdapter(
            data = mutableListOf(),
            onDocumentClicked = { _, _ -> },
            onArchiveClicked = {},
            onObjectSetClicked = { clickAnimationSafely { vm.onObjectSetClicked(it) } }
        )
    }

    private val dashboardArchiveAdapter by lazy {
        DashboardAdapter(
            data = mutableListOf(),
            onDocumentClicked = { target, isLoading ->
                clickAnimationSafely {
                    vm.onTabObjectClicked(
                        target,
                        isLoading,
                        TAB.BIN
                    )
                }
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

    private val tabToItem by lazy {
        buildMap {
            this[TAB.FAVOURITE] = TabItem(
                getString(R.string.favorites),
                DashboardPager.TYPE_FAVOURITES
            )
            this[TAB.RECENT] = TabItem(
                getString(R.string.recent),
                DashboardPager.TYPE_RECENT
            )
            this[TAB.SETS] = TabItem(
                getString(R.string.sets),
                DashboardPager.TYPE_SETS
            )
            this[TAB.SHARED] = TabItem(
                getString(R.string.shared),
                DashboardPager.TYPE_SHARED
            )
            this[TAB.BIN] = TabItem(
                getString(R.string.bin),
                DashboardPager.TYPE_BIN
            )
        }
    }

    private inline fun clickAnimationSafely(block: () -> Any) {
        if (motionListener.isSettled) {
            block()
        }
    }

    private val motionListener = DashboardMotionListener()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
        binding.dashboardRoot.progress = motionProgress
        binding.dashboardRoot.addTransitionListener(motionListener)
        with(vm) {
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
                    vm.favorites.collect { dashboardDefaultAdapter.update(it) }
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
                    vm.count.collect { count ->
                        val selectedObjectsText = resources.getQuantityString(
                            R.plurals.fragment_dashboard_selected_count,
                            count,
                            count
                        )
                        binding.tvSelectedCount.text = selectedObjectsText
                    }
                }
                launch {
                    vm.alerts.collect { alert ->
                        when (alert) {
                            is HomeDashboardViewModel.Alert.Delete -> {
                                val dialog = DeleteAlertFragment.new(alert.count)
                                dialog.onDeletionAccepted = { vm.onDeletionAccepted() }
                                dialog.showChildFragment()
                            }
                        }
                    }
                }
                launch {
                    vm.mode.collect { mode ->
                        when (mode) {
                            HomeDashboardViewModel.Mode.DEFAULT -> {
                                animateSelectionHiding()
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
                                tabToItem[tab] ?: throw IllegalStateException("Wrong tab!")
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

        if (BuildConfig.DEBUG) {
            // For debugging and testing widgets
            binding.widgets.visible()
            binding.widgets.setOnClickListener {
                findNavController().navigate(
                    R.id.homeScreen
                )
            }

            binding.formerDashboardWidgets.visible()
            binding.formerDashboardWidgets.setOnClickListener {
                findNavController().navigate(
                    R.id.homeScreenWidgets
                )
            }
        }
    }

    private fun animateSelectionHiding() {
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
        transitionSet.excludeChildren(binding.dashboardPager, true)
        TransitionManager.beginDelayedTransition(
            binding.dashboardRoot,
            transitionSet
        )
        set.applyTo(binding.dashboardRoot)
    }

    override fun onStart() {
        super.onStart()

        click(binding.btnAddDoc) { vm.onAddNewDocumentClicked() }
        click(binding.btnSearch) { vm.onPageSearchClicked() }
        click(binding.btnLibrary) { vm.onLibraryClicked() }
        click(binding.ivSettings) { vm.onSettingsClicked() }
        click(binding.avatarContainer) { vm.onAvatarClicked() }
        click(binding.tvCancel) { vm.onCancelSelectionClicked() }
        click(binding.tvSelectAll) { vm.onSelectAllClicked() }
        click(binding.tvRestore) { vm.onPutBackClicked() }
        click(binding.tvDelete) { vm.onDeleteObjectsClicked() }

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
            registerOnPageChangeCallback(
                object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        vm.onCancelSelectionClicked()
                    }
                })
        }
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

private class DashboardMotionListener : MotionLayout.TransitionListener {

    var isSettled = true
        private set

    override fun onTransitionStarted(
        motionLayout: MotionLayout?,
        startId: Int,
        endId: Int
    ) {
        isSettled = false
    }

    override fun onTransitionChange(
        motionLayout: MotionLayout?,
        startId: Int,
        endId: Int,
        progress: Float
    ) {
    }

    override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
        isSettled = true
    }

    override fun onTransitionTrigger(
        motionLayout: MotionLayout?,
        triggerId: Int,
        positive: Boolean,
        progress: Float
    ) {
    }
}