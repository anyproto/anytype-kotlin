package com.agileburo.anytype.ui.page

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.extensions.invisible
import com.agileburo.anytype.core_ui.extensions.visible
import com.agileburo.anytype.core_ui.features.page.BlockAdapter
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.menu.DocumentPopUpMenu
import com.agileburo.anytype.core_ui.model.UiBlock
import com.agileburo.anytype.core_ui.reactive.clicks
import com.agileburo.anytype.core_ui.state.ControlPanelState
import com.agileburo.anytype.core_ui.tools.FirstItemInvisibilityDetector
import com.agileburo.anytype.core_ui.tools.OutsideClickDetector
import com.agileburo.anytype.core_ui.widgets.toolbar.ActionToolbarWidget.ActionConfig.ACTION_DELETE
import com.agileburo.anytype.core_ui.widgets.toolbar.ActionToolbarWidget.ActionConfig.ACTION_DUPLICATE
import com.agileburo.anytype.core_ui.widgets.toolbar.ActionToolbarWidget.ActionConfig.ACTION_REDO
import com.agileburo.anytype.core_ui.widgets.toolbar.ActionToolbarWidget.ActionConfig.ACTION_UNDO
import com.agileburo.anytype.core_ui.widgets.toolbar.ColorToolbarWidget
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.Option
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_LIST_BULLETED_LIST
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_LIST_CHECKBOX
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_LIST_NUMBERED_LIST
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_LIST_TOGGLE_LIST
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_MEDIA_BOOKMARK
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_MEDIA_FILE
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_MEDIA_PICTURE
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_MEDIA_VIDEO
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_OTHER_DIVIDER
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_TEXT_HEADER_ONE
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_TEXT_HEADER_THREE
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_TEXT_HEADER_TWO
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_TEXT_HIGHLIGHTED
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_TEXT_TEXT
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_TOOL_PAGE
import com.agileburo.anytype.core_utils.common.EventWrapper
import com.agileburo.anytype.core_utils.ext.*
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.domain.block.model.Block.Content.Text
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.ext.getFirstLinkMarkupParam
import com.agileburo.anytype.domain.ext.getSubstring
import com.agileburo.anytype.ext.extractMarks
import com.agileburo.anytype.presentation.page.PageViewModel
import com.agileburo.anytype.presentation.page.PageViewModelFactory
import com.agileburo.anytype.ui.base.NavigationFragment
import com.agileburo.anytype.ui.page.modals.AddBlockFragment
import com.agileburo.anytype.ui.page.modals.CreateBookmarkFragment
import com.agileburo.anytype.ui.page.modals.PageIconPickerFragment
import com.agileburo.anytype.ui.page.modals.SetLinkFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import kotlinx.android.synthetic.main.fragment_page.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import permissions.dispatcher.*
import timber.log.Timber
import javax.inject.Inject


const val REQUEST_FILE_CODE = 745

@RuntimePermissions
open class PageFragment :
    NavigationFragment(R.layout.fragment_page),
    OnFragmentInteractionListener,
    AddBlockFragment.AddBlockActionReceiver,
    PickiTCallbacks {

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(PageViewModel::class.java)
    }
    private lateinit var pickiT: PickiT

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
            onParagraphTextChanged = { id, editable ->
                vm.onParagraphTextChanged(
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
            onFooterClicked = vm::onOutsideClicked,
            onPageClicked = vm::onPageClicked,
            onTextInputClicked = vm::onTextInputClicked,
            onDownloadFileClicked = vm::onDownloadFileClicked,
            onPageIconClicked = vm::onPageIconClicked,
            onAddUrlClick = vm::onAddVideoUrlClicked,
            onAddLocalVideoClick = vm::onAddLocalVideoClicked,
            onBookmarkPlaceholderClicked = vm::onBookmarkPlaceholderClicked,
            onAddLocalPictureClick = vm::onAddLocalPictureClicked,
            onAddLocalFileClick = vm::onAddLocalFileClicked,
            onTogglePlaceholderClicked = vm::onTogglePlaceholderClicked,
            onToggleClicked = vm::onToggleClicked,
            onMediaBlockMenuClick = vm::onMediaBlockMenuClicked
        )
    }

    private val titleVisibilityDetector by lazy {
        FirstItemInvisibilityDetector { isVisible ->
            if (isVisible) {
                title.invisible()
                emoji.invisible()
            } else {
                title.visible()
                emoji.visible()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_FILE_CODE -> {
                    data?.data?.let {
                        pickiT.getPath(it, Build.VERSION.SDK_INT)
                    } ?: run {
                        toast("Error while getting file")
                    }
                }
                else -> toast("Unknown Request Code:$requestCode")
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun startDownloadWithPermissionCheck(id: String) {
        vm.startDownloadingFile(id)
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun openGallery(type: String) {
        startActivityForResult(getVideoFileIntent(type), REQUEST_FILE_CODE)
    }

    @OnShowRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun showRationaleForReadExternalStoragePermission(request: PermissionRequest) {
        showRationaleDialog(R.string.permission_read_rationale, request)
    }

    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun onReadExternalStoragePermissionDenied() {
        toast(getString(R.string.permission_read_denied))
    }

    @OnNeverAskAgain(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun onReadExternalStoragePermissionNeverAskAgain() {
        toast(getString(R.string.permission_read_never_ask_again))
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showRationaleForWriteExternalStoragePermission(request: PermissionRequest) {
        showRationaleDialog(R.string.permission_write_rationale, request)
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onWriteExternalStoragePermissionDenied() {
        toast(getString(R.string.permission_write_denied))
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onWriteExternalStoragePermissionNeverAskAgain() {
        toast(getString(R.string.permission_write_never_ask_again))
    }

    override fun PickiTonProgressUpdate(progress: Int) {
        Timber.d("PickiTonProgressUpdate progress:$progress")
    }

    override fun PickiTonStartListener() {
        vm.onChooseVideoFileFromMedia()
        Timber.d("PickiTonStartListener")
    }

    override fun PickiTonCompleteListener(
        path: String?,
        wasDriveFile: Boolean,
        wasUnknownProvider: Boolean,
        wasSuccessful: Boolean,
        Reason: String?
    ) {
        Timber.d(
            "PickiTonCompleteListener  path:$path, wasDriveFile$wasDriveFile, " +
                    "wasUnknownProvider:$wasUnknownProvider, wasSuccessful:$wasSuccessful, reason:$Reason"
        )
        vm.onAddVideoFileClicked(filePath = path)
    }

    @Inject
    lateinit var factory: PageViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.open(requireArguments().getString(ID_KEY, ID_EMPTY_VALUE))
        pickiT = PickiT(requireContext(), this)
        setupOnBackPressedDispatcher()
    }

    private fun setupOnBackPressedDispatcher() {
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(this) { vm.onSystemBackPressed() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler.addOnItemTouchListener(
            OutsideClickDetector(vm::onOutsideClicked)
        )

        BottomSheetBehavior.from(sheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            isHideable = true
            addBottomSheetCallback(
                object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {}
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                            activity?.hideSoftInput()
                            vm.onBottomSheetHidden()
                        }
                    }
                }
            )
        }

        recycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = pageAdapter
            addOnScrollListener(titleVisibilityDetector)
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
                    ACTION_UNDO -> vm.onActionUndoClicked()
                    ACTION_REDO -> vm.onActionRedoClicked()
                    else -> toast(NOT_IMPLEMENTED_MESSAGE)
                }
            }
            .launchIn(lifecycleScope)

        fab.clicks().onEach { vm.onPlusButtonPressed() }.launchIn(lifecycleScope)
        menu.clicks().onEach { showToolbarMenu() }.launchIn(lifecycleScope)
        backButton.clicks().onEach {
            hideKeyboard()
            vm.onBackButtonPressed()
        }.launchIn(lifecycleScope)
    }

    private fun showToolbarMenu() {
        DocumentPopUpMenu(
            requireContext(),
            menu,
            vm::onArchiveThisPageClicked
        ).show()
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

    private fun handleBackgroundColorClicked(
        click: ColorToolbarWidget.Click.OnBackgroundColorClicked
    ) = when (colorToolbar.state) {
        ColorToolbarWidget.State.SELECTION -> {
            vm.onMarkupBackgroundColorAction(click.color.hexColorCode())
        }
        ColorToolbarWidget.State.BLOCK -> {
            vm.onBlockBackgroundColorAction(click.color.hexColorCode())
        }
        else -> toast(NOT_IMPLEMENTED_MESSAGE)
    }

    override fun onAddBlockClicked(block: UiBlock) {
        when (block) {
            UiBlock.TEXT -> vm.onAddTextBlockClicked(Text.Style.P)
            UiBlock.HEADER_ONE -> vm.onAddTextBlockClicked(Text.Style.H1)
            UiBlock.HEADER_TWO -> vm.onAddTextBlockClicked(Text.Style.H2)
            UiBlock.HEADER_THREE -> vm.onAddTextBlockClicked(Text.Style.H3)
            UiBlock.HIGHLIGHTED -> vm.onAddTextBlockClicked(Text.Style.QUOTE)
            UiBlock.CHECKBOX -> vm.onAddTextBlockClicked(Text.Style.CHECKBOX)
            UiBlock.BULLETED -> vm.onAddTextBlockClicked(Text.Style.BULLET)
            UiBlock.NUMBERED -> vm.onAddTextBlockClicked(Text.Style.NUMBERED)
            UiBlock.TOGGLE -> vm.onAddTextBlockClicked(Text.Style.TOGGLE)
            UiBlock.PAGE -> vm.onAddNewPageClicked()
            UiBlock.FILE -> vm.onAddFileBlockClicked()
            UiBlock.IMAGE -> vm.onAddImageBlockClicked()
            UiBlock.VIDEO -> vm.onAddVideoBlockClicked()
            UiBlock.BOOKMARK -> vm.onAddBookmarkClicked()
            UiBlock.LINE_DIVIDER -> vm.onAddDividerBlockClicked()
            else -> toast(NOT_IMPLEMENTED_MESSAGE)
        }
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
                    OPTION_LIST_NUMBERED_LIST -> vm.onAddTextBlockClicked(Text.Style.NUMBERED)
                    OPTION_LIST_TOGGLE_LIST -> vm.onAddTextBlockClicked(Text.Style.TOGGLE)
                    else -> toast(NOT_IMPLEMENTED_MESSAGE)
                }
            }
            is Option.Tool -> {
                when (option.type) {
                    OPTION_TOOL_PAGE -> vm.onAddNewPageClicked()
                    else -> toast(NOT_IMPLEMENTED_MESSAGE)
                }
            }
            is Option.Media -> {
                when (option.type) {
                    OPTION_MEDIA_PICTURE -> vm.onAddImageBlockClicked()
                    OPTION_MEDIA_VIDEO -> vm.onAddVideoBlockClicked()
                    OPTION_MEDIA_BOOKMARK -> vm.onAddBookmarkClicked()
                    OPTION_MEDIA_FILE -> vm.onAddFileBlockClicked()
                    else -> toast(NOT_IMPLEMENTED_MESSAGE)
                }
            }
            is Option.Other -> {
                when (option.type) {
                    OPTION_OTHER_DIVIDER -> vm.onAddDividerBlockClicked()
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
        if (!colorToolbar.isVisible) {
            colorToolbar.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            colorToolbar.visible()
            recycler.updatePadding(
                bottom = colorToolbar.height() + dimen(R.dimen.default_toolbar_height)
            )
        }
    }

    private fun showOptionToolbar() {
        if (!optionToolbar.isVisible) {
            optionToolbar.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            optionToolbar.visible()
            recycler.updatePadding(
                bottom = optionToolbar.height() + dimen(R.dimen.default_toolbar_height)
            )
        }
    }

    private fun hideColorToolbar() {
        if (colorToolbar.isVisible) {
            colorToolbar.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, 0)
            colorToolbar.invisible()
            recycler.updatePadding(bottom = dimen(R.dimen.default_toolbar_height))
        }
    }

    private fun hideOptionToolbar() {
        if (optionToolbar.isVisible) {
            optionToolbar.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, 0)
            optionToolbar.invisible()
            recycler.updatePadding(bottom = dimen(R.dimen.default_toolbar_height))
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm.state.observe(viewLifecycleOwner, Observer { render(it) })
        vm.navigation.observe(viewLifecycleOwner, navObserver)
        vm.controlPanelViewState.observe(viewLifecycleOwner, Observer { render(it) })
        vm.focus.observe(viewLifecycleOwner, Observer { handleFocus(it) })
        vm.commands.observe(viewLifecycleOwner, Observer { execute(it) })
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

    private fun execute(event: EventWrapper<PageViewModel.Command>) {
        event.getContentIfNotHandled()?.let { command ->
            when (command) {
                is PageViewModel.Command.OpenPagePicker -> {
                    PageIconPickerFragment.newInstance(
                        context = requireArguments().getString(ID_KEY, ID_EMPTY_VALUE),
                        target = command.target
                    ).show(childFragmentManager, null)
                }
                is PageViewModel.Command.OpenAddBlockPanel -> {
                    AddBlockFragment.newInstance().show(childFragmentManager, null)
                }
                is PageViewModel.Command.OpenBookmarkSetter -> {
                    CreateBookmarkFragment.newInstance(
                        context = command.context,
                        target = command.target
                    ).show(childFragmentManager, null)
                }
                is PageViewModel.Command.OpenGallery -> {
                    openGalleryWithPermissionCheck(command.mediaType)
                }
                is PageViewModel.Command.RequestDownloadPermission -> {
                    startDownloadWithPermissionCheck(command.id)
                }
            }
        }
    }

    private fun render(state: PageViewModel.ViewState) {
        when (state) {
            is PageViewModel.ViewState.Success -> {
                pageAdapter.updateWithDiffUtil(state.blocks)
                resetDocumentTitle(state)
            }
            is PageViewModel.ViewState.OpenLinkScreen -> {
                SetLinkFragment.newInstance(
                    blockId = state.block.id,
                    initUrl = state.block.getFirstLinkMarkupParam(state.range),
                    text = state.block.getSubstring(state.range),
                    rangeEnd = state.range.last,
                    rangeStart = state.range.first
                ).show(childFragmentManager, null)
            }
            is PageViewModel.ViewState.Error -> toast(state.message)
        }
    }

    private fun resetDocumentTitle(state: PageViewModel.ViewState.Success) {
        state.blocks.firstOrNull { view ->
            view is BlockView.Title
        }?.let { view ->
            title.text = (view as BlockView.Title).text
            emoji.text = view.emoji
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
                if (!state.turnIntoToolbar.isVisible) {
                    hideOptionToolbar()
                }
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

        state.actionToolbar.apply {
            if (isVisible) {
                hideKeyboard()
                lifecycleScope.launch {
                    delay(300)
                    if (!actionToolbar.isVisible) {
                        actionToolbar.show()
                        recycler.updatePadding(
                            bottom = actionToolbar.height() + dimen(R.dimen.default_toolbar_height)
                        )
                    }
                }
            } else {
                if (actionToolbar.isVisible) {
                    actionToolbar.hide()
                    recycler.updatePadding(
                        bottom = dimen(R.dimen.default_toolbar_height)
                    )
                }
            }
        }
    }

    private fun hideKeyboard() {
        Timber.d("Hiding keyboard")
        hideSoftInput()
    }

    private fun showRationaleDialog(@StringRes messageResId: Int, request: PermissionRequest) {
        AlertDialog.Builder(requireContext())
            .setPositiveButton(R.string.button_allow) { _, _ -> request.proceed() }
            .setNegativeButton(R.string.button_deny) { _, _ -> request.cancel() }
            .setCancelable(false)
            .setMessage(messageResId)
            .show()
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