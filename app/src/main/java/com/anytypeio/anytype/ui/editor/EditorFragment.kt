package com.anytypeio.anytype.ui.editor

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import androidx.viewbinding.ViewBinding
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.SyncStatus
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_ui.extensions.addTextFromSelectedStart
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.extensions.cursorYBottomCoordinate
import com.anytypeio.anytype.core_ui.features.editor.BlockAdapter
import com.anytypeio.anytype.core_ui.features.editor.DragAndDropAdapterDelegate
import com.anytypeio.anytype.core_ui.features.editor.scrollandmove.DefaultScrollAndMoveTargetDescriptor
import com.anytypeio.anytype.core_ui.features.editor.scrollandmove.ScrollAndMoveStateListener
import com.anytypeio.anytype.core_ui.features.editor.scrollandmove.ScrollAndMoveTargetHighlighter
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_ui.tools.ClipboardInterceptor
import com.anytypeio.anytype.core_ui.tools.EditorHeaderOverlayDetector
import com.anytypeio.anytype.core_ui.tools.MarkupColorToolbarFooter
import com.anytypeio.anytype.core_ui.tools.MentionFooterItemDecorator
import com.anytypeio.anytype.core_ui.tools.NoteHeaderItemDecorator
import com.anytypeio.anytype.core_ui.tools.OutsideClickDetector
import com.anytypeio.anytype.core_ui.tools.SlashWidgetFooterItemDecorator
import com.anytypeio.anytype.core_ui.tools.StyleToolbarItemDecorator
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_ui.widgets.toolbar.BlockToolbarWidget
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.const.FileConstants.REQUEST_PROFILE_IMAGE_CODE
import com.anytypeio.anytype.core_utils.ext.PopupExtensions.calculateRectInWindow
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.core_utils.ext.clipboard
import com.anytypeio.anytype.core_utils.ext.containsItemDecoration
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.drawable
import com.anytypeio.anytype.core_utils.ext.focusAndShowKeyboard
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.hide
import com.anytypeio.anytype.core_utils.ext.hideSoftInput
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.safeNavigate
import com.anytypeio.anytype.core_utils.ext.screen
import com.anytypeio.anytype.core_utils.ext.show
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.syncTranslationWithImeVisibility
import com.anytypeio.anytype.core_utils.ext.throttleFirst
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.BaseFragment
import com.anytypeio.anytype.core_utils.ui.showActionableSnackBar
import com.anytypeio.anytype.databinding.FragmentEditorBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.ext.extractMarks
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
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.ui.alert.AlertUpdateAppFragment
import com.anytypeio.anytype.ui.base.NavigationFragment
import com.anytypeio.anytype.ui.editor.cover.SelectCoverObjectFragment
import com.anytypeio.anytype.ui.editor.gallery.FullScreenPictureFragment
import com.anytypeio.anytype.ui.editor.layout.ObjectLayoutFragment
import com.anytypeio.anytype.ui.editor.modals.CreateBookmarkFragment
import com.anytypeio.anytype.ui.editor.modals.IconPickerFragmentBase
import com.anytypeio.anytype.ui.editor.modals.SelectProgrammingLanguageFragment
import com.anytypeio.anytype.ui.editor.modals.SelectProgrammingLanguageReceiver
import com.anytypeio.anytype.ui.editor.modals.SetBlockTextValueFragment
import com.anytypeio.anytype.ui.editor.modals.TextBlockIconPickerFragment
import com.anytypeio.anytype.ui.editor.sheets.ObjectMenuBaseFragment
import com.anytypeio.anytype.ui.editor.sheets.ObjectMenuBaseFragment.DocumentMenuActionReceiver
import com.anytypeio.anytype.ui.editor.sheets.ObjectMenuFragment
import com.anytypeio.anytype.ui.linking.LinkToObjectFragment
import com.anytypeio.anytype.ui.linking.LinkToObjectOrWebPagesFragment
import com.anytypeio.anytype.ui.linking.OnLinkToAction
import com.anytypeio.anytype.ui.moving.MoveToFragment
import com.anytypeio.anytype.ui.moving.OnMoveToAction
import com.anytypeio.anytype.ui.objects.appearance.ObjectAppearanceSettingFragment
import com.anytypeio.anytype.ui.objects.types.pickers.DraftObjectSelectTypeFragment
import com.anytypeio.anytype.ui.objects.types.pickers.ObjectSelectTypeFragment
import com.anytypeio.anytype.ui.objects.types.pickers.OnObjectSelectTypeAction
import com.anytypeio.anytype.ui.relations.RelationAddBaseFragment.Companion.CTX_KEY
import com.anytypeio.anytype.ui.relations.RelationAddResult
import com.anytypeio.anytype.ui.relations.RelationAddToObjectBlockFragment
import com.anytypeio.anytype.ui.relations.RelationAddToObjectBlockFragment.Companion.RELATION_ADD_RESULT_KEY
import com.anytypeio.anytype.ui.relations.RelationCreateFromScratchForObjectBlockFragment.Companion.RELATION_NEW_RESULT_KEY
import com.anytypeio.anytype.ui.relations.RelationDateValueFragment
import com.anytypeio.anytype.ui.relations.RelationListFragment
import com.anytypeio.anytype.ui.relations.RelationNewResult
import com.anytypeio.anytype.ui.relations.RelationTextValueFragment
import com.anytypeio.anytype.ui.relations.RelationValueFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
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
import javax.inject.Inject
import kotlin.math.abs

open class EditorFragment : NavigationFragment<FragmentEditorBinding>(R.layout.fragment_editor),
    OnFragmentInteractionListener,
    SelectProgrammingLanguageReceiver,
    RelationTextValueFragment.TextValueEditReceiver,
    RelationDateValueFragment.DateValueEditReceiver,
    DocumentMenuActionReceiver,
    ClipboardInterceptor,
    OnMoveToAction,
    OnLinkToAction,
    OnObjectSelectTypeAction {

    private val keyboardDelayJobs = mutableListOf<Job>()

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
                    binding.typeHasTemplateToolbar.id -> {
                        vm.onTypeHasTemplateToolbarHidden()
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

    private val footerMentionDecorator by lazy { MentionFooterItemDecorator(screen) }
    private val noteHeaderDecorator by lazy {
        NoteHeaderItemDecorator(offset = dimen(R.dimen.default_note_title_offset))
    }
    private val markupColorToolbarFooter by lazy { MarkupColorToolbarFooter(screen) }
    private val slashWidgetFooter by lazy { SlashWidgetFooterItemDecorator(screen) }
    private val styleToolbarFooter by lazy { StyleToolbarItemDecorator(screen) }
    private val actionToolbarFooter by lazy { StyleToolbarItemDecorator(screen) }

    private val vm by viewModels<EditorViewModel> { factory }

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
            onPageIconClicked = vm::onPageIconClicked,
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
            dragAndDropSelector = DragAndDropAdapterDelegate()
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

    private val titleVisibilityDetector by lazy {
        EditorHeaderOverlayDetector(
            threshold = dimen(R.dimen.default_toolbar_height),
            thresholdPadding = dimen(R.dimen.dp_8)
        ) { isHeaderOverlaid ->
            if (isHeaderOverlaid) {
                binding.topToolbar.setBackgroundColor(0)
                binding.topToolbar.statusText.animate().alpha(1f)
                    .setDuration(DEFAULT_TOOLBAR_ANIM_DURATION)
                    .start()
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
                binding.topToolbar.statusText.animate().alpha(0f)
                    .setDuration(DEFAULT_TOOLBAR_ANIM_DURATION)
                    .start()
                binding.topToolbar.container.animate().alpha(1f)
                    .setDuration(DEFAULT_TOOLBAR_ANIM_DURATION)
                    .start()
                binding.topToolbar.setStyle(overCover = false)
            }
        }
    }

    private val pickerDelegate = PickerDelegate.Impl(this as BaseFragment<ViewBinding>)
    private val dndDelegate = DragAndDropDelegate()

    @Inject
    lateinit var factory: EditorViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pickerDelegate.initPicker(vm, ctx)
        setupOnBackPressedDispatcher()
        getEditorSettings()
    }

    override fun onStart() {
        with(lifecycleScope) {
            jobs += subscribe(vm.toasts) { toast(it) }
            jobs += subscribe(vm.snacks) { snack ->
                when (snack) {
                    is Snack.ObjectSetNotFound -> {
                        Snackbar
                            .make(
                                binding.root,
                                resources.getString(R.string.snack_object_set_not_found),
                                Snackbar.LENGTH_LONG
                            )
                            .setActionTextColor(requireContext().color(R.color.orange))
                            .setAction(R.string.create_new_set) { vm.onCreateNewSetForType(snack.type) }
                            .show()
                    }
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
                val behavior = BottomSheetBehavior.from(binding.typeHasTemplateToolbar)
                when (state) {
                    is SelectTemplateViewState.Active -> {
                        binding.typeHasTemplateToolbar.count = state.count
                        behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        behavior.addBottomSheetCallback(onHideBottomSheetCallback)
                    }
                    SelectTemplateViewState.Idle -> {
                        behavior.removeBottomSheetCallback(onHideBottomSheetCallback)
                        behavior.state = BottomSheetBehavior.STATE_HIDDEN
                    }
                }
            }
        }
        vm.onStart(id = extractDocumentId())
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

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            itemAnimator = null
            adapter = blockAdapter
            addOnScrollListener(titleVisibilityDetector)
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
            mentionClicks()
                .onEach { vm.onStartMentionWidgetClicked() }
                .launchIn(lifecycleScope)
        }

        binding.bottomMenu
            .doneClicks()
            .onEach { vm.onExitMultiSelectModeClicked() }
            .launchIn(lifecycleScope)

        binding.bottomMenu
            .deleteClicks()
            .onEach { vm.onMultiSelectModeDeleteClicked() }
            .launchIn(lifecycleScope)

        binding.bottomMenu
            .copyClicks()
            .onEach { vm.onMultiSelectCopyClicked() }
            .launchIn(lifecycleScope)

        binding.bottomMenu
            .enterScrollAndMove()
            .onEach { vm.onEnterScrollAndMoveClicked() }
            .launchIn(lifecycleScope)

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

        binding.bottomMenu
            .turnIntoClicks()
            .onEach { vm.onMultiSelectTurnIntoButtonClicked() }
            .launchIn(lifecycleScope)

        binding.bottomMenu
            .styleClicks()
            .onEach { vm.onMultiSelectStyleButtonClicked() }
            .launchIn(lifecycleScope)

        binding.multiSelectTopToolbar
            .doneButton
            .clicks()
            .throttleFirst()
            .onEach { vm.onExitMultiSelectModeClicked() }
            .launchIn(lifecycleScope)

        binding.bottomToolbar
            .homeClicks()
            .onEach { vm.onHomeButtonClicked() }
            .launchIn(lifecycleScope)

        binding.bottomToolbar
            .backClicks()
            .onEach { vm.onBackButtonPressed() }
            .launchIn(lifecycleScope)

        binding.bottomToolbar
            .searchClicks()
            .onEach { vm.onPageSearchClicked() }
            .launchIn(lifecycleScope)

        binding.bottomToolbar
            .addDocClicks()
            .onEach { vm.onAddNewDocumentClicked() }
            .launchIn(lifecycleScope)

        binding.topToolbar.menu
            .clicks()
            .throttleFirst()
            .onEach { vm.onDocumentMenuClicked() }
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

        binding.objectTypesToolbar.setupClicks(
            onItemClick = vm::onObjectTypesWidgetItemClicked,
            onSearchClick = vm::onObjectTypesWidgetSearchClicked,
            onDoneClick = vm::onObjectTypesWidgetDoneClicked
        )

        lifecycleScope.launch {
            binding.slashWidget.clickEvents.collect { item ->
                vm.onSlashItemClicked(item)
            }
        }

        binding.typeHasTemplateToolbar.binding.btnShow.clicks()
            .throttleFirst()
            .onEach { vm.onShowTemplateClicked() }
            .launchIn(lifecycleScope)

        lifecycleScope.launch {
            binding.searchToolbar.events().collect { vm.onSearchToolbarEvent(it) }
        }

        binding.objectNotExist.root.findViewById<TextView>(R.id.btnToDashboard).clicks()
            .throttleFirst()
            .onEach { vm.onHomeButtonClicked() }
            .launchIn(lifecycleScope)

        BottomSheetBehavior.from(binding.styleToolbarMain).state = BottomSheetBehavior.STATE_HIDDEN
        BottomSheetBehavior.from(binding.styleToolbarOther).state = BottomSheetBehavior.STATE_HIDDEN
        BottomSheetBehavior.from(binding.styleToolbarColors).state =
            BottomSheetBehavior.STATE_HIDDEN
        BottomSheetBehavior.from(binding.blockActionToolbar).state =
            BottomSheetBehavior.STATE_HIDDEN
        BottomSheetBehavior.from(binding.undoRedoToolbar).state = BottomSheetBehavior.STATE_HIDDEN
        BottomSheetBehavior.from(binding.styleToolbarBackground).state =
            BottomSheetBehavior.STATE_HIDDEN
        BottomSheetBehavior.from(binding.typeHasTemplateToolbar).state =
            BottomSheetBehavior.STATE_HIDDEN
        BottomSheetBehavior.from(binding.simpleTableWidget).state =
            BottomSheetBehavior.STATE_HIDDEN

        observeNavBackStack()
    }

    private fun setupWindowInsetAnimation() {
        if (BuildConfig.USE_NEW_WINDOW_INSET_API && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            binding.objectTypesToolbar.syncTranslationWithImeVisibility(
                dispatchMode = DISPATCH_MODE_STOP
            )
            binding.toolbar.syncTranslationWithImeVisibility(
                dispatchMode = DISPATCH_MODE_STOP
            )
        }
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
                (binding.recycler.layoutManager as? LinearLayoutManager)
                    ?.scrollToPositionWithOffset(it, dimen(R.dimen.default_toolbar_height))
            }
            .launchIn(lifecycleScope)

        vm.syncStatus.onEach { status -> bindSyncStatus(status) }.launchIn(lifecycleScope)
        vm.isSyncStatusVisible.onEach { isSyncStatusVisible ->
            if (isSyncStatusVisible)
                binding.topToolbar.findViewById<ViewGroup>(R.id.statusContainer).visible()
            else
                binding.topToolbar.findViewById<ViewGroup>(R.id.statusContainer).invisible()
        }.launchIn(lifecycleScope)

        vm.isUndoEnabled.onEach {
            // TODO
        }.launchIn(lifecycleScope)
        vm.isRedoEnabled.onEach {
            // TODO
        }.launchIn(lifecycleScope)

        with(lifecycleScope) {
            launch {
                vm.actions.collectLatest {
                    binding.blockActionToolbar.bind(it)
                    delay(DEFAULT_DELAY_BLOCK_ACTION_TOOLBAR)
                    handler.post {
                        binding.blockActionToolbar.scrollToPosition(0, smooth = true)
                    }
                }
            }
            subscribe(vm.isUndoRedoToolbarIsVisible) { isVisible ->
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

    private fun bindSyncStatus(status: SyncStatus?) {
        binding.topToolbar.status.bind(status)
        if (status == null) {
            binding.topToolbar.hideStatusContainer()
        } else {
            binding.topToolbar.showStatusContainer()
        }
        val tvStatus = binding.topToolbar.statusText
        binding.topToolbar.statusContainer.setOnLongClickListener {
            when (status) {
                SyncStatus.UNKNOWN -> toast(getString(R.string.sync_status_toast_unknown))
                SyncStatus.FAILED -> toast(getString(R.string.sync_status_toast_failed))
                SyncStatus.OFFLINE -> toast(getString(R.string.sync_status_toast_offline))
                SyncStatus.SYNCING -> toast(getString(R.string.sync_status_toast_syncing))
                SyncStatus.SYNCED -> toast(getString(R.string.sync_status_toast_synced))
                else -> {
                    Timber.i("Missed sync status")
                }
            }
            true
        }
        when (status) {
            SyncStatus.UNKNOWN -> tvStatus.setText(R.string.sync_status_unknown)
            SyncStatus.FAILED -> tvStatus.setText(R.string.sync_status_failed)
            SyncStatus.OFFLINE -> tvStatus.setText(R.string.sync_status_offline)
            SyncStatus.SYNCING -> tvStatus.setText(R.string.sync_status_syncing)
            SyncStatus.SYNCED -> tvStatus.setText(R.string.sync_status_synced)
            else -> {
                // Do nothing
            }
        }
    }

    override fun onDestroyView() {
        pickerDelegate.clearPickit()
        super.onDestroyView()
    }

    override fun onDestroy() {
        pickerDelegate.deleteTemporaryFile()
        pickerDelegate.clearOnCopyFile()
        super.onDestroy()
    }

    override fun onSetRelationKeyClicked(blockId: Id, key: Id) {
        vm.onSetRelationKeyClicked(blockId = blockId, key = key)
    }

    private fun execute(event: EventWrapper<Command>) {
        event.getContentIfNotHandled()?.let { command ->
            when (command) {
                is Command.OpenDocumentImagePicker -> {
                    pickerDelegate.openFilePicker(command.mimeType, REQUEST_PROFILE_IMAGE_CODE)
                }
                is Command.OpenTextBlockIconPicker -> {
                    TextBlockIconPickerFragment.new(
                        context = ctx, blockId = command.block
                    ).showChildFragment()
                }
                Command.OpenDocumentEmojiIconPicker -> {
                    hideSoftInput()
                    findNavController().safeNavigate(
                        R.id.pageScreen,
                        R.id.action_pageScreen_to_objectIconPickerScreen,
                        bundleOf(
                            IconPickerFragmentBase.ARG_CONTEXT_ID_KEY to ctx,
                        )
                    )
                }
                is Command.OpenBookmarkSetter -> {
                    CreateBookmarkFragment.newInstance(
                        target = command.target
                    ).showChildFragment()
                }
                is Command.OpenGallery -> {
                    pickerDelegate.openFilePicker(command.mimeType, null)
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
                    val fr = ObjectMenuFragment.new(
                        ctx = ctx,
                        isArchived = command.isArchived,
                        isFavorite = command.isFavorite,
                        isLocked = command.isLocked,
                        isProfile = false,
                        fromName = getFrom()
                    )
                    fr.showChildFragment()
                }
                is Command.OpenProfileMenu -> {
                    hideKeyboard()
                    findNavController().safeNavigate(
                        R.id.pageScreen,
                        R.id.objectMainMenuScreen,
                        bundleOf(
                            ObjectMenuBaseFragment.CTX_KEY to ctx,
                            ObjectMenuBaseFragment.IS_ARCHIVED_KEY to false,
                            ObjectMenuBaseFragment.IS_FAVORITE_KEY to command.isFavorite,
                            ObjectMenuBaseFragment.IS_LOCKED_KEY to command.isLocked,
                            ObjectMenuBaseFragment.IS_PROFILE_KEY to true
                        )
                    )
                }
                is Command.OpenCoverGallery -> {
                    findNavController().safeNavigate(
                        R.id.pageScreen,
                        R.id.action_pageScreen_to_objectCoverScreen,
                        bundleOf(SelectCoverObjectFragment.CTX_KEY to command.ctx)
                    )
                }
                is Command.OpenObjectLayout -> {
                    val fr = ObjectLayoutFragment.new(command.ctx).apply {
                        onDismissListener = { vm.onLayoutDialogDismissed() }
                    }
                    fr.showChildFragment()
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
                        AlertUpdateAppFragment().showChildFragment(TAG_ALERT)
                    } else {
                        // Do nothing
                    }
                }
                is Command.ClearSearchInput -> {
                    binding.searchToolbar.clear()
                }
                is Command.Dialog.SelectLanguage -> {
                    SelectProgrammingLanguageFragment.new(command.target)
                        .showChildFragment()
                }
                is Command.OpenObjectRelationScreen.RelationAdd -> {
                    hideKeyboard()
                    RelationListFragment
                        .new(
                            ctx = command.ctx,
                            target = command.target,
                            mode = RelationListFragment.MODE_ADD
                        )
                        .showChildFragment()
                }
                is Command.OpenObjectRelationScreen.RelationList -> {
                    hideKeyboard()
                    findNavController().safeNavigate(
                        R.id.pageScreen,
                        R.id.objectRelationListScreen,
                        bundleOf(
                            RelationListFragment.ARG_CTX to command.ctx,
                            RelationListFragment.ARG_TARGET to command.target,
                            RelationListFragment.ARG_LOCKED to command.isLocked,
                            RelationListFragment.ARG_MODE to RelationListFragment.MODE_LIST,
                        )
                    )
                }
                is Command.OpenObjectRelationScreen.Value.Default -> {
                    hideKeyboard()
                    val fr = RelationValueFragment.new(
                        ctx = command.ctx,
                        target = command.target,
                        relationId = command.relationId,
                        relationKey = command.relationKey,
                        targetObjectTypes = command.targetObjectTypes,
                        isLocked = command.isLocked
                    )
                    fr.showChildFragment()
                }
                is Command.OpenObjectRelationScreen.Value.Text -> {
                    hideKeyboard()
                    val fr = RelationTextValueFragment.new(
                        ctx = command.ctx,
                        relationId = command.relationId,
                        relationKey = command.relationKey,
                        objectId = command.target,
                        isLocked = command.isLocked
                    )
                    fr.showChildFragment()
                }
                is Command.OpenObjectRelationScreen.Value.Date -> {
                    hideKeyboard()
                    val fr = RelationDateValueFragment.new(
                        ctx = command.ctx,
                        objectId = command.target,
                        relationId = command.relationId,
                        relationKey = command.relationKey
                    )
                    fr.showChildFragment()
                }
                Command.AddSlashWidgetTriggerToFocusedBlock -> {
                    binding.recycler.addTextFromSelectedStart(text = "/")
                }
                is Command.OpenDraftObjectSelectTypeScreen -> {
                    hideKeyboard()
                    val fr = DraftObjectSelectTypeFragment.newInstance(
                        excludeTypes = command.excludedTypes
                    )
                    fr.showChildFragment()
                }
                is Command.OpenObjectSelectTypeScreen -> {
                    hideKeyboard()
                    val fr = ObjectSelectTypeFragment.newInstance(
                        excludeTypes = command.excludedTypes
                    )
                    fr.showChildFragment()
                }
                is Command.OpenMoveToScreen -> {
                    jobs += lifecycleScope.launch {
                        hideSoftInput()
                        delay(DEFAULT_ANIM_DURATION)
                        val fr = MoveToFragment.new(
                            ctx = ctx,
                            blocks = command.blocks,
                            restorePosition = command.restorePosition,
                            restoreBlock = command.restoreBlock
                        )
                        fr.showChildFragment()
                    }
                }
                is Command.OpenObjectSnackbar -> {
                    binding.root.showActionableSnackBar(
                        getFrom(),
                        command.text,
                        command.icon,
                        middleString = R.string.snack_move_to
                    ) {
                        if (command.isSet) {
                            vm.proceedWithOpeningSet(command.id)
                        } else {
                            vm.proceedWithOpeningObject(command.id)
                        }
                    }
                }
                is Command.OpenLinkToScreen -> {
                    jobs += lifecycleScope.launch {
                        hideSoftInput()
                        delay(DEFAULT_ANIM_DURATION)
                        val fr = LinkToObjectFragment.new(
                            target = command.target,
                            position = command.position,
                            ignore = vm.context
                        )
                        fr.showChildFragment()
                    }
                }
                is Command.AddMentionWidgetTriggerToFocusedBlock -> {
                    binding.recycler.addTextFromSelectedStart(text = "@")
                }
                is Command.OpenAddRelationScreen -> {
                    hideSoftInput()
                    findNavController().safeNavigate(
                        R.id.pageScreen,
                        R.id.action_pageScreen_to_relationAddToObjectBlockFragment,
                        bundleOf(
                            CTX_KEY to command.ctx,
                            RelationAddToObjectBlockFragment.TARGET_KEY to command.target
                        )
                    )
                }
                is Command.OpenLinkToObjectOrWebScreen -> {
                    hideSoftInput()
                    val fr = LinkToObjectOrWebPagesFragment.newInstance(
                        ctx = command.ctx,
                        blockId = command.target,
                        rangeStart = command.range.first,
                        rangeEnd = command.range.last,
                        isWholeBlockMarkup = command.isWholeBlockMarkup
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
                is Command.SaveTextToSystemClipboard -> {
                    val clipData = ClipData.newPlainText("Uri", command.text)
                    clipboard().setPrimaryClip(clipData)
                }
                is Command.OpenObjectAppearanceSettingScreen -> {
                    val fr = ObjectAppearanceSettingFragment.new(
                        ctx = command.ctx,
                        block = command.block
                    )
                    fr.showChildFragment()
                }
                is Command.ScrollToPosition -> {
                    val lm = binding.recycler.layoutManager as LinearLayoutManager
                    val margin = resources.getDimensionPixelSize(R.dimen.default_editor_item_offset)
                    lm.scrollToPositionWithOffset(command.pos, margin)
                }
                is Command.OpenSetBlockTextValueScreen -> {
                    val fr = SetBlockTextValueFragment.new(
                        ctx = command.ctx,
                        block = command.block,
                        table = command.table
                    ).apply {
                        onDismissListener = {
                            vm.onSetBlockTextValueScreenDismiss()
                            hideKeyboard()
                        }
                    }
                    fr.showChildFragment()
                }
            }
        }
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


    private fun proceedWithScrollingToActionMenu(command: Command.ScrollToActionMenu): Unit {
        val lastSelected =
            (vm.state.value as ViewState.Success).blocks.indexOfLast { it.id == command.target }
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

    private fun resetDocumentTitle(state: ViewState.Success) {
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

    private fun render(state: ControlPanelState) {

        keyboardDelayJobs.cancel()

        val insets = ViewCompat.getRootWindowInsets(binding.root)

        if (state.navigationToolbar.isVisible) {
            binding.placeholder.requestFocus()
            hideKeyboard()
            binding.bottomToolbar.visible()
        } else {
            binding.bottomToolbar.gone()
        }

        if (state.mainToolbar.isVisible) {
            binding.toolbar.visible()
            binding.toolbar.state = when (state.mainToolbar.targetBlockType) {
                Main.TargetBlockType.Any -> BlockToolbarWidget.State.Any
                Main.TargetBlockType.Title -> BlockToolbarWidget.State.Title
                Main.TargetBlockType.Cell -> BlockToolbarWidget.State.Cell
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

                binding.bottomMenu.update(count)
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
                        if (binding.recycler.itemDecorationCount == 0) {
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
                        if (binding.recycler.itemDecorationCount == 0) {
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
                    applySlideTransition(
                        binding.slashWidget,
                        SLASH_HIDE_ANIM_DURATION,
                        binding.sheet
                    )
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

        state.objectTypesToolbar.apply {
            if (isVisible) {
                binding.objectTypesToolbar.visible()
                binding.objectTypesToolbar.update(data)
            } else {
                binding.objectTypesToolbar.gone()
                binding.objectTypesToolbar.clear()
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
                        if (binding.recycler.itemDecorationCount == 0) {
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
        if (binding.recycler.itemDecorationCount == 0 || binding.recycler.getItemDecorationAt(0) !is ScrollAndMoveTargetHighlighter) {

//            val offset = recycler.computeVerticalScrollOffset()
//
//            lifecycleScope.launch {
//                recycler.layoutChanges().take(1).collect {
//                    if (offset < screen.y / 3) recycler.scrollBy(0, screen.y / 3)
//                }
//            }

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
        binding.bottomMenu.hideScrollAndMoveModeControls()
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
            .getString(ID_KEY)
            ?: throw IllegalStateException("Document id missing")
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

    override fun injectDependencies() {
        componentManager().editorComponent.get(extractDocumentId()).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().editorComponent.release(extractDocumentId())
    }

    private fun getEditorSettings() {
    }

    override fun onExitToDesktopClicked() {
        vm.navigateToDesktop()
    }

    override fun onLanguageSelected(target: Id, key: String) {
        Timber.d("key: $key")
        vm.onSelectProgrammingLanguageClicked(target, key)
    }

    override fun onMoveToBinSuccess() {
        vm.proceedWithExitingBack()
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
        findNavController().safeNavigate(
            R.id.pageScreen,
            R.id.objectIconPickerScreen,
            bundleOf(
                IconPickerFragmentBase.ARG_CONTEXT_ID_KEY to ctx,
            )
        )
    }

    override fun onLayoutClicked() {
        vm.onLayoutClicked()
    }

    override fun onTextValueChanged(ctx: Id, text: String, objectId: Id, relationKey: Key) {
        vm.onRelationTextValueChanged(
            ctx = ctx,
            value = text,
            relationKey = relationKey
        )
    }

    override fun onNumberValueChanged(ctx: Id, number: Double?, objectId: Id, relationKey: Key) {
        vm.onRelationTextValueChanged(
            ctx = ctx,
            value = number,
            relationKey = relationKey
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
            value = timeInSeconds
        )
    }

    override fun onMoveTo(
        target: Id,
        blocks: List<Id>,
        text: String,
        icon: ObjectIcon,
        isSet: Boolean
    ) {
        vm.proceedWithMoveToAction(
            target = target,
            text = text,
            icon = icon,
            blocks = blocks,
            isSet = isSet
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
        isBookmark: Boolean,
        text: String,
        icon: ObjectIcon,
        isSet: Boolean
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

    override fun onProceedWithUpdateType(id: Id) {
        vm.onObjectTypeChanged(type = id, isObjectDraft = false)
    }

    override fun onProceedWithDraftUpdateType(id: Id) {
        vm.onObjectTypeChanged(type = id, isObjectDraft = true)
    }

    private fun observeNavBackStack() {
        findNavController().run {
            val navBackStackEntry = getBackStackEntry(R.id.pageScreen)
            val savedStateHandle = navBackStackEntry.savedStateHandle
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    proceedWithResult(savedStateHandle)
                }
            }
            navBackStackEntry.lifecycle.addObserver(observer)
            viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_DESTROY) {
                    navBackStackEntry.lifecycle.removeObserver(observer)
                }
            })
        }
    }

    private fun proceedWithResult(savedStateHandle: SavedStateHandle) {
        if (savedStateHandle.contains(RELATION_ADD_RESULT_KEY)) {
            val resultRelationAdd = savedStateHandle.get<RelationAddResult>(RELATION_ADD_RESULT_KEY)
            savedStateHandle.remove<RelationAddResult>(RELATION_ADD_RESULT_KEY)
            if (resultRelationAdd != null) {
                vm.proceedWithAddingRelationToTarget(
                    target = resultRelationAdd.target,
                    relationKey = resultRelationAdd.relation
                )
            }
        }
        if (savedStateHandle.contains(RELATION_NEW_RESULT_KEY)) {
            val resultRelationNew = savedStateHandle.get<RelationNewResult>(RELATION_NEW_RESULT_KEY)
            savedStateHandle.remove<RelationNewResult>(RELATION_NEW_RESULT_KEY)
            if (resultRelationNew != null) {
                vm.proceedWithAddingRelationToTarget(
                    target = resultRelationNew.target,
                    relationKey = resultRelationNew.relation
                )
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            pickerDelegate.resolveActivityResult(requestCode, resultCode, data)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    //------------ End of Anytype Custom Context Menu ------------

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentEditorBinding = FragmentEditorBinding.inflate(
        inflater, container, false
    )

    companion object {
        const val ID_KEY = "id"
        const val DEBUG_SETTINGS = "debug_settings"

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
    fun onExitToDesktopClicked()
    fun onSetRelationKeyClicked(blockId: Id, key: Id)
    fun onSetObjectLink(objectId: Id)
    fun onSetWebLink(link: String)
    fun onCreateObject(name: String)
    fun onSetTextBlockValue()
    fun onMentionClicked(target: Id)
    fun onCopyLink(link: String)
}