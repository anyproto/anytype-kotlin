package com.anytypeio.anytype.ui.sets

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.ContextThemeWrapper
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.EditorInfo.IME_ACTION_GO
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.widget.AppCompatEditText
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP
import androidx.core.view.children
import androidx.core.view.marginBottom
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.TimeInMillis
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.extensions.setEmojiOrNull
import com.anytypeio.anytype.core_ui.features.dataview.ViewerGridAdapter
import com.anytypeio.anytype.core_ui.features.dataview.ViewerGridHeaderAdapter
import com.anytypeio.anytype.core_ui.menu.ObjectSetRelationPopupMenu
import com.anytypeio.anytype.core_ui.menu.ObjectSetTypePopupMenu
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_ui.reactive.editorActionEvents
import com.anytypeio.anytype.core_ui.reactive.longClicks
import com.anytypeio.anytype.core_ui.reactive.touches
import com.anytypeio.anytype.core_ui.syncstatus.SpaceSyncStatusScreen
import com.anytypeio.anytype.core_ui.tools.DefaultTextWatcher
import com.anytypeio.anytype.core_ui.views.ButtonPrimarySmallIcon
import com.anytypeio.anytype.core_ui.widgets.FeaturedRelationGroupWidget
import com.anytypeio.anytype.core_ui.widgets.StatusBadgeWidget
import com.anytypeio.anytype.core_ui.widgets.TypeTemplatesWidget
import com.anytypeio.anytype.core_ui.widgets.dv.ObjectSetTitle
import com.anytypeio.anytype.core_ui.widgets.dv.ViewerEditWidget
import com.anytypeio.anytype.core_ui.widgets.dv.ViewerLayoutWidget
import com.anytypeio.anytype.core_ui.widgets.dv.ViewersWidget
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_ui.widgets.toolbar.DataViewInfo
import com.anytypeio.anytype.core_utils.OnSwipeListener
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
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.di.feature.DefaultComponentParam
import com.anytypeio.anytype.presentation.editor.cover.CoverColor
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType.Relation.*
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
import com.anytypeio.anytype.ui.editor.cover.SelectCoverObjectSetFragment
import com.anytypeio.anytype.ui.editor.modals.IconPickerFragmentBase
import com.anytypeio.anytype.ui.editor.sheets.ObjectMenuBaseFragment
import com.anytypeio.anytype.ui.objects.BaseObjectTypeChangeFragment
import com.anytypeio.anytype.ui.objects.creation.ObjectTypeSelectionFragment
import com.anytypeio.anytype.ui.objects.types.pickers.DataViewSelectSourceFragment
import com.anytypeio.anytype.ui.objects.types.pickers.EmptyDataViewSelectSourceFragment
import com.anytypeio.anytype.ui.objects.types.pickers.ObjectSelectTypeFragment
import com.anytypeio.anytype.ui.objects.types.pickers.OnDataViewSelectSourceAction
import com.anytypeio.anytype.ui.objects.types.pickers.ObjectTypeSelectionListener
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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

open class ObjectSetFragment :
    NavigationFragment<FragmentObjectSetBinding>(R.layout.fragment_object_set),
    TextValueEditReceiver,
    DateValueEditReceiver,
    OnDataViewSelectSourceAction,
    ObjectTypeSelectionListener {

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
        get() = binding.topToolbar.root.findViewById(R.id.topBackButton)

    private val topToolbar: ViewGroup
        get() = binding.topToolbar.root

    private val topToolbarTitle: TextView
        get() = binding.topToolbar.root.findViewById(R.id.tvTopToolbarTitle)

    private val topToolbarThreeDotsButton: ViewGroup
        get() = binding.topToolbar.root.findViewById(R.id.threeDotsButton)

    private val topToolbarStatusContainer: View
        get() = binding.topToolbar.root.findViewById(R.id.statusBadge)

    private val topToolbarThreeDotsIcon: ImageView
        get() = binding.topToolbar.root.findViewById(R.id.ivThreeDots)

    private val addNewButton: TextView
        get() = binding.dataViewHeader.addNewButton

    private val addNewIconButton: ButtonPrimarySmallIcon
        get() = binding.dataViewHeader.addNewIconButton

    private val customizeViewButton: ImageView
        get() = binding.dataViewHeader.customizeViewButton

    private val menuButton: FrameLayout
        get() = binding.topToolbar.root.findViewById(R.id.threeDotsButton)

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

    private val bottomPanelTranslationDelta: Float
        get() = (binding.bottomPanel.root.height + binding.bottomPanel.root.marginBottom).toFloat()

    private val actionHandler: (Int) -> Boolean = { action ->
        action == IME_ACTION_GO || action == IME_ACTION_DONE
    }

    private val swipeListener = object : OnSwipeListener() {
        override fun onSwipe(direction: Direction?): Boolean {
            if (direction == Direction.DOWN) vm.onHideViewerCustomizeSwiped()
            return true
        }
    }

    private val swipeDetector by lazy {
        GestureDetector(context, swipeListener)
    }

    private val viewerGridHeaderAdapter by lazy { ViewerGridHeaderAdapter() }

    private val viewerGridAdapter by lazy {
        ViewerGridAdapter(
            onCellClicked = vm::onGridCellClicked,
            onObjectHeaderClicked = vm::onObjectHeaderClicked,
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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideSoftInput()
        setupWindowInsetAnimation()

        setupGridAdapters()
        title.clearFocus()
        topToolbarTitle.alpha = 0f
        binding.root.setTransitionListener(transitionListener)

        with(lifecycleScope) {
            subscribe(addNewButton.clicks().throttleFirst()) { vm.proceedWithDataViewObjectCreate() }
            subscribe(addNewIconButton.buttonClicks()) { vm.proceedWithDataViewObjectCreate() }
            subscribe(addNewIconButton.iconClicks()) { vm.onNewButtonIconClicked() }
            subscribe(dataViewInfo.clicks().throttleFirst()) { type ->
                when (type) {
                    DataViewInfo.TYPE.COLLECTION_NO_ITEMS -> vm.proceedWithDataViewObjectCreate()
                    DataViewInfo.TYPE.SET_NO_QUERY -> vm.onSelectQueryButtonClicked()
                    DataViewInfo.TYPE.SET_NO_ITEMS -> vm.proceedWithDataViewObjectCreate()
                    DataViewInfo.TYPE.INIT -> {}
                }
            }
            subscribe(title.editorActionEvents(actionHandler)) {
                title.apply {
                    hideKeyboard()
                    clearFocus()
                    vm.hideTitleToolbar()
                }
            }
            subscribe(topBackButton.clicks().throttleFirst()) { vm.onBackButtonClicked() }
            subscribe(menuButton.clicks().throttleFirst()) { vm.onMenuClicked() }
            subscribe(customizeViewButton.clicks().throttleFirst()) { vm.onViewerCustomizeButtonClicked() }
            subscribe(viewerTitle.clicks().throttleFirst()) { vm.onExpandViewerMenuClicked() }
            subscribe(binding.unsupportedViewError.clicks().throttleFirst()) { vm.onUnsupportedViewErrorClicked() }

            subscribe(binding.bottomPanel.root.findViewById<FrameLayout>(R.id.btnFilter).clicks().throttleFirst()) {
                vm.onViewerFiltersClicked()
            }
            subscribe(binding.bottomPanel.root.findViewById<FrameLayout>(R.id.btnSettings).clicks()
                    .throttleFirst()
            ) {
            }
            subscribe(
                binding.bottomPanel.root.findViewById<FrameLayout>(R.id.btnSort).clicks()
                    .throttleFirst()
            ) {
                vm.onViewerSortsClicked()
            }
            subscribe(
                binding.bottomPanel.root.findViewById<FrameLayout>(R.id.btnView).clicks()
                    .throttleFirst()
            ) {
                vm.onViewerEditClicked()
            }

            subscribe(binding.bottomPanel.root.touches()) { swipeDetector.onTouchEvent(it) }

            subscribe(
                binding.bottomToolbar.shareClicks().throttleFirst()
            ) { vm.onShareButtonClicked() }

            subscribe(
                binding.bottomToolbar.searchClicks().throttleFirst()
            ) { vm.onSearchButtonClicked() }

            subscribe(
                binding.bottomToolbar.homeClicks().throttleFirst()
            ) { vm.onHomeButtonClicked() }

            subscribe(
                binding.bottomToolbar.addDocClicks().throttleFirst()
            ) { vm.onAddNewDocumentClicked() }

            binding
                .bottomToolbar
                .binding
                .btnAddDoc
                .longClicks(withHaptic = true)
                .onEach {
                    val dialog = ObjectTypeSelectionFragment.new(space = space)
                    dialog.show(childFragmentManager, "set-create-object-of-type-dialog")
                }
                .launchIn(lifecycleScope)
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

        binding.galleryView.onTaskCheckboxClicked = { id ->
            vm.onTaskCheckboxClicked(id)
        }

        binding.listView.onListItemClicked = { id ->
            vm.onObjectHeaderClicked(id)
        }

        binding.listView.onTaskCheckboxClicked = { id ->
            vm.onTaskCheckboxClicked(id)
        }

        title.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            vm.onTitleFocusChanged(hasFocus)
        }

        with(tvDescription) {
            syncFocusWithImeVisibility()
            addTextChangedListener(tvDescriptionTextWatcher)
            imeOptions = IME_ACTION_DONE
            setRawInputType(InputType.TYPE_CLASS_TEXT)
        }

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
                    action = vm::onViewersWidgetAction,
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
                        vm.hideTitleToolbar()
                        hideKeyboard()
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
    }

    private fun setupWindowInsetAnimation() {
        if (BuildConfig.USE_NEW_WINDOW_INSET_API && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            binding.bottomToolbarBox.syncTranslationWithImeVisibility(
                dispatchMode = DISPATCH_MODE_STOP
            )
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
                child.findViewById<View>(R.id.headerContainer).translationX = translationX
            }
        }
    }

    private fun showBottomPanel() {
        val animation =
            binding.bottomPanel.root.animate().translationY(-bottomPanelTranslationDelta).apply {
                duration = BOTTOM_PANEL_ANIM_DURATION
                interpolator = AccelerateDecelerateInterpolator()
            }
        animation.start()
    }

    private fun hideBottomPanel() {
        val animation = binding.bottomPanel.root.animate().translationY(0f).apply {
            duration = BOTTOM_PANEL_ANIM_DURATION
            interpolator = AccelerateDecelerateInterpolator()
        }
        animation.start()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm.navigation.observe(viewLifecycleOwner, navObserver)
        lifecycleScope.subscribe(vm.isCustomizeViewPanelVisible) { isCustomizeViewPanelVisible ->
            if (isCustomizeViewPanelVisible) showBottomPanel() else hideBottomPanel()
        }
    }

    private fun setStatus(status: SpaceSyncAndP2PStatusState?) {
        binding.topToolbar.root.findViewById<StatusBadgeWidget>(R.id.statusBadge).bind(status)
        topToolbarStatusContainer.setOnClickListener {
            vm.onSyncStatusBadgeClicked()
        }
    }

    private fun setupDataViewViewState(state: DataViewViewState) {
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
                    type = DataViewInfo.TYPE.COLLECTION_NO_ITEMS,
                    isReadOnlyAccess = !state.isCreateObjectAllowed
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
                setViewer(viewer = state.viewer)
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
                    type = DataViewInfo.TYPE.SET_NO_ITEMS,
                    isReadOnlyAccess = !state.isCreateObjectAllowed
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
                setViewer(viewer = state.viewer)
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
                setViewer(viewer = state.viewer)
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
                    type = DataViewInfo.TYPE.SET_NO_ITEMS,
                    isReadOnlyAccess = !state.isCreateObjectAllowed
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

    private fun setViewer(viewer: Viewer?) {
        when (viewer) {
            is Viewer.GridView -> {
                with(binding) {
                    unsupportedViewError.gone()
                    unsupportedViewError.text = null
                    galleryView.clear()
                    galleryView.gone()
                    listView.gone()
                    listView.setViews(emptyList())
                }
                viewerGridHeaderAdapter.submitList(viewer.columns)
                viewerGridAdapter.submitList(viewer.rows)
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
                        largeCards = viewer.largeCards
                    )
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
                    listView.setViews(viewer.items)
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
                    unsupportedViewError.gone()
                    unsupportedViewError.text = null
                }
            }
        }
    }

    private fun bindHeader(header: SetOrCollectionHeaderState.Default) {
        setupHeaderMargins(header)

        title.isEnabled = !header.isReadOnlyMode

        if (title.text.toString() != header.title.text) {
            title.pauseTextWatchers {
                title.setText(header.title.text)
            }
        }
        binding.topToolbar.root.findViewById<TextView>(R.id.tvTopToolbarTitle).text = header.title.text

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
        topToolbarThreeDotsButton.apply {
            setBackgroundResource(R.drawable.rect_object_menu_button_default)
        }
        topToolbarStatusContainer.apply {
            setBackgroundResource(R.drawable.rect_object_menu_button_default)
        }
        if (binding.root.currentState == R.id.start) {
            topToolbarThreeDotsIcon.apply {
                imageTintList = ColorStateList.valueOf(Color.WHITE)
            }
        }
    }

    private fun onCoverRemoved() {
        topToolbarThreeDotsButton.background = null
        topToolbarThreeDotsIcon.imageTintList = null
        topToolbarStatusContainer.background = null
    }

    private fun observeCommands(command: ObjectSetCommand) {
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
            is ObjectSetCommand.Modal.SetNameForCreatedObject -> {
                findNavController().safeNavigate(
                    R.id.objectSetScreen,
                    R.id.setNameForNewRecordScreen,
                    SetObjectCreateRecordFragmentBase.args(
                        ctx = command.ctx,
                        target = command.target,
                        space = command.space
                    )
                )
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
                    selectedTypes = command.selectedTypes
                )
                fr.showChildFragment()
            }
            is ObjectSetCommand.Modal.OpenEmptyDataViewSelectQueryScreen -> {
                val fr = EmptyDataViewSelectSourceFragment()
                fr.showChildFragment()
            }
            is ObjectSetCommand.Modal.OpenSelectTypeScreen -> {
                val fr = ObjectSelectTypeFragment.newInstance(
                    excludeTypes = command.excludedTypes
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
        }
    }

    private val transitionListener = object : MotionLayout.TransitionListener {
        override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) {}
        override fun onTransitionChange(
            view: MotionLayout?,
            start: Int,
            end: Int,
            progress: Float
        ) {
        }

        override fun onTransitionTrigger(view: MotionLayout?, id: Int, pos: Boolean, prog: Float) {}
        override fun onTransitionCompleted(motionLayout: MotionLayout?, id: Int) {
            if (id == R.id.start) {
                title.pauseTextWatchers { title.enableEditMode() }
                topToolbarTitle.animate().alpha(0f).setDuration(DEFAULT_ANIM_DURATION).start()
                topToolbarThreeDotsButton.apply {
                    if (background != null) {
                        background?.alpha = DRAWABLE_ALPHA_FULL
                        topToolbarThreeDotsIcon.apply {
                            imageTintList = ColorStateList.valueOf(Color.WHITE)
                        }
                    }
                }
                topToolbarStatusContainer.apply {
                    if (background != null) {
                        background?.alpha = DRAWABLE_ALPHA_FULL
                    }
                }
            }
            if (id == R.id.end) {
                title.pauseTextWatchers { title.enableReadMode() }
                topToolbarTitle.animate().alpha(1f).setDuration(DEFAULT_ANIM_DURATION).start()
                binding.topToolbar.root.findViewById<ImageView>(R.id.ivThreeDots).apply {
                    imageTintList = null
                }
                topToolbarThreeDotsButton.apply {
                    background?.alpha = DRAWABLE_ALPHA_ZERO
                }
                topToolbarStatusContainer.apply {
                    background?.alpha = DRAWABLE_ALPHA_ZERO
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        title.addTextChangedListener(titleTextWatcher)

        vm.navPanelState.onEach {
            if (hasBinding) {
                binding.bottomToolbar.setState(it)
            }
        }.launchIn(lifecycleScope)

        jobs += lifecycleScope.subscribe(vm.commands) { observeCommands(it) }
        jobs += lifecycleScope.subscribe(vm.header) { header ->
            when(header) {
                is SetOrCollectionHeaderState.Default -> {
                    bindHeader(header)
                }
                is SetOrCollectionHeaderState.None -> {
                    // Do nothing.
                }
            }
        }
        jobs += lifecycleScope.subscribe(vm.currentViewer) { setupDataViewViewState(it) }
        jobs += lifecycleScope.subscribe(vm.error) { err ->
            if (err.isNullOrEmpty())
                binding.tvError.gone()
            else {
                binding.tvError.text = err
                binding.tvError.visible()
            }
        }
        jobs += lifecycleScope.subscribe(vm.pagination) { (index, count) ->
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

        vm.onStart(view = view)
    }

    override fun onStop() {
        super.onStop()
        title.removeTextChangedListener(titleTextWatcher)
        vm.onStop()
    }

    override fun onDestroyView() {
        viewerGridAdapter.clear()
        super.onDestroyView()
    }

    private fun setupOnBackPressedDispatcher() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            when {
                childFragmentManager.backStackEntryCount > 0 -> childFragmentManager.popBackStack()
                vm.isCustomizeViewPanelVisible.value -> vm.onHideViewerCustomizeSwiped()
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
    }

    override fun releaseDependencies() {
        componentManager().objectSetComponent.release(ctx)
    }

    fun onCloseCurrentObject() {
        vm.onCloseObject()
    }

    companion object {
        const val CONTEXT_ID_KEY = "arg.object_set.context"
        const val SPACE_ID_KEY = "arg.object_set.space-id"
        private const val INITIAL_VIEW_ID_KEY = "arg.object_set.initial-view"
        val EMPTY_TAG = null
        const val BOTTOM_PANEL_ANIM_DURATION = 150L
        const val DEFAULT_ANIM_DURATION = 300L
        const val DRAWABLE_ALPHA_FULL = 255
        const val DRAWABLE_ALPHA_ZERO = 0

        fun args(
            ctx: Id,
            space: Id,
            view: Id? = null
        ) = bundleOf(
            CONTEXT_ID_KEY to ctx,
            SPACE_ID_KEY to space,
            INITIAL_VIEW_ID_KEY to view
        )
    }
}