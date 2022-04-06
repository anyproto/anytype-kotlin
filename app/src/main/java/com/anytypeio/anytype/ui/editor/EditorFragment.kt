package com.anytypeio.anytype.ui.editor

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.SyncStatus
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.ext.getFirstLinkOrObjectMarkupParam
import com.anytypeio.anytype.core_models.ext.getSubstring
import com.anytypeio.anytype.core_ui.extensions.addTextFromSelectedStart
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.extensions.cursorYBottomCoordinate
import com.anytypeio.anytype.core_ui.features.editor.*
import com.anytypeio.anytype.core_ui.features.editor.holders.other.Code
import com.anytypeio.anytype.core_ui.features.editor.holders.other.Title
import com.anytypeio.anytype.core_ui.features.editor.holders.relations.FeaturedRelationListViewHolder
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Text
import com.anytypeio.anytype.core_ui.features.editor.scrollandmove.DefaultScrollAndMoveTargetDescriptor
import com.anytypeio.anytype.core_ui.features.editor.scrollandmove.ScrollAndMoveStateListener
import com.anytypeio.anytype.core_ui.features.editor.scrollandmove.ScrollAndMoveTargetHighlighter
import com.anytypeio.anytype.core_ui.menu.TextLinkPopupMenu
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_ui.tools.*
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.const.FileConstants.REQUEST_FILE_SAF_CODE
import com.anytypeio.anytype.core_utils.const.FileConstants.REQUEST_MEDIA_CODE
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ext.PopupExtensions.calculateRectInWindow
import com.anytypeio.anytype.databinding.FragmentEditorBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.ext.extractMarks
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.EditorViewModelFactory
import com.anytypeio.anytype.presentation.editor.Snack
import com.anytypeio.anytype.presentation.editor.editor.*
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.UiBlock
import com.anytypeio.anytype.presentation.editor.editor.sam.ScrollAndMoveTarget
import com.anytypeio.anytype.presentation.editor.editor.sam.ScrollAndMoveTargetDescriptor
import com.anytypeio.anytype.presentation.editor.markup.MarkupColorView
import com.anytypeio.anytype.presentation.editor.model.EditorFooter
import com.anytypeio.anytype.presentation.util.CopyFileStatus
import com.anytypeio.anytype.ui.alert.AlertUpdateAppFragment
import com.anytypeio.anytype.ui.base.NavigationFragment
import com.anytypeio.anytype.ui.editor.cover.SelectCoverObjectFragment
import com.anytypeio.anytype.ui.editor.gallery.FullScreenPictureFragment
import com.anytypeio.anytype.ui.editor.layout.ObjectLayoutFragment
import com.anytypeio.anytype.ui.editor.modals.*
import com.anytypeio.anytype.ui.editor.sheets.ObjectMenuBaseFragment
import com.anytypeio.anytype.ui.editor.sheets.ObjectMenuBaseFragment.DocumentMenuActionReceiver
import com.anytypeio.anytype.ui.linking.LinkToObjectFragment
import com.anytypeio.anytype.ui.linking.LinkToObjectOrWebPagesFragment
import com.anytypeio.anytype.ui.linking.OnLinkToAction
import com.anytypeio.anytype.ui.moving.MoveToFragment
import com.anytypeio.anytype.ui.moving.OnMoveToAction
import com.anytypeio.anytype.ui.objects.ObjectAppearanceSettingFragment
import com.anytypeio.anytype.ui.objects.ObjectTypeChangeFragment
import com.anytypeio.anytype.ui.objects.ObjectTypeChangeFragment.Companion.OBJECT_TYPE_REQUEST_KEY
import com.anytypeio.anytype.ui.objects.ObjectTypeChangeFragment.Companion.OBJECT_TYPE_URL_KEY
import com.anytypeio.anytype.ui.relations.*
import com.anytypeio.anytype.ui.relations.RelationAddBaseFragment.Companion.CTX_KEY
import com.anytypeio.anytype.ui.relations.RelationAddToObjectBlockFragment.Companion.RELATION_ADD_RESULT_KEY
import com.anytypeio.anytype.ui.relations.RelationCreateFromScratchForObjectBlockFragment.Companion.RELATION_NEW_RESULT_KEY
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.abs

open class EditorFragment : NavigationFragment<FragmentEditorBinding>(R.layout.fragment_editor),
    OnFragmentInteractionListener,
    TurnIntoActionReceiver,
    SelectProgrammingLanguageReceiver,
    RelationTextValueFragment.TextValueEditReceiver,
    RelationDateValueFragment.DateValueEditReceiver,
    DocumentMenuActionReceiver,
    ClipboardInterceptor,
    OnMoveToAction,
    OnLinkToAction,
    PickiTCallbacks {

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

    private lateinit var pickiT: PickiT

    private val blockAdapter by lazy {
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
            onBackPressedCallback = { vm.onBackPressedCallback() },
            onKeyPressedEvent = vm::onKeyPressedEvent,
            onDragAndDropTrigger = { vh: RecyclerView.ViewHolder -> handleDragAndDropTrigger(vh) },
            onDragListener = dndListener,
            lifecycle = lifecycle
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
        FirstItemInvisibilityDetector { isVisible ->
            if (isVisible) {
                binding.topToolbar.setBackgroundColor(0)
                binding.topToolbar.statusText.animate().alpha(1f).setDuration(DEFAULT_TOOLBAR_ANIM_DURATION)
                    .start()
                binding.topToolbar.container.animate().alpha(0f).setDuration(DEFAULT_TOOLBAR_ANIM_DURATION)
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
                binding.topToolbar.statusText.animate().alpha(0f).setDuration(DEFAULT_TOOLBAR_ANIM_DURATION)
                    .start()
                binding.topToolbar.container.animate().alpha(1f).setDuration(DEFAULT_TOOLBAR_ANIM_DURATION)
                    .start()
                binding.topToolbar.setStyle(overCover = false)
            }
        }
    }

    @Inject
    lateinit var factory: EditorViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(OBJECT_TYPE_REQUEST_KEY) { _, bundle ->
            val id = bundle.getString(OBJECT_TYPE_URL_KEY)
            onObjectTypePicked(id = id)
        }
        pickiT = PickiT(requireContext(), this, requireActivity())
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
                onCopyFileCommand(command)
            }
        }
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
            .onEach {
                vm.onApplyScrollAndMoveClicked()
                onApplyScrollAndMoveClicked()
            }
            .launchIn(lifecycleScope)

        binding.scrollAndMoveBottomAction
            .cancel
            .clicks()
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

        binding.topToolbar.menu
            .clicks()
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
            .onEach { vm.onMarkupUrlClicked() }
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

        binding.undoRedoToolbar.undo.clicks().onEach {
            vm.onActionUndoClicked()
        }.launchIn(lifecycleScope)

        binding.undoRedoToolbar.redo.clicks().onEach {
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

        lifecycleScope.launch {
            binding.searchToolbar.events().collect { vm.onSearchToolbarEvent(it) }
        }

        binding.objectNotExist.root.findViewById<TextView>(R.id.btnToDashboard).setOnClickListener {
            vm.onHomeButtonClicked()
        }

        BottomSheetBehavior.from(binding.styleToolbarMain).state = BottomSheetBehavior.STATE_HIDDEN
        BottomSheetBehavior.from(binding.styleToolbarOther).state = BottomSheetBehavior.STATE_HIDDEN
        BottomSheetBehavior.from(binding.styleToolbarColors).state = BottomSheetBehavior.STATE_HIDDEN
        BottomSheetBehavior.from(binding.blockActionToolbar).state = BottomSheetBehavior.STATE_HIDDEN
        BottomSheetBehavior.from(binding.undoRedoToolbar).state = BottomSheetBehavior.STATE_HIDDEN

        observeNavBackStack()
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
        vm.searchResultScrollPosition
            .filter { it != EditorViewModel.NO_SEARCH_RESULT_POSITION }
            .onEach { binding.recycler.smoothScrollToPosition(it) }
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
            subscribe(vm.actions) { binding.blockActionToolbar.bind(it) }
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
        clearPickit()
        super.onDestroyView()
    }

    override fun onDestroy() {
        pickiT.deleteTemporaryFile(requireContext())
        clearOnCopyFile()
        super.onDestroy()
    }

    override fun onSetRelationKeyClicked(blockId: Id, key: Id) {
        vm.onSetRelationKeyClicked(blockId = blockId, key = key)
    }

    private fun onObjectTypePicked(id: Id?) {
        vm.onObjectTypeChanged(id = id)
    }

    private fun execute(event: EventWrapper<Command>) {
        event.getContentIfNotHandled()?.let { command ->
            when (command) {
                is Command.OpenProfileIconActionMenu -> {
//                    hideSoftInput()
//                    recycler.smoothScrollToPosition(0)
//                    val title = recycler.getChildAt(0)
//                    val shared = title.findViewById<FrameLayout>(R.id.documentIconContainer)
//                    val fr = ProfileIconActionMenuFragment.new(
//                        y = shared.y + dimen(R.dimen.dp_48),
//                        target = command.target,
//                        ctx = ctx,
//                        image = command.image,
//                        name = command.name
//                    ).apply {
//                        enterTransition = Fade()
//                        exitTransition = Fade()
//                        sharedElementEnterTransition = ChangeBounds()
//                    }
//                    childFragmentManager.beginTransaction()
//                        .add(R.id.root, fr)
//                        .addToBackStack(null)
//                        .apply { addSharedElement(shared, getString(R.string.logo_transition)) }
//                        .commit()
                }
                is Command.OpenDocumentEmojiIconPicker -> {
                    hideSoftInput()
                    findNavController().navigate(
                        R.id.action_pageScreen_to_objectIconPickerScreen,
                        bundleOf(
                            ObjectIconPickerBaseFragment.ARG_CONTEXT_ID_KEY to ctx,
                            ObjectIconPickerBaseFragment.ARG_TARGET_ID_KEY to command.target
                        )
                    )
                }
                is Command.OpenAddBlockPanel -> {
//                    hideKeyboard()
//                    AddBlockFragment.newInstance(command.ctx).show(childFragmentManager, null)
                }
                is Command.OpenTurnIntoPanel -> {
//                    TurnIntoFragment.single(
//                        target = command.target,
//                        excludedCategories = command.excludedCategories,
//                        excludedTypes = command.excludedTypes
//
//                    ).show(childFragmentManager, null)
                }
                is Command.OpenMultiSelectTurnIntoPanel -> {
//                    TurnIntoFragment.multiple(
//                        excludedTypes = command.excludedTypes,
//                        excludedCategories = command.excludedCategories
//                    ).show(childFragmentManager, null)
                }
                is Command.OpenBookmarkSetter -> {
                    CreateBookmarkFragment.newInstance(
                        target = command.target
                    ).show(childFragmentManager, null)
                }
                is Command.OpenGallery -> {
                    openFilePicker(command.mimeType)
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
                is Command.Measure -> {
                    val views = blockAdapter.views
                    val position = views.indexOfFirst { it.id == command.target }
                    val lm = binding.recycler.layoutManager as? LinearLayoutManager
                    val target = lm?.findViewByPosition(position)
                    val rect = calculateRectInWindow(target)
                    val dimensions = BlockDimensions(
                        left = rect.left,
                        top = rect.top,
                        bottom = rect.bottom,
                        right = rect.right,
                        height = binding.root.height,
                        width = binding.root.width
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
                    findNavController().navigate(
                        R.id.objectMainMenuScreen,
                        bundleOf(
                            ObjectMenuBaseFragment.CTX_KEY to ctx,
                            ObjectMenuBaseFragment.IS_ARCHIVED_KEY to command.isArchived,
                            ObjectMenuBaseFragment.IS_FAVORITE_KEY to command.isFavorite,
                            ObjectMenuBaseFragment.IS_PROFILE_KEY to false
                        )
                    )
                }
                is Command.OpenProfileMenu -> {
                    hideKeyboard()
                    findNavController().navigate(
                        R.id.objectMainMenuScreen,
                        bundleOf(
                            ObjectMenuBaseFragment.CTX_KEY to ctx,
                            ObjectMenuBaseFragment.IS_ARCHIVED_KEY to false,
                            ObjectMenuBaseFragment.IS_FAVORITE_KEY to command.isFavorite,
                            ObjectMenuBaseFragment.IS_PROFILE_KEY to true
                        )
                    )
                }
                is Command.OpenCoverGallery -> {
                    try {
                        findNavController().navigate(
                            R.id.action_pageScreen_to_objectCoverScreen,
                            bundleOf(SelectCoverObjectFragment.CTX_KEY to command.ctx)
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Error while opening object cover screen")
                        toast("Error while opening object cover screen: ${e.message}")
                    }
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
                        // Do nothing
                    }
                }
                is Command.ClearSearchInput -> {
                    binding.searchToolbar.clear()
                }
                is Command.Dialog.SelectLanguage -> {
                    SelectProgrammingLanguageFragment.new(command.target)
                        .show(childFragmentManager, null)
                }
                is Command.OpenObjectRelationScreen.RelationAdd -> {
                    hideKeyboard()
                    RelationListFragment
                        .new(
                            ctx = command.ctx,
                            target = command.target,
                            mode = RelationListFragment.MODE_ADD
                        )
                        .show(childFragmentManager, null)
                }
                is Command.OpenObjectRelationScreen.RelationList -> {
                    hideKeyboard()
                    findNavController().navigate(
                        R.id.objectRelationListScreen,
                        bundleOf(
                            RelationListFragment.ARG_CTX to command.ctx,
                            RelationListFragment.ARG_TARGET to command.target,
                            RelationListFragment.ARG_MODE to RelationListFragment.MODE_LIST
                        )
                    )
                }
                is Command.OpenObjectRelationScreen.Value.Default -> {
                    hideKeyboard()
                    val fr = RelationValueFragment.new(
                        ctx = command.ctx,
                        target = command.target,
                        relation = command.relation,
                        targetObjectTypes = command.targetObjectTypes
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
                    binding.recycler.addTextFromSelectedStart(text = "/")
                }
                is Command.OpenChangeObjectTypeScreen -> {
                    hideKeyboard()
                    findNavController()
                        .navigate(
                            R.id.objectTypeChangeScreen,
                            bundleOf(
                                ObjectTypeChangeFragment.ARG_SMART_BLOCK_TYPE to command.smartBlockType
                            )
                        )
                }
                is Command.OpenMoveToScreen -> {
                    lifecycleScope.launch {
                        hideSoftInput()
                        delay(DEFAULT_ANIM_DURATION)
                        val fr = MoveToFragment.new(
                            ctx = ctx,
                            blocks = command.blocks,
                            restorePosition = command.restorePosition,
                            restoreBlock = command.restoreBlock
                        )
                        fr.show(childFragmentManager, null)
                    }
                }
                is Command.OpenLinkToScreen -> {
                    lifecycleScope.launch {
                        hideSoftInput()
                        delay(DEFAULT_ANIM_DURATION)
                        val fr = LinkToObjectFragment.new(
                            target = command.target,
                            position = command.position
                        )
                        fr.show(childFragmentManager, null)
                    }
                }
                is Command.AddMentionWidgetTriggerToFocusedBlock -> {
                    binding.recycler.addTextFromSelectedStart(text = "@")
                }
                is Command.OpenAddRelationScreen -> {
                    hideSoftInput()
                    findNavController().navigate(
                        R.id.action_pageScreen_to_relationAddToObjectBlockFragment,
                        bundleOf(
                            CTX_KEY to command.ctx,
                            RelationAddToObjectBlockFragment.TARGET_KEY to command.target
                        )
                    )
                }
                is Command.OpenLinkToObjectOrWebScreen -> {
                    hideSoftInput()
                    val fr = LinkToObjectOrWebPagesFragment.newInstance(command.uri)
                    fr.show(childFragmentManager, null)
                }
                is Command.ShowKeyboard -> {
                    binding.recycler.findFocus()?.focusAndShowKeyboard()
                }
                is Command.OpenFileByDefaultApp -> openFileByDefaultApp(command)
                Command.ShowTextLinkMenu -> {
                    val urlButton = binding.markupToolbar.findViewById<View>(R.id.url)
                    val popup = TextLinkPopupMenu(
                        context = requireContext(),
                        view = urlButton,
                        onCopyLinkClicked = { vm.onCopyLinkClicked() },
                        onEditLinkClicked = { vm.onEditLinkClicked() },
                        onUnlinkClicked = { vm.onUnlinkClicked() }
                    )
                    popup.show()
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
                    fr.show(childFragmentManager, null)
                }
            }
        }
    }

    private fun openFileByDefaultApp(command: Command.OpenFileByDefaultApp) {
        try {
            val uri = Uri.parse(command.uri)
            val intent = Intent().apply {
                action = Intent.ACTION_VIEW
                if (command.mime.isNotEmpty()) {
                    setDataAndTypeAndNormalize(uri, command.mime)
                } else {
                    data = uri
                }
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        } catch (e: Exception) {
            if (e is ActivityNotFoundException) {
                toast("No Application found to open the selected file")
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
                binding.recycler.invalidateItemDecorations()
                val isLocked = vm.mode is Editor.Mode.Locked
                binding.topToolbar.setIsLocked(isLocked)
                resetDocumentTitle(state)
            }
            is ViewState.OpenLinkScreen -> {
                if (childFragmentManager.findFragmentByTag(TAG_LINK) == null) {
                    SetLinkFragment.newInstance(
                        blockId = state.block.id,
                        initUrl = state.block.getFirstLinkOrObjectMarkupParam(state.range),
                        text = state.block.getSubstring(state.range),
                        rangeEnd = state.range.last,
                        rangeStart = state.range.first
                    ).show(childFragmentManager, TAG_LINK)
                }
            }
            ViewState.Loading -> {}
            ViewState.NotExist -> {
                binding.recycler.gone()
                binding.objectNotExist.root.visible()
            }
        }
    }

    private fun resetDocumentTitle(state: ViewState.Success) {
        state.blocks.firstOrNull { view ->
            view is BlockView.Title.Basic || view is BlockView.Title.Profile || view is BlockView.Title.Todo
        }?.let { view ->
            when (view) {
                is BlockView.Title.Basic -> {
                    resetTopToolbarTitle(
                        text = view.text,
                        emoji = view.emoji,
                        image = view.image
                    )
                    if (view.hasCover) {
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
                        text = view.text,
                        emoji = null,
                        image = view.image
                    )
                    if (view.hasCover) {
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
                        text = view.text,
                        emoji = null,
                        image = view.image
                    )
                    if (view.hasCover) {
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
//        when {
//            emoji != null && emoji.isNotEmpty() -> {
//                try {
//                    topToolbar.emoji.invisible()
//                    Glide.with(topToolbar.image).load(Emojifier.uri(emoji)).into(topToolbar.image)
//                } catch (e: Exception) {
//                    topToolbar.emoji.visible()
//                    topToolbar.emoji.text = emoji
//                }
//            }
//            image != null -> {
//                topToolbar.emoji.invisible()
//                topToolbar.image.visible()
//                Glide
//                    .with(topToolbar.image)
//                    .load(image)
//                    .centerInside()
//                    .circleCrop()
//                    .into(topToolbar.image)
//            }
//            else -> {
//                topToolbar.image.setImageDrawable(null)
//            }
//        }
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

        if (state.mainToolbar.isVisible)
            binding.toolbar.visible()
        else
            binding.toolbar.invisible()

        setMainMarkupToolbarState(state)

        state.multiSelect.apply {
            val behavior = BottomSheetBehavior.from(binding.blockActionToolbar)
            if (isVisible) {
                binding.multiSelectTopToolbar.visible()
                when {
                    count > 1 -> {
                        binding.multiSelectTopToolbar.selectText.text =
                            getString(R.string.number_selected_blocks, count)
                    }
                    count == 1 -> {
                        binding.multiSelectTopToolbar.selectText.setText(R.string.one_selected_block)
                    }
                    else -> {
                        binding.multiSelectTopToolbar.selectText.text = null
                    }
                }
                binding.bottomMenu.update(count)
                if (!binding.bottomMenu.isShowing) {
                    binding.recycler.apply { itemAnimator = DefaultItemAnimator() }

                    proceedWithHidingSoftInput()

                    binding.topToolbar.invisible()

                    if (!state.multiSelect.isScrollAndMoveEnabled) {
                        if (!binding.recycler.containsItemDecoration(actionToolbarFooter)) {
                            binding.recycler.addItemDecoration(actionToolbarFooter)
                        }
                        keyboardDelayJobs += lifecycleScope.launch {
                            if (insets != null) {
                                if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                                    delay(DELAY_HIDE_KEYBOARD)
                                }
                            } else {
                                delay(DELAY_HIDE_KEYBOARD)
                            }
                            behavior.apply {
                                setState(BottomSheetBehavior.STATE_EXPANDED)
                                addBottomSheetCallback(onHideBottomSheetCallback)
                            }
                            showSelectButton()
                        }
                    } else {
                        behavior.removeBottomSheetCallback(onHideBottomSheetCallback)
                    }
                }
            } else {
                binding.recycler.apply { itemAnimator = null }
                behavior.apply {
                    setState(BottomSheetBehavior.STATE_HIDDEN)
                    removeBottomSheetCallback(onHideBottomSheetCallback)
                }
                hideSelectButton()
                binding.recycler.removeItemDecoration(actionToolbarFooter)
            }
            if (isScrollAndMoveEnabled)
                enterScrollAndMove()
            else
                exitScrollAndMove()
        }

        state.stylingToolbar.apply {
            val behavior = BottomSheetBehavior.from(binding.styleToolbarMain)
            if (isVisible) {
                binding.styleToolbarMain.setSelectedStyle(style)
                if (behavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                    keyboardDelayJobs += lifecycleScope.launch {
                        if (binding.recycler.itemDecorationCount == 0) {
                            binding.recycler.addItemDecoration(styleToolbarFooter)
                        }
                        proceedWithHidingSoftInput()
                        if (insets != null) {
                            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                                delay(DELAY_HIDE_KEYBOARD)
                            }
                        } else {
                            delay(DELAY_HIDE_KEYBOARD)
                        }
                        behavior.apply {
                            setState(BottomSheetBehavior.STATE_EXPANDED)
                            addBottomSheetCallback(onHideBottomSheetCallback)
                        }
                    }
                }
            } else {
                if (!state.styleColorToolbar.isVisible && !state.styleExtraToolbar.isVisible) {
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
                binding.styleToolbarOther.setProperties(
                    props = state.stylingToolbar.props,
                    config = state.stylingToolbar.config
                )
                lifecycleScope.launch {
                    BottomSheetBehavior.from(binding.styleToolbarOther).apply {
                        setState(BottomSheetBehavior.STATE_EXPANDED)
                        addBottomSheetCallback(onHideBottomSheetCallback)
                    }
                }
            } else {
                BottomSheetBehavior.from(binding.styleToolbarOther).apply {
                    removeBottomSheetCallback(onHideBottomSheetCallback)
                    setState(BottomSheetBehavior.STATE_HIDDEN)
                }
            }
        }

        state.styleColorToolbar.apply {
            if (isVisible) {
                state.stylingToolbar.config?.let { config ->
                    binding.styleToolbarColors.update(
                        config,
                        state.stylingToolbar.props
                    )
                }
                lifecycleScope.launch {
                    BottomSheetBehavior.from(binding.styleToolbarColors).apply {
                        setState(BottomSheetBehavior.STATE_EXPANDED)
                        addBottomSheetCallback(onHideBottomSheetCallback)
                    }
                }
            } else {
                BottomSheetBehavior.from(binding.styleToolbarColors).apply {
                    removeBottomSheetCallback(onHideBottomSheetCallback)
                    setState(BottomSheetBehavior.STATE_HIDDEN)
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
            if (isVisible) {
                if (!binding.slashWidget.isVisible) {
                    binding.slashWidget.scrollToTop()
                    showSlashWidget(this)
                }
                widgetState?.let {
                    binding.slashWidget.onStateChanged(it)
                }
            } else {
                binding.slashWidget.gone()
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
                            ?: ThemeColor.DEFAULT.title
                    )
                }
                if (state.markupMainToolbar.isBackgroundColorSelected) {
                    binding.markupColorToolbar.setBackgroundColor(
                        state.markupMainToolbar.style?.markupHighlightColor
                            ?: state.markupMainToolbar.style?.blockBackroundColor
                            ?: ThemeColor.DEFAULT.title
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
                binding.recycler.post {
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
                binding.recycler.post {
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
                binding.recycler.post {
                    binding.recycler.smoothScrollBy(0, scrollY)
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
                doOnEnd { binding.topToolbar.visible() }
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

    override fun onUrlPasted(url: Url) {
        vm.onUrlPasted(url)
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

    //region PICK IT

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
        Timber.d("PickiTonUriReturned")
        if (pickitProgressDialog == null || pickitProgressDialog?.isShowing == false) {
            pickitProgressDialog = ProgressDialog(requireContext()).apply {
                setMessage(getString(R.string.pickit_waiting))
                setCancelable(false)
            }
            pickitProgressDialog?.show()
        }
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
        Timber.d("PickiTonStartListener")
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
        if (pickitProgressDialog?.isShowing == true) {
            pickitProgressDialog?.dismiss()
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

    override fun PickiTonMultipleCompleteListener(
        paths: ArrayList<String>?,
        wasSuccessful: Boolean,
        Reason: String?
    ) {
        toast("Not implemented yet")
    }

    /**
     * Called when a file was picked from file picker.
     */
    private fun onFilePathReady(filePath: String?) {
        if (filePath != null) {
            vm.onProceedWithFilePath(filePath = filePath)
        } else {
            Timber.e("onFilePathReady, filePath is null")
        }
    }

    private fun clearPickit() {
        pickiT.cancelTask()
        pickitAlertDialog?.dismiss()
        pickitProgressDialog?.dismiss()
    }

    //endregion

    override fun onExitToDesktopClicked() {
        vm.navigateToDesktop()
    }

    override fun onLanguageSelected(target: Id, key: String) {
        Timber.d("key: $key")
        vm.onSelectProgrammingLanguageClicked(target, key)
    }

    override fun onArchiveClicked() {
        vm.onArchiveThisObjectClicked()
    }

    override fun onRestoreFromArchiveClicked() {
        vm.onRestoreThisObjectFromArchive()
    }

    override fun onSearchOnPageClicked() {
        vm.onEnterSearchModeClicked()
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
        findNavController().navigate(
            R.id.objectIconPickerScreen,
            bundleOf(
                ObjectIconPickerBaseFragment.ARG_CONTEXT_ID_KEY to ctx,
                ObjectIconPickerBaseFragment.ARG_TARGET_ID_KEY to ctx,
            )
        )
    }

    override fun onLayoutClicked() {
        vm.onLayoutClicked()
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

    override fun onMoveTo(target: Id, blocks: List<Id>) {
        vm.proceedWithMoveToAction(
            target = target,
            blocks = blocks
        )
    }

    override fun onMoveToClose(blocks: List<Id>, restorePosition: Int?, restoreBlock: Id?) {
        vm.proceedWithMoveToExit(
            blocks = blocks,
            restorePosition = restorePosition,
            restoreBlock = restoreBlock
        )
    }

    override fun onLinkTo(link: Id, target: Id) {
        vm.proceedWithLinkToAction(
            link = link,
            target = target
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

    override fun onSetWebLink(uri: String) {
        vm.proceedToAddUriToTextAsLink(uri)
    }

    override fun onCreateObject(name: String) {
        vm.proceedToCreateObjectAndAddToTextAsLink(name)
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
                    relation = resultRelationAdd.relation
                )
            }
        }
        if (savedStateHandle.contains(RELATION_NEW_RESULT_KEY)) {
            val resultRelationNew = savedStateHandle.get<RelationNewResult>(RELATION_NEW_RESULT_KEY)
            savedStateHandle.remove<RelationNewResult>(RELATION_NEW_RESULT_KEY)
            if (resultRelationNew != null) {
                vm.proceedWithAddingRelationToTarget(
                    target = resultRelationNew.target,
                    relation = resultRelationNew.relation
                )
            }
        }
    }

    //region Drag-and-drop UI logic.

    private var dndTargetPos = -1
    private var dndTargetPrevious: Pair<Float, Int>? = null

    var dndTargetLineAnimator: ViewPropertyAnimator? = null

    private var scrollDownJob: Job? = null
    private var scrollUpJob: Job? = null

    private val dndListener by lazy {
        EditorDragAndDropListener(
            onDragLocation = { target, ratio ->
                handleDragging(target, ratio)
            },
            onDrop = { target, ratio ->
                proceedWithDropping(target, ratio)
            },
            onDragExited = {
                if (binding.dndTargetLine != null) {
                    binding.dndTargetLine.invisible()
                }
            },
            onDragEnded = {
                stopScrollDownJob()
                stopScrollUpJob()
            }
        )
    }

    private fun handleDragAndDropTrigger(vh: RecyclerView.ViewHolder): Boolean {
        if (vm.mode is Editor.Mode.Edit) {
            if (vh is BlockViewHolder.DragAndDropHolder && binding.recycler.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                dndTargetPos = vh.bindingAdapterPosition

                val item = ClipData.Item(EMPTY_TEXT)

                val dragData = ClipData(
                    DRAG_AND_DROP_LABEL,
                    arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                    item
                )

                val shadow = when (vh) {
                    is Text -> TextInputDragShadow(vh.content.id, vh.itemView)
                    is Code -> TextInputDragShadow(vh.content.id, vh.itemView)
                    else -> DefaultEditorDragShadow(vh.itemView)
                }

                vh.itemView.startDragAndDrop(
                    dragData,
                    shadow,
                    null,
                    0
                )
            }
        } else {
            val pos = vh.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                vm.onClickListener(
                    ListenerType.LongClick(vm.views[pos].id, BlockDimensions())
                )
            }
        }
        return true
    }

    private fun handleDragging(target: View, ratio: Float) {
        val vh = binding.recycler.findContainingViewHolder(target)
        if (vh != null) {
            if (vh.bindingAdapterPosition != dndTargetPos) {
                binding.dndTargetLine.visible()
                if (vh is SupportNesting) {
                    when (ratio) {
                        in DragAndDropConfig.topRange -> {
                            if (handleDragAbove(vh, ratio))
                                return
                        }
                        in DragAndDropConfig.middleRange -> {
                            handleDragInside(vh)
                        }
                        in DragAndDropConfig.bottomRange -> {
                            if (handleDragBelow(vh, ratio))
                                return
                        }
                    }
                } else {
                    when (ratio) {
                        in DragAndDropConfig.topHalfRange -> {
                            if (vh is FeaturedRelationListViewHolder) {
                                binding.dndTargetLine.invisible()
                            } else if (vh is Title) {
                                binding.dndTargetLine.invisible()
                            } else {
                                binding.dndTargetLine.visible()
                                if (handleDragAbove(vh, ratio))
                                    return
                            }
                        }
                        in DragAndDropConfig.bottomHalfRange -> {
                            if (handleDragBelow(vh, ratio))
                                return
                        }
                    }
                }
            }

            handleScrollingWhileDragging(vh, ratio)
            dndTargetPrevious = Pair(ratio, vh.bindingAdapterPosition)
        } else {
            toast("onDragLocation: holder not found")
        }
    }

    private fun handleScrollingWhileDragging(
        vh: RecyclerView.ViewHolder,
        ratio: Float
    ) {

        val targetViewPosition = IntArray(2)
        vh.itemView.getLocationOnScreen(targetViewPosition)
        val targetViewY = targetViewPosition[1]

        val targetY = targetViewY + (vh.itemView.height * ratio)

        // Checking whether the touch is at the bottom of the screen.

        if (screen.y - targetY < 200) {
            if (scrollDownJob == null) {
                startScrollingDown()
            }
        } else {
            stopScrollDownJob()
        }

        // Checking whether the touch is at the top of the screen.

        if (targetY < 200) {
            if (scrollUpJob == null) {
                startScrollingUp()
            }
        } else {
            stopScrollUpJob()
        }
    }

    private fun startScrollingDown() {
        scrollDownJob = lifecycleScope.launch {
            while (isActive) {
                binding.recycler.smoothScrollBy(0, 350)
                delay(60)
            }
        }
    }

    private fun startScrollingUp() {
        scrollUpJob = lifecycleScope.launch {
            while (isActive) {
                binding.recycler.smoothScrollBy(0, -350)
                delay(60)
            }
        }
    }

    private fun handleDragBelow(
        vh: RecyclerView.ViewHolder,
        ratio: Float
    ): Boolean {
        val currPos = vh.bindingAdapterPosition
        val prev = dndTargetPrevious
        if (prev != null) {
            val (prevRatio, prevPosition) = prev
            if (vh.bindingAdapterPosition.inc() == prevPosition && prevRatio in DragAndDropConfig.topRange) {
                Timber.d("dnd skipped: prev - $prev, curr: pos ${vh.bindingAdapterPosition}, $ratio")
                val previousTarget = blockAdapter.views[prevPosition]
                val currentTarget = blockAdapter.views[currPos]
                if (previousTarget is BlockView.Indentable && currentTarget is BlockView.Indentable) {
                    if (previousTarget.indent == currentTarget.indent)
                        return true
                } else {
                    return true
                }
            } else {
                Timber.d("dnd not skipped: prev - $prev, curr: pos ${vh.bindingAdapterPosition}, $ratio")
            }
        } else {
            Timber.d("dnd prev was null")
        }

        var indent = 0

        val block = blockAdapter.views[vh.bindingAdapterPosition]

        if (block is BlockView.Indentable) {
            indent = block.indent * dimen(R.dimen.indent)
        }

        binding.dndTargetLine.translationY = vh.itemView.bottom.toFloat()
        //                                    dndTargetLineAnimator?.cancel()
        //                                    dndTargetLineAnimator = dndTargetLine
        //                                        .animate()
        //                                        .translationY(vh.itemView.bottom.toFloat())
        //                                        .setDuration(100)
        //                                    dndTargetLineAnimator?.start()
        binding.dndTargetLine.translationX = indent.toFloat()

        return false
    }

    private fun handleDragInside(vh: RecyclerView.ViewHolder) {
        //dndTargetLineAnimator?.cancel()
        if (vh !is SupportNesting) {
            binding.dndTargetLine.invisible()
        }
        binding.dndTargetLine.translationY = vh.itemView.top.toFloat() + vh.itemView.height / 2
        binding.dndTargetLine.translationX = -(vh.itemView.width.toFloat() - 100)
    }

    private fun handleDragAbove(
        vh: RecyclerView.ViewHolder,
        ratio: Float
    ): Boolean {
        val currPos = vh.bindingAdapterPosition
        val prev = dndTargetPrevious
        if (prev != null) {
            val (prevRatio, prevPosition) = prev
            if (currPos == prevPosition.inc() && prevRatio in DragAndDropConfig.bottomRange) {
                Timber.d("dnd skipped: prev - $prev, curr: pos ${vh.bindingAdapterPosition}, $ratio")
                val previousTarget = blockAdapter.views[prevPosition]
                val currentTarget = blockAdapter.views[currPos]
                if (previousTarget is BlockView.Indentable && currentTarget is BlockView.Indentable) {
                    if (previousTarget.indent == currentTarget.indent)
                        return true
                } else {
                    return true
                }
            } else {
                Timber.d("dnd not skipped: prev - $prev, curr: pos ${vh.bindingAdapterPosition}, $ratio")
            }
        } else {
            Timber.d("dnd prev was null")
        }

        var indent = 0

        val block = blockAdapter.views[vh.bindingAdapterPosition]

        if (block is BlockView.Indentable) {
            indent = block.indent * dimen(R.dimen.indent)
        }

        binding.dndTargetLine.translationY = vh.itemView.top.toFloat()
        //                                    dndTargetLineAnimator?.cancel()
        //                                    dndTargetLineAnimator = dndTargetLine
        //                                        .animate()
        //                                        .translationY(vh.itemView.top.toFloat())
        //                                        .setDuration(100)
        //dndTargetLineAnimator?.start()
        binding.dndTargetLine.translationX = indent.toFloat()
        return false
    }

    private fun proceedWithDropping(target: View, ratio: Float) {
        binding.dndTargetLine.invisible()
        val vh = binding.recycler.findContainingViewHolder(target)

        if (vh != null) {
            if (vh.bindingAdapterPosition != dndTargetPos) {
                blockAdapter.notifyItemMoved(dndTargetPos, vh.bindingAdapterPosition)
                if (vh is SupportNesting) {
                    when (ratio) {
                        in DragAndDropConfig.topRange -> {
                            vm.onDragAndDrop(
                                dragged = blockAdapter.views[dndTargetPos].id,
                                target = blockAdapter.views[vh.bindingAdapterPosition].id,
                                position = Position.TOP
                            )
                        }
                        in DragAndDropConfig.middleRange -> {
                            vm.onDragAndDrop(
                                dragged = blockAdapter.views[dndTargetPos].id,
                                target = blockAdapter.views[vh.bindingAdapterPosition].id,
                                position = Position.INNER
                            )
                        }
                        in DragAndDropConfig.bottomRange -> {
                            try {
                                vm.onDragAndDrop(
                                    dragged = blockAdapter.views[dndTargetPos].id,
                                    target = blockAdapter.views[vh.bindingAdapterPosition].id,
                                    position = Position.BOTTOM
                                )
                            } catch (e: Exception) {
                                toast("Failed to drop. Please, try again later.")
                            }
                        }
                        else -> toast("drop skipped, scenario 1")
                    }
                } else {
                    when (ratio) {
                        in DragAndDropConfig.topHalfRange -> {
                            vm.onDragAndDrop(
                                dragged = blockAdapter.views[dndTargetPos].id,
                                target = blockAdapter.views[vh.bindingAdapterPosition].id,
                                position = Position.TOP
                            )
                        }
                        in DragAndDropConfig.bottomHalfRange -> {
                            vm.onDragAndDrop(
                                dragged = blockAdapter.views[dndTargetPos].id,
                                target = blockAdapter.views[vh.bindingAdapterPosition].id,
                                position = Position.BOTTOM
                            )
                        }
                        else -> toast("drop skipped, scenario 2")
                    }
                }
            }
        } else {
            toast("view holder not found")
        }
    }

    private fun stopScrollDownJob() {
        scrollDownJob?.cancel()
        scrollDownJob = null
    }

    private fun stopScrollUpJob() {
        scrollUpJob?.cancel()
        scrollUpJob = null
    }

    //endregion

    //region READ PERMISSION
    private fun takeReadStoragePermission() {
        if (requireActivity().shouldShowRequestPermissionRationaleCompat(READ_EXTERNAL_STORAGE)) {
            binding.root.showSnackbar(
                R.string.permission_read_rationale,
                Snackbar.LENGTH_INDEFINITE,
                R.string.button_ok
            ) {
                permissionReadStorage.launch(arrayOf(READ_EXTERNAL_STORAGE))
            }
        } else {
            permissionReadStorage.launch(arrayOf(READ_EXTERNAL_STORAGE))
        }
    }

    private val permissionReadStorage =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantResults ->
            val readResult = grantResults[READ_EXTERNAL_STORAGE]
            if (readResult == true) {
                startFilePicker(mMimeType)
            } else {
                binding.root.showSnackbar(R.string.permission_read_denied, Snackbar.LENGTH_SHORT)
            }
        }
    //endregion

    //region UPLOAD FILE LOGIC
    private var mMimeType = ""
    private var mSnackbar: Snackbar? = null

    private fun openFilePicker(mimeType: String) {
        mMimeType = mimeType
        if (requireContext().isPermissionGranted(mimeType)) {
            startFilePicker(mimeType)
        } else {
            takeReadStoragePermission()
        }
    }

    private fun onCopyFileCommand(command: CopyFileStatus) {
        when (command) {
            is CopyFileStatus.Error -> {
                mSnackbar?.dismiss()
                activity?.toast("Error while loading file:${command.msg}")
            }
            is CopyFileStatus.Completed -> {
                mSnackbar?.dismiss()
                onFilePathReady(command.result)
            }
            CopyFileStatus.Started -> {
                mSnackbar = binding.root.showSnackbar(
                    R.string.loading_file,
                    Snackbar.LENGTH_INDEFINITE,
                    R.string.cancel
                ) {
                    vm.onCancelCopyFileToCacheDir()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_MEDIA_CODE -> {
                    data?.data?.let { uri ->
                        pickiT.getPath(uri, Build.VERSION.SDK_INT)
                    }
                }
                REQUEST_FILE_SAF_CODE -> {
                    data?.data?.let { uri ->
                        vm.onStartCopyFileToCacheDir(uri)
                    } ?: run {
                        Timber.e("onActivityResult error, data is null")
                        toast("Error while getting file")
                    }
                }
                else -> {
                    Timber.e("onActivityResult error, Unknown Request Code:$requestCode")
                    toast("Unknown Request Code:$requestCode")
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun clearOnCopyFile() {
        vm.onCancelCopyFileToCacheDir()
        mSnackbar?.dismiss()
        mSnackbar = null
    }
    //endregion

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
        const val DEFAULT_TOOLBAR_ANIM_DURATION = 150L

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
    }
}

interface OnFragmentInteractionListener {
    fun onAddMarkupLinkClicked(blockId: String, link: String, range: IntRange)
    fun onRemoveMarkupLinkClicked(blockId: String, range: IntRange)
    fun onAddBookmarkUrlClicked(target: String, url: String)
    fun onExitToDesktopClicked()
    fun onSetRelationKeyClicked(blockId: Id, key: Id)
    fun onSetObjectLink(id: Id)
    fun onSetWebLink(uri: String)
    fun onCreateObject(name: String)
}