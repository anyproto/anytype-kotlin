package com.anytypeio.anytype.ui.page

import android.Manifest
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.activity.addCallback
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Block.Content.Text
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ext.getFirstLinkMarkupParam
import com.anytypeio.anytype.core_models.ext.getSubstring
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.extensions.cursorYBottomCoordinate
import com.anytypeio.anytype.core_ui.extensions.isKeyboardVisible
import com.anytypeio.anytype.core_ui.extensions.tint
import com.anytypeio.anytype.core_ui.features.page.BlockAdapter
import com.anytypeio.anytype.core_ui.features.page.TurnIntoActionReceiver
import com.anytypeio.anytype.core_ui.features.page.scrollandmove.DefaultScrollAndMoveTargetDescriptor
import com.anytypeio.anytype.core_ui.features.page.scrollandmove.ScrollAndMoveStateListener
import com.anytypeio.anytype.core_ui.features.page.scrollandmove.ScrollAndMoveTargetHighlighter
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_ui.reactive.layoutChanges
import com.anytypeio.anytype.core_ui.tools.*
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ext.PopupExtensions.calculateRectInWindow
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.domain.status.SyncStatus
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.ext.extractMarks
import com.anytypeio.anytype.presentation.page.PageViewModel
import com.anytypeio.anytype.presentation.page.PageViewModelFactory
import com.anytypeio.anytype.presentation.page.editor.*
import com.anytypeio.anytype.presentation.page.editor.actions.ActionItemType
import com.anytypeio.anytype.presentation.page.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import com.anytypeio.anytype.presentation.page.editor.model.UiBlock
import com.anytypeio.anytype.presentation.page.editor.sam.ScrollAndMoveTarget
import com.anytypeio.anytype.presentation.page.editor.sam.ScrollAndMoveTargetDescriptor
import com.anytypeio.anytype.presentation.page.markup.MarkupColorView
import com.anytypeio.anytype.ui.alert.AlertUpdateAppFragment
import com.anytypeio.anytype.ui.base.NavigationFragment
import com.anytypeio.anytype.ui.page.cover.DocCoverAction
import com.anytypeio.anytype.ui.page.cover.DocCoverSliderFragment
import com.anytypeio.anytype.ui.page.gallery.FullScreenPictureFragment
import com.anytypeio.anytype.ui.page.layout.ObjectLayoutFragment
import com.anytypeio.anytype.ui.page.modals.*
import com.anytypeio.anytype.ui.page.modals.actions.BlockActionToolbarFactory
import com.anytypeio.anytype.ui.page.modals.actions.DocumentIconActionMenuFragment
import com.anytypeio.anytype.ui.page.modals.actions.ProfileIconActionMenuFragment
import com.anytypeio.anytype.ui.page.sheets.DocMenuBottomSheet
import com.anytypeio.anytype.ui.page.sheets.DocMenuBottomSheet.DocumentMenuActionReceiver
import com.anytypeio.anytype.ui.relations.RelationDateValueFragment
import com.anytypeio.anytype.ui.relations.RelationListFragment
import com.anytypeio.anytype.ui.relations.RelationTextValueFragment
import com.anytypeio.anytype.ui.relations.RelationValueFragment
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.fragment_page.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
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
    TurnIntoActionReceiver,
    SelectProgrammingLanguageReceiver,
    RelationTextValueFragment.TextValueEditReceiver,
    RelationDateValueFragment.DateValueEditReceiver,
    DocumentMenuActionReceiver,
    ClipboardInterceptor,
    DocCoverAction,
    PickiTCallbacks {

    private val ctx get() = arg<Id>(ID_KEY)

    private val screen: Point by lazy { screen() }

    private val scrollAndMoveStateChannel = Channel<Int>()

    init {
        processScrollAndMoveStateChanges()
    }

    private val scrollAndMoveTargetDescriptor: ScrollAndMoveTargetDescriptor by lazy {
        DefaultScrollAndMoveTargetDescriptor()
    }

    private val onHideBottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                when (bottomSheet.id) {
                    styleToolbarOther.id -> {
                        vm.onCloseBlockStyleExtraToolbarClicked()
                    }
                    styleToolbarMain.id -> {
                        vm.onCloseBlockStyleToolbarClicked()
                    }
                    styleToolbarColors.id -> {
                        vm.onCloseBlockStyleColorToolbarClicked()
                    }
                }
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    private val scrollAndMoveTopMargin by lazy {
        dimen(R.dimen.dp_48)
    }

    private val scrollAndMoveStateListener by lazy {
        ScrollAndMoveStateListener {
            lifecycleScope.launch {
                scrollAndMoveStateChannel.send(it)
            }
        }
    }

    private val scrollAndMoveTargetHighlighter by lazy {
        ScrollAndMoveTargetHighlighter(
            targeted = drawable(R.drawable.scroll_and_move_rectangle),
            disabled = drawable(R.drawable.scroll_and_move_disabled),
            line = drawable(R.drawable.scroll_and_move_line),
            screen = screen,
            padding = dimen(R.dimen.scroll_and_move_start_end_padding),
            indentation = dimen(R.dimen.indent),
            descriptor = scrollAndMoveTargetDescriptor
        )
    }

    private val footerMentionDecorator by lazy { MentionFooterItemDecorator(screen) }
    private val markupColorToolbarFooter by lazy { MarkupColorToolbarFooter(screen) }
    private val slashWidgetFooter by lazy { SlashWidgetFooterItemDecorator(screen) }
    private val styleToolbarFooter by lazy { StyleToolbarItemDecorator(screen) }

    private val vm by viewModels<PageViewModel> { factory }

    private lateinit var pickiT: PickiT

    private val pageAdapter by lazy {
        BlockAdapter(
            restore = vm.restore,
            blocks = mutableListOf(),
            onTextChanged = { id, editable ->
                vm.onTextChanged(
                    id = id,
                    text = editable.toString(),
                    marks = editable.extractMarks()
                )
            },
            onTextBlockTextChanged = vm::onTextBlockTextChanged,
            onDescriptionChanged = vm::onDescriptionBlockTextChanged,
            onTitleBlockTextChanged = vm::onTitleBlockTextChanged,
            onSelectionChanged = vm::onSelectionChanged,
            onCheckboxClicked = vm::onCheckboxClicked,
            onTitleCheckboxClicked = vm::onTitleCheckboxClicked,
            onFocusChanged = vm::onBlockFocusChanged,
            onSplitLineEnterClicked = { id, editable, range ->
                vm.onEnterKeyClicked(
                    target = id,
                    text = editable.toString(),
                    marks = editable.extractMarks(),
                    range = range
                )
            },
            onEmptyBlockBackspaceClicked = vm::onEmptyBlockBackspaceClicked,
            onNonEmptyBlockBackspaceClicked = { id, editable ->
                vm.onNonEmptyBlockBackspaceClicked(
                    id = id,
                    text = editable.toString(),
                    marks = editable.extractMarks()
                )
            },
            onTextInputClicked = vm::onTextInputClicked,
            onPageIconClicked = vm::onPageIconClicked,
            onProfileIconClicked = vm::onProfileIconClicked,
            onCoverClicked = vm::onAddCoverClicked,
            onTogglePlaceholderClicked = vm::onTogglePlaceholderClicked,
            onToggleClicked = vm::onToggleClicked,
            onContextMenuStyleClick = vm::onEditorContextMenuStyleClicked,
            onTitleTextInputClicked = vm::onTitleTextInputClicked,
            onClickListener = vm::onClickListener,
            clipboardInterceptor = this,
            onMentionEvent = vm::onMentionEvent,
            onSlashEvent = vm::onSlashTextWatcherEvent,
            onBackPressedCallback = { vm.onBackPressedCallback() }
        )
    }

    private fun searchScrollAndMoveTarget() {

        recycler.findFocus().let { child ->
            if (child is TextInputWidget) child.text
        }

        val centerX = screen.x / 2f

        val centerY = (targeter.y + (targeter.height / 2f)) - scrollAndMoveTopMargin

        var target: View? = recycler.findChildViewUnder(centerX, centerY)

        if (target == null) {
            target = recycler.findChildViewUnder(centerX, centerY - 5)
            if (target == null) {
                target = recycler.findChildViewUnder(centerX, centerY + 5)
            }
        }

        if (target == null) {
            scrollAndMoveTargetDescriptor.clear()
        } else {
            val position = recycler.getChildAdapterPosition(target)
            val top = target.top
            val height = target.height

            val view = pageAdapter.views[position]

            val indent = if (view is BlockView.Indentable) view.indent else 0

            val ratio = if (centerY < top) {
                val delta = top - centerY
                delta / height
            } else {
                val delta = centerY - top
                delta / height
            }

            scrollAndMoveTargetDescriptor.update(
                target = ScrollAndMoveTarget(
                    position = position,
                    ratio = ratio,
                    indent = indent
                )
            )
        }
    }

    private val titleVisibilityDetector by lazy {
        FirstItemInvisibilityDetector { isVisible ->
            if (isVisible) {
                topToolbar.title.invisible()
                topToolbar.container.invisible()
            } else {
                topToolbar.title.visible()
                topToolbar.container.visible()
            }
        }
    }

    override fun onAddObjectClicked(url: String, layout: ObjectType.Layout) {
        vm.onAddNewObjectClicked(
            type = url,
            layout = layout
        )
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
    fun startDownload(id: String) {
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

    @Inject
    lateinit var factory: PageViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pickiT = PickiT(requireContext(), this, requireActivity())
        setupOnBackPressedDispatcher()
        getEditorSettings()
    }

    override fun onStart() {
        vm.onStart(id = extractDocumentId())
        super.onStart()
    }

    override fun onStop() {
        vm.onStop()
        super.onStop()
    }

    private fun setupOnBackPressedDispatcher() =
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(this) {
                vm.onSystemBackPressed(childFragmentManager.backStackEntryCount > 0)
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
            itemAnimator = null
            adapter = pageAdapter
            addOnScrollListener(titleVisibilityDetector)
        }

        toolbar.apply {
            enterMultiSelectModeClicks()
                .onEach { vm.onEnterMultiSelectModeClicked() }
                .launchIn(lifecycleScope)
            openSlashWidgetClicks()
                .onEach { vm.onStartSlashWidgetClicked() }
                .launchIn(lifecycleScope)
            hideKeyboardClicks()
                .onEach { vm.onHideKeyboardClicked() }
                .launchIn(lifecycleScope)
            changeStyleClicks()
                .onEach { vm.onBlockToolbarStyleClicked() }
                .launchIn(lifecycleScope)
            openBlockActionClicks()
                .onEach { vm.onBlockToolbarBlockActionsClicked() }
                .launchIn(lifecycleScope)
        }

        bottomMenu
            .doneClicks()
            .onEach { vm.onExitMultiSelectModeClicked() }
            .launchIn(lifecycleScope)

        bottomMenu
            .deleteClicks()
            .onEach { vm.onMultiSelectModeDeleteClicked() }
            .launchIn(lifecycleScope)

        bottomMenu
            .copyClicks()
            .onEach { vm.onMultiSelectCopyClicked() }
            .launchIn(lifecycleScope)

        bottomMenu
            .enterScrollAndMove()
            .onEach { vm.onEnterScrollAndMoveClicked() }
            .launchIn(lifecycleScope)

        bottomMenu
            .applyScrollAndMoveClicks()
            .onEach {
                vm.onApplyScrollAndMoveClicked()
                onApplyScrollAndMoveClicked()
            }
            .launchIn(lifecycleScope)

        bottomMenu
            .exitScrollAndMoveClicks()
            .onEach { vm.onExitScrollAndMoveClicked() }
            .launchIn(lifecycleScope)

        bottomMenu
            .turnIntoClicks()
            .onEach { vm.onMultiSelectTurnIntoButtonClicked() }
            .launchIn(lifecycleScope)

        bottomMenu
            .styleClicks()
            .onEach { vm.onMultiSelectStyleButtonClicked() }
            .launchIn(lifecycleScope)

        select
            .clicks()
            .onEach { vm.onMultiSelectModeSelectAllClicked() }
            .launchIn(lifecycleScope)

        bottomToolbar
            .navigationClicks()
            .onEach { vm.onOpenPageNavigationButtonClicked() }
            .launchIn(lifecycleScope)

        bottomToolbar
            .addPageClick()
            .onEach { vm.onPlusButtonPressed() }
            .launchIn(lifecycleScope)

        bottomToolbar
            .searchClicks()
            .onEach { vm.onPageSearchClicked() }
            .launchIn(lifecycleScope)

        topToolbar.menu
            .clicks()
            .onEach {
                vm.onDocumentMenuClicked()
            }
            .launchIn(lifecycleScope)

        markupToolbar
            .highlightClicks()
            .onEach { vm.onMarkupHighlightToggleClicked() }
            .launchIn(lifecycleScope)

        markupToolbar
            .colorClicks()
            .onEach { vm.onMarkupColorToggleClicked() }
            .launchIn(lifecycleScope)

        markupToolbar
            .linkClicks()
            .onEach { vm.onMarkupUrlClicked() }
            .launchIn(lifecycleScope)

        markupToolbar
            .markup()
            .onEach { type -> vm.onStyleToolbarMarkupAction(type, null) }
            .launchIn(lifecycleScope)

        setMarkupUrlToolbar
            .onApply()
            .onEach { vm.onSetLink(it) }
            .launchIn(lifecycleScope)

        markupColorToolbar.onColorClickedListener = { color ->
            if (color is MarkupColorView.Text) {
                vm.onStyleToolbarMarkupAction(
                    type = Markup.Type.TEXT_COLOR,
                    param = color.code
                )
            } else {
                vm.onStyleToolbarMarkupAction(
                    type = Markup.Type.BACKGROUND_COLOR,
                    param = color.code
                )
            }
        }

        blocker.clicks()
            .onEach { vm.onBlockerClicked() }
            .launchIn(lifecycleScope)

        topToolbar.back.clicks().onEach {
            hideSoftInput()
            vm.onBackButtonPressed()
        }.launchIn(lifecycleScope)

        topToolbar.undo.clicks().onEach {
            vm.onActionUndoClicked()
        }.launchIn(lifecycleScope)

        topToolbar.redo.clicks().onEach {
            vm.onActionRedoClicked()
        }.launchIn(lifecycleScope)

        lifecycleScope.subscribe(styleToolbarMain.styles) {
            vm.onUpdateTextBlockStyle(it)
        }

        lifecycleScope.subscribe(styleToolbarMain.other) {
            vm.onBlockStyleToolbarOtherClicked()
        }

        lifecycleScope.subscribe(styleToolbarMain.colors) {
            vm.onBlockStyleToolbarColorClicked()
        }

        lifecycleScope.subscribe(styleToolbarColors.events) {
            vm.onStylingToolbarEvent(it)
        }

        lifecycleScope.subscribe(styleToolbarOther.actions) {
            vm.onStylingToolbarEvent(it)
        }

        mentionSuggesterToolbar.setupClicks(
            mentionClick = vm::onMentionSuggestClick,
            newPageClick = vm::onAddMentionNewPageClicked
        )

        lifecycleScope.launch {
            slashWidget.clickEvents.collect { item ->
                vm.onSlashItemClicked(item)
            }
        }

        lifecycleScope.launch {
            searchToolbar.events().collect { vm.onSearchToolbarEvent(it) }
        }

        BottomSheetBehavior.from(styleToolbarMain).state = BottomSheetBehavior.STATE_HIDDEN
        BottomSheetBehavior.from(styleToolbarOther).state = BottomSheetBehavior.STATE_HIDDEN
        BottomSheetBehavior.from(styleToolbarColors).state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun onApplyScrollAndMoveClicked() {
        scrollAndMoveTargetDescriptor.current()?.let { target ->
            vm.onApplyScrollAndMove(
                target = pageAdapter.views[target.position].id,
                ratio = target.ratio
            )
        }
    }

    override fun onAddBookmarkUrlClicked(target: String, url: String) {
        vm.onAddBookmarkUrl(target = target, url = url)
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
            UiBlock.CODE -> vm.onAddTextBlockClicked(Text.Style.CODE_SNIPPET)
            UiBlock.PAGE -> vm.onAddNewPageClicked()
            UiBlock.FILE -> vm.onAddFileBlockClicked(Block.Content.File.Type.FILE)
            UiBlock.IMAGE -> vm.onAddFileBlockClicked(Block.Content.File.Type.IMAGE)
            UiBlock.VIDEO -> vm.onAddFileBlockClicked(Block.Content.File.Type.VIDEO)
            UiBlock.BOOKMARK -> vm.onAddBookmarkBlockClicked()
            UiBlock.LINE_DIVIDER -> vm.onAddDividerBlockClicked(Block.Content.Divider.Style.LINE)
            UiBlock.THREE_DOTS -> vm.onAddDividerBlockClicked(Block.Content.Divider.Style.DOTS)
            UiBlock.LINK_TO_OBJECT -> vm.onAddLinkToObjectClicked()
            UiBlock.RELATION -> vm.onAddRelationBlockClicked()
            else -> toast(NOT_IMPLEMENTED_MESSAGE)
        }
    }

    override fun onTurnIntoBlockClicked(target: String, uiBlock: UiBlock) {
        vm.onTurnIntoBlockClicked(target, uiBlock)
    }

    override fun onTurnIntoMultiSelectBlockClicked(block: UiBlock) {
        vm.onTurnIntoMultiSelectBlockClicked(block)
    }

    override fun onAddMarkupLinkClicked(blockId: String, link: String, range: IntRange) {
        vm.onAddLinkPressed(blockId, link, range)
    }

    override fun onRemoveMarkupLinkClicked(blockId: String, range: IntRange) {
        vm.onUnlinkPressed(blockId, range)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm.state.observe(viewLifecycleOwner) { render(it) }
        vm.navigation.observe(viewLifecycleOwner, navObserver)
        vm.controlPanelViewState.observe(viewLifecycleOwner) { render(it) }
        vm.commands.observe(viewLifecycleOwner) { execute(it) }
        vm.toasts.onEach { toast(it) }.launchIn(lifecycleScope)
        vm.searchResultScrollPosition
            .filter { it != PageViewModel.NO_SEARCH_RESULT_POSITION }
            .onEach { recycler.smoothScrollToPosition(it) }
            .launchIn(lifecycleScope)

        vm.syncStatus.onEach { status -> bindSyncStatus(status) }.launchIn(lifecycleScope)
        vm.isUndoEnabled.onEach { topToolbar.setUndoState(it) }.launchIn(lifecycleScope)
        vm.isRedoEnabled.onEach { topToolbar.setRedoState(it) }.launchIn(lifecycleScope)
    }

    private fun bindSyncStatus(status: SyncStatus?) {
        when (status) {
            SyncStatus.UNKNOWN, SyncStatus.FAILED, SyncStatus.OFFLINE -> topToolbar.status.tint(
                color = requireContext().color(R.color.sync_status_red)
            )
            SyncStatus.SYNCING -> topToolbar.status.tint(
                color = requireContext().color(R.color.sync_status_orange)
            )
            SyncStatus.SYNCED -> topToolbar.status.tint(
                color = requireContext().color(R.color.sync_status_green)
            )
            else -> topToolbar.status.tint(Color.WHITE)
        }
    }

    override fun onDestroyView() {
        clearPickit()
        super.onDestroyView()
    }

    override fun onDestroy() {
        pickiT.deleteTemporaryFile(requireContext())
        super.onDestroy()
    }

    override fun onBlockActionClicked(id: String, action: ActionItemType) {
        Blurry.delete(root)
        vm.onActionMenuItemClicked(id, action)
    }

    override fun onSetRelationKeyClicked(blockId: Id, key: Id) {
        vm.onSetRelationKeyClicked(blockId = blockId, key = key)
    }

    private fun execute(event: EventWrapper<Command>) {
        event.getContentIfNotHandled()?.let { command ->
            when (command) {
                is Command.OpenDocumentIconActionMenu -> {
                    hideSoftInput()
                    recycler.smoothScrollToPosition(0)
                    val title = recycler.getChildAt(0)
                    val shared = title.findViewById<FrameLayout>(R.id.documentIconContainer)
                    val fr = DocumentIconActionMenuFragment.new(
                        y = shared.y + dimen(R.dimen.dp_48),
                        emoji = command.emoji,
                        target = command.target,
                        ctx = ctx,
                        image = command.image
                    ).apply {
                        enterTransition = Fade()
                        exitTransition = Fade()
                        sharedElementEnterTransition = ChangeBounds()
                    }
                    childFragmentManager.beginTransaction()
                        .add(R.id.root, fr)
                        .addToBackStack(null)
                        .apply { addSharedElement(shared, getString(R.string.logo_transition)) }
                        .commit()
                }
                is Command.OpenProfileIconActionMenu -> {
                    hideSoftInput()
                    recycler.smoothScrollToPosition(0)
                    val title = recycler.getChildAt(0)
                    val shared = title.findViewById<FrameLayout>(R.id.documentIconContainer)
                    val fr = ProfileIconActionMenuFragment.new(
                        y = shared.y + dimen(R.dimen.dp_48),
                        target = command.target,
                        ctx = ctx,
                        image = command.image,
                        name = command.name
                    ).apply {
                        enterTransition = Fade()
                        exitTransition = Fade()
                        sharedElementEnterTransition = ChangeBounds()
                    }
                    childFragmentManager.beginTransaction()
                        .add(R.id.root, fr)
                        .addToBackStack(null)
                        .apply { addSharedElement(shared, getString(R.string.logo_transition)) }
                        .commit()
                }
                is Command.OpenDocumentEmojiIconPicker -> {
                    DocumentEmojiIconPickerFragment.new(
                        context = requireArguments().getString(ID_KEY, ID_EMPTY_VALUE),
                        target = command.target
                    ).show(childFragmentManager, null)
                }
                is Command.OpenAddBlockPanel -> {
                    hideKeyboard()
                    AddBlockFragment.newInstance(command.ctx).show(childFragmentManager, null)
                }
                is Command.OpenTurnIntoPanel -> {
                    TurnIntoFragment.single(
                        target = command.target,
                        excludedCategories = command.excludedCategories,
                        excludedTypes = command.excludedTypes

                    ).show(childFragmentManager, null)
                }
                is Command.OpenMultiSelectTurnIntoPanel -> {
                    TurnIntoFragment.multiple(
                        excludedTypes = command.excludedTypes,
                        excludedCategories = command.excludedCategories
                    ).show(childFragmentManager, null)
                }
                is Command.OpenBookmarkSetter -> {
                    CreateBookmarkFragment.newInstance(
                        target = command.target
                    ).show(childFragmentManager, null)
                }
                is Command.OpenGallery -> {
                    openGalleryWithPermissionCheck(command.mediaType)
                }
                is Command.RequestDownloadPermission -> {
                    startDownloadWithPermissionCheck(command.id)
                }
                is Command.PopBackStack -> {
                    childFragmentManager.popBackStack()
                }
                is Command.OpenActionBar -> {
                    lifecycleScope.launch {
                        if (root.isKeyboardVisible()) {
                            hideKeyboard()
                            delay(300)
                            blurContainer()
                            navigateToBlockActionPreview(command)
                        } else {
                            blurContainer()
                            navigateToBlockActionPreview(command)
                        }
                    }
                }
                is Command.CloseKeyboard -> {
                    hideSoftInput()
                }
                is Command.Measure -> {
                    val views = pageAdapter.views
                    val position = views.indexOfFirst { it.id == command.target }
                    val lm = recycler.layoutManager as? LinearLayoutManager
                    val target = lm?.findViewByPosition(position)
                    val rect = calculateRectInWindow(target)
                    val dimensions = BlockDimensions(
                        left = rect.left,
                        top = rect.top,
                        bottom = rect.bottom,
                        right = rect.right,
                        height = root.height,
                        width = root.width
                    )
                    vm.onMeasure(
                        target = command.target,
                        dimensions = dimensions
                    )
                }
                is Command.Browse -> {
                    try {
                        Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(command.url)
                        }.let {
                            startActivity(it)
                        }
                    } catch (e: Throwable) {
                        toast("Couldn't parse url: ${command.url}")
                    }
                }
                is Command.OpenDocumentMenu -> {
                    hideKeyboard()
                    val fr = DocMenuBottomSheet.new(
                        title = command.title,
                        status = command.status,
                        image = command.image,
                        emoji = command.emoji
                    )
                    fr.show(childFragmentManager, null)
                }
                is Command.OpenProfileMenu -> {
                    hideKeyboard()
                    val fr = DocMenuBottomSheet.new(
                        title = command.title,
                        status = command.status,
                        image = command.image,
                        emoji = command.emoji,
                        isProfile = true
                    )
                    fr.show(childFragmentManager, null)
                }
                is Command.OpenCoverGallery -> {
                    val fr = DocCoverSliderFragment.new(command.ctx)
                    fr.show(childFragmentManager, null)
                }
                is Command.OpenObjectLayout -> {
                    val fr = ObjectLayoutFragment.new(command.ctx).apply {
                        onDismissListener = { vm.onLayoutDialogDismissed() }
                    }
                    fr.show(childFragmentManager, null)
                }
                is Command.OpenFullScreenImage -> {
                    val screen = FullScreenPictureFragment.new(command.target, command.url).apply {
                        enterTransition = Fade()
                        exitTransition = Fade()
                    }
                    childFragmentManager
                        .beginTransaction()
                        .add(R.id.root, screen)
                        .addToBackStack(null)
                        .commit()
                }
                is Command.AlertDialog -> {
                    if (childFragmentManager.findFragmentByTag(TAG_ALERT) == null) {
                        AlertUpdateAppFragment().show(childFragmentManager, TAG_ALERT)
                    } else {
                    }
                }
                is Command.ClearSearchInput -> {
                    searchToolbar.clear()
                }
                is Command.Dialog.SelectLanguage -> {
                    SelectProgrammingLanguageFragment.new(command.target)
                        .show(childFragmentManager, null)
                }
                is Command.OpenObjectRelationScreen.Add -> {
                    hideKeyboard()
                    RelationListFragment
                        .new(
                            ctx = command.ctx,
                            target = command.target,
                            mode = RelationListFragment.MODE_ADD
                        )
                        .show(childFragmentManager, null)
                }
                is Command.OpenObjectRelationScreen.List -> {
                    hideKeyboard()
                    RelationListFragment
                        .new(
                            ctx = command.ctx,
                            target = command.target,
                            mode = RelationListFragment.MODE_LIST
                        )
                        .show(childFragmentManager, null)
                }
                is Command.OpenObjectRelationScreen.Value.Default -> {
                    hideKeyboard()
                    val fr = RelationValueFragment.new(
                        ctx = command.ctx,
                        target = command.target,
                        relation = command.relation
                    )
                    fr.show(childFragmentManager, null)
                }
                is Command.OpenObjectRelationScreen.Value.Text -> {
                    hideKeyboard()
                    val fr = RelationTextValueFragment.new(
                        ctx = command.ctx,
                        objectId = command.target,
                        relationId = command.relation
                    )
                    fr.show(childFragmentManager, null)
                }
                is Command.OpenObjectRelationScreen.Value.Date -> {
                    hideKeyboard()
                    val fr = RelationDateValueFragment.new(
                        ctx = command.ctx,
                        objectId = command.target,
                        relationId = command.relation
                    )
                    fr.show(childFragmentManager, null)
                }
                Command.AddSlashWidgetTriggerToFocusedBlock -> {
                    recycler.findFocus()?.let { child: View? ->
                        if (child is TextInputWidget) {
                            child.text?.insert(child.selectionStart, "/")
                        }
                    }
                }
            }
        }
    }

    private fun blurContainer() {
        Blurry.with(context)
            .radius(12)
            .sampling(6)
            .onto(root)
    }

    private fun navigateToBlockActionPreview(command: Command.OpenActionBar) {
        childFragmentManager.beginTransaction()
            .add(
                R.id.root,
                BlockActionToolbarFactory.newInstance(
                    block = command.block,
                    dimensions = command.dimensions
                ),
                null
            )
            .addToBackStack(null)
            .commit()
    }

    private fun render(state: ViewState) {
        when (state) {
            is ViewState.Success -> {
                pageAdapter.updateWithDiffUtil(state.blocks)
                resetDocumentTitle(state)
            }
            is ViewState.OpenLinkScreen -> {
                if (childFragmentManager.findFragmentByTag(TAG_LINK) == null) {
                    SetLinkFragment.newInstance(
                        blockId = state.block.id,
                        initUrl = state.block.getFirstLinkMarkupParam(state.range),
                        text = state.block.getSubstring(state.range),
                        rangeEnd = state.range.last,
                        rangeStart = state.range.first
                    ).show(childFragmentManager, TAG_LINK)
                }
            }
        }
    }

    private fun resetDocumentTitle(state: ViewState.Success) {
        state.blocks.firstOrNull { view ->
            view is BlockView.Title.Basic || view is BlockView.Title.Profile
        }?.let { view ->
            when (view) {
                is BlockView.Title.Basic -> resetTopToolbarTitle(
                    view.text,
                    view.emoji,
                    view.image
                )
                is BlockView.Title.Profile -> resetTopToolbarTitle(view.text, null, view.image)
                else -> {
                }
            }
        }
    }

    private fun resetTopToolbarTitle(text: String?, emoji: String?, image: String?) {
        topToolbar.title.text = text
        when {
            emoji != null && emoji.isNotEmpty() -> {
                try {
                    topToolbar.emoji.invisible()
                    Glide.with(topToolbar.image).load(Emojifier.uri(emoji)).into(topToolbar.image)
                } catch (e: Exception) {
                    topToolbar.emoji.visible()
                    topToolbar.emoji.text = emoji
                }
            }
            image != null -> {
                topToolbar.emoji.invisible()
                topToolbar.image.visible()
                Glide
                    .with(topToolbar.image)
                    .load(image)
                    .centerInside()
                    .circleCrop()
                    .into(topToolbar.image)
            }
            else -> {
                topToolbar.image.setImageDrawable(null)
            }
        }
    }

    private fun render(state: ControlPanelState) {
        if (state.navigationToolbar.isVisible) {
            placeholder.requestFocus()
            hideKeyboard()
            bottomToolbar.visible()
        } else {
            bottomToolbar.gone()
        }

        if (state.mainToolbar.isVisible)
            toolbar.visible()
        else
            toolbar.invisible()

        setMainMarkupToolbarState(state)

        state.markupUrlToolbar.apply {
            if (isVisible) {
                setMarkupUrlToolbar.visible()
                setMarkupUrlToolbar.takeFocus()
                setMarkupUrlToolbar.bind(state.markupMainToolbar.style?.markupUrl)
                blocker.visible()
            } else {
                setMarkupUrlToolbar.invisible()
                blocker.invisible()
            }
        }

        state.multiSelect.apply {
            if (isVisible) {
                select.visible()
                if (count == 0) {
                    selectText.setText(R.string.select_all)
                } else {
                    selectText.text = getString(R.string.unselect_all, count)
                }
                bottomMenu.update(count)
                if (!bottomMenu.isShowing) {
                    recycler.apply { itemAnimator = DefaultItemAnimator() }
                    hideSoftInput()
                    Timber.d("Hiding top menu")
                    topToolbar.invisible()
                    lifecycleScope.launch {
                        delay(DELAY_BEFORE_INIT_SAM_SEARCH)
                        activity?.runOnUiThread {
                            bottomMenu.showWithAnimation()
                            showSelectButton()
                        }
                    }
                }
            } else {
                recycler.apply { itemAnimator = null }
                bottomMenu.hideWithAnimation()
                hideSelectButton()
            }
            if (isScrollAndMoveEnabled)
                enterScrollAndMove()
            else
                exitScrollAndMove()
        }

        state.stylingToolbar.apply {
            val behavior = BottomSheetBehavior.from(styleToolbarMain)
            if (isVisible) {
                styleToolbarMain.setSelectedStyle(style)
                if (behavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                    lifecycleScope.launch {
                        if (recycler.itemDecorationCount == 0) {
                            recycler.addItemDecoration(styleToolbarFooter)
                        }
                        hideSoftInput()
                        delay(DEFAULT_ANIM_DURATION)
                        behavior.apply {
                            setState(BottomSheetBehavior.STATE_EXPANDED)
                            addBottomSheetCallback(onHideBottomSheetCallback)
                        }
                    }
                }
            } else {
                if (!state.styleColorToolbar.isVisible && !state.styleExtraToolbar.isVisible) {
                    recycler.removeItemDecoration(styleToolbarFooter)
                }
                behavior.apply {
                    removeBottomSheetCallback(onHideBottomSheetCallback)
                    setState(BottomSheetBehavior.STATE_HIDDEN)
                }
            }
        }

        state.styleExtraToolbar.apply {
            if (isVisible) {
                styleToolbarOther.setProperties(
                    props = state.stylingToolbar.props,
                    config = state.stylingToolbar.config
                )
                lifecycleScope.launch {
                    BottomSheetBehavior.from(styleToolbarOther).apply {
                        setState(BottomSheetBehavior.STATE_EXPANDED)
                        addBottomSheetCallback(onHideBottomSheetCallback)
                    }
                }
            } else {
                BottomSheetBehavior.from(styleToolbarOther).apply {
                    removeBottomSheetCallback(onHideBottomSheetCallback)
                    setState(BottomSheetBehavior.STATE_HIDDEN)
                }
            }
        }

        state.styleColorToolbar.apply {
            if (isVisible) {
                state.stylingToolbar.config?.let { config ->
                    styleToolbarColors.update(
                        config,
                        state.stylingToolbar.props
                    )
                }
                lifecycleScope.launch {
                    BottomSheetBehavior.from(styleToolbarColors).apply {
                        setState(BottomSheetBehavior.STATE_EXPANDED)
                        addBottomSheetCallback(onHideBottomSheetCallback)
                    }
                }
            } else {
                BottomSheetBehavior.from(styleToolbarColors).apply {
                    removeBottomSheetCallback(onHideBottomSheetCallback)
                    setState(BottomSheetBehavior.STATE_HIDDEN)
                }
            }
        }

        state.mentionToolbar.apply {
            if (isVisible) {
                if (!mentionSuggesterToolbar.isVisible) {
                    showMentionToolbar(this)
                }
                if (updateList) {
                    mentionSuggesterToolbar.addItems(mentions)
                }
                mentionFilter?.let {
                    mentionSuggesterToolbar.updateFilter(it)
                }
            } else {
                mentionSuggesterToolbar.invisible()
                recycler.removeItemDecoration(footerMentionDecorator)
            }
        }

        state.slashWidget.apply {
            if (isVisible) {
                if (!slashWidget.isVisible) {
                    showSlashWidget(this)
                }
                widgetState?.let {
                    slashWidget.onStateChanged(it)
                }
            } else {
                slashWidget.gone()
                recycler.removeItemDecoration(slashWidgetFooter)
            }
        }

        state.searchToolbar.apply {
            if (isVisible) {
                searchToolbar.visible()
                searchToolbar.focus()
            } else {
                searchToolbar.gone()
            }
        }
    }

    private fun setMainMarkupToolbarState(state: ControlPanelState) {
        if (state.markupMainToolbar.isVisible) {
            markupToolbar.setProps(
                props = state.markupMainToolbar.style,
                supportedTypes = state.markupMainToolbar.supportedTypes,
                isBackgroundColorSelected = state.markupMainToolbar.isBackgroundColorSelected,
                isTextColorSelected = state.markupMainToolbar.isTextColorSelected
            )
            markupToolbar.visible()

            if (state.markupColorToolbar.isVisible) {
                if (state.markupMainToolbar.isTextColorSelected) {
                    markupColorToolbar.setTextColor(
                        state.markupMainToolbar.style?.markupTextColor
                            ?: state.markupMainToolbar.style?.blockTextColor
                            ?: ThemeColor.DEFAULT.title
                    )
                }
                if (state.markupMainToolbar.isBackgroundColorSelected) {
                    markupColorToolbar.setBackgroundColor(
                        state.markupMainToolbar.style?.markupHighlightColor
                            ?: state.markupMainToolbar.style?.blockBackroundColor
                            ?: ThemeColor.DEFAULT.title
                    )
                }
                if (markupColorToolbar.translationY > 0) {
                    recycler.addItemDecoration(markupColorToolbarFooter)
                }
                showMarkupColorToolbarWithAnimation()
            } else {
                if (markupColorToolbar.translationY == 0f) {
                    recycler.removeItemDecoration(markupColorToolbarFooter)
                    hideMarkupColorToolbarWithAnimation()
                }
            }

        } else {
            markupToolbar.invisible()
            if (markupColorToolbar.translationY == 0f) {
                //recycler.smoothScrollBy(0, -markupColorToolbar.height)
                markupColorToolbar.translationY = dimen(R.dimen.dp_104).toFloat()
            }
        }
    }

    private fun showMarkupColorToolbarWithAnimation() {

        val focus = recycler.findFocus()
        check(focus is TextInputWidget)
        val cursorCoord = focus.cursorYBottomCoordinate()

        val parentBottom = calculateRectInWindow(recycler).bottom
        val toolbarHeight = markupToolbar.height + markupColorToolbar.height

        val minPosY = parentBottom - toolbarHeight

        if (minPosY <= cursorCoord) {
            val scrollY = (parentBottom - minPosY) - (parentBottom - cursorCoord)
            Timber.d("New scroll y: $scrollY")
            recycler.post {
                recycler.smoothScrollBy(0, scrollY)
            }
        }

        markupColorToolbar
            .animate()
            .translationY(0f)
            .setDuration(DEFAULT_ANIM_DURATION)
            .start()
    }

    private fun hideMarkupColorToolbarWithAnimation() {
        markupColorToolbar
            .animate()
            .translationY(dimen(R.dimen.dp_104).toFloat())
            .setDuration(DEFAULT_ANIM_DURATION)
            .start()
    }

    private fun showMentionToolbar(state: ControlPanelState.Toolbar.MentionToolbar) {
        state.cursorCoordinate?.let { cursorCoordinate ->
            val parentBottom = calculateRectInWindow(recycler).bottom
            val toolbarHeight = mentionSuggesterToolbar.getMentionSuggesterWidgetMinHeight()
            val minPosY = parentBottom - toolbarHeight

            if (minPosY <= cursorCoordinate) {
                val scrollY = (parentBottom - minPosY) - (parentBottom - cursorCoordinate)
                recycler.addItemDecoration(footerMentionDecorator)
                recycler.post {
                    recycler.smoothScrollBy(0, scrollY)
                }
            }
            mentionSuggesterToolbar.updateLayoutParams<ConstraintLayout.LayoutParams> {
                height = toolbarHeight
            }
            val set = ConstraintSet().apply {
                clone(sheet)
                setVisibility(R.id.mentionSuggesterToolbar, View.VISIBLE)
                connect(
                    R.id.mentionSuggesterToolbar,
                    ConstraintSet.BOTTOM,
                    R.id.sheet,
                    ConstraintSet.BOTTOM
                )
            }
            val transitionSet = TransitionSet().apply {
                addTransition(ChangeBounds())
                duration = SHOW_MENTION_TRANSITION_DURATION
                interpolator = LinearInterpolator()
                ordering = TransitionSet.ORDERING_TOGETHER
            }
            TransitionManager.beginDelayedTransition(sheet, transitionSet)
            set.applyTo(sheet)
        }
    }

    private fun showSlashWidget(state: ControlPanelState.Toolbar.SlashWidget) {
        state.cursorCoordinate?.let { cursorCoordinate ->
            val parentBottom = calculateRectInWindow(recycler).bottom
            val toolbarHeight = slashWidget.getWidgetMinHeight()
            val minPosY = parentBottom - toolbarHeight

            if (minPosY <= cursorCoordinate) {
                val scrollY = (parentBottom - minPosY) - (parentBottom - cursorCoordinate)
                recycler.addItemDecoration(slashWidgetFooter)
                recycler.post {
                    recycler.smoothScrollBy(0, scrollY)
                }
            }
            slashWidget.updateLayoutParams<ConstraintLayout.LayoutParams> {
                height = toolbarHeight
            }
            val set = ConstraintSet().apply {
                clone(sheet)
                setVisibility(R.id.slashWidget, View.VISIBLE)
                connect(
                    R.id.slashWidget,
                    ConstraintSet.BOTTOM,
                    R.id.sheet,
                    ConstraintSet.BOTTOM
                )
            }
            val transitionSet = TransitionSet().apply {
                addTransition(ChangeBounds())
                duration = SHOW_MENTION_TRANSITION_DURATION
                interpolator = LinearInterpolator()
                ordering = TransitionSet.ORDERING_TOGETHER
            }
            TransitionManager.beginDelayedTransition(sheet, transitionSet)
            set.applyTo(sheet)
        }
    }

    private fun enterScrollAndMove() {
        if (recycler.itemDecorationCount == 0 || recycler.getItemDecorationAt(0) !is ScrollAndMoveTargetHighlighter) {

            val offset = recycler.computeVerticalScrollOffset()

            lifecycleScope.launch {
                recycler.layoutChanges().take(1).collect {
                    if (offset < screen.y / 3) recycler.scrollBy(0, screen.y / 3)
                }
            }

            recycler.addItemDecoration(scrollAndMoveTargetHighlighter)

            showTargeterWithAnimation()

            recycler.addOnScrollListener(scrollAndMoveStateListener)
            bottomMenu.showScrollAndMoveModeControls()
            select.invisible()

            scrollAndMoveHint.showWithAnimation()

            bottomMenu.hideMultiSelectControls()

            lifecycleScope.launch {
                delay(300)
                searchScrollAndMoveTarget()
                recycler.invalidate()
            }
        } else {
            Timber.d("Skipping enter scroll-and-move")
        }
    }

    private fun showTargeterWithAnimation() {
        targeter.translationY = -targeter.y
        ObjectAnimator.ofFloat(
            targeter,
            TARGETER_ANIMATION_PROPERTY,
            0f
        ).apply {
            duration = 300
            doOnStart { targeter.visible() }
            interpolator = OvershootInterpolator()
            start()
        }
    }

    private fun exitScrollAndMove() {
        recycler.apply {
            removeItemDecoration(scrollAndMoveTargetHighlighter)
            removeOnScrollListener(scrollAndMoveStateListener)
        }
        scrollAndMoveHint.hideWithAnimation()
        targeter.invisible()
        bottomMenu.hideScrollAndMoveModeControls()
        scrollAndMoveTargetDescriptor.clear()
    }

    private fun hideSelectButton() {
        ObjectAnimator.ofFloat(
            select,
            SELECT_BUTTON_ANIMATION_PROPERTY,
            -requireContext().dimen(R.dimen.dp_48)
        ).apply {
            duration = SELECT_BUTTON_HIDE_ANIMATION_DURATION
            interpolator = DecelerateInterpolator()
            doOnEnd { topToolbar?.visible() }
            start()
        }
    }

    override fun onClipboardAction(action: ClipboardInterceptor.Action) {
        when (action) {
            is ClipboardInterceptor.Action.Copy -> vm.onCopy(action.selection)
            is ClipboardInterceptor.Action.Paste -> vm.onPaste(action.selection)
        }
    }

    private fun showSelectButton() {
        ObjectAnimator.ofFloat(
            select,
            SELECT_BUTTON_ANIMATION_PROPERTY,
            0f
        ).apply {
            duration = SELECT_BUTTON_SHOW_ANIMATION_DURATION
            interpolator = DecelerateInterpolator()
            start()
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

    private fun extractDocumentId(): String {
        return requireArguments()
            .getString(ID_KEY)
            ?: throw IllegalStateException("Document id missing")
    }

    private fun processScrollAndMoveStateChanges() {
        lifecycleScope.launch {
            scrollAndMoveStateChannel
                .consumeAsFlow()
                .mapLatest { searchScrollAndMoveTarget() }
                .debounce(SAM_DEBOUNCE)
                .collect { recycler.invalidate() }
        }
    }

    override fun injectDependencies() {
        componentManager().pageComponent.get(extractDocumentId()).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().pageComponent.release(extractDocumentId())
    }

    private fun getEditorSettings() {
    }

    // ----------- PickiT Listeners ------------------------------

    private var pickitProgressDialog: ProgressDialog? = null
    private var pickitProgressBar: ProgressBar? = null
    private var pickitAlertDialog: AlertDialog? = null

    /**
     *  When selecting a file from Google Drive, for example, the Uri will be returned before
     *  the file is available (if it has not yet been cached/downloaded).
     *  Google Drive will first have to download the file before we have access to it.
     *  This can be used to let the user know that we(the application),
     *  are waiting for the file to be returned.
     */
    override fun PickiTonUriReturned() {
        pickitProgressDialog = ProgressDialog(requireContext()).apply {
            setMessage(getString(R.string.pickit_waiting))
            setCancelable(false)
        }
        pickitProgressDialog?.show()
    }

    /**
     *  This will return the progress of the file creation (in percentage)
     *  and will only be called if the selected file is not local
     */
    override fun PickiTonProgressUpdate(progress: Int) {
        Timber.d("PickiTonProgressUpdate progress:$progress")
        pickitProgressBar?.progress = progress
    }

    /**
     *  This will be call once the file creations starts and will only be called
     *  if the selected file is not local
     */
    override fun PickiTonStartListener() {
        if (pickitProgressDialog?.isShowing == true) {
            pickitProgressDialog?.cancel()
        }
        pickitAlertDialog =
            AlertDialog.Builder(requireContext(), R.style.SyncFromCloudDialog).apply {
                val view =
                    LayoutInflater.from(requireContext()).inflate(R.layout.dialog_layout, null)
                setView(view)
                view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                    pickiT.cancelTask()
                    if (pickitAlertDialog?.isShowing == true) {
                        pickitAlertDialog?.cancel()
                    }
                }
                pickitProgressBar = view.findViewById(R.id.mProgressBar)
            }.create()
        pickitAlertDialog?.show()
        Timber.d("PickiTonStartListener")
    }

    /**
     *  If the selected file was from Dropbox/Google Drive or OnDrive, then this will
     *  be called after the file was created. If the selected file was a local file then this will
     *  be called directly, returning the path as a String.
     *  Additionally, a boolean will be returned letting you know if the file selected was
     *  from Dropbox/Google Drive or OnDrive.
     */
    override fun PickiTonCompleteListener(
        path: String?,
        wasDriveFile: Boolean,
        wasUnknownProvider: Boolean,
        wasSuccessful: Boolean,
        Reason: String?
    ) {
        Timber.d("PickiTonCompleteListener path:$path, wasDriveFile:$wasDriveFile, wasUnknownProvider:$wasUnknownProvider, wasSuccessful:$wasSuccessful, reason:$Reason")
        if (pickitAlertDialog?.isShowing == true) {
            pickitAlertDialog?.cancel()
        }
        if (BuildConfig.DEBUG) {
            when {
                wasDriveFile -> toast(getString(R.string.pickit_drive))
                wasUnknownProvider -> toast(getString(R.string.pickit_file_selected))
                else -> toast(getString(R.string.pickit_local_file))
            }
        }
        when {
            wasSuccessful -> onFilePathReady(path)
            else -> toast("Error: $Reason")
        }
    }

    private fun onFilePathReady(filePath: String?) {
        vm.onProceedWithFilePath(filePath)
    }

    private fun clearPickit() {
        pickiT.cancelTask()
        pickitAlertDialog?.dismiss()
        pickitProgressDialog?.dismiss()
    }

    override fun onExitToDesktopClicked() {
        vm.navigateToDesktop()
    }

    override fun onLanguageSelected(target: Id, key: String) {
        Timber.d("key: $key")
        vm.onSelectProgrammingLanguageClicked(target, key)
    }

    override fun onArchiveClicked() {
        vm.onArchiveThisPageClicked()
    }

    override fun onSearchOnPageClicked() {
        vm.onEnterSearchModeClicked()
    }

    override fun onDocRelationsClicked() {
        vm.onDocRelationsClicked()
    }

    override fun onAddCoverClicked() {
        vm.onAddCoverClicked()
    }

    override fun onLayoutClicked() {
        vm.onLayoutClicked()
    }

    override fun onImagePicked(path: String) {
        vm.onDocCoverImagePicked(path)
    }

    override fun onImageSelected(hash: String) {
        vm.onDocCoverImageSelected(hash)
    }

    override fun onRemoveCover() {
        vm.onRemoveCover()
    }

    override fun onDismissBlockActionToolbar() {
        Blurry.delete(root)
        vm.onDismissBlockActionMenu(childFragmentManager.backStackEntryCount > 0)
    }

    override fun onTextValueChanged(ctx: Id, text: String, objectId: Id, relationId: Id) {
        vm.onRelationTextValueChanged(
            ctx = ctx,
            value = text,
            relationId = relationId
        )
    }

    override fun onNumberValueChanged(ctx: Id, number: Double?, objectId: Id, relationId: Id) {
        vm.onRelationTextValueChanged(
            ctx = ctx,
            value = number,
            relationId = relationId
        )
    }

    override fun onDateValueChanged(
        ctx: Id,
        timeInSeconds: Number?,
        objectId: Id,
        relationId: Id
    ) {
        vm.onRelationTextValueChanged(
            ctx = ctx,
            relationId = relationId,
            value = timeInSeconds
        )
    }

    //------------ End of Anytype Custom Context Menu ------------

    companion object {
        const val ID_KEY = "id"
        const val DEBUG_SETTINGS = "debug_settings"
        const val ID_EMPTY_VALUE = ""

        const val NOT_IMPLEMENTED_MESSAGE = "Not implemented."

        const val FAB_SHOW_ANIMATION_START_DELAY = 250L
        const val FAB_SHOW_ANIMATION_DURATION = 100L

        const val DEFAULT_ANIM_DURATION = 150L

        const val SHOW_MENTION_TRANSITION_DURATION = 150L
        const val SELECT_BUTTON_SHOW_ANIMATION_DURATION = 200L
        const val SELECT_BUTTON_HIDE_ANIMATION_DURATION = 200L
        const val SELECT_BUTTON_ANIMATION_PROPERTY = "translationY"
        const val TARGETER_ANIMATION_PROPERTY = "translationY"

        const val SAM_DEBOUNCE = 100L
        const val DELAY_BEFORE_INIT_SAM_SEARCH = 300L

        const val TAG_ALERT = "tag.alert"
        const val TAG_LINK = "tag.link"
    }
}

interface OnFragmentInteractionListener {
    fun onAddMarkupLinkClicked(blockId: String, link: String, range: IntRange)
    fun onRemoveMarkupLinkClicked(blockId: String, range: IntRange)
    fun onBlockActionClicked(id: String, action: ActionItemType)
    fun onDismissBlockActionToolbar()
    fun onAddBookmarkUrlClicked(target: String, url: String)
    fun onExitToDesktopClicked()
    fun onSetRelationKeyClicked(blockId: Id, key: Id)
}