package com.agileburo.anytype.ui.linking

import android.os.Bundle
import android.view.View
import androidx.core.view.minusAssign
import androidx.core.view.plusAssign
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.features.navigation.FilterView
import com.agileburo.anytype.core_ui.features.navigation.PageNavigationAdapter
import com.agileburo.anytype.core_ui.layout.AppBarLayoutStateChangeListener
import com.agileburo.anytype.core_ui.layout.State
import com.agileburo.anytype.core_utils.ext.*
import com.agileburo.anytype.core_utils.ui.ViewState
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.domain.block.model.Position
import com.agileburo.anytype.emojifier.Emojifier
import com.agileburo.anytype.presentation.linking.LinkToObjectViewModel
import com.agileburo.anytype.presentation.linking.LinkToObjectViewModelFactory
import com.agileburo.anytype.presentation.navigation.PageNavigationView
import com.agileburo.anytype.ui.base.ViewStateFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_link_to_object.*
import kotlinx.android.synthetic.main.view_link_to_object_bottom.*
import kotlinx.android.synthetic.main.view_link_to_object_preview.*
import kotlinx.android.synthetic.main.view_page_navigation_open_bottom.avatarSmall
import kotlinx.android.synthetic.main.view_page_navigation_open_bottom.pageTitleSmall
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class LinkToObjectFragment :
    ViewStateFragment<ViewState<PageNavigationView>>(R.layout.fragment_link_to_object) {

    private val position: Position
        get() {
            val args = requireArguments()
            val value = args.getString(POSITION_KEY)
            checkNotNull(value)
            return Position.valueOf(value)
        }

    private val replace: Boolean
        get() = arguments?.getBoolean(REPLACE_KEY) ?: false

    private val target: String
        get() = arguments?.getString(TARGET_ID_KEY) ?: throw IllegalStateException("Missing target")

    private val targetContext: String
        get() = arguments?.getString(CONTEXT_ID_KEY)
            ?: throw IllegalStateException("Missing target")

    private val vm by viewModels<LinkToObjectViewModel> { factory }

    @Inject
    lateinit var factory: LinkToObjectViewModelFactory

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.state.observe(viewLifecycleOwner, this)
        vm.navigation.observe(viewLifecycleOwner, navObserver)
        observeLinkingButtonState()
        vm.onViewCreated()
    }

    private fun observeLinkingButtonState() {
        lifecycleScope.launch {
            vm.isLinkingDisabled.collect { isDisabled ->
                if (isDisabled)
                    disableLinking()
                else
                    enableLinking()
            }
        }
    }

    private fun enableLinking() {
        btnLinkToThisObject.isEnabled = true
        btnLinkToObjectSmall.isEnabled = true
        btnLinkToThisObject.alpha = 1f
        btnLinkToObjectSmall.alpha = 1f
    }

    private fun disableLinking() {
        btnLinkToThisObject.isEnabled = false
        btnLinkToObjectSmall.isEnabled = false
        btnLinkToThisObject.alpha = 0.2f
        btnLinkToObjectSmall.alpha = 0.2f
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
                btnLinkToThisObject.setOnClickListener {
                    vm.onLinkToObjectClicked(
                        context = targetContext,
                        target = target,
                        replace = replace,
                        position = position
                    )
                }
                btnLinkToObjectSmall.setOnClickListener {
                    vm.onLinkToObjectClicked(
                        context = targetContext,
                        target = target,
                        replace = replace,
                        position = position
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
        componentManager().linkToObjectComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().linkToObjectComponent.release()
    }

    companion object {
        const val TARGET_ID_KEY = "arg.link_to.target"
        const val REPLACE_KEY = "arg.link_to.replace"
        const val POSITION_KEY = "arg.link_to.position"
        const val CONTEXT_ID_KEY = "arg.link_to.context"
        const val POSITION_FROM = 0
        const val POSITION_TO = 1
    }
}