package com.anytypeio.anytype.ui.editor

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_models.TimeInMillis
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.misc.OpenObjectNavigation
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.core_ui.extensions.addTextFromSelectedStart
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.extensions.cursorYBottomCoordinate
import com.anytypeio.anytype.core_ui.features.editor.AttachToChatToolbar
import com.anytypeio.anytype.core_ui.features.editor.BlockAdapter
import com.anytypeio.anytype.core_ui.features.editor.DragAndDropAdapterDelegate
import com.anytypeio.anytype.core_ui.features.editor.EditorDatePicker
import com.anytypeio.anytype.core_ui.features.editor.modal.SelectLanguageBottomSheet
import com.anytypeio.anytype.core_ui.features.editor.scrollandmove.DefaultScrollAndMoveTargetDescriptor
import com.anytypeio.anytype.core_ui.features.editor.scrollandmove.ScrollAndMoveStateListener
import com.anytypeio.anytype.core_ui.features.editor.scrollandmove.ScrollAndMoveTargetHighlighter
import com.anytypeio.anytype.core_ui.menu.ObjectTypePopupMenu
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_ui.reactive.longClicks
import com.anytypeio.anytype.core_ui.syncstatus.SpaceSyncStatusScreen
import com.anytypeio.anytype.core_ui.tools.ClipboardInterceptor
import com.anytypeio.anytype.core_ui.tools.EditorHeaderOverlayDetector
import com.anytypeio.anytype.core_ui.tools.LastItemBottomOffsetDecorator
import com.anytypeio.anytype.core_ui.tools.MarkupColorToolbarFooter
import com.anytypeio.anytype.core_ui.tools.MentionFooterItemDecorator
import com.anytypeio.anytype.core_ui.tools.NoteHeaderItemDecorator
import com.anytypeio.anytype.core_ui.tools.OutsideClickDetector
import com.anytypeio.anytype.core_ui.tools.SlashWidgetFooterItemDecorator
import com.anytypeio.anytype.core_ui.tools.StyleToolbarItemDecorator
import com.anytypeio.anytype.core_ui.widgets.FeaturedRelationGroupWidget
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_ui.widgets.toolbar.BlockToolbarWidget
import com.anytypeio.anytype.core_ui.widgets.toolbar.ChooseTypeHorizontalWidget
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.const.FileConstants.REQUEST_PROFILE_IMAGE_CODE
import com.anytypeio.anytype.core_utils.ext.Mimetype
import com.anytypeio.anytype.core_utils.ext.PopupExtensions.calculateRectInWindow
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.core_utils.ext.clipboard
import com.anytypeio.anytype.core_utils.ext.containsItemDecoration
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.drawable
import com.anytypeio.anytype.core_utils.ext.focusAndShowKeyboard
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.hide
import com.anytypeio.anytype.core_utils.ext.hideKeyboard
import com.anytypeio.anytype.core_utils.ext.hideSoftInput
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.lastDecorator
import com.anytypeio.anytype.core_utils.ext.parseImagePath
import com.anytypeio.anytype.core_utils.ext.safeNavigate
import com.anytypeio.anytype.core_utils.ext.screen
import com.anytypeio.anytype.core_utils.ext.show
import com.anytypeio.anytype.core_utils.ext.startMarketPageOrWeb
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.syncTranslationWithImeVisibility
import com.anytypeio.anytype.core_utils.ext.throttleFirst
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.intents.ActivityCustomTabsHelper
import com.anytypeio.anytype.core_utils.ui.showActionableSnackBar
import com.anytypeio.anytype.databinding.FragmentEditorBinding
import com.anytypeio.anytype.device.launchMediaPicker
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.di.feature.DefaultComponentParam
import com.anytypeio.anytype.ext.FragmentResultContract
import com.anytypeio.anytype.ext.extractMarks
import com.anytypeio.anytype.library_syntax_highlighter.obtainLanguages
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.EditorViewModelFactory
import com.anytypeio.anytype.presentation.editor.Snack
import com.anytypeio.anytype.presentation.editor.editor.Command
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.ViewState
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState.Toolbar.Main
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.sam.ScrollAndMoveTarget
import com.anytypeio.anytype.presentation.editor.editor.sam.ScrollAndMoveTargetDescriptor
import com.anytypeio.anytype.presentation.editor.markup.MarkupColorView
import com.anytypeio.anytype.presentation.editor.model.EditorFooter
import com.anytypeio.anytype.presentation.editor.template.SelectTemplateViewState
import com.anytypeio.anytype.presentation.relations.value.tagstatus.RelationContext
import com.anytypeio.anytype.ui.alert.AlertUpdateAppFragment
import com.anytypeio.anytype.ui.base.NavigationFragment
import com.anytypeio.anytype.ui.base.navigation
import com.anytypeio.anytype.ui.editor.cover.SelectCoverObjectFragment
import com.anytypeio.anytype.ui.editor.modals.CreateBookmarkFragment
import com.anytypeio.anytype.ui.editor.modals.IconPickerFragmentBase
import com.anytypeio.anytype.ui.editor.modals.SetBlockTextValueFragment
import com.anytypeio.anytype.ui.editor.modals.TextBlockIconPickerFragment
import com.anytypeio.anytype.ui.editor.sheets.ObjectMenuBaseFragment.DocumentMenuActionReceiver
import com.anytypeio.anytype.ui.editor.sheets.ObjectMenuFragment
import com.anytypeio.anytype.ui.linking.LinkToObjectFragment
import com.anytypeio.anytype.ui.linking.LinkToObjectOrWebPagesFragment
import com.anytypeio.anytype.ui.linking.OnLinkToAction
import com.anytypeio.anytype.ui.media.MediaActivity
import com.anytypeio.anytype.ui.moving.MoveToFragment
import com.anytypeio.anytype.ui.moving.OnMoveToAction
import com.anytypeio.anytype.ui.multiplayer.ShareSpaceFragment
import com.anytypeio.anytype.ui.objects.appearance.ObjectAppearanceSettingFragment
import com.anytypeio.anytype.ui.objects.creation.ObjectTypeSelectionFragment
import com.anytypeio.anytype.ui.objects.types.pickers.EditorObjectTypeUpdateFragment
import com.anytypeio.anytype.ui.objects.types.pickers.ObjectTypeSelectionListener
import com.anytypeio.anytype.ui.objects.types.pickers.ObjectTypeUpdateListener
import com.anytypeio.anytype.ui.primitives.ObjectFieldsFragment
import com.anytypeio.anytype.ui.relations.RelationAddToObjectBlockFragment
import com.anytypeio.anytype.ui.relations.RelationDateValueFragment
import com.anytypeio.anytype.ui.relations.RelationTextValueFragment
import com.anytypeio.anytype.ui.relations.value.ObjectValueFragment
import com.anytypeio.anytype.ui.relations.value.TagOrStatusValueFragment
import com.anytypeio.anytype.ui.templates.EditorTemplateFragment.Companion.ARG_TEMPLATE_ID
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject
import kotlin.math.abs
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

open class EditorFragment : NavigationFragment<FragmentEditorBinding>(R.layout.fragment_editor),
    OnFragmentInteractionListener,
    RelationTextValueFragment.TextValueEditReceiver,
    RelationDateValueFragment.DateValueEditReceiver,
    DocumentMenuActionReceiver,
    ClipboardInterceptor,
    OnMoveToAction,
    OnLinkToAction,
    ObjectTypeSelectionListener,
    ObjectTypeUpdateListener {

    private val keyboardDelayJobs = mutableListOf<Job>()

    protected val ctx get() = arg<Id>(CTX_KEY)
    protected val space get() = arg<Id>(SPACE_ID_KEY)

    /**
     * Navigation destination ID for safeNavigate calls.
     * Override in subclasses that use different navigation graphs.
     */
    protected open val navigationDestinationId: Int = R.id.pageScreen

    private val sideEffect: OpenObjectNavigation.SideEffect
        get() {
            val attachedChatId = argOrNull<Id>(ATTACH_TO_CHAT_ID_KEY)
            val attachedSpaceId = argOrNull<Id>(ATTACH_TO_CHAT_SPACE_KEY)
            return if (attachedChatId != null && attachedSpaceId != null) {
                OpenObjectNavigation.SideEffect.AttachToChat(
                    chat = attachedChatId,
                    space = attachedSpaceId
                )
            } else {
                OpenObjectNavigation.SideEffect.None
            }
        }

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
                    binding.styleToolbarOther.id -> {
                        vm.onCloseBlockStyleExtraToolbarClicked()
                    }
                    binding.styleToolbarMain.id -> {
                        vm.onCloseBlockStyleToolbarClicked()
                    }
                    binding.styleToolbarColors.id -> {
                        vm.onCloseBlockStyleColorToolbarClicked()
                    }
                    binding.blockActionToolbar.id -> {
                        vm.onBlockActionPanelHidden()
                    }
                    binding.undoRedoToolbar.id -> {
                        vm.onUndoRedoToolbarIsHidden()
                    }
                    binding.styleToolbarBackground.id -> {
                        vm.onCloseBlockStyleBackgroundToolbarClicked()
                    }
                    binding.simpleTableWidget.id -> {
                        vm.onHideSimpleTableWidget()
                    }
                }
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    private val scrollAndMoveTopMargin by lazy {
        0
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

    private val defaultBottomOffsetDecorator by lazy {
        LastItemBottomOffsetDecorator(
            dimen(R.dimen.dp_48)
        )
    }

    private val footerMentionDecorator by lazy { MentionFooterItemDecorator(screen) }
    private val noteHeaderDecorator by lazy {
        NoteHeaderItemDecorator(offset = dimen(R.dimen.default_note_title_offset))
    }
    private val markupColorToolbarFooter by lazy { MarkupColorToolbarFooter(screen) }
    private val slashWidgetFooter by lazy { SlashWidgetFooterItemDecorator(screen) }
    private val styleToolbarFooter by lazy { StyleToolbarItemDecorator(screen) }
    private val actionToolbarFooter by lazy { StyleToolbarItemDecorator(screen) }

    protected val vm by viewModels<EditorViewModel> { factory }

    private val blockAdapter by lazy {
        BlockAdapter(
            restore = vm.restore,
            initialBlock = mutableListOf(),
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
            onSplitDescription = { id, editable, range ->
                vm.onSplitObjectDescription(
                    target = id,
                    text = editable.toString(),
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
            onPageIconClicked = vm::onObjectIconClicked,
            onCoverClicked = vm::onAddCoverClicked,
            onTogglePlaceholderClicked = vm::onTogglePlaceholderClicked,
            onToggleClicked = vm::onToggleClicked,
            onTitleTextInputClicked = vm::onTitleTextInputClicked,
            onClickListener = vm::onClickListener,
            clipboardInterceptor = this,
            onMentionEvent = vm::onMentionEvent,
            onSlashEvent = vm::onSlashTextWatcherEvent,
            onBackPressedCallback = { vm.onBackPressedCallback() },
            onKeyPressedEvent = vm::onKeyPressedEvent,
            onDragAndDropTrigger = { vh: RecyclerView.ViewHolder, event: MotionEvent? ->
                dndDelegate.handleDragAndDropTrigger(vh, event)
            },
            onDragListener = dndDelegate.dndListener,
            lifecycle = lifecycle,
            dragAndDropSelector = DragAndDropAdapterDelegate(),
            onCellSelectionChanged = vm::onCellSelectionChanged
        )
    }

    private fun searchScrollAndMoveTarget() {

        binding.recycler.findFocus().let { child ->
            if (child is TextInputWidget) child.text
        }

        val centerX = screen.x / 2f

        val centerY = (binding.targeter.y + (binding.targeter.height / 2f)) - scrollAndMoveTopMargin

        var target: View? = binding.recycler.findChildViewUnder(centerX, centerY)

        if (target == null) {
            target = binding.recycler.findChildViewUnder(centerX, centerY - 5)
            if (target == null) {
                target = binding.recycler.findChildViewUnder(centerX, centerY + 5)
            }
        }

        if (target == null) {
            scrollAndMoveTargetDescriptor.clear()
        } else {
            val position = binding.recycler.getChildAdapterPosition(target)
            val top = target.top
            val height = target.height

            val view = blockAdapter.views[position]

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

    val titleVisibilityDetector by lazy {
        EditorHeaderOverlayDetector(
            threshold = dimen(R.dimen.default_toolbar_height),
            thresholdPadding = dimen(R.dimen.dp_8)
        ) { isHeaderOverlaid ->
            if (isHeaderOverlaid) {
                binding.topToolbar.setBackgroundColor(0)
                binding.topToolbar.container.animate().alpha(0f)
                    .setDuration(DEFAULT_TOOLBAR_ANIM_DURATION)
                    .start()
                if (blockAdapter.views.isNotEmpty()) {
                    val firstView = blockAdapter.views.first()
                    if (firstView is BlockView.Title && firstView.hasCover) {
                        binding.topToolbar.setStyle(overCover = true)
                    } else {
                        binding.topToolbar.setStyle(overCover = false)
                    }
                }
            } else {
                binding.topToolbar.setBackgroundColor(requireContext().color(R.color.defaultCanvasColor))
                binding.topToolbar.container.animate().alpha(1f)
                    .setDuration(DEFAULT_TOOLBAR_ANIM_DURATION)
                    .start()
                binding.topToolbar.setStyle(overCover = false)
            }
        }
    }

    private val pickerDelegate = PickerDelegate.Impl(this) { actions ->
        when (actions) {
            PickerDelegate.Actions.OnCancelCopyFileToCacheDir -> {
                vm.onCancelCopyFileToCacheDir()
            }
            is PickerDelegate.Actions.OnPickedDocImageFromDevice -> {
                vm.onPickedDocImageFromDevice(actions.ctx, actions.filePath)
            }
            is PickerDelegate.Actions.OnProceedWithFilePath -> {
                vm.onProceedWithFilePath(filePath = actions.filePath)
            }
            is PickerDelegate.Actions.OnStartCopyFileToCacheDir -> {
                vm.onStartCopyFileToCacheDir(actions.uri)
            }
        }
    }
    private val dndDelegate = DragAndDropDelegate()

    @Inject
    lateinit var factory: EditorViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pickerDelegate.initPicker(ctx)
        setupOnBackPressedDispatcher()
        getEditorSettings()
    }

    override fun onStart() {
        with(lifecycleScope) {
            jobs += subscribe(vm.toasts) { toast(it) }
            jobs += subscribe(vm.snacks) { snack ->
                when (snack) {
                    is Snack.UndoRedo -> {
                        Snackbar.make(requireView(), snack.message, Snackbar.LENGTH_SHORT).apply {
                            anchorView = binding.undoRedoToolbar
                        }.show()
                    }
                }
            }
            jobs += subscribe(vm.footers) { footer ->
                when (footer) {
                    EditorFooter.None -> {
                        if (binding.recycler.containsItemDecoration(noteHeaderDecorator)) {
                            binding.recycler.removeItemDecoration(noteHeaderDecorator)
                        }
                    }
                    EditorFooter.Note -> {
                        if (!binding.recycler.containsItemDecoration(noteHeaderDecorator)) {
                            binding.recycler.addItemDecoration(noteHeaderDecorator)
                        }
                    }
                }
            }
            jobs += subscribe(vm.copyFileStatus) { command ->
                pickerDelegate.onCopyFileCommand(command)
            }
            jobs += subscribe(vm.selectTemplateViewState) { state ->
                when (state) {
                    is SelectTemplateViewState.Active -> {
                        binding.topToolbar.showTemplates()
                        binding.topToolbar.setTemplates(count = state.count)
                    }
                    SelectTemplateViewState.Idle -> {
                        binding.topToolbar.hideTemplates()
                    }
                }
            }
        }
        vm.onStart(id = extractDocumentId(), space = space, saveAsLastOpened = saveAsLastOpened())
        super.onStart()
    }

    override fun onStop() {
        vm.onStop()
        pickerDelegate.onStop()
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(CURRENT_MEDIA_UPLOAD_KEY, vm.currentMediaUploadDescription)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            vm.onRestoreSavedState(savedInstanceState.getParcelable(CURRENT_MEDIA_UPLOAD_KEY))
        }
    }

    private fun setupOnBackPressedDispatcher() =
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(this) {
                vm.onSystemBackPressed(childFragmentManager.backStackEntryCount > 0)
            }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWindowInsetAnimation()

        dndDelegate.init(blockAdapter, vm, this)
        binding.recycler.addOnItemTouchListener(
            OutsideClickDetector(vm::onOutsideClicked)
        )

        observeSelectingTemplate()

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            itemAnimator = null
            adapter = blockAdapter
            addOnScrollListener(titleVisibilityDetector)
            addItemDecoration(defaultBottomOffsetDecorator)
        }

        binding.toolbar.apply {
            blockActionsClick()
                .onEach { vm.onBlockToolbarBlockActionsClicked() }
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
            undoRedoClicks()
                .onEach { vm.onUndoRedoActionClicked() }
                .launchIn(lifecycleScope)
        }

        binding.scrollAndMoveBottomAction
            .apply
            .clicks()
            .throttleFirst()
            .onEach {
                vm.onApplyScrollAndMoveClicked()
                onApplyScrollAndMoveClicked()
            }
            .launchIn(lifecycleScope)

        binding.scrollAndMoveBottomAction
            .cancel
            .clicks()
            .throttleFirst()
            .onEach { vm.onExitScrollAndMoveClicked() }
            .launchIn(lifecycleScope)

        binding.multiSelectTopToolbar
            .doneButton
            .clicks()
            .throttleFirst()
            .onEach { vm.onExitMultiSelectModeClicked() }
            .launchIn(lifecycleScope)

        binding.multiSelectTopToolbar
            .selectAllBlocks
            .clicks()
            .throttleFirst()
            .onEach { vm.onSelectAllClicked() }
            .launchIn(lifecycleScope)


        binding.bottomToolbar
            .shareClicks()
            .onEach { vm.onShareButtonClicked() }
            .launchIn(lifecycleScope)

        binding.bottomToolbar
            .homeClicks()
            .onEach { vm.onHomeButtonClicked() }
            .launchIn(lifecycleScope)

        binding.bottomToolbar
            .chatClicks()
            .onEach { vm.onHomeButtonClicked() }
            .launchIn(lifecycleScope)

        binding.bottomToolbar
            .searchClicks()
            .onEach { vm.onPageSearchClicked() }
            .launchIn(lifecycleScope)

        binding.bottomToolbar
            .addDocClicks()
            .onEach { vm.onAddNewDocumentClicked() }
            .launchIn(lifecycleScope)

        binding
            .bottomToolbar
            .binding
            .btnAddDoc
            .longClicks(withHaptic = true)
            .onEach {
                val dialog = ObjectTypeSelectionFragment.new(space = space)
                dialog.show(childFragmentManager, "editor-create-object-of-type-dialog")
            }
            .launchIn(lifecycleScope)

        binding.topToolbar.menu
            .clicks()
            .throttleFirst()
            .onEach { vm.onDocumentMenuClicked() }
            .launchIn(lifecycleScope)

        binding.topToolbar.back
            .clicks()
            .throttleFirst()
            .onEach { vm.onBackButtonPressed() }
            .launchIn(lifecycleScope)

        binding.markupToolbar
            .highlightClicks()
            .onEach { vm.onMarkupHighlightToggleClicked() }
            .launchIn(lifecycleScope)

        binding.markupToolbar
            .colorClicks()
            .onEach { vm.onMarkupColorToggleClicked() }
            .launchIn(lifecycleScope)

        binding.markupToolbar
            .linkClicks()
            .onEach { vm.onEditLinkClicked() }
            .launchIn(lifecycleScope)

        binding.markupToolbar
            .markup()
            .onEach { type -> vm.onStyleToolbarMarkupAction(type, null) }
            .launchIn(lifecycleScope)

        binding.blockActionToolbar.actionListener = { action -> vm.onMultiSelectAction(action) }

        binding.markupColorToolbar.onColorClickedListener = { color ->
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

        binding.simpleTableWidget.setListener {
            vm.onSimpleTableWidgetItemClicked(it)
        }

        binding.undoRedoToolbar.undo.clicks()
            .throttleFirst()
            .onEach {
                vm.onActionUndoClicked()
            }.launchIn(lifecycleScope)

        binding.undoRedoToolbar.redo.clicks()
            .throttleFirst()
            .onEach {
                vm.onActionRedoClicked()
            }.launchIn(lifecycleScope)

        lifecycleScope.subscribe(binding.styleToolbarMain.styles) {
            vm.onUpdateTextBlockStyle(it)
        }

        lifecycleScope.subscribe(binding.styleToolbarMain.other) {
            vm.onBlockStyleToolbarOtherClicked()
        }

        lifecycleScope.subscribe(binding.styleToolbarMain.colors) {
            vm.onBlockStyleToolbarColorClicked()
        }

        lifecycleScope.subscribe(binding.styleToolbarColors.events) {
            vm.onStylingToolbarEvent(it)
        }

        lifecycleScope.subscribe(binding.styleToolbarOther.actions) {
            vm.onStylingToolbarEvent(it)
        }

        lifecycleScope.subscribe(binding.styleToolbarBackground.actions) {
            vm.onStylingToolbarEvent(it)
        }

        binding.mentionSuggesterToolbar.setupClicks(
            mentionClick = vm::onMentionSuggestClick,
            newPageClick = vm::onAddMentionNewPageClicked
        )

        lifecycleScope.launch {
            binding.slashWidget.clickEvents.collect { item ->
                vm.onSlashItemClicked(item)
            }
        }

        binding.topToolbar.templates.clicks()
            .throttleFirst()
            .onEach { vm.onTemplatesToolbarClicked() }
            .launchIn(lifecycleScope)

        lifecycleScope.launch {
            binding.searchToolbar.events().collect { vm.onSearchToolbarEvent(it) }
        }

        binding.objectNotExist.root.findViewById<TextView>(R.id.btnToDashboard).clicks()
            .throttleFirst()
            .onEach { vm.onHomeButtonClicked() }
            .launchIn(lifecycleScope)

        binding.chooseTypeWidget.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ChooseTypeHorizontalWidget(
                    state = vm.typesWidgetState.collectAsStateWithLifecycle().value,
                    onTypeClicked = vm::onTypesWidgetItemClicked
                )
            }
        }

        binding.syncStatusWidget.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                SpaceSyncStatusScreen(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(bottom = 16.dp)
                        .windowInsetsPadding(WindowInsets.navigationBars),
                    modifierCard = Modifier.padding(start = 8.dp, end = 8.dp),
                    uiState = vm.syncStatusWidget.collectAsStateWithLifecycle().value,
                    onDismiss = vm::onSyncWidgetDismiss,
                    onUpdateAppClick = vm::onUpdateAppClick
                )
            }
        }

        binding.editorDatePicker.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                EditorDatePicker(
                    uiState = vm.mentionDatePicker.collectAsStateWithLifecycle().value,
                    onEvent = vm::onEditorDatePickerEvent
                )
            }
        }

        binding.selectLanguageSheet.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val state = vm.selectLanguageState.collectAsStateWithLifecycle().value
                if (state != null) {
                    SelectLanguageBottomSheet(
                        languages = remember { requireContext().obtainLanguages() },
                        selectedLanguage = state.currentLang,
                        onLanguageSelected = { key ->
                            vm.onSelectProgrammingLanguageClicked(state.target, key)
                            vm.onDismissSelectLanguage()
                        },
                        onDismiss = { vm.onDismissSelectLanguage() }
                    )
                }
            }
        }

        sideEffectHandler()

        BottomSheetBehavior.from(binding.styleToolbarMain).state = BottomSheetBehavior.STATE_HIDDEN
        BottomSheetBehavior.from(binding.styleToolbarOther).state = BottomSheetBehavior.STATE_HIDDEN
        BottomSheetBehavior.from(binding.styleToolbarColors).state =
            BottomSheetBehavior.STATE_HIDDEN
        BottomSheetBehavior.from(binding.blockActionToolbar).state =
            BottomSheetBehavior.STATE_HIDDEN
        BottomSheetBehavior.from(binding.undoRedoToolbar).state = BottomSheetBehavior.STATE_HIDDEN
        BottomSheetBehavior.from(binding.styleToolbarBackground).state =
            BottomSheetBehavior.STATE_HIDDEN
        BottomSheetBehavior.from(binding.simpleTableWidget).state =
            BottomSheetBehavior.STATE_HIDDEN

    }

    private fun sideEffectHandler() {
        if (sideEffect is OpenObjectNavigation.SideEffect.AttachToChat) {
            binding.attachToChatPanel.apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    AttachToChatToolbar(
                        onAttachClicked = {
                            val effect = sideEffect
                            if (effect is OpenObjectNavigation.SideEffect.AttachToChat) {
                                val bundle = bundleOf(
                                    FragmentResultContract.ATTACH_TO_CHAT_CHAT_ID_KEY to effect.chat,
                                    FragmentResultContract.ATTACH_TO_CHAT_SPACE_ID_KEY to effect.space,
                                    FragmentResultContract.ATTACH_TO_CHAT_TARGET_ID_KEY to ctx
                                )

                                // Closing keyboard before exit
                                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                val focus = requireActivity().currentFocus ?: View(requireContext())
                                imm.hideSoftInputFromWindow(focus.windowToken, 0)

                                parentFragmentManager.setFragmentResult(
                                    FragmentResultContract.ATTACH_TO_CHAT_CONTRACT_KEY, bundle
                                )

                                parentFragmentManager.popBackStack()
                            }
                        },
                        onCancelClicked = {
                            binding.attachToChatPanel.gone()
                        }
                    )
                }
                visible()
            }
        }
    }

    open fun setupWindowInsetAnimation() {
        binding.toolbar.syncTranslationWithImeVisibility(
            dispatchMode = DISPATCH_MODE_STOP
        )
        binding.chooseTypeWidget.syncTranslationWithImeVisibility(
            dispatchMode = DISPATCH_MODE_STOP
        )
    }

    private fun onApplyScrollAndMoveClicked() {
        scrollAndMoveTargetDescriptor.current()?.let { target ->
            vm.onApplyScrollAndMove(
                target = blockAdapter.views[target.position].id,
                ratio = target.ratio
            )
        }
    }

    override fun onAddBookmarkUrlClicked(target: String, url: String) {
        vm.onAddBookmarkUrl(target = target, url = url)
    }

    override fun onSetBlockWebLink(blockId: Id, link: Id) {
        vm.onAddWebLinkToBlock(blockId = blockId, link = link)
    }

    override fun onSetBlockObjectLink(blockId: Id, objectId: Id) {
        vm.onAddObjectLinkToBlock(blockId = blockId, objectId = objectId)
    }

    override fun onRemoveMarkupLinkClicked(blockId: String, range: IntRange) {
        vm.onUnlinkPressed(blockId = blockId, range = range)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm.state.observe(viewLifecycleOwner) { render(it) }
        vm.navigation.observe(viewLifecycleOwner, navObserver)
        vm.controlPanelViewState.observe(viewLifecycleOwner) { render(it) }
        vm.commands.observe(viewLifecycleOwner) { execute(it) }
        vm.searchResultScrollPosition
            .filter { it != EditorViewModel.NO_SEARCH_RESULT_POSITION }
            .onEach {
                if (hasBinding) {
                    (binding.recycler.layoutManager as? LinearLayoutManager)
                        ?.scrollToPositionWithOffset(it, dimen(R.dimen.default_toolbar_height))
                }
            }
            .launchIn(lifecycleScope)

        vm.spaceSyncStatus.onEach { status -> bindSyncStatus(status) }.launchIn(lifecycleScope)

        vm.isUndoEnabled.onEach {
            // TODO
        }.launchIn(lifecycleScope)
        vm.isRedoEnabled.onEach {
            // TODO
        }.launchIn(lifecycleScope)

        vm.navPanelState.onEach {
            if (hasBinding) {
                binding.bottomToolbar.setState(it)
            }
        }.launchIn(lifecycleScope)

        with(lifecycleScope) {
            launch {
                vm.actions.collectLatest {
                    if (hasBinding) {
                        binding.blockActionToolbar.bind(it)
                        delay(DEFAULT_DELAY_BLOCK_ACTION_TOOLBAR)
                        handler.post {
                            if (hasBinding) {
                                binding.blockActionToolbar.scrollToPosition(0, smooth = true)
                            }
                        }
                    }
                }
            }
            subscribe(vm.isUndoRedoToolbarIsVisible) { isVisible ->
                if (hasBinding) {
                    val behavior = BottomSheetBehavior.from(binding.undoRedoToolbar)
                    if (isVisible) {
                        behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        behavior.addBottomSheetCallback(onHideBottomSheetCallback)
                    } else {
                        behavior.removeBottomSheetCallback(onHideBottomSheetCallback)
                        behavior.state = BottomSheetBehavior.STATE_HIDDEN
                    }
                }
            }
        }
    }

    private fun bindSyncStatus(status: SpaceSyncAndP2PStatusState) {
        binding.topToolbar.status.bind(status)
        if (status is SpaceSyncAndP2PStatusState.Init) {
            binding.topToolbar.hideStatusContainer()
        } else {
            binding.topToolbar.showStatusContainer()
        }
        binding.topToolbar.status.setOnClickListener {
            vm.onSyncStatusBadgeClicked()
        }
    }

    override fun onDestroyView() {
        // Clear all text selections to prevent MultiSelectPopupWindow crashes
        clearActiveTextSelections()
        
        pickerDelegate.clearPickit()
        super.onDestroyView()
    }

    override fun onDestroy() {
        pickerDelegate.deleteTemporaryFile()
        pickerDelegate.clearOnCopyFile()
        super.onDestroy()
    }

    private fun clearActiveTextSelections() {
        try {
            // Lose focus on any currently focused child
            binding.recycler.clearFocus()
            binding.recycler.findFocus()?.clearFocus()
            // Walk the tree
            clearTextViewsRecursively(binding.recycler)
        } catch (e: Exception) {
            // Catch only expected exceptions
            Timber.w(e, "Error clearing text selections")
        }
    }

    private fun clearTextViewsRecursively(view: View) {
        when (view) {
            is EditText -> {
                // Remove focus and any selection
                view.clearFocus()
                if (view.hasSelection() && view.text.isNotEmpty()) {
                    view.setSelection(0, 0)
                }
                clearActionModeCallbacks(view)
            }
            is TextView -> {
                // Other TextViews: just clear any lingering callbacks
                view.clearFocus()
                clearActionModeCallbacks(view)
            }
            is ViewGroup -> {
                for (i in 0 until view.childCount) {
                    clearTextViewsRecursively(view.getChildAt(i))
                }
            }
            else -> Unit
        }
    }

    private fun clearActionModeCallbacks(view: TextView) {
        view.customSelectionActionModeCallback = null
        view.customInsertionActionModeCallback = null
    }

    override fun onSetRelationKeyClicked(blockId: Id, key: Id) {
        vm.onSetRelationKeyClicked(blockId = blockId, key = key)
    }

    private fun execute(event: EventWrapper<Command>) {
        Timber.d("Executing command: $event")
        event.getContentIfNotHandled()?.let { command ->
            when (command) {
                is Command.OpenDocumentImagePicker -> {
                    try {
                        pickProfileIcon.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                    } catch (e: Exception) {
                        Timber.w(e, "Error while opening photo picker")
                        toast("Error while opening photo picker")
                        pickerDelegate.openFilePicker(Mimetype.MIME_IMAGE_ALL, REQUEST_PROFILE_IMAGE_CODE)
                    }
                }
                is Command.OpenTextBlockIconPicker -> {
                    if (childFragmentManager.findFragmentByTag(TAG_TEXT_BLOCK_ICON_PICKER) == null) {
                        TextBlockIconPickerFragment.new(
                            context = ctx,
                            blockId = command.block,
                            space = space
                        ).showChildFragment(TAG_TEXT_BLOCK_ICON_PICKER)
                    }
                }
                is Command.OpenDocumentEmojiIconPicker -> {
                    hideSoftInput()
                    findNavController().safeNavigate(
                        R.id.pageScreen,
                        R.id.action_pageScreen_to_objectIconPickerScreen,
                        bundleOf(
                            IconPickerFragmentBase.ARG_CONTEXT_ID_KEY to command.ctx,
                            IconPickerFragmentBase.ARG_SPACE_ID_KEY to command.space,
                        )
                    )
                }
                is Command.OpenBookmarkSetter -> {
                    CreateBookmarkFragment.newInstance(
                        target = command.target,
                        url = command.url
                    ).showChildFragment()
                }
                is Command.OpenPhotoPicker -> {
                    launchMediaPicker(
                        pickMedia = pickMedia,
                        pickerDelegate = pickerDelegate,
                        mediaType = PickVisualMedia.ImageOnly,
                        fallbackMimeType = Mimetype.MIME_IMAGE_ALL
                    )
                }
                is Command.OpenVideoPicker -> {
                    launchMediaPicker(
                        pickMedia = pickMedia,
                        pickerDelegate = pickerDelegate,
                        mediaType = PickVisualMedia.VideoOnly,
                        fallbackMimeType = Mimetype.MIME_VIDEO_ALL
                    )
                }
                is Command.OpenFilePicker -> {
                    pickerDelegate.openFilePicker(Mimetype.MIME_FILE_ALL, null)
                }
                is Command.PopBackStack -> {
                    childFragmentManager.popBackStack()
                }
                is Command.CloseKeyboard -> {
                    hideSoftInput()
                }
                is Command.ScrollToActionMenu -> {
                    proceedWithScrollingToActionMenu(command)
                }
                is Command.Browse -> {
                    try {
                        ActivityCustomTabsHelper.openUrl(
                            activity = requireActivity(),
                            url = command.url
                        )
                    } catch (e: Throwable) {
                        toast("Couldn't parse url: ${command.url}")
                    }
                }
                is Command.OpenAppStore -> {
                    startMarketPageOrWeb()
                }
                is Command.OpenDocumentMenu -> {
                    hideKeyboard()
                    findNavController().safeNavigate(
                        currentDestinationId = navigationDestinationId,
                        id = R.id.objectMenuScreen,
                        args = ObjectMenuFragment.args(
                            ctx = command.ctx,
                            space = command.space,
                            isArchived = command.isArchived,
                            isFavorite = command.isFavorite,
                            isLocked = command.isLocked,
                            isReadOnly = command.isReadOnly,
                            fromName = getFrom(),
                            isTemplate = command.isTemplate
                        ),
                        errorMessage = "Error while opening document menu"
                    )
                }
                is Command.OpenCoverGallery -> {
                    findNavController().safeNavigate(
                        currentDestinationId = R.id.pageScreen,
                        id = R.id.action_pageScreen_to_objectCoverScreen,
                        args = SelectCoverObjectFragment.args(
                            ctx = command.ctx,
                            space = space
                        )
                    )
                }
                is Command.OpenFullScreenImage -> {
                    runCatching {
                        MediaActivity.start(
                            context = requireContext(),
                            mediaType = MediaActivity.TYPE_IMAGE,
                            obj = command.obj,
                            space = space
                        )
                    }.onFailure {
                        Timber.e(it, "Error while launching media image viewer")
                    }
                }
                is Command.AlertDialog -> {
                    if (childFragmentManager.findFragmentByTag(TAG_ALERT) == null) {
                        AlertUpdateAppFragment().apply {
                            onCancel = { navigation().exit(space) }
                        }.showChildFragment(TAG_ALERT)
                    } else {
                        // Do nothing
                    }
                }
                is Command.ClearSearchInput -> {
                    if (hasBinding) {
                        binding.searchToolbar.clear()
                    } else {
                        Timber.w("Missing binding for command: $command")
                    }
                }
                is Command.OpenObjectRelationScreen.RelationList -> {
                    hideKeyboard()
                    findNavController().safeNavigate(
                        R.id.pageScreen,
                        R.id.objectRelationListScreen,
                        bundleOf(
                            ObjectFieldsFragment.ARG_CTX to command.ctx,
                            ObjectFieldsFragment.ARG_SPACE to space,
                            ObjectFieldsFragment.ARG_TARGET to command.target,
                            ObjectFieldsFragment.ARG_LOCKED to command.isLocked,
                        )
                    )
                }
                is Command.OpenObjectRelationScreen.Value.Text -> {
                    hideKeyboard()
                    runCatching {
                        val fr = RelationTextValueFragment.new(
                            ctx = command.ctx,
                            relationKey = command.relationKey,
                            objectId = command.target,
                            isLocked = command.isReadOnlyValue,
                            space = command.space
                        )
                        fr.showChildFragment()
                    }.onFailure {
                        Timber.e(it, "Error while opening relation text value from editor")
                    }
                }
                is Command.OpenObjectRelationScreen.Value.Date -> {
                    hideKeyboard()
                    val fr = RelationDateValueFragment.new(
                        ctx = command.ctx,
                        space = command.space,
                        objectId = command.target,
                        relationKey = command.relationKey,
                        isLocked = command.isReadOnlyValue
                    )
                    fr.showChildFragment()
                }
                Command.AddSlashWidgetTriggerToFocusedBlock -> {
                    if (hasBinding) {
                        binding.recycler.addTextFromSelectedStart(text = "/")
                    } else {
                        Timber.w("Missing binding for command: $command")
                    }
                }
                is Command.OpenObjectSelectTypeScreen -> {
                    runCatching {
                        hideKeyboard()
                        val dialog = EditorObjectTypeUpdateFragment.newInstance(
                            space = space,
                            fromFeatured = command.fromFeatured
                        )
                        dialog.show(childFragmentManager, null)
                    }
                }
                is Command.OpenMoveToScreen -> {
                    jobs += lifecycleScope.launch {
                        hideSoftInput()
                        delay(DEFAULT_ANIM_DURATION)
                        val fr = MoveToFragment.new(
                            ctx = ctx,
                            space = space,
                            blocks = command.blocks,
                            restorePosition = command.restorePosition,
                            restoreBlock = command.restoreBlock
                        )
                        fr.showChildFragment()
                    }
                }
                is Command.OpenObjectSnackbar -> {
                    if (hasBinding) {
                        binding.root.showActionableSnackBar(
                            from = command.fromText,
                            to = command.toText,
                            icon = command.icon,
                            middleString = R.string.snack_move_to
                        ) {
                            if (command.isDataView) {
                                vm.proceedWithOpeningDataViewObject(
                                    target = command.id,
                                    space = SpaceId(command.space)
                                )
                            } else {
                                vm.proceedWithOpeningObject(command.id)
                            }
                        }
                    } else {
                        Timber.w("Missing binding for command")
                    }
                }
                is Command.OpenLinkToScreen -> {
                    jobs += lifecycleScope.launch {
                        hideSoftInput()
                        delay(DEFAULT_ANIM_DURATION)
                        if (childFragmentManager.findFragmentByTag(TAG_LINK_TO_OBJECT) == null) {
                            val fr = LinkToObjectFragment.new(
                                target = command.target,
                                position = command.position,
                                ignore = vm.context,
                                space = space
                            )
                            fr.showChildFragment(TAG_LINK_TO_OBJECT)
                        }
                    }
                }
                is Command.AddMentionWidgetTriggerToFocusedBlock -> {
                    if (hasBinding) {
                        binding.recycler.addTextFromSelectedStart(text = "@")
                    } else {
                        Timber.w("Didn't have binding for command: $command")
                    }                }
                is Command.OpenAddRelationScreen -> {
                    hideSoftInput()
                    runCatching {
                        val fr = RelationAddToObjectBlockFragment.newInstance(
                            ctx = command.ctx,
                            target = command.target,
                            space = space
                        )
                        fr.showChildFragment()
                    }.onFailure {
                        Timber.e(it, "Error while opening relation-add-to-object block screen")
                    }
                }
                is Command.OpenLinkToObjectOrWebScreen -> {
                    hideSoftInput()
                    val fr = LinkToObjectOrWebPagesFragment.newInstance(
                        ctx = command.ctx,
                        blockId = command.target,
                        rangeStart = command.range.first,
                        rangeEnd = command.range.last,
                        isWholeBlockMarkup = command.isWholeBlockMarkup,
                        space = space
                    )
                    fr.showChildFragment()
                }
                is Command.ShowKeyboard -> {
                    binding.recycler.findFocus()?.focusAndShowKeyboard()
                }
                is Command.OpenFileByDefaultApp -> {
                    vm.startSharingFile(command.id) { uri ->
                        openFileByDefaultApp(uri)
                    }
                }
                is Command.PlayVideo -> {
                    runCatching {
                        MediaActivity.start(
                            context = requireContext(),
                            mediaType = MediaActivity.TYPE_VIDEO,
                            obj = command.obj,
                            name = "",
                            space = space
                        )
                    }.onFailure {
                        Timber.e(it, "Error while launching video player")
                    }
                }
                is Command.PlayAudio -> {
                    runCatching {
                        MediaActivity.start(
                            context = requireContext(),
                            mediaType = MediaActivity.TYPE_AUDIO,
                            obj = command.obj,
                            name = command.name,
                            space = space
                        )
                    }.onFailure {
                        Timber.e(it, "Error while launching audio player")
                    }
                }
                is Command.SaveTextToSystemClipboard -> {
                    val clipData = ClipData.newPlainText("Uri", command.text)
                    clipboard().setPrimaryClip(clipData)
                }
                is Command.OpenObjectAppearanceSettingScreen -> {
                    val fr = ObjectAppearanceSettingFragment.new(
                        ctx = command.ctx,
                        block = command.block,
                        space = space
                    )
                    fr.showChildFragment()
                }
                is Command.ScrollToPosition -> {
                    if (hasBinding) {
                        val lm = binding.recycler.layoutManager as LinearLayoutManager
                        val margin =
                            resources.getDimensionPixelSize(R.dimen.default_editor_item_offset)
                        lm.scrollToPositionWithOffset(command.pos, margin)
                    } else {
                        Timber.w("Missing binding for command")
                    }
                }
                is Command.OpenSetBlockTextValueScreen -> {
                    val fr = SetBlockTextValueFragment.new(
                        ctx = command.ctx,
                        block = command.block,
                        table = command.table,
                        space = space
                    ).apply {
                        onDismissListener = {
                            vm.onSetBlockTextValueScreenDismiss()
                            hideKeyboard()
                        }
                    }
                    fr.showChildFragment()
                }
                is Command.OpenObjectTypeMenu -> openObjectTypeMenu(command)
                is Command.OpenObjectRelationScreen.Value.TagOrStatus -> {
                    findNavController().safeNavigate(
                        R.id.pageScreen,
                        R.id.nav_relations,
                        TagOrStatusValueFragment.args(
                            ctx = command.ctx,
                            space = command.space,
                            obj = command.target,
                            relation = command.relationKey,
                            isLocked = command.isReadOnlyValue,
                            context = RelationContext.OBJECT
                        )
                    )
                }
                is Command.OpenObjectRelationScreen.Value.ObjectValue -> {
                    findNavController().safeNavigate(
                        R.id.pageScreen,
                        R.id.objectValueScreen,
                        ObjectValueFragment.args(
                            ctx = command.ctx,
                            space = command.space,
                            obj = command.target,
                            relation = command.relationKey,
                            isLocked = command.isReadOnlyValue,
                            relationContext = RelationContext.OBJECT
                        )
                    )
                }
                is Command.SetObjectIcon -> {
                    findNavController().safeNavigate(
                        R.id.pageScreen,
                        R.id.objectIconPickerScreen,
                        bundleOf(
                            IconPickerFragmentBase.ARG_CONTEXT_ID_KEY to ctx,
                            IconPickerFragmentBase.ARG_SPACE_ID_KEY to command.space
                        )
                    )
                }
                is Command.OpenShareScreen -> {
                    findNavController().safeNavigate(
                        currentDestinationId = R.id.pageScreen,
                        id = R.id.shareSpaceScreen,
                        args = ShareSpaceFragment.args(command.space),
                        errorMessage = "Error while opening share screen"
                    )
                }
            }
        }
    }

    private fun openObjectTypeMenu(command: Command.OpenObjectTypeMenu) {
        val layoutManager = binding.recycler.layoutManager as LinearLayoutManager
        val featuredGroupWidget = findFeaturedGroupWidget(layoutManager)
        featuredGroupWidget?.getObjectTypeView()?.let { anchor ->
            createObjectTypeMenu(anchor, command).show()
        }
    }

    private fun findFeaturedGroupWidget(layoutManager: LinearLayoutManager): FeaturedRelationGroupWidget? {
        return (0 until layoutManager.childCount)
            .map { layoutManager.getChildAt(it) }
            .find { it is FeaturedRelationGroupWidget } as? FeaturedRelationGroupWidget
    }

    private fun createObjectTypeMenu(anchor: View, command: Command.OpenObjectTypeMenu): PopupMenu {
        val themeWrapper = ContextThemeWrapper(context, R.style.DefaultPopupMenuStyle)
        return ObjectTypePopupMenu(
            context = themeWrapper,
            anchor = anchor,
            items = command.items,
            onChangeTypeClicked = vm::onChangeObjectTypeClicked,
            onOpenSetClicked = vm::proceedWithOpeningDataViewObject,
            onCreateSetClicked = vm::onCreateNewSetForType,
            onOpenTypeClicked = vm::onOpenTypeClicked
        )
    }

    private fun getFrom() = (blockAdapter.views
        .firstOrNull { it is BlockView.TextSupport } as? BlockView.TextSupport)
        ?.text

    private fun openFileByDefaultApp(uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, requireContext().contentResolver.getType(uri))
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
        } catch (e: Exception) {
            if (e is ActivityNotFoundException) {
                toast("No application found to open the selected file")
            } else {
                toast("Could not open file: ${e.message}")
            }
            Timber.e(e, "Error while opening file")
        }
    }


    private fun proceedWithScrollingToActionMenu(command: Command.ScrollToActionMenu) {
        val lastSelected =
            (vm.state.value as? ViewState.Success)?.blocks?.indexOfLast { it.id == command.target }
                ?: return
        if (lastSelected != -1) {
            val lm = binding.recycler.layoutManager as LinearLayoutManager
            val targetView = lm.findViewByPosition(lastSelected)
            if (targetView != null) {
                val behavior = BottomSheetBehavior.from(binding.blockActionToolbar)
                val toolbarTop: Float = if (behavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                    binding.blockActionToolbar.y - binding.blockActionToolbar.measuredHeight
                } else {
                    binding.blockActionToolbar.y
                }
                val targetBottom = targetView.y + targetView.measuredHeight
                val delta = toolbarTop - targetBottom
                if (delta < 0) binding.recycler.smoothScrollBy(0, abs(delta.toInt()))
            }
        }
    }

    private fun render(state: ViewState) {
        when (state) {
            is ViewState.Success -> {
                blockAdapter.updateWithDiffUtil(state.blocks)
                binding.recycler.visible()
                binding.recycler.invalidateItemDecorations()
                val isLocked = vm.mode is Editor.Mode.Locked
                binding.topToolbar.setIsLocked(isLocked)
                resetDocumentTitle(state)
                binding.loadingContainer.root.gone()
            }
            ViewState.Loading -> {}
            ViewState.NotExist -> {
                binding.recycler.gone()
                binding.loadingContainer.root.gone()
                binding.objectNotExist.root.visible()
            }
        }
    }

    open fun resetDocumentTitle(state: ViewState.Success) {
        val title = state.blocks.firstOrNull { view ->
            view is BlockView.Title.Basic || view is BlockView.Title.Profile || view is BlockView.Title.Todo
        }
        if (title != null) {
            when (title) {
                is BlockView.Title.Basic -> {
                    resetTopToolbarTitle(
                        text = title.text,
                        emoji = title.emoji,
                        image = title.image,
                    )
                    if (title.hasCover) {
                        val mng = binding.recycler.layoutManager as LinearLayoutManager
                        val pos = mng.findFirstVisibleItemPosition()
                        if (pos == -1 || pos == 0) {
                            binding.topToolbar.setStyle(overCover = true)
                        }
                    } else {
                        binding.topToolbar.setStyle(overCover = false)
                    }
                }
                is BlockView.Title.Profile -> {
                    resetTopToolbarTitle(
                        text = title.text,
                        emoji = null,
                        image = title.image,
                    )
                    if (title.hasCover) {
                        val mng = binding.recycler.layoutManager as LinearLayoutManager
                        val pos = mng.findFirstVisibleItemPosition()
                        if (pos == -1 || pos == 0) {
                            binding.topToolbar.setStyle(overCover = true)
                        }
                    } else {
                        binding.topToolbar.setStyle(overCover = false)
                    }
                }
                is BlockView.Title.Todo -> {
                    resetTopToolbarTitle(
                        text = title.text,
                        emoji = null,
                        image = title.image,
                    )
                    if (title.hasCover) {
                        val mng = binding.recycler.layoutManager as LinearLayoutManager
                        val pos = mng.findFirstVisibleItemPosition()
                        if (pos == -1 || pos == 0) {
                            binding.topToolbar.setStyle(overCover = true)
                        }
                    } else {
                        binding.topToolbar.setStyle(overCover = false)
                    }
                }
                else -> {
                }
            }
        }
    }

    private fun resetTopToolbarTitle(text: String?, emoji: String?, image: String?) {
        binding.topToolbar.title.text = text
        val iconView = binding.topToolbar.icon
        when {
            text.isNullOrBlank() -> {
                iconView.setIcon(ObjectIcon.None)
                iconView.gone()
            }
            !emoji.isNullOrBlank() -> {
                iconView.setIcon(ObjectIcon.Basic.Emoji(emoji))
                iconView.visible()
            }
            !image.isNullOrBlank() -> {
                iconView.setIcon(ObjectIcon.Basic.Image(image))
                iconView.visible()
            }
            else -> {
                iconView.setIcon(ObjectIcon.None)
                iconView.gone()
            }
        }
    }

    open fun render(state: ControlPanelState) {

        keyboardDelayJobs.cancel()

        val insets = ViewCompat.getRootWindowInsets(binding.root)

        if (state.navigationToolbar.isVisible) {
            binding.placeholder.requestFocus()
            binding.placeholder.hideKeyboard()
            binding.bottomToolbarContainer.visible()
        } else {
            binding.bottomToolbarContainer.gone()
        }

        if (state.mainToolbar.isVisible) {
            binding.toolbar.visible()
            binding.toolbar.state = when (state.mainToolbar.targetBlockType) {
                Main.TargetBlockType.Any -> BlockToolbarWidget.State.Any
                Main.TargetBlockType.Title -> BlockToolbarWidget.State.Title
                Main.TargetBlockType.Cell -> BlockToolbarWidget.State.Cell
                Main.TargetBlockType.Description -> BlockToolbarWidget.State.Description
            }
        } else {
            binding.toolbar.invisible()
        }

        setMainMarkupToolbarState(state)

        state.multiSelect.apply {
            val behavior = BottomSheetBehavior.from(binding.blockActionToolbar)
            if (isVisible) {

                binding.multiSelectTopToolbar.apply {
                    setBlockSelectionText(count)
                    visible()
                }

               binding.multiSelectTopToolbar.selectAllBlocks.isVisible = isSelectAllVisible

                binding.recycler.apply {
                    if (itemAnimator == null) itemAnimator = DefaultItemAnimator()
                }

                proceedWithHidingSoftInput()

                binding.topToolbar.invisible()

                if (!state.multiSelect.isScrollAndMoveEnabled) {
                    if (!binding.recycler.containsItemDecoration(actionToolbarFooter)) {
                        binding.recycler.addItemDecoration(actionToolbarFooter)
                    }
                    if (behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                        keyboardDelayJobs += lifecycleScope.launch {
                            binding.blockActionToolbar.scrollToPosition(0)
                            delayKeyboardHide(insets)
                            behavior.apply {
                                setState(BottomSheetBehavior.STATE_EXPANDED)
                                addBottomSheetCallback(onHideBottomSheetCallback)
                            }
                            showSelectButton()
                        }
                    }
                } else {
                    behavior.removeBottomSheetCallback(onHideBottomSheetCallback)
                }
            } else {
                binding.recycler.apply { itemAnimator = null }
                behavior.apply {
                    setState(BottomSheetBehavior.STATE_HIDDEN)
                    removeBottomSheetCallback(onHideBottomSheetCallback)
                }
                if (!state.simpleTableWidget.isVisible) hideSelectButton()
                binding.recycler.removeItemDecoration(actionToolbarFooter)
            }
            if (isScrollAndMoveEnabled)
                enterScrollAndMove()
            else
                exitScrollAndMove()
        }

        state.styleTextToolbar.apply {
            val behavior = BottomSheetBehavior.from(binding.styleToolbarMain)
            if (isVisible) {
                binding.styleToolbarMain.setSelectedStyle(this.state)
                if (behavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                    keyboardDelayJobs += lifecycleScope.launch {
                        if (binding.recycler.lastDecorator() == defaultBottomOffsetDecorator) {
                            binding.recycler.addItemDecoration(styleToolbarFooter)
                        }
                        proceedWithHidingSoftInput()
                        delayKeyboardHide(insets)
                        behavior.apply {
                            setState(BottomSheetBehavior.STATE_EXPANDED)
                            addBottomSheetCallback(onHideBottomSheetCallback)
                        }
                    }
                }
            } else {
                if (!state.styleColorBackgroundToolbar.isVisible && !state.styleExtraToolbar.isVisible) {
                    binding.recycler.removeItemDecoration(styleToolbarFooter)
                }
                behavior.apply {
                    removeBottomSheetCallback(onHideBottomSheetCallback)
                    setState(BottomSheetBehavior.STATE_HIDDEN)
                }
            }
        }

        state.styleExtraToolbar.apply {
            if (isVisible) {
                binding.styleToolbarOther.setProperties(state.styleExtraToolbar.state)
                BottomSheetBehavior.from(binding.styleToolbarOther).apply {
                    setState(BottomSheetBehavior.STATE_EXPANDED)
                    addBottomSheetCallback(onHideBottomSheetCallback)
                }
            } else {
                BottomSheetBehavior.from(binding.styleToolbarOther).apply {
                    removeBottomSheetCallback(onHideBottomSheetCallback)
                    setState(BottomSheetBehavior.STATE_HIDDEN)
                }
            }
        }

        state.styleColorBackgroundToolbar.apply {
            val behavior = BottomSheetBehavior.from(binding.styleToolbarColors)
            if (isVisible) {
                binding.styleToolbarColors.update(state.styleColorBackgroundToolbar.state)
                if (behavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                    keyboardDelayJobs += lifecycleScope.launch {
                        proceedWithHidingSoftInput()
                        delayKeyboardHide(insets)
                        behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        behavior.addBottomSheetCallback(onHideBottomSheetCallback)
                    }
                }
            } else {
                behavior.apply {
                    removeBottomSheetCallback(onHideBottomSheetCallback)
                    setState(BottomSheetBehavior.STATE_HIDDEN)
                }
            }
        }

        state.styleBackgroundToolbar.apply {
            val behavior = BottomSheetBehavior.from(binding.styleToolbarBackground)
            if (isVisible) {
                state.styleBackgroundToolbar.state.let {
                    binding.styleToolbarBackground.update(it)
                }
                if (behavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                    keyboardDelayJobs += lifecycleScope.launch {
                        if (binding.recycler.lastDecorator() == defaultBottomOffsetDecorator) {
                            binding.recycler.addItemDecoration(styleToolbarFooter)
                        }
                        proceedWithHidingSoftInput()
                        delayKeyboardHide(insets)
                        behavior.apply {
                            setState(BottomSheetBehavior.STATE_EXPANDED)
                            addBottomSheetCallback(onHideBottomSheetCallback)
                        }
                    }
                }
            } else {
                BottomSheetBehavior.from(binding.styleToolbarBackground).apply {
                    setState(BottomSheetBehavior.STATE_HIDDEN)
                    addBottomSheetCallback(onHideBottomSheetCallback)
                }
            }
        }

        state.mentionToolbar.apply {
            if (isVisible) {
                if (!binding.mentionSuggesterToolbar.isVisible) {
                    showMentionToolbar(this)
                }
                if (updateList) {
                    binding.mentionSuggesterToolbar.addItems(mentions)
                }
                mentionFilter?.let {
                    binding.mentionSuggesterToolbar.updateFilter(it)
                }
            } else {
                binding.mentionSuggesterToolbar.invisible()
                binding.mentionSuggesterToolbar.clear()
                binding.recycler.removeItemDecoration(footerMentionDecorator)
            }
        }

        state.slashWidget.apply {
            TransitionManager.endTransitions(binding.sheet)
            if (isVisible) {
                if (!binding.slashWidget.isVisible) {
                    binding.slashWidget.scrollToTop()
                    showSlashWidget(this)
                }
                widgetState?.let {
                    binding.slashWidget.onStateChanged(it)
                }
            } else {
                if (binding.slashWidget.isVisible) {
                    binding.slashWidget.gone()
                }
                binding.recycler.removeItemDecoration(slashWidgetFooter)
            }
        }

        state.searchToolbar.apply {
            if (isVisible) {
                binding.searchToolbar.visible()
                binding.searchToolbar.focus()
            } else {
                binding.searchToolbar.gone()
            }
        }

        state.simpleTableWidget.apply {
            val behavior = BottomSheetBehavior.from(binding.simpleTableWidget)
            if (isVisible) {
                binding.multiSelectTopToolbar.apply {
                    setTableSelectionText(
                        count = state.simpleTableWidget.selectedCount,
                        tab = state.simpleTableWidget.tab
                    )
                    visible()
                }

                binding.simpleTableWidget.onStateChanged(
                    items = state.simpleTableWidget.items,
                    tab = state.simpleTableWidget.tab

                )
                if (behavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                    keyboardDelayJobs += lifecycleScope.launch {
                        if (binding.recycler.lastDecorator() == defaultBottomOffsetDecorator) {
                            binding.recycler.addItemDecoration(styleToolbarFooter)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            hidingSoftInput()
                            delayKeyboardHide(insets)
                        } else {
                            proceedWithHidingSoftInput()
                            delayKeyboardHide(insets)
                        }
                        behavior.apply {
                            setState(BottomSheetBehavior.STATE_EXPANDED)
                            addBottomSheetCallback(onHideBottomSheetCallback)
                        }
                        showSelectButton()
                    }
                }
            } else {
                if (behavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    behavior.removeBottomSheetCallback(onHideBottomSheetCallback)
                    behavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
                if (!state.multiSelect.isVisible) hideSelectButton()
            }
        }
    }

    private fun applySlideTransition(transTarget: View, transDuration: Long, transRoot: ViewGroup) {
        val transitionSet = TransitionSet().apply {
            addTransition(Slide())
            duration = transDuration
            interpolator = DecelerateInterpolator(DECELERATE_INTERPOLATOR_FACTOR)
            ordering = TransitionSet.ORDERING_TOGETHER
            addTarget(transTarget)
        }
        TransitionManager.endTransitions(transRoot)
        TransitionManager.beginDelayedTransition(
            transRoot,
            transitionSet
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun hidingSoftInput() {
        ViewCompat.getWindowInsetsController(requireView())?.hide(WindowInsetsCompat.Type.ime())
    }

    private fun proceedWithHidingSoftInput() {
        // TODO enable when switching to API 30
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            val controller = root.windowInsetsController
//            if (controller != null) {
//                controller.hide(WindowInsetsCompat.Type.ime())
//            } else {
//                hideSoftInput()
//            }
//        } else {
//            hideSoftInput()
//        }
        hideSoftInput()
    }

    private suspend fun delayKeyboardHide(insets: WindowInsetsCompat?) {
        if (insets != null) {
            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                delay(DELAY_HIDE_KEYBOARD)
            }
        } else {
            delay(DELAY_HIDE_KEYBOARD)
        }
    }

    private fun hideBlockActionPanel() {
        BottomSheetBehavior.from(binding.blockActionToolbar).apply {
            setState(BottomSheetBehavior.STATE_HIDDEN)
        }
    }

    private fun setMainMarkupToolbarState(state: ControlPanelState) {
        if (state.markupMainToolbar.isVisible) {
            binding.markupToolbar.setProps(
                props = state.markupMainToolbar.style,
                supportedTypes = state.markupMainToolbar.supportedTypes,
                isBackgroundColorSelected = state.markupMainToolbar.isBackgroundColorSelected,
                isTextColorSelected = state.markupMainToolbar.isTextColorSelected
            )
            binding.markupToolbar.visible()

            if (state.markupColorToolbar.isVisible) {
                if (state.markupMainToolbar.isTextColorSelected) {
                    binding.markupColorToolbar.setTextColor(
                        state.markupMainToolbar.style?.markupTextColor
                            ?: state.markupMainToolbar.style?.blockTextColor
                            ?: ThemeColor.DEFAULT.code
                    )
                }
                if (state.markupMainToolbar.isBackgroundColorSelected) {
                    binding.markupColorToolbar.setBackgroundColor(
                        state.markupMainToolbar.style?.markupHighlightColor
                            ?: state.markupMainToolbar.style?.blockBackroundColor
                            ?: ThemeColor.DEFAULT.code
                    )
                }
                if (binding.markupColorToolbar.translationY > 0) {
                    binding.recycler.addItemDecoration(markupColorToolbarFooter)
                }
                showMarkupColorToolbarWithAnimation()
            } else {
                if (binding.markupColorToolbar.translationY == 0f) {
                    binding.recycler.removeItemDecoration(markupColorToolbarFooter)
                    hideMarkupColorToolbarWithAnimation()
                }
            }

        } else {
            binding.markupToolbar.invisible()
            if (binding.markupColorToolbar.translationY == 0f) {
                binding.markupColorToolbar.translationY = dimen(R.dimen.dp_104).toFloat()
            }
        }
    }

    private fun showMarkupColorToolbarWithAnimation() {

        val focus = binding.recycler.findFocus()

        if (focus != null && focus is TextInputWidget) {
            val cursorCoord = focus.cursorYBottomCoordinate()

            val parentBottom = calculateRectInWindow(binding.recycler).bottom
            val toolbarHeight = binding.markupToolbar.height + binding.markupColorToolbar.height

            val minPosY = parentBottom - toolbarHeight

            if (minPosY <= cursorCoord) {
                val scrollY = (parentBottom - minPosY) - (parentBottom - cursorCoord)
                Timber.d("New scroll y: $scrollY")
                handler.post {
                    binding.recycler.smoothScrollBy(0, scrollY)
                }
            }

            binding.markupColorToolbar
                .animate()
                .translationY(0f)
                .setDuration(DEFAULT_ANIM_DURATION)
                .start()
        }
    }

    private fun hideMarkupColorToolbarWithAnimation() {
        binding.markupColorToolbar
            .animate()
            .translationY(dimen(R.dimen.dp_104).toFloat())
            .setDuration(DEFAULT_ANIM_DURATION)
            .start()
    }

    private fun showMentionToolbar(state: ControlPanelState.Toolbar.MentionToolbar) {
        state.cursorCoordinate?.let { cursorCoordinate ->
            val parentBottom = calculateRectInWindow(binding.recycler).bottom
            val toolbarHeight = binding.mentionSuggesterToolbar.getMentionSuggesterWidgetMinHeight()
            val minPosY = parentBottom - toolbarHeight

            if (minPosY <= cursorCoordinate) {
                val scrollY = (parentBottom - minPosY) - (parentBottom - cursorCoordinate)
                binding.recycler.addItemDecoration(footerMentionDecorator)
                handler.post {
                    binding.recycler.smoothScrollBy(0, scrollY)
                }
            }
            binding.mentionSuggesterToolbar.updateLayoutParams<ConstraintLayout.LayoutParams> {
                height = toolbarHeight
            }
            val set = ConstraintSet().apply {
                clone(binding.sheet)
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
            TransitionManager.beginDelayedTransition(binding.sheet, transitionSet)
            set.applyTo(binding.sheet)
        }
    }

    private fun showSlashWidget(state: ControlPanelState.Toolbar.SlashWidget) {
        state.cursorCoordinate?.let { cursorCoordinate ->
            val parentBottom = calculateRectInWindow(binding.recycler).bottom
            val toolbarHeight = binding.slashWidget.getWidgetMinHeight()
            val minPosY = parentBottom - toolbarHeight

            if (minPosY <= cursorCoordinate) {
                val scrollY = (parentBottom - minPosY) - (parentBottom - cursorCoordinate)
                binding.recycler.addItemDecoration(slashWidgetFooter)
                handler.post {
                    binding.recycler.smoothScrollBy(
                        0,
                        scrollY,
                        DecelerateInterpolator(DECELERATE_INTERPOLATOR_FACTOR),
                        SLASH_SHOW_ANIM_DURATION.toInt()
                    )
                }
            }
            binding.slashWidget.updateLayoutParams<ConstraintLayout.LayoutParams> {
                height = toolbarHeight
            }
            val set = ConstraintSet().apply {
                clone(binding.sheet)
                setVisibility(R.id.slashWidget, View.VISIBLE)
                connect(
                    R.id.slashWidget,
                    ConstraintSet.BOTTOM,
                    R.id.sheet,
                    ConstraintSet.BOTTOM
                )
            }
            applySlideTransition(binding.slashWidget, SLASH_SHOW_ANIM_DURATION, binding.sheet)
            set.applyTo(binding.sheet)
        }
    }

    private fun enterScrollAndMove() {
        if (binding.recycler.lastDecorator() !is ScrollAndMoveTargetHighlighter) {

            binding.recycler.addItemDecoration(scrollAndMoveTargetHighlighter)

            showTargeterWithAnimation()

            binding.recycler.addOnScrollListener(scrollAndMoveStateListener)
            binding.multiSelectTopToolbar.invisible()

            showTopScrollAndMoveToolbar()
            binding.scrollAndMoveBottomAction.show()

            hideBlockActionPanel()

            lifecycleScope.launch {
                delay(300)
                searchScrollAndMoveTarget()
                binding.recycler.invalidate()
            }
        } else {
            Timber.d("Skipping enter scroll-and-move")
        }
    }

    private fun showTargeterWithAnimation() {
        binding.targeter.translationY = -binding.targeter.y
        ObjectAnimator.ofFloat(
            binding.targeter,
            TARGETER_ANIMATION_PROPERTY,
            0f
        ).apply {
            duration = 300
            doOnStart { binding.targeter.visible() }
            interpolator = OvershootInterpolator()
            start()
        }
    }

    private fun exitScrollAndMove() {
        binding.recycler.apply {
            removeItemDecoration(scrollAndMoveTargetHighlighter)
            removeOnScrollListener(scrollAndMoveStateListener)
        }
        hideTopScrollAndMoveToolbar()
        binding.scrollAndMoveBottomAction.hide()
        binding.targeter.invisible()
        scrollAndMoveTargetDescriptor.clear()
    }

    private fun hideSelectButton() {
        if (binding.multiSelectTopToolbar.translationY >= 0) {
            ObjectAnimator.ofFloat(
                binding.multiSelectTopToolbar,
                SELECT_BUTTON_ANIMATION_PROPERTY,
                -requireContext().dimen(R.dimen.dp_48)
            ).apply {
                duration = SELECT_BUTTON_HIDE_ANIMATION_DURATION
                interpolator = DecelerateInterpolator()
                doOnEnd { if (hasBinding) binding.topToolbar.visible() }
                start()
            }
        }
    }

    private fun showSelectButton() {
        if (binding.multiSelectTopToolbar.translationY < 0) {
            ObjectAnimator.ofFloat(
                binding.multiSelectTopToolbar,
                SELECT_BUTTON_ANIMATION_PROPERTY,
                0f
            ).apply {
                duration = SELECT_BUTTON_SHOW_ANIMATION_DURATION
                interpolator = DecelerateInterpolator()
                start()
            }
        }
    }

    private fun hideTopScrollAndMoveToolbar() {
        ObjectAnimator.ofFloat(
            binding.scrollAndMoveHint,
            SELECT_BUTTON_ANIMATION_PROPERTY,
            -requireContext().dimen(R.dimen.dp_48)
        ).apply {
            duration = SELECT_BUTTON_HIDE_ANIMATION_DURATION
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    private fun showTopScrollAndMoveToolbar() {
        ObjectAnimator.ofFloat(
            binding.scrollAndMoveHint,
            SELECT_BUTTON_ANIMATION_PROPERTY,
            0f
        ).apply {
            duration = SELECT_BUTTON_SHOW_ANIMATION_DURATION
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    override fun onClipboardAction(action: ClipboardInterceptor.Action) {
        when (action) {
            is ClipboardInterceptor.Action.Copy -> vm.onCopy(action.selection)
            is ClipboardInterceptor.Action.Paste -> vm.onPaste(action.selection)
        }
    }

    override fun onBookmarkPasted(url: Url) {
        vm.onBookmarkPasted(url)
    }

    override fun onLinkPasted(url: Url) {
        vm.proceedToAddUriToTextAsLink(url)
    }

    private fun hideKeyboard() {
        Timber.d("Hiding keyboard")
        hideSoftInput()
    }

    private fun extractDocumentId(): String {
        return requireArguments()
            .getString(CTX_KEY)
            ?: throw IllegalStateException("Document id missing")
    }

    open fun saveAsLastOpened(): Boolean {
        return true
    }

    private fun processScrollAndMoveStateChanges() {
        lifecycleScope.launch {
            scrollAndMoveStateChannel
                .consumeAsFlow()
                .mapLatest { searchScrollAndMoveTarget() }
                .debounce(SAM_DEBOUNCE)
                .collect { binding.recycler.invalidate() }
        }
    }

    private fun getEditorSettings() {
    }

    override fun onSearchOnPageClicked() {
        vm.onEnterSearchModeClicked()
    }

    override fun onSetTextBlockValue() {
        vm.onSetTextBlockValue()
    }

    override fun onMentionClicked(target: Id) {
        vm.onMentionClicked(target = target)
    }

    override fun onUndoRedoClicked() {
        vm.onUndoRedoActionClicked()
    }

    override fun onDocRelationsClicked() {
        vm.onDocRelationsClicked()
    }

    override fun onAddCoverClicked() {
        vm.onAddCoverClicked()
    }

    override fun onSetIconClicked() {
        vm.onSetObjectIconClicked()
    }

    override fun onTextValueChanged(ctx: Id, text: String, objectId: Id, relationKey: Key) {
        vm.onRelationTextValueChanged(
            ctx = ctx,
            value = text,
            relationKey = relationKey,
            isValueEmpty = text.isEmpty()
        )
    }

    override fun onNumberValueChanged(ctx: Id, number: Double?, objectId: Id, relationKey: Key) {
        vm.onRelationTextValueChanged(
            ctx = ctx,
            value = number,
            relationKey = relationKey,
            isValueEmpty = number == null
        )
    }

    override fun onDateValueChanged(
        ctx: Id,
        timeInSeconds: Number?,
        objectId: Id,
        relationKey: Key
    ) {
        vm.onRelationTextValueChanged(
            ctx = ctx,
            relationKey = relationKey,
            value = timeInSeconds,
            isValueEmpty = timeInSeconds == null
        )
    }

    override fun onOpenDateObject(timeInMillis: TimeInMillis) {
        vm.onOpenDateObjectByTimeInMillis(timeInMillis)
    }

    override fun onMoveTo(
        target: Id,
        space: Id,
        blocks: List<Id>,
        text: String,
        icon: ObjectIcon,
        isDataView: Boolean
    ) {
        vm.proceedWithMoveToAction(
            target = target,
            text = text,
            icon = icon,
            blocks = blocks,
            isDataView = isDataView,
            space = space
        )
    }

    override fun onMoveToClose(blocks: List<Id>, restorePosition: Int?, restoreBlock: Id?) {
        vm.proceedWithMoveToExit(
            blocks = blocks,
            restorePosition = restorePosition,
            restoreBlock = restoreBlock
        )
    }

    override fun onLinkTo(
        link: Id,
        target: Id,
        isBookmark: Boolean
    ) {
        vm.proceedWithLinkToAction(
            link = link,
            target = target,
            isBookmark = isBookmark
        )
    }

    override fun onLinkToClose(block: Id, position: Int?) {
        vm.proceedWithLinkToExit(
            block = block,
            position = position
        )
    }

    override fun onSetObjectLink(id: Id) {
        vm.proceedToAddObjectToTextAsLink(id)
    }

    override fun onSetWebLink(link: String) {
        vm.proceedToAddUriToTextAsLink(link)
    }

    override fun onCopyLink(link: String) {
        vm.onCopyLinkClicked(link)
    }

    override fun onCreateObject(name: String) {
        vm.proceedToCreateObjectAndAddToTextAsLink(name)
    }

    override fun onUpdateObjectType(objType: ObjectWrapper.Type, fromFeatured: Boolean) {
        vm.onObjectTypeChanged(objType = objType, fromFeatured = fromFeatured)
    }

    override fun onSelectObjectType(objType: ObjectWrapper.Type) {
        vm.onAddNewDocumentClicked(objType = objType)
    }

    override fun onAddRelationToTarget(target: Id, relationKey: Key) {
        vm.proceedWithAddingRelationToTarget(
            target = target,
            relationKey = relationKey
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            pickerDelegate.resolveActivityResult(requestCode, resultCode, data)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    open fun observeSelectingTemplate() {
        val navController = findNavController()
        val navBackStackEntry = try {
            navController.getBackStackEntry(R.id.pageScreen)
        } catch (e: IllegalArgumentException) {
            Timber.w(
                e,
                "pageScreen not found in NavController back stack, skipping template observation"
            )
            return
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME
                && navBackStackEntry.savedStateHandle.contains(ARG_TEMPLATE_ID)
            ) {
                val resultTemplateId = navBackStackEntry.savedStateHandle.get<String>(ARG_TEMPLATE_ID)
                if (resultTemplateId != null) {
                    navBackStackEntry.savedStateHandle.remove<String>(ARG_TEMPLATE_ID)
                    vm.onProceedWithApplyingTemplateByObjectId(template = resultTemplateId)
                }
            }
        }
        navBackStackEntry.lifecycle.addObserver(observer)

        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                navBackStackEntry.lifecycle.removeObserver(observer)
            }
        })
    }

    //------------ End of Anytype Custom Context Menu ------------

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentEditorBinding = FragmentEditorBinding.inflate(
        inflater, container, false
    )

    override fun injectDependencies() {
        componentManager().editorComponent
            .get(
                key = ctx,
                param = DefaultComponentParam(
                    ctx = ctx,
                    space = SpaceId(space)
                )
            )
            .inject(this)
    }

    override fun releaseDependencies() {
        componentManager().editorComponent.release(ctx)
    }

    //region Media Picker
    private val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            val path = uri.parseImagePath(requireContext())
            if (path != null) {
                vm.onProceedWithFilePath(path)
            } else {
                toast("Error while parsing path for media file")
            }
        } else {
            Timber.i("No media selected")
        }
    }

    private val pickProfileIcon = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            val path = uri.parseImagePath(requireContext())
            if (path != null) {
                vm.onPickedDocImageFromDevice(
                    ctx = ctx,
                    path = path
                )
            } else {
                toast("Error while parsing path for media file")
            }
        } else {
            Timber.i("No media selected")
        }
    }
    //endregion

    companion object {

        fun args(
            ctx: Id,
            space: Id,
            effect: OpenObjectNavigation.SideEffect = OpenObjectNavigation.SideEffect.None
        ) : Bundle {
            return when(effect) {
                is OpenObjectNavigation.SideEffect.AttachToChat -> {
                    bundleOf(
                        CTX_KEY to ctx,
                        SPACE_ID_KEY to space,
                        ATTACH_TO_CHAT_ID_KEY to effect.chat,
                        ATTACH_TO_CHAT_SPACE_KEY to effect.space
                    )
                }
                OpenObjectNavigation.SideEffect.None -> {
                    bundleOf(CTX_KEY to ctx, SPACE_ID_KEY to space)
                }
            }
        }

        const val CTX_KEY = "args.editor.ctx-id"
        const val SPACE_ID_KEY = "args.editor.space-id"

        private const val ATTACH_TO_CHAT_ID_KEY = "args.editor.attach-to-chat.id"
        private const val ATTACH_TO_CHAT_SPACE_KEY = "args.editor.attach-to-chat.space"

        const val DEFAULT_ANIM_DURATION = 150L
        const val DEFAULT_DELAY_BLOCK_ACTION_TOOLBAR = 100L
        const val DEFAULT_TOOLBAR_ANIM_DURATION = 150L
        const val SLASH_SHOW_ANIM_DURATION = 400L
        const val SLASH_HIDE_ANIM_DURATION = 1200L
        const val DECELERATE_INTERPOLATOR_FACTOR = 2.5f

        const val SHOW_MENTION_TRANSITION_DURATION = 150L
        const val SELECT_BUTTON_SHOW_ANIMATION_DURATION = 200L
        const val SELECT_BUTTON_HIDE_ANIMATION_DURATION = 200L
        const val SELECT_BUTTON_ANIMATION_PROPERTY = "translationY"
        const val TARGETER_ANIMATION_PROPERTY = "translationY"

        const val SAM_DEBOUNCE = 100L
        const val DELAY_HIDE_KEYBOARD = 300L

        const val TAG_ALERT = "tag.alert"
        const val TAG_LINK = "tag.link"
        const val TAG_TEXT_BLOCK_ICON_PICKER = "tag.text.block.icon.picker"
        const val TAG_LINK_TO_OBJECT = "tag.link.to.object"

        const val EMPTY_TEXT = ""
        const val DRAG_AND_DROP_LABEL = "Anytype's editor drag-and-drop."
        private const val CURRENT_MEDIA_UPLOAD_KEY = "currentMediaUploadDescription"
    }
}

interface OnFragmentInteractionListener {
    fun onSetBlockWebLink(blockId: String, link: String)
    fun onSetBlockObjectLink(blockId: Id, objectId: Id)
    fun onRemoveMarkupLinkClicked(blockId: String, range: IntRange)
    fun onAddBookmarkUrlClicked(target: String, url: String)
    fun onSetRelationKeyClicked(blockId: Id, key: Id)
    fun onSetObjectLink(objectId: Id)
    fun onSetWebLink(link: String)
    fun onCreateObject(name: String)
    fun onSetTextBlockValue()
    fun onMentionClicked(target: Id)
    fun onCopyLink(link: String)
    fun onAddRelationToTarget(target: Id, relationKey: Key)
}