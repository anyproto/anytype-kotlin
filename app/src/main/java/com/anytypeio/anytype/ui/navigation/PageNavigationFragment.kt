package com.anytypeio.anytype.ui.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.anytypeio.anytype.databinding.FragmentPageNavigationBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.navigation.PageNavigationView
import com.anytypeio.anytype.presentation.navigation.PageNavigationViewModel
import com.anytypeio.anytype.presentation.navigation.PageNavigationViewModelFactory
import com.anytypeio.anytype.ui.base.ViewStateFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.tabs.TabLayoutMediator
import timber.log.Timber
import javax.inject.Inject

@Deprecated("Legacy screen, is not used")
class PageNavigationFragment
    : ViewStateFragment<ViewState<PageNavigationView>, FragmentPageNavigationBinding>(R.layout.fragment_page_navigation) {

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
                binding.appBarLayout.addOnOffsetChangedListener(object : AppBarLayoutStateChangeListener() {
                    override fun onStateChanged(state: State) {
                        if (state == State.COLLAPSED) {
                            binding.pagePreviewSmall.root.visible()
                        } else {
                            binding.pagePreviewSmall.root.gone()
                        }
                    }
                })
                binding.viewPager.adapter = PageNavigationAdapter(vm::onPageLinkClick) { links ->
                    val filterView = FilterView(requireContext()).apply {
                        cancelClicked = { closeFilterView() }
                        pageClicked = {
                            closeFilterView()
                            vm.onPageLinkClick(it)
                        }
                        bind(links)
                    }
                    binding.filterContainer.plusAssign(filterView)
                    filterView.inputField.requestFocus()
                    context?.imm()?.showSoftInput(filterView.inputField, InputMethodManager.SHOW_FORCED)
                }
                binding.viewPager.setCurrentItem(1, false)
                TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                    when (position) {
                        POSITION_FROM -> tab.text = getString(R.string.page_nav_link_from)
                        POSITION_TO -> tab.text = getString(R.string.page_nav_links_to)
                    }
                }.attach()
                binding.pagePreviewContainer.btnOpenPage.setOnClickListener { vm.onOpenPageClicked() }
                binding.pagePreviewSmall.btnOpenPageSmall.setOnClickListener { vm.onOpenPageClicked() }
                vm.onGetPageLinks(requireArguments().getString(TARGET_ID_KEY, ""))
            }
            ViewState.Loading -> {
                binding.progressBar.visible()
            }
            is ViewState.Success -> {
                binding.progressBar.invisible()
                state.data.title.let { title ->
                    binding.pagePreviewSmall.pageTitleSmall.text =
                        title.ifEmpty { getString(R.string.untitled) }
                    binding.pagePreviewContainer.tvPageTitle.text =
                        title.ifEmpty { getString(R.string.untitled) }
                }
                if (state.data.subtitle.isNotEmpty()) {
                    binding.pagePreviewContainer.tvPageSubtitle.visible()
                    binding.pagePreviewContainer.tvPageSubtitle.text = state.data.subtitle
                } else {
                    binding.pagePreviewContainer.tvPageSubtitle.text = null
                    binding.pagePreviewContainer.tvPageSubtitle.gone()
                }

                binding.pagePreviewContainer.imageIcon.setImageDrawable(null)
                binding.pagePreviewSmall.avatarSmall.setImageDrawable(null)
                binding.pagePreviewContainer.emojiIcon.setImageDrawable(null)

                state.data.image?.let { url ->
                    Glide
                        .with(binding.pagePreviewContainer.imageIcon)
                        .load(url)
                        .centerInside()
                        .circleCrop()
                        .into(binding.pagePreviewContainer.imageIcon)
                    Glide
                        .with(binding.pagePreviewSmall.avatarSmall)
                        .load(url)
                        .centerInside()
                        .circleCrop()
                        .into(binding.pagePreviewSmall.avatarSmall)
                }

                state.data.emoji?.let { emoji ->
                    try {
                        Glide
                            .with(binding.pagePreviewContainer.emojiIcon)
                            .load(Emojifier.uri(emoji))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(binding.pagePreviewContainer.emojiIcon)
                        Glide
                            .with(binding.pagePreviewSmall.avatarSmall)
                            .load(Emojifier.uri(emoji))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(binding.pagePreviewSmall.avatarSmall)
                    } catch (e: Exception) {
                        Timber.e(e, "Error while setting emoji icon for: $emoji")
                    }
                }

                (binding.viewPager.adapter as? PageNavigationAdapter)?.setPageLinks(
                    inbound = state.data.inbound,
                    outbound = state.data.outbound
                )
            }
            is ViewState.Error -> {
                binding.progressBar.invisible()
                binding.coordinatorLayout.showSnackbar(state.error)
            }
        }
    }

    private fun closeFilterView() {
        binding.filterContainer.getChildAt(0)?.let { binding.filterContainer.minusAssign(it) }
        requireActivity().hideSoftInput()
    }

    override fun injectDependencies() {
        componentManager().navigationComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().navigationComponent.release()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPageNavigationBinding = FragmentPageNavigationBinding.inflate(
        inflater, container, false
    )

    companion object {
        const val TARGET_ID_KEY = "ID_KEY"
        const val POSITION_FROM = 0
        const val POSITION_TO = 1
    }
}