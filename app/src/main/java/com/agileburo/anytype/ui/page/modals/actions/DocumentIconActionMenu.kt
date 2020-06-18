package com.agileburo.anytype.ui.page.modals.actions

import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.agileburo.anytype.R
import com.agileburo.anytype.core_utils.ext.toast
import com.agileburo.anytype.core_utils.ui.BaseFragment
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.library_page_icon_picker_widget.ui.ActionMenuAdapter
import com.agileburo.anytype.library_page_icon_picker_widget.ui.ActionMenuAdapter.Companion.OPTION_CHOOSE_EMOJI
import com.agileburo.anytype.library_page_icon_picker_widget.ui.ActionMenuAdapter.Companion.OPTION_CHOOSE_RANDOM_EMOJI
import com.agileburo.anytype.library_page_icon_picker_widget.ui.ActionMenuAdapter.Companion.OPTION_CHOOSE_UPLOAD_PHOTO
import com.agileburo.anytype.library_page_icon_picker_widget.ui.ActionMenuAdapter.Companion.OPTION_REMOVE
import com.agileburo.anytype.library_page_icon_picker_widget.ui.ActionMenuDivider
import com.agileburo.anytype.presentation.page.picker.DocumentIconPickerViewModel
import com.agileburo.anytype.presentation.page.picker.PageIconPickerViewModelFactory
import com.agileburo.anytype.ui.page.modals.DocumentEmojiIconPickerFragment
import kotlinx.android.synthetic.main.action_toolbar_page_icon.*
import javax.inject.Inject

class DocumentIconActionMenu : BaseFragment(R.layout.action_toolbar_page_icon),
    Observer<DocumentIconPickerViewModel.ViewState> {

    private val target: String
        get() = requireArguments()
            .getString(ARG_TARGET_ID_KEY)
            ?: throw IllegalStateException(MISSING_TARGET_ERROR)

    @Inject
    lateinit var factory: PageIconPickerViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(DocumentIconPickerViewModel::class.java)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm.state.observe(viewLifecycleOwner, this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        container.setOnClickListener { exit() }

        arguments?.getString(EMOJI_KEY)?.let { emoji -> logo.text = emoji }

        setupLogoTranslation()
        setupAdapter()
        showMenuWithAnimation()
    }

    private fun setupLogoTranslation() {
        val y = arguments?.getFloat(Y_KEY)
        if (y != null && y != 0.0f) {
            val delta = y - logo.y
            logo.y = y
            menu.y = menu.y + delta
        }
    }

    private fun showMenuWithAnimation() {
        val xAnim = menu.animate().scaleX(1f).apply {
            duration = ANIM_DURATION
            startDelay = ANIM_START_DELAY
            interpolator = OvershootInterpolator()
        }
        val yAnim = menu.animate().scaleY(1f).apply {
            duration = ANIM_DURATION
            startDelay = ANIM_START_DELAY
            interpolator = OvershootInterpolator()
        }

        xAnim.start()
        yAnim.start()
    }

    private fun setupAdapter() {

        val drawable = requireContext().getDrawable(R.drawable.action_menu_divider)

        checkNotNull(drawable)

        val divider = ActionMenuDivider(drawable)

        recycler.apply {
            addItemDecoration(divider)
            adapter = ActionMenuAdapter(
                options = intArrayOf(
                    OPTION_CHOOSE_EMOJI,
                    OPTION_CHOOSE_RANDOM_EMOJI,
                    OPTION_CHOOSE_UPLOAD_PHOTO,
                    OPTION_REMOVE
                )
            ) { option ->
                when (option) {
                    OPTION_CHOOSE_EMOJI -> {
                        parentFragment?.childFragmentManager?.let { manager ->
                            manager.popBackStack()
                            DocumentEmojiIconPickerFragment.newInstance(
                                context = target,
                                target = target
                            ).show(manager, null)
                        }
                    }
                    OPTION_REMOVE -> vm.onEvent(
                        DocumentIconPickerViewModel.Contract.Event.OnRemoveEmojiSelected(
                            context = target,
                            target = target
                        )
                    )
                    OPTION_CHOOSE_RANDOM_EMOJI -> vm.onEvent(
                        DocumentIconPickerViewModel.Contract.Event.OnSetRandomEmojiClicked(
                            context = target,
                            target = target
                        )
                    )
                    else -> toast("Not implemented")
                }
            }
        }
    }

    override fun onChanged(state: DocumentIconPickerViewModel.ViewState) {
        when (state) {
            is DocumentIconPickerViewModel.ViewState.Exit -> exit()
            is DocumentIconPickerViewModel.ViewState.Error -> toast(state.message)
        }
    }

    private fun exit() {
        parentFragment?.childFragmentManager?.popBackStack()
    }

    override fun injectDependencies() {
        componentManager().pageIconPickerSubComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().pageIconPickerSubComponent.release()
    }

    companion object {
        fun new(
            y: Float?,
            emoji: String?,
            target: String
        ): DocumentIconActionMenu = DocumentIconActionMenu().apply {
            arguments = bundleOf(
                Y_KEY to y,
                EMOJI_KEY to emoji,
                ARG_TARGET_ID_KEY to target
            )
        }

        private const val Y_KEY = "y"
        private const val EMOJI_KEY = "emoji"
        private const val ANIM_START_DELAY = 200L
        private const val ANIM_DURATION = 200L
        private const val ARG_TARGET_ID_KEY = "arg.picker.target.id"
        private const val MISSING_TARGET_ERROR = "Missing target id"
    }
}