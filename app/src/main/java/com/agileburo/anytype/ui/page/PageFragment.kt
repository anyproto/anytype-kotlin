package com.agileburo.anytype.ui.page

import android.Manifest
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.addCallback
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.animation.doOnEnd
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.agileburo.anytype.BuildConfig
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.common.Alignment
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.extensions.range
import com.agileburo.anytype.core_ui.features.page.BlockAdapter
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.TurnIntoActionReceiver
import com.agileburo.anytype.core_ui.features.page.styling.StylingEvent
import com.agileburo.anytype.core_ui.features.page.styling.StylingMode
import com.agileburo.anytype.core_ui.menu.AnytypeContextMenuEvent
import com.agileburo.anytype.core_ui.menu.ContextMenuType
import com.agileburo.anytype.core_ui.menu.DocumentPopUpMenu
import com.agileburo.anytype.core_ui.model.UiBlock
import com.agileburo.anytype.core_ui.reactive.clicks
import com.agileburo.anytype.core_ui.state.ControlPanelState
import com.agileburo.anytype.core_ui.tools.ClipboardInterceptor
import com.agileburo.anytype.core_ui.tools.FirstItemInvisibilityDetector
import com.agileburo.anytype.core_ui.tools.OutsideClickDetector
import com.agileburo.anytype.core_ui.widgets.ActionItemType
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
import com.agileburo.anytype.presentation.page.editor.Command
import com.agileburo.anytype.presentation.page.editor.ViewState
import com.agileburo.anytype.presentation.settings.EditorSettings
import com.agileburo.anytype.ui.base.NavigationFragment
import com.agileburo.anytype.ui.menu.AnytypeContextMenu
import com.agileburo.anytype.ui.page.modals.*
import com.agileburo.anytype.ui.page.modals.actions.BlockActionToolbarFactory
import com.agileburo.anytype.ui.page.modals.actions.DocumentIconActionMenu
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import kotlinx.android.synthetic.main.fragment_page.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
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
    TurnIntoActionReceiver,
    ClipboardInterceptor,
    PickiTCallbacks {

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(PageViewModel::class.java)
    }
    private lateinit var pickiT: PickiT
    private var anytypeContextMenu: AnytypeContextMenu? = null

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
            onFooterClicked = vm::onOutsideClicked,
            onTextInputClicked = vm::onTextInputClicked,
            onPageIconClicked = vm::onPageIconClicked,
            onTogglePlaceholderClicked = vm::onTogglePlaceholderClicked,
            onToggleClicked = vm::onToggleClicked,
            onMarkupActionClicked = vm::onMarkupActionClicked,
            onLongClickListener = vm::onBlockLongPressedClicked,
            onTitleTextInputClicked = vm::onTitleTextInputClicked,
            onClickListener = vm::onClickListener,
            clipboardInterceptor = this,
            anytypeContextMenuListener = anytypeContextMenuListener
        )
    }

    private val titleVisibilityDetector by lazy {
        FirstItemInvisibilityDetector { isVisible ->
            if (isVisible) {
                topToolbar.title.invisible()
                topToolbar.icon.invisible()
            } else {
                topToolbar.title.visible()
                topToolbar.icon.visible()
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
        vm.open(id = extractDocumentId())
        pickiT = PickiT(requireContext(), this, requireActivity())
        setupOnBackPressedDispatcher()
        getEditorSettings()
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

        toolbar
            .unfocusClicks()
            .onEach { vm.onHideKeyboardClicked() }
            .launchIn(lifecycleScope)

        toolbar
            .addBlockClicks()
            .onEach { vm.onAddBlockToolbarClicked() }
            .launchIn(lifecycleScope)

        toolbar
            .enterMultiSelectModeClicks()
            .onEach { vm.onEnterMultiSelectModeClicked() }
            .launchIn(lifecycleScope)

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
            .turnIntoClicks()
            .onEach { vm.onMultiSelectTurnIntoButtonClicked() }
            .launchIn(lifecycleScope)

        select
            .clicks()
            .onEach { vm.onMultiSelectModeSelectAllClicked() }
            .launchIn(lifecycleScope)

        fab.clicks().onEach { vm.onPlusButtonPressed() }.launchIn(lifecycleScope)

        topToolbar.menu.clicks().onEach { showToolbarMenu() }.launchIn(lifecycleScope)

        topToolbar.back.clicks().onEach {
            hideSoftInput()
            vm.onBackButtonPressed()
        }.launchIn(lifecycleScope)

        lifecycleScope.launch {
            styleToolbar.events.collect { event ->
                when (event) {
                    is StylingEvent.Coloring.Text -> {
                        if (styleToolbar.mode == StylingMode.MARKUP)
                            vm.onMarkupTextColorAction(event.color.title)
                        else
                            vm.onToolbarTextColorAction(event.color.title)
                    }
                    is StylingEvent.Coloring.Background -> {
                        if (styleToolbar.mode == StylingMode.MARKUP)
                            vm.onMarkupBackgroundColorAction(event.color.title)
                        else
                            vm.onBlockBackgroundColorAction(event.color.title)
                    }
                    is StylingEvent.Markup.Bold -> {
                        vm.onBlockStyleMarkupActionClicked(
                            action = Markup.Type.BOLD
                        )
                    }
                    is StylingEvent.Markup.Italic -> {
                        vm.onBlockStyleMarkupActionClicked(
                            action = Markup.Type.ITALIC
                        )
                    }
                    is StylingEvent.Markup.Strikethrough -> {
                        vm.onBlockStyleMarkupActionClicked(
                            action = Markup.Type.STRIKETHROUGH
                        )
                    }
                    is StylingEvent.Markup.Code -> {
                        vm.onBlockStyleMarkupActionClicked(
                            action = Markup.Type.KEYBOARD
                        )
                    }
                    is StylingEvent.Alignment.Left -> {
                        vm.onBlockAlignmentActionClicked(
                            alignment = Alignment.START
                        )
                    }
                    is StylingEvent.Alignment.Center -> {
                        vm.onBlockAlignmentActionClicked(
                            alignment = Alignment.CENTER
                        )
                    }
                    is StylingEvent.Alignment.Right -> {
                        vm.onBlockAlignmentActionClicked(
                            alignment = Alignment.END
                        )
                    }
                    is StylingEvent.Sliding.Color -> {
                        vm.onStyleColorSlideClicked()
                    }
                    is StylingEvent.Sliding.Background -> {
                        vm.onStyleBackgroundSlideClicked()
                    }
                }
            }
        }

        lifecycleScope.launch {
            styleToolbar.closeButtonClicks().collect {
                vm.onCloseBlockStyleToolbarClicked()
            }
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
        vm.state.observe(viewLifecycleOwner, Observer { render(it) })
        vm.navigation.observe(viewLifecycleOwner, navObserver)
        vm.controlPanelViewState.observe(viewLifecycleOwner, Observer { render(it) })
        vm.focus.observe(viewLifecycleOwner, Observer { handleFocus(it) })
        vm.commands.observe(viewLifecycleOwner, Observer { execute(it) })
        vm.error.observe(viewLifecycleOwner, Observer { renderError(it) })
    }

    override fun onDestroyView() {
        removeContextMenu()
        clearPickit()
        super.onDestroyView()
    }

    override fun onDestroy() {
        pickiT.deleteTemporaryFile()
        super.onDestroy()
    }

    override fun onBlockActionClicked(id: String, action: ActionItemType) {
        vm.onActionBarItemClicked(id, action)
    }

    private fun handleFocus(focus: Id) {
        Timber.d("Handling focus: $focus")
        if (focus.isEmpty()) {
            placeholder.requestFocus()
            hideKeyboard()
            fab.apply {
                scaleX = 0f
                scaleY = 0f
                visible()
                animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .apply {
                        startDelay = FAB_SHOW_ANIMATION_START_DELAY
                        duration = FAB_SHOW_ANIMATION_DURATION
                        interpolator = AccelerateInterpolator()
                    }
                    .start()
            }
        } else {
            fab.gone()
        }
    }

    private fun execute(event: EventWrapper<Command>) {
        event.getContentIfNotHandled()?.let { command ->
            when (command) {
                is Command.OpenDocumentIconActionMenu -> {
                    hideSoftInput()
                    if (recycler.scrollY > 0) recycler.smoothScrollToPosition(0)
                    val shared = recycler.getChildAt(0).findViewById<TextView>(R.id.logo)
                    val fr = DocumentIconActionMenu.new(
                        y = shared.y + dimen(R.dimen.dp_48),
                        emoji = shared.text.toString(),
                        target = command.target
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
                    DocumentEmojiIconPickerFragment.newInstance(
                        context = requireArguments().getString(ID_KEY, ID_EMPTY_VALUE),
                        target = command.target
                    ).show(childFragmentManager, null)
                }
                is Command.OpenAddBlockPanel -> {
                    AddBlockFragment.newInstance().show(childFragmentManager, null)
                }
                is Command.OpenTurnIntoPanel -> {
                    TurnIntoFragment.single(
                        target = command.target
                    ).show(childFragmentManager, null)
                }
                is Command.OpenMultiSelectTurnIntoPanel -> {
                    TurnIntoFragment.multiple().show(childFragmentManager, null)
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
                        .add(R.id.root, BlockActionToolbarFactory.newInstance(command.block), null)
                        .addToBackStack(null)
                        .commit()
                }
                is Command.CloseKeyboard -> {
                    hideSoftInput()
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
            view is BlockView.Title
        }?.let { view ->
            topToolbar.title.text = (view as BlockView.Title).text
            topToolbar.icon.text = view.emoji
        }
    }

    private fun render(state: ControlPanelState) {
        Timber.d("Rendering new control panel state:\n$state")

        if (state.mainToolbar.isVisible)
            toolbar.visible()
        else
            toolbar.invisible()

        state.multiSelect.apply {
            if (isVisible) {
                if (count == 0) {
                    selectText.setText(R.string.select_all)
                } else {
                    selectText.setText(R.string.unselect_all)
                }
                bottomMenu.update(count)
                if (!bottomMenu.isShowing) {
                    //recycler.apply { itemAnimator = DefaultItemAnimator() }
                    hideSoftInput()
                    Timber.d("Hiding top menu")
                    topToolbar.invisible()
                    lifecycleScope.launch {
                        delay(300)
                        bottomMenu.showWithAnimation()
                        showSelectButton()
                    }
                }
            } else {
                //recycler.apply { itemAnimator = null }
                bottomMenu.hideWithAnimation()
                hideSelectButton()
            }
        }

        state.stylingToolbar.apply {
            if (isVisible) {
                styleToolbar.props = props
                hideSoftInput()
                lifecycleScope.launch {
                    delay(300)
                    mode?.let { styleToolbar.mode = it }
                    type?.let { styleToolbar.applyStylingType(it) }
                    styleToolbar.showWithAnimation()
                    recycler.updatePadding(bottom = dimen(R.dimen.dp_203) + dimen(R.dimen.dp_16))
                }
            } else {
                styleToolbar.hideWithAnimation()
                recycler.updatePadding(bottom = dimen(R.dimen.default_toolbar_height))
            }
        }
    }

    private fun hideSelectButton() {
        ObjectAnimator.ofFloat(
            select,
            SELECT_BUTTON_ANIMATION_PROPERTY,
            -requireContext().dimen(R.dimen.dp_48)
        ).apply {
            duration = SELECT_BUTTON_HIDE_ANIMATION_DURATION
            interpolator = DecelerateInterpolator()
            doOnEnd { topToolbar.visible() }
            start()
        }
    }

    override fun onClipboardAction(action: ClipboardInterceptor.Action) {
        when(action) {
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

    override fun injectDependencies() {
        componentManager().pageComponent.get(extractDocumentId()).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().pageComponent.release(extractDocumentId())
    }

    private fun getEditorSettings() {
        val editorSettings: EditorSettings? = arguments?.getParcelable(DEBUG_SETTINGS)
        if (editorSettings == null || editorSettings.customContextMenu) {
            initContextMenuListener()
        }
    }

    //------------ Anytype Custom Context Menu ------------

    private var anytypeContextMenuListener: ((AnytypeContextMenuEvent) -> Unit)? = null

    private fun initContextMenuListener() {
        anytypeContextMenuListener = {
            when (it) {
                AnytypeContextMenuEvent.Detached -> removeContextMenu()
                is AnytypeContextMenuEvent.Selected -> onAnytypeContextMenuEvent(it.view, it.type)
                is AnytypeContextMenuEvent.Create -> onAnytypeContextMenuEvent(it.view, it.type)
                AnytypeContextMenuEvent.MarkupChanged -> anytypeContextMenu?.showAtLocation()
            }
        }
    }

    private fun onAnytypeContextMenuEvent(
        originatingView: TextView,
        type: ContextMenuType
    ) {
        if (anytypeContextMenu == null) {
            anytypeContextMenu =
                AnytypeContextMenu(
                    type = type,
                    context = requireContext(),
                    anchorView = originatingView,
                    parent = recycler,
                    onMarkupActionClicked = {
                        vm.onMarkupActionClicked(it, originatingView.range())
                    }
                )
        }
        anytypeContextMenu?.showAtLocation()
    }

    private fun removeContextMenu() {
        anytypeContextMenu?.finish()
        anytypeContextMenu = null
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
        pickitAlertDialog = AlertDialog.Builder(requireContext(), R.style.SyncFromCloudDialog).apply {
            val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_layout, null)
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
        vm.onAddVideoFileClicked(filePath)
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

        const val SELECT_BUTTON_SHOW_ANIMATION_DURATION = 200L
        const val SELECT_BUTTON_HIDE_ANIMATION_DURATION = 200L
        const val SELECT_BUTTON_ANIMATION_PROPERTY = "translationY"
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