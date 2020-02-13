package com.agileburo.anytype.ui.page

import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.extensions.invisible
import com.agileburo.anytype.core_ui.extensions.visible
import com.agileburo.anytype.core_ui.features.page.BlockAdapter
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.reactive.clicks
import com.agileburo.anytype.core_ui.state.ControlPanelState
import com.agileburo.anytype.core_ui.tools.OutsideClickDetector
import com.agileburo.anytype.core_ui.widgets.toolbar.ActionToolbarWidget.ActionConfig.ACTION_DELETE
import com.agileburo.anytype.core_ui.widgets.toolbar.ActionToolbarWidget.ActionConfig.ACTION_DUPLICATE
import com.agileburo.anytype.core_ui.widgets.toolbar.ColorToolbarWidget
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.Option
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_LIST_BULLETED_LIST
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_LIST_CHECKBOX
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_LIST_NUMBERED_LIST
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_TEXT_HEADER_ONE
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_TEXT_HEADER_THREE
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_TEXT_HEADER_TWO
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_TEXT_HIGHLIGHTED
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_TEXT_TEXT
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_TOOL_PAGE
import com.agileburo.anytype.core_utils.ext.gone
import com.agileburo.anytype.core_utils.ext.hexColorCode
import com.agileburo.anytype.core_utils.ext.hideSoftInput
import com.agileburo.anytype.core_utils.ext.toast
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.domain.block.model.Block.Content.Text
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.ext.getFirstLinkMarkupParam
import com.agileburo.anytype.domain.ext.getSubstring
import com.agileburo.anytype.ext.extractMarks
import com.agileburo.anytype.presentation.page.PageViewModel
import com.agileburo.anytype.presentation.page.PageViewModelFactory
import com.agileburo.anytype.ui.base.NavigationFragment
import com.agileburo.anytype.ui.page.modals.LinkFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.fragment_page.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


class PageFragment : NavigationFragment(R.layout.fragment_page), OnFragmentInteractionListener {

    private val pageAdapter by lazy {
        BlockAdapter(
            blocks = mutableListOf(),
            onTextChanged = { id, editable ->
                vm.onTextChanged(
                    id = id,
                    text = editable.toString(),
                    marks = editable.extractMarks()
                )
            },
            onSelectionChanged = vm::onSelectionChanged,
            onCheckboxClicked = vm::onCheckboxClicked,
            onFocusChanged = vm::onBlockFocusChanged,
            onSplitLineEnterClicked = vm::onSplitLineEnterClicked,
            onEndLineEnterClicked = { id, editable ->
                vm.onEndLineEnterClicked(
                    id = id,
                    text = editable.toString(),
                    marks = editable.extractMarks()
                )
            },
            onEmptyBlockBackspaceClicked = vm::onEmptyBlockBackspaceClicked,
            onNonEmptyBlockBackspaceClicked = vm::onNonEmptyBlockBackspaceClicked,
            onFooterClicked = vm::onOutsideClicked
        )
    }

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(PageViewModel::class.java)
    }

    @Inject
    lateinit var factory: PageViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vm.open(requireArguments().getString(ID_KEY, ID_EMPTY_VALUE))
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(this) {
                vm.onSystemBackPressed()
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler.addOnItemTouchListener(
            OutsideClickDetector(vm::onOutsideClicked)
        )

        BottomSheetBehavior.from(sheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
        }

        recycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = pageAdapter
        }

        toolbar
            .keyboardClicks()
            .onEach { vm.onHideKeyboardClicked() }
            .launchIn(lifecycleScope)

        toolbar
            .turnIntoClicks()
            .onEach { vm.onTurnIntoToolbarToggleClicked() }
            .launchIn(lifecycleScope)

        toolbar
            .colorClicks()
            .onEach { vm.onColorToolbarToogleClicked() }
            .launchIn(lifecycleScope)

        toolbar
            .addButtonClicks()
            .onEach { vm.onAddBlockToolbarClicked() }
            .launchIn(lifecycleScope)

        toolbar
            .actionClicks()
            .onEach { vm.onActionToolbarClicked() }
            .launchIn(lifecycleScope)

        markupToolbar
            .markupClicks()
            .onEach { vm.onMarkupActionClicked(it) }
            .launchIn(lifecycleScope)

        markupToolbar
            .hideKeyboardClicks()
            .onEach { vm.onHideKeyboardClicked() }
            .launchIn(lifecycleScope)

        markupToolbar
            .colorClicks()
            .onEach { vm.onMarkupToolbarColorClicked() }
            .launchIn(lifecycleScope)

        colorToolbar
            .observeClicks()
            .onEach { click ->
                when (click) {
                    is ColorToolbarWidget.Click.OnTextColorClicked -> {
                        handleTextColorClick(click)
                    }
                    is ColorToolbarWidget.Click.OnBackgroundColorClicked -> {
                        handleBackgroundColorClicked(click)
                    }
                    else -> {
                        toast(NOT_IMPLEMENTED_MESSAGE)
                    }

                }
            }
            .launchIn(lifecycleScope)

        optionToolbar
            .optionClicks()
            .onEach { option ->
                if (optionToolbar.state == OptionToolbarWidget.State.ADD_BLOCK)
                    handleAddBlockToolbarOptionClicked(option)
                else if (optionToolbar.state == OptionToolbarWidget.State.TURN_INTO)
                    handleTurnIntoToolbarOptionClicked(option)
            }
            .launchIn(lifecycleScope)

        actionToolbar
            .actionClicks()
            .onEach { action ->
                when (action.type) {
                    ACTION_DELETE -> vm.onActionDeleteClicked()
                    ACTION_DUPLICATE -> vm.onActionDuplicateClicked()
                    else -> toast(NOT_IMPLEMENTED_MESSAGE)
                }
            }
            .launchIn(lifecycleScope)

        fab.clicks().onEach { toast(NOT_IMPLEMENTED_MESSAGE) }.launchIn(lifecycleScope)
    }

    private fun handleTextColorClick(click: ColorToolbarWidget.Click.OnTextColorClicked) =
        when (colorToolbar.state) {
            ColorToolbarWidget.State.SELECTION -> {
                vm.onMarkupTextColorAction(click.color.hexColorCode())
            }
            ColorToolbarWidget.State.BLOCK -> {
                vm.onToolbarTextColorAction(click.color.hexColorCode())
            }
            else -> toast(NOT_IMPLEMENTED_MESSAGE)
        }

    private fun handleBackgroundColorClicked(click: ColorToolbarWidget.Click.OnBackgroundColorClicked) =
        when (colorToolbar.state) {
            ColorToolbarWidget.State.SELECTION -> {
                vm.onMarkupBackgroundColorAction(click.color.hexColorCode())
            }
            ColorToolbarWidget.State.BLOCK -> {
                vm.onBlockBackgroundColorAction(click.color.hexColorCode())
            }
            else -> toast(NOT_IMPLEMENTED_MESSAGE)
        }

    private fun handleAddBlockToolbarOptionClicked(option: Option) {
        when (option) {
            is Option.Text -> {
                when (option.type) {
                    OPTION_TEXT_TEXT -> vm.onAddTextBlockClicked(Text.Style.P)
                    OPTION_TEXT_HEADER_ONE -> vm.onAddTextBlockClicked(Text.Style.H1)
                    OPTION_TEXT_HEADER_TWO -> vm.onAddTextBlockClicked(Text.Style.H2)
                    OPTION_TEXT_HEADER_THREE -> vm.onAddTextBlockClicked(Text.Style.H3)
                    OPTION_TEXT_HIGHLIGHTED -> vm.onAddTextBlockClicked(Text.Style.QUOTE)
                    else -> toast(NOT_IMPLEMENTED_MESSAGE)
                }
            }
            is Option.List -> {
                when (option.type) {
                    OPTION_LIST_BULLETED_LIST -> vm.onAddTextBlockClicked(Text.Style.BULLET)
                    OPTION_LIST_CHECKBOX -> vm.onAddTextBlockClicked(Text.Style.CHECKBOX)
                    else -> toast(NOT_IMPLEMENTED_MESSAGE)
                }
            }
            is Option.Tool -> {
                when (option.type) {
                    OPTION_TOOL_PAGE -> vm.onAddNewPageClicked()
                    else -> toast(NOT_IMPLEMENTED_MESSAGE)
                }
            }
        }
    }

    private fun handleTurnIntoToolbarOptionClicked(option: Option) {
        when (option) {
            is Option.Text -> {
                when (option.type) {
                    OPTION_TEXT_TEXT -> vm.onTurnIntoStyleClicked(Text.Style.P)
                    OPTION_TEXT_HEADER_ONE -> vm.onTurnIntoStyleClicked(Text.Style.H1)
                    OPTION_TEXT_HEADER_TWO -> vm.onTurnIntoStyleClicked(Text.Style.H2)
                    OPTION_TEXT_HEADER_THREE -> vm.onTurnIntoStyleClicked(Text.Style.H3)
                    OPTION_TEXT_HIGHLIGHTED -> vm.onTurnIntoStyleClicked(Text.Style.QUOTE)
                    else -> toast(NOT_IMPLEMENTED_MESSAGE)
                }
            }
            is Option.List -> {
                when (option.type) {
                    OPTION_LIST_BULLETED_LIST -> vm.onTurnIntoStyleClicked(Text.Style.BULLET)
                    OPTION_LIST_CHECKBOX -> vm.onTurnIntoStyleClicked(Text.Style.CHECKBOX)
                    OPTION_LIST_NUMBERED_LIST -> vm.onTurnIntoStyleClicked(Text.Style.NUMBERED)
                    else -> toast(NOT_IMPLEMENTED_MESSAGE)
                }
            }
        }
    }

    override fun onAddMarkupLinkClicked(blockId: String, link: String, range: IntRange) {
        vm.onAddLinkPressed(blockId, link, range)
    }

    override fun onRemoveMarkupLinkClicked(blockId: String, range: IntRange) {
        vm.onUnlinkPressed(blockId, range)
    }

    private fun showColorToolbar() {
        colorToolbar.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        colorToolbar.visible()
    }

    private fun showOptionToolbar() {
        optionToolbar.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        optionToolbar.visible()
    }

    private fun hideColorToolbar() {
        colorToolbar.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, 0)
        colorToolbar.invisible()
    }

    private fun hideOptionToolbar() {
        optionToolbar.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, 0)
        optionToolbar.invisible()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm.state.observe(viewLifecycleOwner, Observer { render(it) })
        vm.navigation.observe(viewLifecycleOwner, navObserver)
        vm.controlPanelViewState.observe(viewLifecycleOwner, Observer { render(it) })
        vm.focus.observe(viewLifecycleOwner, Observer { handleFocus(it) })
    }

    private fun handleFocus(focus: Id) {
        Timber.d("Handling focus: $focus")
        if (focus.isEmpty()) {
            placeholder.requestFocus()
            hideKeyboard()
            fab.visible()
        } else {
            fab.gone()
        }
    }

    private fun render(state: PageViewModel.ViewState) {
        when (state) {
            is PageViewModel.ViewState.Success -> {
                pageAdapter.updateWithDiffUtil(state.blocks + listOf(BlockView.Footer))
            }
            is PageViewModel.ViewState.OpenLinkScreen -> {
                LinkFragment.newInstance(
                    blockId = state.block.id,
                    initUrl = state.block.getFirstLinkMarkupParam(state.range),
                    text = state.block.getSubstring(state.range),
                    rangeEnd = state.range.last,
                    rangeStart = state.range.first
                ).show(childFragmentManager, null)
            }
        }
    }

    private fun render(state: ControlPanelState) {
        Timber.d("Rendering new control panel state:\n$state")
        markupToolbar.setState(state.markupToolbar)
        toolbar.setState(state.blockToolbar)

        state.focus?.let { toolbar.setTurnIntoTarget(it.type) }

        state.colorToolbar.apply {
            colorToolbar.state = when {
                state.blockToolbar.isVisible -> ColorToolbarWidget.State.BLOCK
                state.markupToolbar.isVisible -> ColorToolbarWidget.State.SELECTION
                else -> ColorToolbarWidget.State.IDLE
            }
            if (isVisible) {
                hideKeyboard()
                lifecycleScope.launch {
                    delay(300)
                    showColorToolbar()
                }
            } else
                hideColorToolbar()
        }

        state.addBlockToolbar.apply {
            if (isVisible) {
                optionToolbar.state = OptionToolbarWidget.State.ADD_BLOCK
                hideKeyboard()
                lifecycleScope.launch {
                    delay(300)
                    showOptionToolbar()
                }
            } else {
                if (!state.turnIntoToolbar.isVisible)
                    hideOptionToolbar()
            }
        }

        state.turnIntoToolbar.apply {
            if (isVisible) {
                optionToolbar.state = OptionToolbarWidget.State.TURN_INTO
                hideKeyboard()
                lifecycleScope.launch {
                    delay(300)
                    optionToolbar.show()
                }
            } else {
                if (!state.addBlockToolbar.isVisible)
                    hideOptionToolbar()
            }
        }

        with(state.actionToolbar) {
            if (isVisible) {
                hideKeyboard()
                lifecycleScope.launch {
                    delay(300)
                    actionToolbar.show()
                }
            } else
                actionToolbar.hide()
        }
    }

    private fun hideKeyboard() {
        Timber.d("Hiding keyboard")
        hideSoftInput()
    }

    override fun injectDependencies() {
        componentManager().pageComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().pageComponent.release()
    }

    companion object {
        const val ID_KEY = "id"
        const val ID_EMPTY_VALUE = ""
        const val NOT_IMPLEMENTED_MESSAGE = "Not implemented."
    }
}

interface OnFragmentInteractionListener {
    fun onAddMarkupLinkClicked(blockId: String, link: String, range: IntRange)
    fun onRemoveMarkupLinkClicked(blockId: String, range: IntRange)
}