package com.anytypeio.anytype.ui.sets

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.EditorInfo.IME_ACTION_GO
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.widget.AppCompatEditText
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.TimeInMillis
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.extensions.setEmojiOrNull
import com.anytypeio.anytype.core_ui.features.dataview.ViewerGridAdapter
import com.anytypeio.anytype.core_ui.features.dataview.ViewerGridHeaderAdapter
import com.anytypeio.anytype.core_ui.features.sets.SetObjectNameBottomSheet
import com.anytypeio.anytype.core_ui.menu.BackHistoryMenu
import com.anytypeio.anytype.core_ui.menu.ObjectHeaderContextMenu
import com.anytypeio.anytype.core_ui.menu.ObjectSetRelationPopupMenu
import com.anytypeio.anytype.core_ui.menu.ObjectSetTypePopupMenu
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_ui.reactive.editorActionEvents
import com.anytypeio.anytype.core_ui.reactive.longClicks
import com.anytypeio.anytype.core_ui.syncstatus.SpaceSyncStatusScreen
import com.anytypeio.anytype.core_ui.tools.DefaultTextWatcher
import com.anytypeio.anytype.core_ui.views.ButtonPrimarySmallIcon
import com.anytypeio.anytype.core_ui.widgets.CircularFabButton
import com.anytypeio.anytype.core_ui.widgets.FeaturedRelationGroupWidget
import com.anytypeio.anytype.core_ui.widgets.TypeTemplatesWidget
import com.anytypeio.anytype.core_ui.widgets.dv.ObjectSetTitle
import com.anytypeio.anytype.core_ui.widgets.dv.ViewerEditWidget
import com.anytypeio.anytype.core_ui.widgets.dv.ViewerLayoutWidget
import com.anytypeio.anytype.core_ui.widgets.dv.ViewersWidget
import com.anytypeio.anytype.core_ui.widgets.dv.scroll.MotionOrigin
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_ui.widgets.toolbar.DataViewInfo
import com.anytypeio.anytype.core_utils.clipboard.copyPlainTextToClipboard
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.drawable
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.hideKeyboard
import com.anytypeio.anytype.core_utils.ext.hideSoftInput
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.safeNavigate
import com.anytypeio.anytype.core_utils.ext.startMarketPageOrWeb
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.syncFocusWithImeVisibility
import com.anytypeio.anytype.core_utils.ext.syncTranslationWithImeVisibility
import com.anytypeio.anytype.core_utils.ext.throttleFirst
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.intents.ActivityCustomTabsHelper
import com.anytypeio.anytype.databinding.FragmentObjectSetBinding
import com.anytypeio.anytype.feature_create_object.presentation.CreateObjectViewModelFactory
import com.anytypeio.anytype.feature_create_object.presentation.NewCreateObjectViewModel
import com.anytypeio.anytype.feature_create_object.ui.CreateObjectSheetHost
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.di.feature.DefaultComponentParam
import com.anytypeio.anytype.presentation.editor.cover.CoverColor
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType.Relation.SetQuery
import com.anytypeio.anytype.presentation.navigation.NavPanelState
import com.anytypeio.anytype.presentation.relations.value.tagstatus.RelationContext
import com.anytypeio.anytype.presentation.sets.DataViewViewState
import com.anytypeio.anytype.presentation.sets.ObjectSetCommand
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModelFactory
import com.anytypeio.anytype.presentation.sets.SetOrCollectionHeaderState
import com.anytypeio.anytype.presentation.sets.ViewEditAction
import com.anytypeio.anytype.presentation.sets.ViewerLayoutWidgetUi
import com.anytypeio.anytype.presentation.sets.ViewersWidgetUi
import com.anytypeio.anytype.presentation.sets.isVisible
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.ui.base.NavigationFragment
import com.anytypeio.anytype.ui.media.MediaActivity
import com.anytypeio.anytype.ui.editor.cover.SelectCoverObjectSetFragment
import com.anytypeio.anytype.ui.editor.modals.IconPickerFragmentBase
import com.anytypeio.anytype.ui.editor.sheets.ObjectMenuBaseFragment
import com.anytypeio.anytype.presentation.main.MainViewModel
import com.anytypeio.anytype.ui.home.WidgetOverlayFragment
import com.anytypeio.anytype.ui.home.routeUploadSnackbar
import com.anytypeio.anytype.ui.objects.BaseObjectTypeChangeFragment
import com.anytypeio.anytype.ui.objects.creation.ObjectTypeSelectionFragment
import com.anytypeio.anytype.ui.objects.types.pickers.CollectionAddObjectTypeFragment
import com.anytypeio.anytype.ui.objects.types.pickers.CollectionObjectTypeSelectionListener
import com.anytypeio.anytype.ui.objects.types.pickers.DataViewSelectSourceFragment
import com.anytypeio.anytype.ui.objects.types.pickers.EmptyDataViewSelectSourceFragment
import com.anytypeio.anytype.ui.objects.types.pickers.ObjectTypeSelectionListener
import com.anytypeio.anytype.ui.objects.types.pickers.OnDataViewSelectSourceAction
import com.anytypeio.anytype.ui.relations.RelationDateValueFragment
import com.anytypeio.anytype.ui.relations.RelationDateValueFragment.DateValueEditReceiver
import com.anytypeio.anytype.ui.relations.RelationTextValueFragment
import com.anytypeio.anytype.ui.relations.RelationTextValueFragment.TextValueEditReceiver
import com.anytypeio.anytype.ui.relations.value.ObjectValueFragment
import com.anytypeio.anytype.ui.relations.value.TagOrStatusValueFragment
import com.anytypeio.anytype.ui.sets.modals.ObjectSetSettingsFragment
import com.anytypeio.anytype.ui.sets.modals.SetObjectCreateBookmarkRecordFragment
import com.anytypeio.anytype.ui.sets.modals.SetObjectCreateRecordFragmentBase
import com.anytypeio.anytype.ui.sets.modals.sort.ViewerSortFragment
import com.anytypeio.anytype.ui.templates.EditorTemplateFragment.Companion.ARG_TARGET_TYPE_ID
import com.anytypeio.anytype.ui.templates.EditorTemplateFragment.Companion.ARG_TARGET_TYPE_KEY
import com.anytypeio.anytype.ui.templates.EditorTemplateFragment.Companion.ARG_TEMPLATE_ID
import javax.inject.Inject
import java.util.WeakHashMap
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

open class ObjectSetFragment :
    NavigationFragment<FragmentObjectSetBinding>(R.layout.fragment_object_set),
    TextValueEditReceiver,
    DateValueEditReceiver,
    OnDataViewSelectSourceAction,
    ObjectTypeSelectionListener,
    CollectionObjectTypeSelectionListener {

    private lateinit var createObjectFactory: CreateObjectViewModelFactory

    private val createObjectVm by viewModels<NewCreateObjectViewModel> { createObjectFactory }

    private val mainVm: MainViewModel by activityViewModels()

    private fun createObjectComponentKey(): String = "object-set-create-object:$ctx"

    // Owned outside the ComposeView's setContent so the first tap isn't lost
    // while composition is still pending.
    private val createObjectSheetVisible = androidx.compose.runtime.mutableStateOf(false)
    private var isSheetHostInstalled = false

    // UI-only, scoped to this object. Never retain renderers or their animation state.
    private val viewerAnchors = linkedMapOf<Id, Bundle>()
    private var activeViewer: Viewer? = null
    private var pendingAnchor: Bundle? = null
    private var pendingAnchorInputGeneration = 0L
    private var pendingReveal: Id? = null
    private var pendingRevealInputGeneration = 0L
    private var pendingPositionPreDraw: ViewTreeObserver.OnPreDrawListener? = null
    private var submissionGeneration = 0L
    private var committedViewerId: Id? = null
    private var retainedHeaderProgress = 0f
    private var retainedBoardState: Bundle? = null
    private var retainedViewerId: Id? = null
    private var retainedPageIndex = 0
    private var activeViewerPageIndex = 0
    private var pageToRestore: Int? = null
    private var lastContentSelection: Pair<Id?, Long>? = null
    private var hasCover = false
    private var headerReadOnly = false
    private var headerEditing = false
    private var headerImeVisible: Boolean? = null
    private var headerImeLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    private var headerImeObservation: Runnable? = null
    private var headerImeObservationPosted = false
    private var titleInReadMode: Boolean? = null
    private var renderedCoverButtons: Boolean? = null
    private var renderedAccessibilityActions = -1
    private val headerAccessibility = WeakHashMap<View, Int>()
    private val headerAccessibilityBounds = Rect()
    private val embedded: Boolean get() = arguments?.getBoolean(EMBEDDED_KEY) == true

    // Controls

    private val title: TextInputWidget
        get() = binding.objectHeader.root.findViewById(R.id.tvSetTitle)

    private val tvDescription: AppCompatEditText
        get() = binding.objectHeader.root.findViewById(R.id.tvSetDescription)

    private val tvDescriptionTextWatcher by lazy {
        DefaultTextWatcher {
            vm.onDescriptionChanged(it.toString())
        }
    }

    private val header: LinearLayout
        get() = binding.objectHeader.root

    private val topBackButton: View
        get() = binding.topToolbar.back

    private val topToolbar: ViewGroup
        get() = binding.topToolbar

    private val topToolbarTitle: TextView
        get() = binding.topToolbar.title

    private val topToolbarThreeDotsButton: ViewGroup
        get() = binding.topToolbar.menu as ViewGroup

    private val topToolbarStatusContainer: View
        get() = binding.topToolbar.status

    private val topToolbarThreeDotsIcon: ImageView
        get() = binding.topToolbar.menu.findViewById(R.id.ivThreeDots)

    private val addNewButton: TextView
        get() = binding.dataViewHeader.addNewButton

    private val addNewIconButton: ButtonPrimarySmallIcon
        get() = binding.dataViewHeader.addNewIconButton

    private val customizeViewButton: ImageView
        get() = binding.dataViewHeader.customizeViewButton

    private val menuButton: FrameLayout
        get() = binding.topToolbar.menu as FrameLayout

    private val featuredRelations: FeaturedRelationGroupWidget
        get() = binding.objectHeader.root.findViewById(R.id.featuredRelationsWidget)

    private val dataViewHeader: ConstraintLayout
        get() = binding.dataViewHeader.root

    private val viewerTitle: TextView
        get() = binding.dataViewHeader.root.findViewById(R.id.tvCurrentViewerName)

    private val initView: View get() = binding.initState.root
    private val dataViewInfo: DataViewInfo get() = binding.dataViewInfo
    private val rvHeaders: RecyclerView get() = binding.root.findViewById(R.id.rvHeader)
    private val rvRows: RecyclerView get() = binding.root.findViewById(R.id.rvRows)

    private val actionHandler: (Int) -> Boolean = { action ->
        action == IME_ACTION_GO || action == IME_ACTION_DONE
    }

    private val viewerGridHeaderAdapter by lazy { ViewerGridHeaderAdapter() }

    private var contextMenuAnchorView: View? = null

    private val viewerGridAdapter by lazy {
        ViewerGridAdapter(
            onCellClicked = vm::onGridCellClicked,
            onObjectHeaderClicked = vm::onObjectHeaderClicked,
            onObjectHeaderLongClicked = { id, view ->
                contextMenuAnchorView = view
                vm.onObjectHeaderLongClicked(id)
            },
            onTaskCheckboxClicked = vm::onTaskCheckboxClicked
        )
    }

    private val ctx: Id get() = argString(CONTEXT_ID_KEY)
    private val space: Id get() = arg<String>(SPACE_ID_KEY)
    private val view: Id? get() = argOrNull<Id>(INITIAL_VIEW_ID_KEY)

    lateinit var titleTextWatcher: DefaultTextWatcher

    @Inject
    lateinit var factory: ObjectSetViewModelFactory
    private val vm: ObjectSetViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupOnBackPressedDispatcher()
        titleTextWatcher = DefaultTextWatcher { vm.onTitleChanged(it.toString()) }
        savedInstanceState?.getBundle(SCROLL_STATE_KEY)?.let { state ->
            if (state.getString("object") == ctx && state.getString("space") == space) {
                retainedHeaderProgress = state.getFloat("header")
                retainedBoardState = state.getBundle("board")
                retainedViewerId = state.getString("activeViewer")
                retainedPageIndex = state.getInt("page").coerceAtLeast(0)
                pageToRestore = retainedPageIndex
                state.getBundle("viewers")?.let { viewers ->
                    viewers.keySet().take(MAX_RETAINED_VIEWERS).forEach { id ->
                        viewers.getBundle(id)?.let { viewerAnchors[id] = it }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideSoftInput()
        setupWindowInsetAnimation()

        setupGridAdapters()

        // DROID-4318: Bottom buttons (Search + Create) stay fixed across
        // scrolling. Visibility is driven by view-state, not by scroll.
        binding.fabCreate.isVisible = true

        title.clearFocus()
        setupScrollHost()

        addNewButton.setOnClickListener { vm.proceedWithDataViewObjectCreate() }
        addNewIconButton.setOnButtonClickListener { vm.proceedWithDataViewObjectCreate() }
        addNewIconButton.setOnIconClickListener { vm.onNewButtonIconClicked() }

        with(lifecycleScope) {
            subscribe(dataViewInfo.clicks().throttleFirst()) { type ->
                when (type) {
                    DataViewInfo.TYPE.COLLECTION_NO_ITEMS -> vm.proceedWithDataViewObjectCreate()
                    DataViewInfo.TYPE.SET_NO_QUERY -> vm.onSelectQueryButtonClicked()
                    DataViewInfo.TYPE.SET_NO_ITEMS -> vm.proceedWithDataViewObjectCreate()
                    DataViewInfo.TYPE.BOARD_NO_GROUP_BY -> {}
                    DataViewInfo.TYPE.INIT -> {}
                }
            }
            subscribe(title.editorActionEvents(actionHandler)) {
                title.hideKeyboard()
                finishHeaderEditing()
            }
            subscribe(topBackButton.clicks().throttleFirst()) { vm.onBackButtonClicked() }
            topBackButton.setOnLongClickListener {
                vm.onBackButtonLongClicked()
                true
            }
            binding.backHistoryMenu.apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    val state by vm.backHistoryMenu.collectAsStateWithLifecycle()
                    BackHistoryMenu(
                        state = state,
                        onChannelsClicked = { vm.onBackHistoryChannelsClicked() },
                        onHomeClicked = { vm.onBackHistoryHomeClicked() },
                        onItemClicked = { vm.onBackHistoryItemClicked(it) },
                        onDismiss = { vm.onBackHistoryMenuDismissed() }
                    )
                }
            }
            // Board (Kanban) viewer is hosted persistently: its composition is set up once
            // here and fed view-state via setBoard(...) in setViewer, so emissions diff
            // instead of tearing down the composition (which would reset scroll / cancel a
            // drag). See BoardViewWidget.
            binding.boardView.apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                onCardClick = { id -> vm.onObjectHeaderClicked(id) }
                onCardMoved = { cardId, sourceColumnId, targetColumnId, targetOrderedIds ->
                    vm.onBoardCardDropped(cardId, sourceColumnId, targetColumnId, targetOrderedIds)
                }
                onCardReordered = { columnId, orderedCardIds ->
                    vm.onBoardCardReordered(columnId, orderedCardIds)
                }
                onColumnLoadMore = { columnId -> vm.onBoardColumnLoadMore(columnId) }
                onCreateInColumn = { columnId -> vm.onBoardCreateObjectInColumn(columnId) }
            }
            binding.topToolbar.container.setOnClickListener {
                WidgetOverlayFragment.show(parentFragmentManager, space)
            }
            subscribe(menuButton.clicks().throttleFirst()) { vm.onMenuClicked() }
            subscribe(customizeViewButton.clicks().throttleFirst()) { vm.onViewerCustomizeButtonClicked() }
            subscribe(viewerTitle.clicks().throttleFirst()) { vm.onExpandViewerMenuClicked() }
            subscribe(binding.unsupportedViewError.clicks().throttleFirst()) { vm.onUnsupportedViewErrorClicked() }

            // DROID-4508: Two Compose bottom-bar buttons — Search (left) +
            // Create (right) — mirror the editor's look (CircularFabButton,
            // background_secondary, subtle shadow, no border). Buttons stay
            // fixed; visibility comes from VM state, not scroll.
            binding.fabSearchOnPage.apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    Box(modifier = Modifier.padding(8.dp)) {
                        CircularFabButton(
                            iconRes = R.drawable.ic_doc_search,
                            contentDescription = stringResource(id = R.string.content_desc_search_button),
                            backgroundColor = colorResource(id = R.color.background_secondary),
                            elevation = 2.dp,
                            showBorder = false,
                            iconSize = 24.dp,
                            onClick = { vm.onSearchButtonClicked() }
                        )
                    }
                }
            }

            binding.fabCreate.apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    val navState by vm.navPanelState.collectAsStateWithLifecycle(
                        initialValue = NavPanelState.Init
                    )
                    val isCreateEnabled =
                        (navState as? NavPanelState.Default)?.isCreateEnabled == true
                    Box(modifier = Modifier.padding(8.dp)) {
                        CircularFabButton(
                            iconRes = R.drawable.ic_create_obj_32,
                            contentDescription = stringResource(
                                id = R.string.main_navigation_content_desc_create_button
                            ),
                            backgroundColor = colorResource(id = R.color.background_secondary),
                            elevation = 2.dp,
                            showBorder = false,
                            iconSize = 24.dp,
                            isEnabled = isCreateEnabled,
                            onClick = { showCreateObjectSheet() }
                        )
                    }
                }
            }

            binding.fabSearchOnPage.isVisible = true
        }

        with(binding.paginatorToolbar) {
            onNumberClickCallback = { (num, isSelected) ->
                vm.onPaginatorToolbarNumberClicked(num, isSelected)
            }
            onNext = { vm.onPaginatorNextElsePrevious(true) }
            onPrevious = { vm.onPaginatorNextElsePrevious(false) }
        }

        binding.galleryView.onGalleryItemClicked = { id ->
            vm.onObjectHeaderClicked(id)
        }

        binding.galleryView.onGalleryItemLongClicked = { id, view ->
            contextMenuAnchorView = view
            vm.onObjectHeaderLongClicked(id)
        }

        binding.galleryView.onTaskCheckboxClicked = { id ->
            vm.onTaskCheckboxClicked(id)
        }

        binding.listView.onListItemClicked = { id ->
            vm.onObjectHeaderClicked(id)
        }

        binding.listView.onListItemLongClicked = { id, view ->
            contextMenuAnchorView = view
            vm.onObjectHeaderLongClicked(id)
        }

        binding.listView.onTaskCheckboxClicked = { id ->
            vm.onTaskCheckboxClicked(id)
        }

        title.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            vm.onTitleFocusChanged(hasFocus)
            updateHeaderEditing()
        }

        with(tvDescription) {
            syncFocusWithImeVisibility()
            addTextChangedListener(tvDescriptionTextWatcher)
            imeOptions = IME_ACTION_DONE
            setRawInputType(InputType.TYPE_CLASS_TEXT)
            onFocusChangeListener = View.OnFocusChangeListener { _, _ -> updateHeaderEditing() }
            setOnEditorActionListener { _, action, _ ->
                if (action != IME_ACTION_DONE) false else {
                    hideKeyboard()
                    finishHeaderEditing()
                    true
                }
            }
        }
        setupHeaderImeObservation()

        setFragmentResultListener(BaseObjectTypeChangeFragment.OBJECT_TYPE_REQUEST_KEY) { _, bundle ->
            val query = bundle.getString(BaseObjectTypeChangeFragment.OBJECT_TYPE_URL_KEY)
            if (query != null) {
                vm.onObjectSetQueryPicked(query = query)
            } else {
                toast("Error while setting the Set query. The query is empty")
            }
        }

        binding.templatesWidget.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                TypeTemplatesWidget(
                    state = vm.typeTemplatesWidgetState.collectAsStateWithLifecycle().value,
                    onDismiss = vm::onDismissTemplatesWidget,
                    editClick = vm::onEditTemplateButtonClicked,
                    doneClick = vm::onDoneTemplateButtonClicked,
                    moreClick = vm::onMoreTemplateButtonClicked,
                    menuClick = vm::onMoreMenuClicked,
                    action = vm::onTypeTemplatesWidgetAction,
                    scope = lifecycleScope
                )
            }
        }

        observeSelectingTemplate()

        binding.viewersWidget.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ViewersWidget(
                    state = vm.viewersWidgetState.collectAsStateWithLifecycle().value,
                    action = ::onViewersWidgetAction,
                )
            }
        }

        binding.viewerEditWidget.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ViewerEditWidget(
                    state = vm.viewerEditWidgetState.collectAsStateWithLifecycle().value,
                    action = vm::onViewerEditWidgetAction,
                )
            }
        }

        binding.viewerLayoutWidget.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ViewerLayoutWidget(
                    uiState = vm.viewerLayoutWidgetState.collectAsStateWithLifecycle().value,
                    action = vm::onViewerLayoutWidgetAction,
                )
            }
        }

        binding.titleWidget.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ObjectSetTitle(
                    isVisible = vm.isTitleToolbarVisible.collectAsStateWithLifecycle().value,
                    doneAction = {
                        hideKeyboard()
                        finishHeaderEditing()
                    }
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

        binding.setObjectNameSheet.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val state = vm.setObjectNameState.collectAsStateWithLifecycle().value

                SetObjectNameBottomSheet(
                    isVisible = state.isVisible,
                    icon = state.currentIcon,
                    isIconChangeAllowed = state.isIconChangeAllowed,
                    initialText = state.inputText,
                    onTextChanged = vm::onSetObjectNameChanged,
                    onDismiss = vm::onSetObjectNameDismissed,
                    onIconClicked = vm::onSetObjectNameIconClicked,
                    onOpenClicked = vm::onSetObjectNameOpenClicked
                )
            }
        }
    }

    private fun setupWindowInsetAnimation() {
        // DROID-4318: `bottomToolbarBox` removed in favour of the two
        // floating action buttons. The object-set screen has no inline IME
        // text input near the FABs, so we don't sync their translation to
        // the keyboard — they're allowed to be covered if an IME appears.
        title.syncFocusWithImeVisibility()
        binding.viewerEditWidget.syncTranslationWithImeVisibility(
            dispatchMode = DISPATCH_MODE_STOP
        )
        binding.templatesWidget.syncTranslationWithImeVisibility(
            dispatchMode = DISPATCH_MODE_STOP
        )
        binding.titleWidget.syncTranslationWithImeVisibility(
            dispatchMode = DISPATCH_MODE_STOP
        )
        binding.setObjectNameSheet.syncTranslationWithImeVisibility(
            dispatchMode = DISPATCH_MODE_STOP
        )
    }

    override fun onApplyWindowRootInsets() {
        // The embedded type page already applies Scaffold/system-bar insets.
        if (!embedded) super.onApplyWindowRootInsets()
    }

    private fun setupHeaderImeObservation() {
        if (embedded) return
        val root = binding.root
        headerImeObservation = Runnable {
            if (!hasBinding || binding.root !== root) return@Runnable
            headerImeObservationPosted = false
            if (!headerEditing) return@Runnable
            // On API <30 visible bounds can change after inset dispatch, and an ancestor
            // may consume the dispatch. Sample fresh root insets after layout instead.
            val visible = ViewCompat.getRootWindowInsets(root)
                ?.isVisible(WindowInsetsCompat.Type.ime()) ?: return@Runnable
            val dismissed = headerImeVisible == true && !visible
            headerImeVisible = visible
            if (dismissed) finishHeaderEditing()
        }
        headerImeLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            scheduleHeaderImeObservation()
        }.also(root.viewTreeObserver::addOnGlobalLayoutListener)
    }

    private fun scheduleHeaderImeObservation() {
        if (!headerEditing || headerImeObservationPosted) return
        val observation = headerImeObservation ?: return
        headerImeObservationPosted = true
        binding.root.post(observation)
    }

    private fun setupScrollHost() {
        val host = binding.scrollHost
        topToolbar.setBackgroundColor(requireContext().getColor(R.color.background_primary))
        host.embeddedMode = embedded
        host.excludeNestedScrollTarget = binding.boardView
        host.setStableViewportChild(binding.boardView, binding.boardView::setViewportTopInset)
        host.eligibleNestedScrollTarget = { target ->
            target === rvRows || target === binding.listView || target === binding.galleryView
        }
        host.onGeometryChanging = { binding.boardView.cancelDrag() }
        host.onHeaderChanged = ::renderHeaderScrollState
        binding.boardView.scrollCoordinator = if (embedded) null else host.coordinator
        retainedBoardState?.let(binding.boardView::restoreScrollState)
        host.coordinator.restoreProgress(if (embedded) 0f else retainedHeaderProgress)
        topToolbar.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            if (hasBinding && binding.scrollHost === host) {
                host.pinHeight = if (host.embeddedMode || !topToolbar.isVisible) 0
                    else toolbarPinHeight()
            }
        }
        host.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            if (hasBinding && binding.scrollHost === host) renderHeaderScrollState()
        }
        listOf(rvRows, binding.listView, binding.galleryView).forEach { recycler ->
            // Stable object anchors below replace RecyclerView's index-only hierarchy state.
            recycler.isSaveEnabled = false
        }
        rvRows.updatePadding(bottom = (80 * resources.displayMetrics.density).toInt())
        rvRows.clipToPadding = false
        applyEmbeddedMode()
        renderHeaderScrollState()
    }

    private fun applyEmbeddedMode() {
        val localHeaderHidden = embedded || vm.currentViewer.value is DataViewViewState.TypeSet
        binding.scrollHost.embeddedMode = localHeaderHidden
        binding.boardView.scrollCoordinator = if (localHeaderHidden) null else binding.scrollHost.coordinator
        if (localHeaderHidden) {
            header.gone()
            topToolbar.gone()
        }
        binding.scrollHost.pinHeight = if (localHeaderHidden || !topToolbar.isVisible) 0
            else toolbarPinHeight()
    }

    private fun toolbarPinHeight(): Int = if (topToolbar.isLaidOut)
        (topToolbar.bottom - binding.scrollHost.top).coerceAtLeast(0)
    else topToolbar.layoutParams.height.coerceAtLeast(0)

    private fun updateHeaderEditing() {
        val editing = !headerReadOnly && (title.hasFocus() || tvDescription.hasFocus())
        val coordinator = binding.scrollHost.coordinator
        if (editing && !headerEditing) {
            binding.boardView.cancelDrag()
            coordinator.setExpanded(true)
        }
        headerEditing = editing
        if (!editing) headerImeVisible = null
        coordinator.setBlocked(MotionOrigin.Editing, editing)
        renderHeaderScrollState()
        scheduleHeaderImeObservation()
    }

    private fun finishHeaderEditing() {
        binding.root.requestFocus()
        title.clearFocus()
        tvDescription.clearFocus()
        vm.hideTitleToolbar()
        updateHeaderEditing()
    }

    private fun renderHeaderScrollState() {
        val host = binding.scrollHost
        val coordinator = host.coordinator
        val collapsed = coordinator.range > 0f && coordinator.offset >= coordinator.range
        val progress = coordinator.progress
        topToolbar.background?.alpha = (progress * DRAWABLE_ALPHA_FULL).toInt()
        val coverButtons = hasCover && !collapsed
        if (renderedCoverButtons != coverButtons) {
            renderedCoverButtons = coverButtons
            topToolbarThreeDotsButton.setBackgroundResource(
                if (coverButtons) R.drawable.rect_object_menu_button_default else R.drawable.bg_nav_circular_button
            )
            topToolbarStatusContainer.setBackgroundResource(
                if (coverButtons) R.drawable.rect_object_menu_button_default else 0
            )
            topToolbarThreeDotsIcon.imageTintList = if (coverButtons)
                ColorStateList.valueOf(Color.WHITE) else null
        }
        topToolbarThreeDotsButton.background?.alpha = if (coverButtons)
            ((1f - progress) * DRAWABLE_ALPHA_FULL).toInt() else DRAWABLE_ALPHA_FULL
        topToolbarStatusContainer.background?.alpha = ((1f - progress) * DRAWABLE_ALPHA_FULL).toInt()
        val readMode = headerReadOnly || collapsed
        if (titleInReadMode != readMode) {
            titleInReadMode = readMode
            title.pauseTextWatchers {
                if (readMode) title.enableReadMode() else title.enableEditMode()
            }
        }
        title.isEnabled = !headerReadOnly
        tvDescription.isEnabled = !headerReadOnly
        topToolbarTitle.importantForAccessibility = if (collapsed)
            View.IMPORTANT_FOR_ACCESSIBILITY_AUTO else View.IMPORTANT_FOR_ACCESSIBILITY_NO
        header.importantForAccessibility = if (collapsed || host.embeddedMode)
            View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS else View.IMPORTANT_FOR_ACCESSIBILITY_AUTO
        updateHeaderAccessibility(header)
        val expand = AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_EXPAND
        val collapse = AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_COLLAPSE
        val actions = if (host.embeddedMode || coordinator.range == 0f || coordinator.isBlocked) 0
            else (if (coordinator.offset > 0f) 1 else 0) or
                (if (coordinator.offset < coordinator.range) 2 else 0)
        if (renderedAccessibilityActions == actions) return
        renderedAccessibilityActions = actions
        ViewCompat.removeAccessibilityAction(topToolbar, expand.id)
        ViewCompat.removeAccessibilityAction(topToolbar, collapse.id)
        if (actions != 0) {
            if (actions and 1 != 0) {
                ViewCompat.replaceAccessibilityAction(topToolbar, expand,
                    getString(R.string.dataview_expand_header)) { _, _ ->
                    binding.boardView.cancelDrag()
                    coordinator.setExpanded(true)
                    true
                }
            }
            if (actions and 2 != 0) {
                ViewCompat.replaceAccessibilityAction(topToolbar, collapse,
                    getString(R.string.dataview_collapse_header)) { _, _ ->
                    binding.boardView.cancelDrag()
                    coordinator.setExpanded(false)
                    true
                }
            }
        }
    }

    private fun updateHeaderAccessibility(parent: ViewGroup) {
        parent.children.forEach { child ->
            val original = headerAccessibility.getOrPut(child) { child.importantForAccessibility }
            child.getDrawingRect(headerAccessibilityBounds)
            header.offsetDescendantRectToMyCoords(child, headerAccessibilityBounds)
            val occluded = child.height > 0 &&
                headerAccessibilityBounds.bottom + header.top <= binding.scrollHost.pinHeight
            child.importantForAccessibility = if (occluded)
                View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS else original
            // A partially visible featured group can still contain fully hidden actions.
            if (child is ViewGroup) updateHeaderAccessibility(child)
        }
    }

    private fun setupGridAdapters() {

        val horizontalDivider = drawable(R.drawable.divider_dv_horizontal)
        val verticalDivider = drawable(R.drawable.divider_dv_grid)

        rvHeaders.apply {
            adapter = viewerGridHeaderAdapter
            addItemDecoration(
                DividerItemDecoration(
                    context,
                    DividerItemDecoration.HORIZONTAL
                ).apply {
                    setDrawable(horizontalDivider)
                }
            )
        }

        rvRows.apply {
            adapter = viewerGridAdapter
            addItemDecoration(
                DividerItemDecoration(
                    context,
                    DividerItemDecoration.VERTICAL
                ).apply {
                    setDrawable(verticalDivider)
                }
            )
        }

        binding.gridContainer.root.setOnScrollChangeListener { _, scrollX, _, _, _ ->
            val translationX = scrollX.toFloat()
            viewerGridAdapter.recordNamePositionX = translationX
            rvRows.children.forEach { child ->
                // Use the holder's cached binding instead of findViewById — this runs
                // for every visible row on every scroll frame.
                val holder = rvRows.getChildViewHolder(child)
                if (holder is ViewerGridAdapter.RecordHolder) {
                    holder.binding.headerContainer.translationX = translationX
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm.navigation.observe(viewLifecycleOwner, navObserver)
    }

    private fun setStatus(status: SpaceSyncAndP2PStatusState?) {
        binding.topToolbar.status.bind(status)
        topToolbarStatusContainer.setOnClickListener {
            vm.onSyncStatusBadgeClicked()
        }
    }

    private fun setupDataViewViewState(state: DataViewViewState, pageIndex: Int) {
        @Suppress("SENSELESS_COMPARISON")
        if (state == null) {
            Timber.w("ObjectSetFragment: vm.currentViewer emitted null")
            return
        }
        when (state) {
            is DataViewViewState.Collection.NoView -> {
                topToolbarThreeDotsButton.visible()
                topToolbarStatusContainer.visible()
                initView.gone()
                header.visible()
                dataViewHeader.gone()
                dataViewInfo.hide()
                toast(getString(R.string.set_collection_view_not_present))
                setViewer(viewer = null)
                if (state.isEditingViewAllowed) {
                    customizeViewButton.visible()
                } else {
                    customizeViewButton.invisible()
                }
                setupNewButtons(state.isCreateObjectAllowed)

            }
            is DataViewViewState.Set.NoView -> {
                topToolbarThreeDotsButton.visible()
                topToolbarStatusContainer.visible()
                initView.gone()
                header.visible()
                dataViewHeader.gone()
                dataViewInfo.hide()
                toast(getString(R.string.set_collection_view_not_present))
                setViewer(viewer = null)
                if (state.isEditingViewAllowed) {
                    customizeViewButton.visible()
                } else {
                    customizeViewButton.invisible()
                }
                setupNewButtons(state.isCreateObjectAllowed)
            }
            is DataViewViewState.Collection.NoItems -> {
                topToolbarThreeDotsButton.visible()
                topToolbarStatusContainer.visible()
                initView.gone()
                header.visible()
                dataViewHeader.visible()
                viewerTitle.isEnabled = true
                customizeViewButton.isEnabled = true
                if (state.isEditingViewAllowed) {
                    customizeViewButton.visible()
                } else {
                    customizeViewButton.invisible()
                }
                setupNewButtons(state.isCreateObjectAllowed)
                setCurrentViewerName(state.title)
                dataViewInfo.show(
                    type = if (state.isBoardGroupByRequired) {
                        DataViewInfo.TYPE.BOARD_NO_GROUP_BY
                    } else {
                        DataViewInfo.TYPE.COLLECTION_NO_ITEMS
                    },
                    // The group-by hint points at view settings, so it is gated by view-edit
                    // permission; the "no items" hint is gated by object creation.
                    isReadOnlyAccess = if (state.isBoardGroupByRequired) {
                        !state.isEditingViewAllowed
                    } else {
                        !state.isCreateObjectAllowed
                    }
                )
                setViewer(viewer = null)
            }
            is DataViewViewState.Collection.Default -> {
                topToolbarThreeDotsButton.visible()
                topToolbarStatusContainer.visible()
                header.visible()
                initView.gone()
                dataViewHeader.visible()
                viewerTitle.isEnabled = true
                customizeViewButton.isEnabled = true
                if (state.isEditingViewAllowed) {
                    customizeViewButton.visible()
                } else {
                    customizeViewButton.invisible()
                }
                setupNewButtons(state.isCreateObjectAllowed)
                setCurrentViewerName(state.viewer?.title)
                dataViewInfo.hide()
                setViewer(viewer = state.viewer, canCreateObject = state.isCreateObjectAllowed, pageIndex = pageIndex)
            }
            is DataViewViewState.Set.NoQuery -> {
                topToolbarThreeDotsButton.visible()
                topToolbarStatusContainer.visible()
                initView.gone()
                header.visible()
                dataViewHeader.visible()
                viewerTitle.isEnabled = false
                addNewButton.isEnabled = false
                customizeViewButton.isEnabled = false
                if (state.isEditingViewAllowed) {
                    customizeViewButton.visible()
                } else {
                    customizeViewButton.invisible()
                }
                setupNewButtons(state.isCreateObjectAllowed)
                setCurrentViewerName(getString(R.string.viewer_default_title))
                dataViewInfo.show(
                    type = DataViewInfo.TYPE.SET_NO_QUERY,
                    isReadOnlyAccess = !state.isCreateObjectAllowed
                )
                setViewer(viewer = null)
            }
            is DataViewViewState.Set.NoItems -> {
                topToolbarThreeDotsButton.visible()
                topToolbarStatusContainer.visible()
                initView.gone()
                header.visible()
                dataViewHeader.visible()
                viewerTitle.isEnabled = true
                setupNewButtons(state.isCreateObjectAllowed)
                customizeViewButton.isEnabled = state.isEditingViewAllowed
                if (state.isEditingViewAllowed) {
                    customizeViewButton.visible()
                } else {
                    customizeViewButton.invisible()
                }
                setCurrentViewerName(state.title)
                dataViewInfo.show(
                    type = if (state.isBoardGroupByRequired) {
                        DataViewInfo.TYPE.BOARD_NO_GROUP_BY
                    } else {
                        DataViewInfo.TYPE.SET_NO_ITEMS
                    },
                    // The group-by hint points at view settings, so it is gated by view-edit
                    // permission; the "no items" hint is gated by object creation.
                    isReadOnlyAccess = if (state.isBoardGroupByRequired) {
                        !state.isEditingViewAllowed
                    } else {
                        !state.isCreateObjectAllowed
                    }
                )
                setViewer(viewer = null)
            }
            is DataViewViewState.Set.Default -> {
                topToolbarThreeDotsButton.visible()
                topToolbarStatusContainer.visible()
                initView.gone()
                header.visible()
                dataViewHeader.visible()
                viewerTitle.isEnabled = true
                setupNewButtons(state.isCreateObjectAllowed)
                if (state.isEditingViewAllowed) {
                    customizeViewButton.visible()
                } else {
                    customizeViewButton.invisible()
                }
                customizeViewButton.isEnabled = true
                setCurrentViewerName(state.viewer?.title)
                setViewer(viewer = state.viewer, canCreateObject = state.isCreateObjectAllowed, pageIndex = pageIndex)
                dataViewInfo.hide()
            }
            DataViewViewState.Init -> {
                topToolbarThreeDotsButton.invisible()
                topToolbarStatusContainer.invisible()
                header.gone()
                dataViewHeader.invisible()
                initView.visible()
                dataViewInfo.hide()
                setViewer(viewer = null)
            }
            is DataViewViewState.Error -> {
                topToolbarThreeDotsButton.visible()
                topToolbarStatusContainer.visible()
                header.visible()
                initView.gone()
                dataViewHeader.gone()
                toast(state.msg)
                dataViewInfo.hide()
                setViewer(viewer = null)
            }
            is DataViewViewState.TypeSet.Default -> {
                topToolbarThreeDotsButton.gone()
                topToolbarStatusContainer.gone()
                topToolbarTitle.gone()
                topBackButton.gone()
                topToolbar.gone()
                initView.gone()
                header.gone()
                dataViewHeader.visible()
                viewerTitle.isEnabled = true
                setupNewButtonsForTypeSet(state.isCreateObjectAllowed)
                if (state.isEditingViewAllowed) {
                    customizeViewButton.visible()
                } else {
                    customizeViewButton.invisible()
                }
                customizeViewButton.isEnabled = true
                setCurrentViewerName(state.viewer?.title)
                setViewer(viewer = state.viewer, canCreateObject = state.isCreateObjectAllowed, pageIndex = pageIndex)
                dataViewInfo.hide()
            }
            is DataViewViewState.TypeSet.NoItems -> {
                topToolbarThreeDotsButton.gone()
                topToolbarStatusContainer.gone()
                topToolbarTitle.gone()
                topToolbar.gone()
                topBackButton.gone()
                initView.gone()
                header.gone()
                dataViewHeader.visible()
                viewerTitle.isEnabled = true
                setupNewButtonsForTypeSet(state.isCreateObjectAllowed)
                if (state.isEditingViewAllowed) {
                    customizeViewButton.visible()
                } else {
                    customizeViewButton.invisible()
                }
                customizeViewButton.isEnabled = true
                setCurrentViewerName(state.title)
                dataViewInfo.show(
                    type = if (state.isBoardGroupByRequired) {
                        DataViewInfo.TYPE.BOARD_NO_GROUP_BY
                    } else {
                        DataViewInfo.TYPE.SET_NO_ITEMS
                    },
                    // The group-by hint points at view settings, so it is gated by view-edit
                    // permission; the "no items" hint is gated by object creation.
                    isReadOnlyAccess = if (state.isBoardGroupByRequired) {
                        !state.isEditingViewAllowed
                    } else {
                        !state.isCreateObjectAllowed
                    }
                )
                setViewer(viewer = null)
            }

            is DataViewViewState.TypeSet.Error -> {
                topToolbarThreeDotsButton.gone()
                topToolbarStatusContainer.gone()
                topToolbarTitle.gone()
                topBackButton.gone()
                topToolbar.gone()
                initView.gone()
                header.gone()
                dataViewHeader.visible()
                setViewer(viewer = null)
            }
        }
        applyEmbeddedMode()
        renderHeaderScrollState()
    }

    private fun setCurrentViewerName(title: String?) {
        viewerTitle.text = if (title.isNullOrEmpty()) {
            getString(R.string.untitled)
        } else {
            title
        }
    }

    private fun setupNewButtons(isCreateObjectAllowed: Boolean) {
        if (isCreateObjectAllowed) {
            addNewButton.gone()
            addNewIconButton.visible()
        } else {
            addNewButton.gone()
            addNewIconButton.gone()
        }
    }

    private fun setupNewButtonsForTypeSet(isCreateObjectAllowed: Boolean) {
        if (isCreateObjectAllowed) {
            addNewButton.gone()
            addNewIconButton.visible()
        } else {
            addNewButton.gone()
            addNewIconButton.gone()
        }
    }

    private fun setViewer(viewer: Viewer?, canCreateObject: Boolean = false, pageIndex: Int = 0) {
        val changedViewer = activeViewer?.id != viewer?.id || activeViewer?.javaClass != viewer?.javaClass
        if (changedViewer) {
            saveActiveViewerAnchor()
            binding.scrollHost.cancelMotion()
            binding.boardView.cancelDrag()
            listOf(rvRows, binding.listView, binding.galleryView).forEach { it.stopScroll() }
            committedViewerId = null
            pendingReveal = null
            pendingAnchor = viewer?.let { viewerAnchors[it.id] ?: bundleOf("index" to 0) }
            pendingAnchorInputGeneration = binding.scrollHost.coordinator.inputGeneration
        } else if (activeViewer is Viewer.GalleryView && viewer is Viewer.GalleryView &&
            (activeViewer as Viewer.GalleryView).largeCards != viewer.largeCards) {
            binding.scrollHost.cancelMotion()
            binding.galleryView.stopScroll()
            saveActiveViewerAnchor()
            pendingAnchor = viewerAnchors[viewer.id]
            pendingAnchorInputGeneration = binding.scrollHost.coordinator.inputGeneration
        }
        activeViewer = viewer
        if (retainedViewerId == null) viewer?.let { retainedViewerId = it.id }
        val submission = ++submissionGeneration
        val onCommitted: () -> Unit = {
            if (hasBinding && submission == submissionGeneration && activeViewer?.id == viewer?.id) {
                committedViewerId = viewer?.id
                activeViewerPageIndex = pageIndex
                applyPendingContentPosition()
            }
        }
        binding.gridContainer.root.isVisible = viewer is Viewer.GridView
        when (viewer) {
            is Viewer.GridView -> {
                with(binding) {
                    unsupportedViewError.gone()
                    unsupportedViewError.text = null
                    galleryView.clear()
                    galleryView.gone()
                    listView.gone()
                    listView.setViews(emptyList())
                    boardView.gone()
                    boardView.clear()
                }
                viewerGridHeaderAdapter.submitList(viewer.columns)
                viewerGridAdapter.submitList(viewer.rows, onCommitted)
            }
            is Viewer.GalleryView -> {
                viewerGridHeaderAdapter.submitList(emptyList())
                viewerGridAdapter.submitList(emptyList())
                with(binding) {
                    unsupportedViewError.gone()
                    unsupportedViewError.text = null
                    listView.gone()
                    listView.setViews(emptyList())
                    galleryView.visible()
                    galleryView.setViews(
                        views = viewer.items,
                        largeCards = viewer.largeCards,
                        onCommitted = onCommitted
                    )
                    boardView.gone()
                    boardView.clear()
                }
            }
            is Viewer.ListView -> {
                viewerGridHeaderAdapter.submitList(emptyList())
                viewerGridAdapter.submitList(emptyList())
                with(binding) {
                    unsupportedViewError.gone()
                    unsupportedViewError.text = null
                    galleryView.gone()
                    galleryView.clear()
                    listView.visible()
                    listView.setViews(viewer.items, onCommitted)
                    boardView.gone()
                    boardView.clear()
                }
            }
            is Viewer.Board -> {
                viewerGridHeaderAdapter.submitList(emptyList())
                viewerGridAdapter.submitList(emptyList())
                with(binding) {
                    unsupportedViewError.gone()
                    unsupportedViewError.text = null
                    galleryView.gone()
                    galleryView.clear()
                    listView.gone()
                    listView.setViews(emptyList())
                    boardView.visible()
                    // Set the create permission here — co-located with board rendering — so any
                    // viewer-rendering state that routes through setViewer() can't forget it and
                    // silently hide the per-column "＋ New" affordance (widget default is false).
                    boardView.canCreateObject = canCreateObject
                    boardView.setBoard(viewer)
                }
            }
            is Viewer.Unsupported -> {
                viewerGridHeaderAdapter.submitList(emptyList())
                viewerGridAdapter.submitList(emptyList())
                with(binding) {
                    galleryView.gone()
                    galleryView.clear()
                    listView.gone()
                    listView.setViews(emptyList())
                    boardView.gone()
                    boardView.clear()
                    when(viewer.type) {
                        Viewer.Unsupported.TYPE_GRAPH -> {
                            unsupportedViewError.setText(R.string.error_graph_view_not_supported)
                        }
                        Viewer.Unsupported.TYPE_CALENDAR -> {
                            unsupportedViewError.setText(R.string.error_calendar_view_not_supported)
                        }
                        Viewer.Unsupported.TYPE_KANBAN -> {
                            unsupportedViewError.setText(R.string.error_kanban_view_not_supported)
                        }
                        else -> {
                            unsupportedViewError.setText(R.string.error_generic_view_not_supported)
                        }
                    }
                    unsupportedViewError.visible()
                }
            }
            null -> {
                viewerGridHeaderAdapter.submitList(emptyList())
                viewerGridAdapter.submitList(emptyList())
                with(binding) {
                    galleryView.gone()
                    galleryView.clear()
                    listView.gone()
                    listView.setViews(emptyList())
                    boardView.gone()
                    boardView.clear()
                    unsupportedViewError.gone()
                    unsupportedViewError.text = null
                }
            }
        }
    }

    private fun activeRecyclerView(): RecyclerView? = when (activeViewer) {
        is Viewer.GridView -> rvRows
        is Viewer.ListView -> binding.listView
        is Viewer.GalleryView -> binding.galleryView
        else -> null
    }

    private fun committedObjectIds(): List<Id> = when (activeViewer) {
        is Viewer.GridView -> viewerGridAdapter.currentList.map { it.id }
        is Viewer.ListView -> binding.listView.currentItems.map { it.objectId }
        is Viewer.GalleryView -> binding.galleryView.currentItems.map { it.objectId }
        else -> emptyList()
    }

    private fun saveActiveViewerAnchor() {
        val viewer = activeViewer ?: return
        if (viewer is Viewer.Board) {
            retainedBoardState = binding.boardView.saveScrollState()
            return
        }
        if (committedViewerId != viewer.id || pendingAnchor != null) return
        val recycler = activeRecyclerView() ?: return
        val manager = recycler.layoutManager as? LinearLayoutManager ?: return
        val position = manager.findFirstVisibleItemPosition()
        val ids = committedObjectIds()
        val id = ids.getOrNull(position) ?: return
        val item = manager.findViewByPosition(position) ?: return
        val snapshot = bundleOf(
            "id" to id,
            "before" to ids.getOrNull(position - 1),
            "after" to ids.getOrNull(position + 1),
            "index" to position,
            "page" to activeViewerPageIndex,
            "offset" to (manager.getDecoratedTop(item) - recycler.paddingTop),
            "x" to if (viewer is Viewer.GridView) binding.gridContainer.root.scrollX else 0
        )
        viewerAnchors.remove(viewer.id)
        viewerAnchors[viewer.id] = snapshot
        while (viewerAnchors.size > MAX_RETAINED_VIEWERS) {
            viewerAnchors.remove(viewerAnchors.keys.first())
        }
    }

    private fun applyPendingContentPosition() {
        if (pendingAnchor == null && pendingReveal == null) return
        val recycler = activeRecyclerView() ?: return
        val viewerId = activeViewer?.id ?: return
        val submission = submissionGeneration
        val host = binding.scrollHost
        pendingPositionPreDraw?.let { host.viewTreeObserver.removeOnPreDrawListener(it) }
        pendingPositionPreDraw = ViewTreeObserver.OnPreDrawListener {
            pendingPositionPreDraw?.let { host.viewTreeObserver.removeOnPreDrawListener(it) }
            pendingPositionPreDraw = null
            if (!hasBinding || submission != submissionGeneration || activeViewer?.id != viewerId) {
                return@OnPreDrawListener true
            }
            val coordinator = host.coordinator
            if (pendingAnchorInputGeneration != coordinator.inputGeneration) pendingAnchor = null
            if (pendingRevealInputGeneration != coordinator.inputGeneration) pendingReveal = null
            val ids = committedObjectIds()
            if (ids.isEmpty()) return@OnPreDrawListener true // Loading is not an anchor at the top.
            val manager = recycler.layoutManager as? LinearLayoutManager ?: return@OnPreDrawListener true
            val reveal = pendingReveal
            if (reveal != null) {
                val position = ids.indexOf(reveal)
                if (position < 0) return@OnPreDrawListener true // Wait for the created record's submission.
                pendingReveal = null
                pendingAnchor = null
                if (binding.viewerViewport.height == 0) coordinator.setExpanded(false)
                val token = coordinator.begin("reveal:$viewerId", MotionOrigin.ProgrammaticReveal)
                recycler.stopScroll()
                manager.scrollToPositionWithOffset(position, 0)
                coordinator.end(token)
                // The requested position is applied on the next layout. Suppress the old frame.
                return@OnPreDrawListener false
            }
            val anchor = pendingAnchor ?: return@OnPreDrawListener true
            pendingAnchor = null
            val position = sequenceOf("id", "after", "before")
                .mapNotNull { key -> anchor.getString(key)?.let(ids::indexOf)?.takeIf { it >= 0 } }
                .firstOrNull() ?: anchor.getInt("index").coerceIn(0, ids.lastIndex)
            val token = coordinator.begin("restore:$viewerId", MotionOrigin.Restoration)
            manager.scrollToPositionWithOffset(position,
                anchor.getInt("offset").coerceIn(-recycler.height, recycler.height))
            if (activeViewer is Viewer.GridView) {
                binding.gridContainer.root.scrollTo(anchor.getInt("x").coerceAtLeast(0), 0)
            }
            coordinator.end(token)
            false
        }
        host.viewTreeObserver.addOnPreDrawListener(pendingPositionPreDraw)
        host.invalidate()
    }

    private fun scrollToObject(objectId: Id) {
        if (activeRecyclerView() == null) return
        binding.scrollHost.cancelMotion()
        pendingReveal = objectId
        pendingRevealInputGeneration = binding.scrollHost.coordinator.inputGeneration
        applyPendingContentPosition()
    }

    private fun onViewersWidgetAction(action: ViewersWidgetUi.Action) {
        val target = when (action) {
            is ViewersWidgetUi.Action.SetActive -> action.id
            is ViewersWidgetUi.Action.OnMove -> action.currentViews.firstOrNull()?.id
            else -> null
        }
        val page = if (target != null && target != activeViewer?.id) {
            saveActiveViewerAnchor()
            viewerAnchors[target]?.getInt("page")?.coerceAtLeast(0) ?: 0
        } else null
        vm.onViewersWidgetAction(action, restoredPage = page)
    }

    private fun bindHeader(header: SetOrCollectionHeaderState.Default) {
        setupHeaderMargins(header)

        headerReadOnly = header.isReadOnlyMode
        title.isEnabled = !headerReadOnly
        tvDescription.isEnabled = !headerReadOnly
        updateHeaderEditing()

        if (title.text.toString() != header.title.text) {
            title.pauseTextWatchers {
                title.setText(header.title.text)
            }
        }
        binding.topToolbar.title.text =
            header.title.text.ifBlank { getString(R.string.untitled) }

        // Mirror the body header icon into the top-bar pill so the user has a
        // persistent, visually anchored tap target for the widget overlay.
        // The resolved icon already includes the object-type fallback when no
        // custom emoji/image is set, so it can always be displayed.
        binding.topToolbar.icon.setIcon(header.title.icon)
        binding.topToolbar.icon.visible()

        binding.objectHeader.root.findViewById<ViewGroup>(R.id.docEmojiIconContainer).apply {
            if (header.title.emoji != null) visible() else gone()
            jobs += this.clicks()
                .throttleFirst()
                .onEach { vm.onObjectIconClicked() }
                .launchIn(lifecycleScope)
        }

        binding.objectHeader.root.findViewById<ImageView>(R.id.imageIcon).apply {
            jobs += this.clicks()
                .throttleFirst()
                .onEach { vm.onObjectIconClicked() }
                .launchIn(lifecycleScope)

            if (header.title.image != null) {
                this.visible()
                this.load(header.title.image)
            } else {
                this.gone()
                this.setImageDrawable(null)
            }
        }

        binding.objectHeader.root.findViewById<ImageView>(R.id.emojiIcon)
            .setEmojiOrNull(header.title.emoji)

        setCover(
            coverColor = header.title.coverColor,
            coverGradient = header.title.coverGradient,
            coverImage = header.title.coverImage
        )

        renderHeaderScrollState()
        binding.scrollHost.invalidateGeometry()
        if (tvDescription.hasFocus()) return

        val description = header.description

        if (description is SetOrCollectionHeaderState.Description.Default) {
            tvDescriptionTextWatcher.pause {
                if (tvDescription.text.toString() != description.description) {
                    tvDescription.setText(description.description)
                }
            }
            tvDescription.visible()
        } else {
            tvDescription.gone()
        }
    }

    private fun setupHeaderMargins(header: SetOrCollectionHeaderState.Default) {
        when {
            header.title.emoji != null -> {
                title.updateLayoutParams<LinearLayout.LayoutParams> {
                    topMargin = dimen(R.dimen.dp_12)
                }
                binding.objectHeader.docEmojiIconContainer.updateLayoutParams<FrameLayout.LayoutParams> {
                    topMargin =
                        if (!header.title.hasCover) dimen(R.dimen.dp_12) else dimen(R.dimen.dp_72)
                }
            }
            header.title.image != null -> {
                title.updateLayoutParams<LinearLayout.LayoutParams> {
                    topMargin = dimen(R.dimen.dp_10)
                }
                binding.objectHeader.imageIcon.updateLayoutParams<FrameLayout.LayoutParams> {
                    topMargin =
                        if (!header.title.hasCover) dimen(R.dimen.dp_3) else dimen(R.dimen.dp_54)
                }
            }
            else -> {
                title.updateLayoutParams<LinearLayout.LayoutParams> {
                    topMargin =
                        if (!header.title.hasCover) dimen(R.dimen.dp_32) else dimen(R.dimen.dp_10)
                }
            }
        }
    }

    private fun setCover(
        coverColor: CoverColor?,
        coverImage: String?,
        coverGradient: String?
    ) {
        val ivCover = binding.objectHeader.root.findViewById<ImageView>(R.id.cover)
        val container =
            binding.objectHeader.root.findViewById<FrameLayout>(R.id.coverAndIconContainer)

        ivCover.clicks()
            .throttleFirst()
            .onEach { vm.onCoverClicked() }
            .launchIn(lifecycleScope)

        when {
            coverColor != null -> {
                ivCover?.apply {
                    visible()
                    setImageDrawable(null)
                    setBackgroundColor(coverColor.color)
                }
                container.updatePadding(top = 0)
                onObjectCoverUpdated()
            }
            coverImage != null -> {
                ivCover?.apply {
                    visible()
                    setBackgroundColor(0)
                    load(coverImage)
                }
                container.updatePadding(top = 0)
                onObjectCoverUpdated()
            }
            coverGradient != null -> {
                ivCover?.apply {
                    visible()
                    setImageDrawable(null)
                    setBackgroundColor(0)
                    when (coverGradient) {
                        CoverGradient.YELLOW -> setBackgroundResource(com.anytypeio.anytype.core_ui.R.drawable.cover_gradient_yellow)
                        CoverGradient.RED -> setBackgroundResource(com.anytypeio.anytype.core_ui.R.drawable.cover_gradient_red)
                        CoverGradient.BLUE -> setBackgroundResource(com.anytypeio.anytype.core_ui.R.drawable.cover_gradient_blue)
                        CoverGradient.TEAL -> setBackgroundResource(com.anytypeio.anytype.core_ui.R.drawable.cover_gradient_teal)
                        CoverGradient.PINK_ORANGE -> setBackgroundResource(com.anytypeio.anytype.core_ui.R.drawable.wallpaper_gradient_1)
                        CoverGradient.BLUE_PINK -> setBackgroundResource(com.anytypeio.anytype.core_ui.R.drawable.wallpaper_gradient_2)
                        CoverGradient.GREEN_ORANGE -> setBackgroundResource(com.anytypeio.anytype.core_ui.R.drawable.wallpaper_gradient_3)
                        CoverGradient.SKY -> setBackgroundResource(com.anytypeio.anytype.core_ui.R.drawable.wallpaper_gradient_4)
                    }
                }
                container.updatePadding(top = 0)
                onObjectCoverUpdated()
            }
            else -> {
                ivCover?.apply {
                    gone()
                    setImageDrawable(null)
                    setBackgroundColor(0)
                }
                container.updatePadding(top = dimen(R.dimen.dp_48))
                onCoverRemoved()
            }
        }
    }

    private fun onObjectCoverUpdated() {
        hasCover = true
        renderHeaderScrollState()
        binding.scrollHost.invalidateGeometry()
    }

    private fun onCoverRemoved() {
        hasCover = false
        renderHeaderScrollState()
        binding.scrollHost.invalidateGeometry()
    }

    private fun observeCommands(command: ObjectSetCommand) {
        @Suppress("SENSELESS_COMPARISON")
        if (command == null) {
            Timber.w("ObjectSetFragment: vm.commands emitted null")
            return
        }
        when (command) {
            is ObjectSetCommand.Modal.Menu -> {
                findNavController().safeNavigate(
                    R.id.objectSetScreen,
                    R.id.objectSetMainMenuScreen,
                    bundleOf(
                        ObjectMenuBaseFragment.CTX_KEY to command.ctx,
                        ObjectMenuBaseFragment.SPACE_KEY to command.space,
                        ObjectMenuBaseFragment.IS_ARCHIVED_KEY to command.isArchived,
                        ObjectMenuBaseFragment.IS_FAVORITE_KEY to command.isFavorite,
                        ObjectMenuBaseFragment.IS_LOCKED_KEY to false,
                        ObjectMenuBaseFragment.FROM_NAME to title.text.toString(),
                        ObjectMenuBaseFragment.IS_LOCKED_KEY to command.isReadOnly,
                        ObjectMenuBaseFragment.IS_READ_ONLY_KEY to command.isReadOnly
                    )
                )
            }
            is ObjectSetCommand.Modal.EditGridTextCell -> {
                runCatching {
                    val fr = RelationTextValueFragment.new(
                        ctx = ctx,
                        objectId = command.recordId,
                        flow = RelationTextValueFragment.FLOW_DATAVIEW,
                        relationKey = command.relationKey,
                        space = command.space
                    )
                    fr.showChildFragment(EMPTY_TAG)
                }.onFailure {
                    Timber.e(it, "Error while opening relation text value for grid cell")
                }
            }
            is ObjectSetCommand.Modal.EditIntrinsicTextRelation -> {
                runCatching {
                    val fr = RelationTextValueFragment.new(
                        ctx = ctx,
                        objectId = ctx,
                        flow = RelationTextValueFragment.FLOW_SET_OR_COLLECTION,
                        relationKey = command.relation,
                        space = command.space
                    )
                    fr.showChildFragment(EMPTY_TAG)
                }.onFailure {
                    Timber.e(it, "Error while opening relation text value in set")
                }
            }
            is ObjectSetCommand.Modal.EditObjectRelationValue -> {
                findNavController().safeNavigate(
                    R.id.objectSetScreen,
                    R.id.objectValueScreen,
                    ObjectValueFragment.args(
                        ctx = command.ctx,
                        obj = command.ctx,
                        relation = command.relation,
                        space = command.space,
                        isLocked = false,
                        relationContext = RelationContext.OBJECT_SET
                    )
                )
            }
            is ObjectSetCommand.Modal.EditTagOrStatusRelationValue -> {
                val bundle = TagOrStatusValueFragment.args(
                    ctx = command.ctx,
                    obj = command.ctx,
                    relation = command.relation,
                    space = command.space,
                    isLocked = false,
                    context = RelationContext.OBJECT_SET
                )
                findNavController().safeNavigate(R.id.objectSetScreen, R.id.nav_relations, bundle)
            }
            is ObjectSetCommand.Modal.EditGridDateCell -> {
                val fr = RelationDateValueFragment.new(
                    ctx = ctx,
                    space = command.space,
                    objectId = command.objectId,
                    flow = RelationDateValueFragment.FLOW_DV,
                    relationKey = command.relationKey
                )
                fr.showChildFragment(EMPTY_TAG)
            }
            is ObjectSetCommand.Modal.EditObjectCell -> {
                findNavController().safeNavigate(
                    R.id.objectSetScreen,
                    R.id.objectValueScreen,
                    ObjectValueFragment.args(
                        ctx = command.ctx,
                        space = command.space,
                        obj = command.target,
                        relation = command.relationKey,
                        isLocked = false,
                        relationContext = RelationContext.DATA_VIEW
                    )
                )
            }
            is ObjectSetCommand.Modal.EditTagOrStatusCell -> {
                val bundle = TagOrStatusValueFragment.args(
                    ctx = command.ctx,
                    space = command.space,
                    obj = command.target,
                    relation = command.relationKey,
                    isLocked = false,
                    context = RelationContext.DATA_VIEW
                )
                findNavController().safeNavigate(R.id.objectSetScreen, R.id.nav_relations, bundle)
            }
            is ObjectSetCommand.Modal.OpenSettings -> {
                val fr = ObjectSetSettingsFragment.new(
                    ctx = command.ctx,
                    dv = command.dv,
                    viewer = command.viewer,
                    space = space
                )
                fr.showChildFragment(EMPTY_TAG)
            }
            is ObjectSetCommand.Intent.MailTo -> {
                try {
                    Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:" + command.email)
                    }.let {
                        startActivity(it)
                    }
                } catch (e: Exception) {
                    toast("An error occurred. Email address may be invalid: ${e.message}")
                }
            }
            is ObjectSetCommand.Intent.GoTo -> {
                try {
                    Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(command.url)
                    }.let {
                        startActivity(it)
                    }
                } catch (e: Exception) {
                    toast("An error occurred. Url may be invalid: ${e.message}")
                }
            }
            is ObjectSetCommand.Modal.OpenIconActionMenu -> {
                findNavController().safeNavigate(
                    R.id.objectSetScreen,
                    R.id.action_objectSetScreen_to_objectSetIconPickerScreen,
                    bundleOf(
                        IconPickerFragmentBase.ARG_CONTEXT_ID_KEY to command.target,
                        IconPickerFragmentBase.ARG_SPACE_ID_KEY to command.space,
                    )
                )
            }
            is ObjectSetCommand.Intent.Call -> {
                try {
                    Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${command.phone}")
                    }.let {
                        startActivity(it)
                    }
                } catch (e: Exception) {
                    toast("An error occurred. Phone number may be invalid: ${e.message}")
                }
            }
            is ObjectSetCommand.Modal.ModifyViewerFilters -> {
                val fr = ViewerFilterFragment.new(
                    ctx = command.ctx,
                    viewer = command.viewer,
                    space = space
                )
                fr.showChildFragment(EMPTY_TAG)
            }
            is ObjectSetCommand.Modal.ModifyViewerSorts -> {
                val fr = ViewerSortFragment.new(ctx = ctx, space = space, viewer = command.viewer)
                fr.showChildFragment(EMPTY_TAG)
            }
            is ObjectSetCommand.Modal.OpenCoverActionMenu -> {
                findNavController().safeNavigate(
                    R.id.objectSetScreen,
                    R.id.action_objectSetScreen_to_objectSetCoverScreen,
                    SelectCoverObjectSetFragment.args(
                        ctx = command.ctx,
                        space = command.space
                    )
                )
            }
            is ObjectSetCommand.Modal.CreateBookmark -> {
                val fr = SetObjectCreateBookmarkRecordFragment().apply {
                    arguments = SetObjectCreateRecordFragmentBase.args(
                        ctx = command.ctx,
                        space = command.space,
                    )
                }
                fr.showChildFragment()
            }
            is ObjectSetCommand.Modal.OpenDataViewSelectQueryScreen -> {
                val fr = DataViewSelectSourceFragment.newInstance(
                    space = space,
                    selectedTypes = command.selectedTypes
                )
                fr.showChildFragment()
            }
            is ObjectSetCommand.Modal.OpenEmptyDataViewSelectQueryScreen -> {
                val fr = EmptyDataViewSelectSourceFragment.newInstance(space = space)
                fr.showChildFragment()
            }
            ObjectSetCommand.Modal.OpenSelectTypeScreen -> {
                val fr = CollectionAddObjectTypeFragment.newInstance(
                    space = space
                )
                fr.showChildFragment()
            }

            is ObjectSetCommand.Modal.ShowObjectSetTypePopupMenu -> {
                val themeWrapper = ContextThemeWrapper(context, R.style.DefaultPopupMenuStyle)
                val popup = ObjectSetTypePopupMenu(
                    context = themeWrapper,
                    view = featuredRelations.findViewById(command.anchor),
                    onChangeTypeClicked = {
                        vm.onClickListener(SetQuery(queries = emptyList()))
                    },
                    onConvertToCollection = {
                        vm.proceedWithConvertingToCollection()
                    }
                )
                popup.show()
            }
            is ObjectSetCommand.Modal.ShowObjectSetRelationPopupMenu -> {
                val themeWrapper = ContextThemeWrapper(context, R.style.DefaultPopupMenuStyle)
                val popup = ObjectSetRelationPopupMenu(
                    context = themeWrapper,
                    view = featuredRelations.findViewById(command.anchor),
                    onConvertToCollection = {
                        vm.proceedWithConvertingToCollection()
                    }
                )
                popup.show()
            }
            is ObjectSetCommand.Modal.ShowObjectHeaderContextMenu -> {
                contextMenuAnchorView?.let { anchor ->
                    showObjectHeaderContextMenu(
                        objectId = command.objectId,
                        anchor = anchor,
                        showMoveToBin = command.canMoveToBin,
                        showRemoveFromCollection = command.isCollection,
                        layout = command.layout
                    )
                }
            }
            is ObjectSetCommand.ShowOnlyAccessError -> {
                toast(
                    getString(R.string.multiplayer_read_only_access_error)
                )
            }
            ObjectSetCommand.Intent.OpenAppStore -> {
                startMarketPageOrWeb()
            }

            is ObjectSetCommand.Browse -> {
                ActivityCustomTabsHelper.openUrl(
                    activity = requireActivity(),
                    url = command.url
                )
            }
            is ObjectSetCommand.PlayMedia -> {
                runCatching {
                    val mediaType = when (command.layout) {
                        ObjectType.Layout.IMAGE -> MediaActivity.TYPE_IMAGE
                        ObjectType.Layout.VIDEO -> MediaActivity.TYPE_VIDEO
                        ObjectType.Layout.AUDIO -> MediaActivity.TYPE_AUDIO
                        else -> {
                            Timber.w("PlayMedia dispatched with unsupported layout: ${command.layout}")
                            return@runCatching
                        }
                    }
                    MediaActivity.start(
                        context = requireContext(),
                        mediaType = mediaType,
                        obj = command.targetObjectId,
                        name = command.name,
                        space = space
                    )
                }.onFailure {
                    Timber.e(it, "Error while launching media viewer")
                }
            }
            is ObjectSetCommand.CopyLinkToClipboard -> {
                requireContext().copyPlainTextToClipboard(
                    plainText = command.link,
                    label = "Object link",
                    successToast = getString(R.string.link_copied)
                )
            }
            is ObjectSetCommand.ScrollToObject -> {
                scrollToObject(command.objectId)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // combine's collector may start later; a retained VM already owns any pending selection.
        vm.selectedViewerId?.let { retainedViewerId = it }

        pageToRestore?.let { page ->
            // Select the saved fixed page before any record subscription or anchor attempt.
            vm.onPaginatorToolbarNumberClicked(number = page, isSelected = false)
            pageToRestore = null
        }

        title.addTextChangedListener(titleTextWatcher)

        // fabCreate stays visible. NavPanelState.isCreateEnabled is consumed
        // directly inside the fabCreate ComposeView (see onViewCreated) to gate
        // its click and dim it in read-only contexts.

        jobs += lifecycleScope.subscribe(vm.contentSelection) { selection ->
            if (lastContentSelection != null && lastContentSelection != selection) {
                saveActiveViewerAnchor()
                pendingAnchor = null
                pendingReveal = null
                binding.boardView.cancelDrag()
                binding.scrollHost.cancelMotion()
                listOf(rvRows, binding.listView, binding.galleryView).forEach { it.stopScroll() }
            }
            selection.first?.let { retainedViewerId = it }
            lastContentSelection = selection
        }

        jobs += lifecycleScope.subscribe(vm.commands) { observeCommands(it) }
        jobs += lifecycleScope.subscribe(vm.header) { header ->
            @Suppress("SENSELESS_COMPARISON")
            if (header == null) {
                Timber.w("ObjectSetFragment: vm.header emitted null")
                return@subscribe
            }
            when(header) {
                is SetOrCollectionHeaderState.Default -> {
                    bindHeader(header)
                }
                is SetOrCollectionHeaderState.None -> {
                    // Do nothing.
                }
            }
        }
        jobs += lifecycleScope.subscribe(vm.viewerContent) { content ->
            vm.renderedPageIndex(content)?.let { page -> setupDataViewViewState(content.state, page) }
        }
        jobs += lifecycleScope.subscribe(vm.error) { err ->
            if (err.isNullOrEmpty())
                binding.tvError.gone()
            else {
                binding.tvError.text = err
                binding.tvError.visible()
            }
        }
        jobs += lifecycleScope.subscribe(vm.pagination) { (index, count) ->
            retainedPageIndex = index
            binding.paginatorToolbar.set(count = count, index = index)
            if (count > 1) {
                binding.paginatorToolbar.visible()
            } else {
                binding.paginatorToolbar.gone()
            }
        }
        jobs += lifecycleScope.subscribe(vm.featured) { featured ->
            if (featured != null) {
                featuredRelations.visible()
                featuredRelations.set(
                    item = featured,
                    click = vm::onClickListener
                )
            } else {
                featuredRelations.clear()
                featuredRelations.gone()
            }
        }
        jobs += lifecycleScope.subscribe(vm.toasts) { toast(it) }

        jobs += lifecycleScope.subscribe(vm.spaceSyncStatus) { setStatus(it) }

        jobs += lifecycleScope.subscribe(createObjectVm.uploadSnackbar) { variant ->
            routeUploadSnackbar(mainVm, variant)
        }

        vm.onStart(view = retainedViewerId ?: view)
    }

    override fun onStop() {
        retainScrollState()
        binding.boardView.cancelDrag()
        binding.scrollHost.cancelMotion()
        super.onStop()
        title.removeTextChangedListener(titleTextWatcher)
        vm.onStop()
    }

    override fun onDestroyView() {
        retainScrollState()
        headerImeLayoutListener?.let(binding.root.viewTreeObserver::removeOnGlobalLayoutListener)
        headerImeObservation?.let(binding.root::removeCallbacks)
        headerImeLayoutListener = null
        headerImeObservation = null
        headerImeObservationPosted = false
        headerImeVisible = null
        pendingPositionPreDraw?.let { binding.scrollHost.viewTreeObserver.removeOnPreDrawListener(it) }
        pendingPositionPreDraw = null
        binding.scrollHost.onHeaderChanged = null
        binding.scrollHost.onGeometryChanging = null
        binding.scrollHost.cancelMotion()
        binding.scrollHost.setStableViewportChild(null)
        binding.boardView.clear()
        activeViewer = null
        lastContentSelection = null
        committedViewerId = null
        pendingAnchor = null
        pendingReveal = null
        submissionGeneration++
        titleInReadMode = null
        renderedCoverButtons = null
        renderedAccessibilityActions = -1
        headerAccessibility.clear()
        headerEditing = false
        viewerGridAdapter.clear()
        isSheetHostInstalled = false
        super.onDestroyView()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        // TextInputWidget also restores its own edit/read mode from the hierarchy.
        titleInReadMode = null
        renderHeaderScrollState()
        updateHeaderEditing()
    }

    private fun retainScrollState() {
        saveActiveViewerAnchor()
        vm.selectedViewerId?.let { retainedViewerId = it }
        retainedPageIndex = vm.selectedPageIndex
        retainedHeaderProgress = binding.scrollHost.coordinator.savedProgress
        retainedBoardState = binding.boardView.saveScrollState()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (hasBinding) retainScrollState()
        val viewers = Bundle().apply { viewerAnchors.forEach { (id, snapshot) -> putBundle(id, snapshot) } }
        outState.putBundle(SCROLL_STATE_KEY, bundleOf(
            "object" to ctx,
            "space" to space,
            "header" to retainedHeaderProgress,
            "activeViewer" to retainedViewerId,
            "page" to retainedPageIndex,
            "board" to retainedBoardState,
            "viewers" to viewers
        ))
        super.onSaveInstanceState(outState)
    }

    private fun setupOnBackPressedDispatcher() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            when {
                childFragmentManager.backStackEntryCount > 0 -> childFragmentManager.popBackStack()
                vm.typeTemplatesWidgetState.value.showWidget -> vm.onDismissTemplatesWidget()
                vm.viewersWidgetState.value.showWidget -> handleViewersWidgetState()
                vm.viewerEditWidgetState.value.isVisible() -> handleViewerEditWidgetState()
                else -> vm.onSystemBackPressed()
            }
        }
    }

    private fun handleViewersWidgetState() = when {
        vm.viewerEditWidgetState.value.isVisible() -> handleViewerEditWidgetState()
        else -> vm.onViewersWidgetAction(ViewersWidgetUi.Action.Dismiss)
    }

    private fun handleViewerEditWidgetState() = when {
        vm.viewerLayoutWidgetState.value.showWidget -> vm.onViewerLayoutWidgetAction(
            ViewerLayoutWidgetUi.Action.Dismiss
        )
        else -> vm.onViewerEditWidgetAction(ViewEditAction.Dismiss)
    }

    override fun onTextValueChanged(
        ctx: String,
        text: String,
        objectId: String,
        relationKey: Key
    ) = vm.onRelationTextValueChanged(
        value = text,
        objectId = objectId,
        relationKey = relationKey
    )

    override fun onNumberValueChanged(
        ctx: Id,
        number: Double?,
        objectId: Id,
        relationKey: Key
    ) = vm.onRelationTextValueChanged(
        value = number,
        objectId = objectId,
        relationKey = relationKey
    )

    override fun onDateValueChanged(
        ctx: Id,
        timeInSeconds: Number?,
        objectId: Id,
        relationKey: Key
    ) {
        vm.onRelationTextValueChanged(
            value = timeInSeconds,
            objectId = objectId,
            relationKey = relationKey
        )
    }

    override fun onOpenDateObject(timeInMillis: TimeInMillis) {
        vm.onOpenDateObjectByTimeInMillis(timeInMillis)
    }

    override fun onProceedWithSelectSource(id: Id) {
        vm.onObjectSetQueryPicked(query = id)
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentObjectSetBinding = FragmentObjectSetBinding.inflate(
        inflater, container, false
    )

    override fun onSelectObjectType(objType: ObjectWrapper.Type) {
        vm.onAddNewDocumentClicked(objType)
    }

    override fun onSelectObjectTypeForCollection(objType: ObjectWrapper.Type) {
        vm.onNewTypeForViewerClicked(objType)
    }

    private fun observeSelectingTemplate() {
        try {
            val navController = findNavController()
            val navBackStackEntry = navController.getBackStackEntry(R.id.objectSetScreen)
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME
                    && navBackStackEntry.savedStateHandle.contains(ARG_TEMPLATE_ID)
                ) {
                    val resultTemplateId =
                        navBackStackEntry.savedStateHandle.get<String>(ARG_TEMPLATE_ID)
                    val resultTypeId =
                        navBackStackEntry.savedStateHandle.get<String>(ARG_TARGET_TYPE_ID)
                    val resultTypeKey =
                        navBackStackEntry.savedStateHandle.get<String>(ARG_TARGET_TYPE_KEY)
                    if (!resultTemplateId.isNullOrBlank() && !resultTypeId.isNullOrBlank() && !resultTypeKey.isNullOrBlank()) {
                        navBackStackEntry.savedStateHandle.remove<String>(ARG_TEMPLATE_ID)
                        navBackStackEntry.savedStateHandle.remove<String>(ARG_TARGET_TYPE_ID)
                        navBackStackEntry.savedStateHandle.remove<String>(ARG_TARGET_TYPE_KEY)
                        vm.proceedWithSelectedTemplate(
                            template = resultTemplateId,
                            typeId = resultTypeId,
                            typeKey = resultTypeKey
                        )
                    }
                }
            }

            navBackStackEntry.lifecycle.addObserver(observer)

            viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_DESTROY) {
                    navBackStackEntry.lifecycle.removeObserver(observer)
                }
            })
        } catch (
            e: Exception
        ) {
            Timber.w(e)
        }
    }

    private fun installCreateObjectSheetHost() {
        if (isSheetHostInstalled) return
        val composeView = binding.createObjectSheetHost
        composeView.setViewCompositionStrategy(
            androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        composeView.setContent {
            CreateObjectSheetHost(
                vm = createObjectVm,
                visible = createObjectSheetVisible.value,
                onDismiss = { createObjectSheetVisible.value = false },
                onCreateObjectOfType = { objType ->
                    vm.onAddNewDocumentClicked(objType = objType)
                }
            )
        }
        isSheetHostInstalled = true
    }

    private fun showCreateObjectSheet() {
        installCreateObjectSheetHost()
        createObjectSheetVisible.value = true
    }

    override fun injectDependencies() {
        componentManager().objectSetComponent
            .get(
                key = ctx,
                param = DefaultComponentParam(
                    ctx = ctx,
                    space = SpaceId(space)
                )
            )
            .inject(this)
        val createObjectVmParams = NewCreateObjectViewModel.VmParams(
            spaceId = SpaceId(space),
            showAttachObject = false,
            showMediaSection = true
        )
        createObjectFactory = componentManager()
            .createObjectFeatureComponent
            .get(key = createObjectComponentKey(), param = createObjectVmParams)
            .viewModelFactory()
    }

    override fun releaseDependencies() {
        componentManager().objectSetComponent.release(ctx)
        componentManager().createObjectFeatureComponent.release(createObjectComponentKey())
    }

    private fun showObjectHeaderContextMenu(
        objectId: Id,
        anchor: View,
        showMoveToBin: Boolean,
        showRemoveFromCollection: Boolean,
        layout: ObjectType.Layout? = null
    ) {
        val themeWrapper = ContextThemeWrapper(context, R.style.DefaultPopupMenuStyle)
        val popup = ObjectHeaderContextMenu(
            context = themeWrapper,
            view = anchor,
            showMoveToBin = showMoveToBin,
            showRemoveFromCollection = showRemoveFromCollection,
            layout = layout,
            onOpenAsObjectClicked = {
                vm.onOpenAsObject(objectId)
            },
            onCopyLinkClicked = {
                vm.onCopyLink(objectId)
            },
            onMoveToBinClicked = {
                vm.onMoveToBin(objectId)
            },
            onRemoveFromCollectionClicked = {
                vm.onRemoveFromCollection(objectId)
            },
            onOpenInBrowserClicked = {
                vm.onOpenBookmarkInBrowser(objectId)
            },
            onOpenFileClicked = {
                vm.onOpenFile(objectId)
            }
        )
        popup.show()
        contextMenuAnchorView = null
    }

    fun onCloseCurrentObject() {
        vm.onCloseObject()
    }

    fun onMenuClicked() {
        vm.onMenuClicked()
    }

    companion object {
        const val CONTEXT_ID_KEY = "arg.object_set.context"
        const val SPACE_ID_KEY = "arg.object_set.space-id"
        private const val INITIAL_VIEW_ID_KEY = "arg.object_set.initial-view"
        private const val EMBEDDED_KEY = "arg.object_set.embedded"
        private const val SCROLL_STATE_KEY = "object_set.scroll_state"
        private const val MAX_RETAINED_VIEWERS = 16
        val EMPTY_TAG = null
        const val DRAWABLE_ALPHA_FULL = 255

        fun args(
            ctx: Id,
            space: Id,
            view: Id? = null,
            embedded: Boolean = false
        ) = bundleOf(
            CONTEXT_ID_KEY to ctx,
            SPACE_ID_KEY to space,
            INITIAL_VIEW_ID_KEY to view,
            EMBEDDED_KEY to embedded
        )
    }
}
