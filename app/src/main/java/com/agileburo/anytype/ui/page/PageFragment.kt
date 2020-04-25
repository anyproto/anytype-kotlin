package com.agileburo.anytype.ui.page

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.extensions.invisible
import com.agileburo.anytype.core_ui.extensions.visible
import com.agileburo.anytype.core_ui.features.page.BlockAdapter
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.styling.StylingEvent
import com.agileburo.anytype.core_ui.features.page.styling.StylingMode
import com.agileburo.anytype.core_ui.menu.DocumentPopUpMenu
import com.agileburo.anytype.core_ui.model.UiBlock
import com.agileburo.anytype.core_ui.reactive.clicks
import com.agileburo.anytype.core_ui.state.ControlPanelState
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
import com.agileburo.anytype.ui.base.NavigationFragment
import com.agileburo.anytype.ui.page.modals.*
import com.agileburo.anytype.ui.page.modals.actions.BlockActionToolbarFactory
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
            onSplitLineEnterClicked = vm::onSplitLineEnterClicked,
            onEndLineEnterClicked = { id, editable ->
                vm.onEndLineEnterClicked(
                    id = id,
                    text = editable.toString(),
                    marks = editable.extractMarks()
                )
            },
            onEndLineEnterTitleClicked = { vm.onEndLineEnterTitleClicked() },
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
            onMediaBlockMenuClick = vm::onMediaBlockMenuClicked,
            onBookmarkMenuClicked = vm::onBookmarkMenuClicked,
            onMarkupActionClicked = vm::onMarkupActionClicked,
            onLongClickListener = vm::onBlockLongPressedClicked
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
            "PickiTonCompleteListener path:$path, wasDriveFile:$wasDriveFile, wasUnknownProvider:$wasUnknownProvider, wasSuccessful:$wasSuccessful, reason:$Reason"
        )
        vm.onAddVideoFileClicked(filePath = path)
    }

    @Inject
    lateinit var factory: PageViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.open(id = extractDocumentId())
        pickiT = PickiT(requireContext(), this)
        setupOnBackPressedDispatcher()
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


        fab.clicks().onEach { vm.onPlusButtonPressed() }.launchIn(lifecycleScope)
        menu.clicks().onEach { showToolbarMenu() }.launchIn(lifecycleScope)

        backButton.clicks().onEach {
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
            requireContext(),
            menu,
            vm::onArchiveThisPageClicked
        ).show()
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
    }

    override fun onBlockActionClicked(id: String, action: ActionItemType) {
        vm.onActionBarItemClicked(id, action)
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
                is PageViewModel.Command.PopBackStack -> {
                    childFragmentManager.popBackStack()
                }
                is PageViewModel.Command.OpenActionBar -> {
                    hideKeyboard()
                    childFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.action_bar_enter, R.anim.action_bar_exit)
                        .add(R.id.root, BlockActionToolbarFactory.newInstance(command.block), null)
                        .addToBackStack(null)
                        .commit()
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

        if (state.mainToolbar.isVisible)
            toolbar.visible()
        else
            toolbar.invisible()

        state.stylingToolbar.apply {
            if (isVisible) {
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

    override fun onDismissBlockActionToolbar() {
        vm.onSystemBackPressed(childFragmentManager.backStackEntryCount > 0)
    }
}

interface OnFragmentInteractionListener {
    fun onAddMarkupLinkClicked(blockId: String, link: String, range: IntRange)
    fun onRemoveMarkupLinkClicked(blockId: String, range: IntRange)
    fun onBlockActionClicked(id: String, action: ActionItemType)
    fun onDismissBlockActionToolbar()
}