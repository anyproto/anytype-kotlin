package com.anytypeio.anytype.ui.moving

import android.os.Bundle
import android.view.View
import androidx.core.view.minusAssign
import androidx.core.view.plusAssign
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.features.navigation.FilterView
import com.anytypeio.anytype.core_ui.features.navigation.PageNavigationAdapter
import com.anytypeio.anytype.core_ui.layout.AppBarLayoutStateChangeListener
import com.anytypeio.anytype.core_ui.layout.State
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.moving.MoveToViewModel
import com.anytypeio.anytype.presentation.moving.MoveToViewModelFactory
import com.anytypeio.anytype.presentation.navigation.PageNavigationView
import com.anytypeio.anytype.ui.base.ViewStateFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_move_to.*
import kotlinx.android.synthetic.main.view_move_to_bottom.*
import kotlinx.android.synthetic.main.view_move_to_preview.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MoveToFragment : ViewStateFragment<ViewState<PageNavigationView>>(R.layout.fragment_move_to) {

    private val targets: List<String>
        get() = arguments?.getStringArrayList(TARGETS_ID_KEY)
            ?: throw IllegalStateException("Missing target")

    private val targetContext: String
        get() = arguments?.getString(CONTEXT_ID_KEY)
            ?: throw IllegalStateException("Missing target")

    private val vm by viewModels<MoveToViewModel> { factory }

    @Inject
    lateinit var factory: MoveToViewModelFactory

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.state.observe(viewLifecycleOwner, this)
        vm.navigation.observe(viewLifecycleOwner, navObserver)
        vm.onViewCreated()
        observeLinkingButtonState()
    }

    private fun observeLinkingButtonState() {
        lifecycleScope.launch {
            vm.isMovingDisabled.collect { isDisabled ->
                if (isDisabled)
                    disableMoving()
                else
                    enableMoving()
            }
        }
    }

    private fun enableMoving() {
        btnMoveToSmall.isEnabled = true
        btnMoveTo.isEnabled = true
        btnMoveTo.alpha = 1f
        btnMoveToSmall.alpha = 1f
    }

    private fun disableMoving() {
        btnMoveToSmall.isEnabled = false
        btnMoveTo.isEnabled = false
        btnMoveTo.alpha = 0.2f
        btnMoveToSmall.alpha = 0.2f
    }

    override fun render(state: ViewState<PageNavigationView>) {
        when (state) {
            ViewState.Init -> {
                appBarLayout.addOnOffsetChangedListener(
                    object : AppBarLayoutStateChangeListener() {
                        override fun onStateChanged(state: State) {
                            if (state == State.COLLAPSED) {
                                pagePreviewSmall.visible()
                            } else {
                                pagePreviewSmall.gone()
                            }
                        }
                    }
                )
                viewPager.adapter = PageNavigationAdapter(
                    onClick = { vm.onLinkClicked(it, targetContext) }
                ) { links ->
                    filterContainer.plusAssign(
                        FilterView(requireContext()).apply {
                            cancelClicked = { closeFilterView() }
                            pageClicked = {
                                closeFilterView()
                                vm.onLinkClicked(it, targetContext)
                            }
                            bind(links)
                        }
                    )
                }
                TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                    when (position) {
                        POSITION_FROM -> tab.text = getString(R.string.page_nav_link_from)
                        POSITION_TO -> tab.text = getString(R.string.page_nav_links_to)
                    }
                }.attach()
                btnMoveTo.setOnClickListener {
                    vm.onMoveToClicked(
                        context = targetContext,
                        targets = targets
                    )
                }
                btnMoveToSmall.setOnClickListener {
                    vm.onMoveToClicked(
                        context = targetContext,
                        targets = targets
                    )
                }
                vm.onStart(targetContext)
            }
            ViewState.Loading -> {
                progressBar.visible()
            }
            is ViewState.Success -> {
                progressBar.invisible()
                state.data.title.let { title ->
                    pageTitleSmall.text =
                        if (title.isEmpty()) getString(R.string.untitled) else title
                    tvPageTitle.text =
                        if (title.isEmpty()) getString(R.string.untitled) else title
                }
                if (state.data.subtitle.isNotEmpty()) {
                    tvPageSubtitle.visible()
                    tvPageSubtitle.text = state.data.subtitle
                } else {
                    tvPageSubtitle.text = null
                    tvPageSubtitle.gone()
                }

                imageIcon.setImageDrawable(null)
                avatarSmall.setImageDrawable(null)
                emojiIcon.setImageDrawable(null)

                state.data.image?.let { url ->
                    Glide
                        .with(imageIcon)
                        .load(url)
                        .centerInside()
                        .circleCrop()
                        .into(imageIcon)
                    Glide
                        .with(avatarSmall)
                        .load(url)
                        .centerInside()
                        .circleCrop()
                        .into(avatarSmall)
                }

                state.data.emoji?.let { emoji ->
                    try {
                        Glide
                            .with(emojiIcon)
                            .load(Emojifier.uri(emoji))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(emojiIcon)
                        Glide
                            .with(avatarSmall)
                            .load(Emojifier.uri(emoji))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(avatarSmall)
                    } catch (e: Exception) {
                        Timber.e(e, "Error while setting emoji icon for: $emoji")
                    }
                }

                (viewPager.adapter as? PageNavigationAdapter)?.setPageLinks(
                    inbound = state.data.inbound,
                    outbound = state.data.outbound
                )
            }
            is ViewState.Error -> {
                progressBar.invisible()
                coordinatorLayout.showSnackbar(state.error)
            }
        }
    }

    private fun closeFilterView() {
        filterContainer.getChildAt(0)?.let { filterContainer.minusAssign(it) }
        requireActivity().hideSoftInput()
    }

    override fun injectDependencies() {
        componentManager().moveToComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().moveToComponent.release()
    }

    companion object {
        const val TARGETS_ID_KEY = "arg.link_to.targets"
        const val CONTEXT_ID_KEY = "arg.link_to.context"
        const val POSITION_FROM = 0
        const val POSITION_TO = 1
    }
}