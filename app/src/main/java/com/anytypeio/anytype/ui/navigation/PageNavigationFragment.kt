package com.anytypeio.anytype.ui.navigation

import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.minusAssign
import androidx.core.view.plusAssign
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.features.navigation.FilterView
import com.anytypeio.anytype.core_ui.features.navigation.PageNavigationAdapter
import com.anytypeio.anytype.core_ui.layout.AppBarLayoutStateChangeListener
import com.anytypeio.anytype.core_ui.layout.State
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.navigation.PageNavigationView
import com.anytypeio.anytype.presentation.navigation.PageNavigationViewModel
import com.anytypeio.anytype.presentation.navigation.PageNavigationViewModelFactory
import com.anytypeio.anytype.ui.base.ViewStateFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_page_navigation.*
import kotlinx.android.synthetic.main.view_page_navigation_open_bottom.*
import kotlinx.android.synthetic.main.view_page_preview.*
import timber.log.Timber
import javax.inject.Inject

class PageNavigationFragment
    : ViewStateFragment<ViewState<PageNavigationView>>(R.layout.fragment_page_navigation) {

    @Inject
    lateinit var factory: PageNavigationViewModelFactory
    private val vm by viewModels<PageNavigationViewModel> { factory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.state.observe(viewLifecycleOwner, this)
        vm.navigation.observe(viewLifecycleOwner, navObserver)
        vm.onViewCreated()
    }

    override fun render(state: ViewState<PageNavigationView>) {
        when (state) {
            ViewState.Init -> {
                appBarLayout.addOnOffsetChangedListener(object : AppBarLayoutStateChangeListener() {
                    override fun onStateChanged(state: State) {
                        if (state == State.COLLAPSED) {
                            pagePreviewSmall.visible()
                        } else {
                            pagePreviewSmall.gone()
                        }
                    }
                })
                viewPager.adapter = PageNavigationAdapter(vm::onPageLinkClick) { links ->
                    val filterView = FilterView(requireContext()).apply {
                        cancelClicked = { closeFilterView() }
                        pageClicked = {
                            closeFilterView()
                            vm.onPageLinkClick(it)
                        }
                        bind(links)
                    }
                    filterContainer.plusAssign(filterView)
                    filterView.inputField.requestFocus()
                    context?.imm()?.showSoftInput(filterView.inputField, InputMethodManager.SHOW_FORCED)
                }
                viewPager.setCurrentItem(1, false)
                TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                    when (position) {
                        POSITION_FROM -> tab.text = getString(R.string.page_nav_link_from)
                        POSITION_TO -> tab.text = getString(R.string.page_nav_links_to)
                    }
                }.attach()
                btnOpenPage.setOnClickListener { vm.onOpenPageClicked() }
                btnOpenPageSmall.setOnClickListener { vm.onOpenPageClicked() }
                vm.onGetPageLinks(requireArguments().getString(TARGET_ID_KEY, ""))
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
        componentManager().navigationComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().navigationComponent.release()
    }

    companion object {
        const val TARGET_ID_KEY = "ID_KEY"
        const val POSITION_FROM = 0
        const val POSITION_TO = 1
    }
}