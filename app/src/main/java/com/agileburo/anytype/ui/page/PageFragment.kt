package com.agileburo.anytype.ui.page

import android.Manifest
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
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
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.agileburo.anytype.BuildConfig
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.features.page.BlockAdapter
import com.agileburo.anytype.core_ui.features.page.BlockDimensions
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.TurnIntoActionReceiver
import com.agileburo.anytype.core_ui.features.page.scrollandmove.*
import com.agileburo.anytype.core_ui.menu.DocumentPopUpMenu
import com.agileburo.anytype.core_ui.model.UiBlock
import com.agileburo.anytype.core_ui.reactive.clicks
import com.agileburo.anytype.core_ui.state.ControlPanelState
import com.agileburo.anytype.core_ui.tools.ClipboardInterceptor
import com.agileburo.anytype.core_ui.tools.FirstItemInvisibilityDetector
import com.agileburo.anytype.core_ui.tools.MentionFooterItemDecorator
import com.agileburo.anytype.core_ui.tools.OutsideClickDetector
import com.agileburo.anytype.core_ui.widgets.ActionItemType
import com.agileburo.anytype.core_utils.common.EventWrapper
import com.agileburo.anytype.core_utils.ext.*
import com.agileburo.anytype.core_utils.ext.PopupExtensions.calculateRectInWindow
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.domain.block.model.Block.Content.Text
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.ext.getFirstLinkMarkupParam
import com.agileburo.anytype.domain.ext.getSubstring
import com.agileburo.anytype.ext.extractMarks
import com.agileburo.anytype.presentation.page.PageViewModel
import com.agileburo.anytype.presentation.page.PageViewModelFactory
import com.agileburo.anytype.presentation.page.editor.Command
import com.agileburo.anytype.presentation.page.editor.ViewState
import com.agileburo.anytype.ui.base.NavigationFragment
import com.agileburo.anytype.ui.page.modals.*
import com.agileburo.anytype.ui.page.modals.actions.BlockActionToolbarFactory
import com.agileburo.anytype.ui.page.modals.actions.DocumentIconActionMenuFragment
import com.agileburo.anytype.ui.page.modals.actions.ProfileIconActionMenuFragment
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
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
    ClipboardInterceptor,
    PickiTCallbacks {

    private val screen: Point by lazy { screen() }

    private val scrollAndMoveStateChannel = Channel<Int>()

    init {
        processScrollAndMoveStateChanges()
    }

    private val scrollAndMoveTargetDescriptor: ScrollAndMoveTargetDescriptor by lazy {
        DefaultScrollAndMoveTargetDescriptor()
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
            descriptor = scrollAndMoveTargetDescriptor
        )
    }

    private val footerMentionDecorator by lazy {
        MentionFooterItemDecorator(screen = screen)
    }

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(PageViewModel::class.java)
    }

    private lateinit var pickiT: PickiT

    private val pageAdapter by lazy {
        BlockAdapter(
            blocks = mutableListOf(),
            onTitleTextChanged = { editable ->
                vm.onTitleTextChanged(
                    text = editable.toString()
                )
            },
            onTextChanged = { id, editable ->
                vm.onTextChanged(
                    id = id,
                    text = editable.toString(),
                    marks = editable.extractMarks()
                )
            },
            onTextBlockTextChanged = vm::onTextBlockTextChanged,
            onSelectionChanged = vm::onSelectionChanged,
            onCheckboxClicked = vm::onCheckboxClicked,
            onFocusChanged = vm::onBlockFocusChanged,
            onSplitLineEnterClicked = { id, index, editable ->
                vm.onSplitLineEnterClicked(
                    target = id,
                    index = index,
                    text = editable.toString(),
                    marks = editable.extractMarks()
                )
            },
            onEndLineEnterClicked = { id, editable ->
                vm.onEndLineEnterClicked(
                    id = id,
                    text = editable.toString(),
                    marks = editable.extractMarks()
                )
            },
            onEndLineEnterTitleClicked = { vm.onEndLineEnterTitleClicked() },
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
            onTogglePlaceholderClicked = vm::onTogglePlaceholderClicked,
            onToggleClicked = vm::onToggleClicked,
            onContextMenuStyleClick = vm::onEditorContextMenuStyleClicked,
            onTitleTextInputClicked = vm::onTitleTextInputClicked,
            onClickListener = vm::onClickListener,
            clipboardInterceptor = this,
            onMentionEvent = vm::onMentionEvent
        )
    }

    private fun searchScrollAndMoveTarget() {

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
                    ratio = ratio
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
            //itemAnimator = null
            adapter = pageAdapter
            addOnScrollListener(titleVisibilityDetector)
        }

        toolbar.apply {
            enterMultiSelectModeClicks()
                .onEach { vm.onEnterMultiSelectModeClicked() }
                .launchIn(lifecycleScope)
            addBlockClicks()
                .onEach { vm.onAddBlockToolbarClicked() }
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
            .onEach { onApplyScrollAndMoveClicked() }
            .launchIn(lifecycleScope)

        bottomMenu
            .exitScrollAndMoveClicks()
            .onEach { vm.onExitScrollAndMoveClicked() }
            .launchIn(lifecycleScope)

        bottomMenu
            .turnIntoClicks()
            .onEach { vm.onMultiSelectTurnIntoButtonClicked() }
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

        topToolbar.menu.clicks().onEach { showToolbarMenu() }.launchIn(lifecycleScope)

        topToolbar.back.clicks().onEach {
            hideSoftInput()
            vm.onBackButtonPressed()
        }.launchIn(lifecycleScope)

        mentionSuggesterToolbar.setupClicks(
            mentionClick = vm::onMentionSuggestClick,
            newPageClick = vm::onAddMentionNewPageClicked
        )

        lifecycleScope.launch {
            styleToolbar.events.collect { event ->
                vm.onStylingToolbarEvent(event)
            }
        }

        lifecycleScope.launch {
            styleToolbar.closeButtonClicks().collect {
                vm.onCloseBlockStyleToolbarClicked()
            }
        }
    }

    private fun onApplyScrollAndMoveClicked() {
        scrollAndMoveTargetDescriptor.current()?.let { target ->
            vm.onApplyScrollAndMove(
                target = pageAdapter.views[target.position].id,
                ratio = target.ratio
            )
        }
    }

    private fun showToolbarMenu() {
        DocumentPopUpMenu(
            context = requireContext(),
            view = topToolbar.menu,
            onArchiveClicked = vm::onArchiveThisPageClicked,
            onRedoClicked = vm::onActionRedoClicked,
            onUndoClicked = vm::onActionUndoClicked
        ).show()
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
            UiBlock.FILE -> vm.onAddFileBlockClicked()
            UiBlock.IMAGE -> vm.onAddImageBlockClicked()
            UiBlock.VIDEO -> vm.onAddVideoBlockClicked()
            UiBlock.BOOKMARK -> vm.onAddBookmarkBlockClicked()
            UiBlock.LINE_DIVIDER -> vm.onAddDividerBlockClicked()
            else -> toast(NOT_IMPLEMENTED_MESSAGE)
        }
    }

    override fun onTurnIntoBlockClicked(target: String, block: UiBlock) {
        vm.onTurnIntoBlockClicked(target, block)
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
        vm.state.observe(viewLifecycleOwner, { render(it) })
        vm.navigation.observe(viewLifecycleOwner, navObserver)
        vm.controlPanelViewState.observe(viewLifecycleOwner, { render(it) })
        vm.commands.observe(viewLifecycleOwner, { execute(it) })
        vm.error.observe(viewLifecycleOwner, { renderError(it) })
    }

    override fun onDestroyView() {
        clearPickit()
        super.onDestroyView()
    }

    override fun onDestroy() {
        pickiT.deleteTemporaryFile()
        super.onDestroy()
    }

    override fun onBlockActionClicked(id: String, action: ActionItemType) {
        vm.onActionMenuItemClicked(id, action)
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
                    AddBlockFragment.newInstance().show(childFragmentManager, null)
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
                    hideKeyboard()
                    childFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.action_bar_enter, R.anim.action_bar_exit)
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
            }
        }
    }

    private fun render(state: ViewState) {
        when (state) {
            is ViewState.Success -> {
                pageAdapter.updateWithDiffUtil(state.blocks)
                resetDocumentTitle(state)
            }
            is ViewState.OpenLinkScreen -> {
                SetLinkFragment.newInstance(
                    blockId = state.block.id,
                    initUrl = state.block.getFirstLinkMarkupParam(state.range),
                    text = state.block.getSubstring(state.range),
                    rangeEnd = state.range.last,
                    rangeStart = state.range.first
                ).show(childFragmentManager, null)
            }
        }
    }

    private fun resetDocumentTitle(state: ViewState.Success) {
        state.blocks.firstOrNull { view ->
            view is BlockView.Title.Document || view is BlockView.Title.Profile
        }?.let { view ->
            when (view) {
                is BlockView.Title.Document -> resetTopToolbarTitle(
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
        if (emoji != null) topToolbar.emoji.text = emoji
        if (image != null) {
            topToolbar.image.visible()
            Glide
                .with(topToolbar.image)
                .load(image)
                .centerInside()
                .circleCrop()
                .into(topToolbar.image)
        } else {
            topToolbar.image.setImageDrawable(null)
        }
    }

    private fun render(state: ControlPanelState) {
        Timber.d("Rendering new control panel state:\n$state")

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
                    //recycler.apply { itemAnimator = DefaultItemAnimator() }
                    hideSoftInput()
                    Timber.d("Hiding top menu")
                    topToolbar.invisible()
                    lifecycleScope.launch {
                        delay(DELAY_BEFORE_INIT_SAM_SEARCH)
                        bottomMenu.showWithAnimation()
                        showSelectButton()
                    }
                }
            } else {
                //recycler.apply { itemAnimator = null }
                bottomMenu.hideWithAnimation()
                hideSelectButton()
            }
            if (isScrollAndMoveEnabled)
                enterScrollAndMove()
            else
                exitScrollAndMove()
        }

        state.stylingToolbar.apply {
            if (isVisible) {
                styleToolbar.update(
                    config = config!!,
                    props = props
                )
                hideSoftInput()
                lifecycleScope.launch {
                    delay(300)
                    mode?.let { styleToolbar.mode = it }
                    styleToolbar.showWithAnimation()
                    recycler.updatePadding(bottom = dimen(R.dimen.dp_203) + dimen(R.dimen.dp_16))
                }
            } else {
                styleToolbar.hideWithAnimation()
                recycler.updatePadding(bottom = dimen(R.dimen.default_toolbar_height))
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

    private fun enterScrollAndMove() {
        if (recycler.itemDecorationCount == 0) {

            val offset = recycler.computeVerticalScrollOffset()

            recycler.addItemDecoration(scrollAndMoveTargetHighlighter)

            if (offset < screen.y / 3) {
                lifecycleScope.launch {
                    delay(100)
                    recycler.smoothScrollBy(0, screen.y / 3)
                }
            }

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

    private fun renderError(message: String) {
        toast(message)
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

    //------------ End of Anytype Custom Context Menu ------------

    companion object {
        const val ID_KEY = "id"
        const val DEBUG_SETTINGS = "debug_settings"
        const val ID_EMPTY_VALUE = ""

        const val NOT_IMPLEMENTED_MESSAGE = "Not implemented."

        const val FAB_SHOW_ANIMATION_START_DELAY = 250L
        const val FAB_SHOW_ANIMATION_DURATION = 100L

        const val SHOW_MENTION_TRANSITION_DURATION = 150L
        const val SELECT_BUTTON_SHOW_ANIMATION_DURATION = 200L
        const val SELECT_BUTTON_HIDE_ANIMATION_DURATION = 200L
        const val SELECT_BUTTON_ANIMATION_PROPERTY = "translationY"
        const val TARGETER_ANIMATION_PROPERTY = "translationY"

        const val SAM_DEBOUNCE = 100L
        const val DELAY_BEFORE_INIT_SAM_SEARCH = 300L
    }

    override fun onDismissBlockActionToolbar() {
        vm.onSystemBackPressed(childFragmentManager.backStackEntryCount > 0)
    }
}

interface OnFragmentInteractionListener {
    fun onAddMarkupLinkClicked(blockId: String, link: String, range: IntRange)
    fun onRemoveMarkupLinkClicked(blockId: String, range: IntRange)
    fun onBlockActionClicked(id: String, action: ActionItemType)
    fun onDismissBlockActionToolbar()
    fun onAddBookmarkUrlClicked(target: String, url: String)
}