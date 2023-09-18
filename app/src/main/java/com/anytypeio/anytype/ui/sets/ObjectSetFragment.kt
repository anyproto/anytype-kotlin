package com.anytypeio.anytype.ui.sets

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
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
import androidx.compose.ui.platform.ViewCompositionStrategy
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
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.SyncStatus
import com.anytypeio.anytype.core_ui.extensions.setEmojiOrNull
import com.anytypeio.anytype.core_ui.features.dataview.ViewerGridAdapter
import com.anytypeio.anytype.core_ui.features.dataview.ViewerGridHeaderAdapter
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_ui.reactive.editorActionEvents
import com.anytypeio.anytype.core_ui.reactive.touches
import com.anytypeio.anytype.core_ui.tools.DefaultTextWatcher
import com.anytypeio.anytype.core_ui.views.ButtonPrimarySmallIcon
import com.anytypeio.anytype.core_ui.widgets.FeaturedRelationGroupWidget
import com.anytypeio.anytype.core_ui.widgets.ObjectTypeTemplatesWidget
import com.anytypeio.anytype.core_ui.widgets.StatusBadgeWidget
import com.anytypeio.anytype.core_ui.widgets.dv.ViewerEditWidget
import com.anytypeio.anytype.core_ui.widgets.dv.ViewerLayoutWidget
import com.anytypeio.anytype.core_ui.widgets.dv.ViewersWidget
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_ui.widgets.toolbar.DataViewInfo
import com.anytypeio.anytype.core_utils.OnSwipeListener
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.drawable
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.hideKeyboard
import com.anytypeio.anytype.core_utils.ext.hideSoftInput
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.safeNavigate
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.syncFocusWithImeVisibility
import com.anytypeio.anytype.core_utils.ext.syncTranslationWithImeVisibility
import com.anytypeio.anytype.core_utils.ext.throttleFirst
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.databinding.FragmentObjectSetBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.editor.cover.CoverColor
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient
import com.anytypeio.anytype.presentation.sets.ViewersWidgetUi
import com.anytypeio.anytype.presentation.sets.DataViewViewState
import com.anytypeio.anytype.presentation.sets.ObjectSetCommand
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModelFactory
import com.anytypeio.anytype.presentation.sets.SetOrCollectionHeaderState
import com.anytypeio.anytype.presentation.sets.ViewerEditWidgetUi
import com.anytypeio.anytype.presentation.sets.ViewerLayoutWidgetUi
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.ui.base.NavigationFragment
import com.anytypeio.anytype.ui.editor.cover.SelectCoverObjectSetFragment
import com.anytypeio.anytype.ui.editor.modals.IconPickerFragmentBase
import com.anytypeio.anytype.ui.editor.sheets.ObjectMenuBaseFragment
import com.anytypeio.anytype.ui.objects.BaseObjectTypeChangeFragment
import com.anytypeio.anytype.ui.objects.types.pickers.DataViewSelectSourceFragment
import com.anytypeio.anytype.ui.objects.types.pickers.EmptyDataViewSelectSourceFragment
import com.anytypeio.anytype.ui.objects.types.pickers.OnDataViewSelectSourceAction
import com.anytypeio.anytype.ui.relations.RelationDateValueFragment
import com.anytypeio.anytype.ui.relations.RelationDateValueFragment.DateValueEditReceiver
import com.anytypeio.anytype.ui.relations.RelationTextValueFragment
import com.anytypeio.anytype.ui.relations.RelationTextValueFragment.TextValueEditReceiver
import com.anytypeio.anytype.ui.relations.RelationValueBaseFragment
import com.anytypeio.anytype.ui.relations.RelationValueDVFragment
import com.anytypeio.anytype.ui.sets.modals.CreateDataViewViewerFragment
import com.anytypeio.anytype.ui.sets.modals.EditDataViewViewerFragment
import com.anytypeio.anytype.ui.sets.modals.ManageViewerFragment
import com.anytypeio.anytype.ui.sets.modals.ObjectSetSettingsFragment
import com.anytypeio.anytype.ui.sets.modals.SetObjectCreateRecordFragmentBase
import com.anytypeio.anytype.ui.sets.modals.sort.ViewerSortFragment
import com.anytypeio.anytype.ui.templates.EditorTemplateFragment.Companion.ARG_TEMPLATE_ID
import com.bumptech.glide.Glide
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

open class ObjectSetFragment :
    NavigationFragment<FragmentObjectSetBinding>(R.layout.fragment_object_set),
    TextValueEditReceiver,
    DateValueEditReceiver,
    OnDataViewSelectSourceAction {

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

    private val topToolbarTitle: TextView
        get() = binding.topToolbar.root.findViewById(R.id.tvTopToolbarTitle)

    private val topToolbarThreeDotsButton: ViewGroup
        get() = binding.topToolbar.root.findViewById(R.id.threeDotsButton)

    private val topToolbarStatusContainer: ViewGroup
        get() = binding.topToolbar.root.findViewById(R.id.statusContainer)

    private val topToolbarThreeDotsIcon: ImageView
        get() = binding.topToolbar.root.findViewById(R.id.ivThreeDots)

    private val topToolbarStatusText: TextView
        get() = binding.topToolbar.root.findViewById(R.id.tvStatus)

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

    private val ctx: String get() = argString(CONTEXT_ID_KEY)

    @Inject
    lateinit var factory: ObjectSetViewModelFactory
    private val vm: ObjectSetViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupOnBackPressedDispatcher()
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
                }
            }
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

            subscribe(binding.bottomToolbar.homeClicks().throttleFirst()) { vm.onHomeButtonClicked() }
            subscribe(
                binding.bottomToolbar.backClicks().throttleFirst()
            ) { vm.onBackButtonClicked() }
            subscribe(
                binding.bottomToolbar.searchClicks().throttleFirst()
            ) { vm.onSearchButtonClicked() }
            subscribe(
                binding.bottomToolbar.addDocClicks().throttleFirst()
            ) { vm.onAddNewDocumentClicked() }
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

        title.addTextChangedListener(
            DefaultTextWatcher { vm.onTitleChanged(it.toString()) }
        )

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
                ObjectTypeTemplatesWidget(
                    state = vm.templatesWidgetState.collectAsStateWithLifecycle().value,
                    onDismiss = vm::onDismissTemplatesWidget,
                    itemClick = vm::onTemplateItemClicked,
                    editClick = vm::onEditTemplateButtonClicked,
                    doneClick = vm::onDoneTemplateButtonClicked,
                    moreClick = vm::onMoreTemplateButtonClicked,
                    menuClick = vm::onMoreMenuClicked,
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
                    action = vm::onViewersWidgetAction
                )
            }
        }

        binding.viewerEditWidget.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ViewerEditWidget(
                    state = vm.viewerEditWidgetState.collectAsStateWithLifecycle().value,
                    action = vm::onViewerEditWidgetAction
                )
            }
        }

        binding.viewerLayoutWidget.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ViewerLayoutWidget(
                    uiState = vm.viewerLayoutWidgetState.collectAsStateWithLifecycle().value,
                    action = vm::onViewerLayoutWidgetAction,
                    scope = lifecycleScope
                )
            }
        }
    }

    private fun setupWindowInsetAnimation() {
        if (BuildConfig.USE_NEW_WINDOW_INSET_API && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            binding.bottomToolbar.syncTranslationWithImeVisibility(
                dispatchMode = DISPATCH_MODE_STOP
            )
            title.syncFocusWithImeVisibility()
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
        lifecycleScope.subscribe(vm.toasts) { toast(it) }
        lifecycleScope.subscribe(vm.status) { setStatus(it) }
        lifecycleScope.subscribe(vm.isCustomizeViewPanelVisible) { isCustomizeViewPanelVisible ->
            if (isCustomizeViewPanelVisible) showBottomPanel() else hideBottomPanel()
        }
    }

    private fun setStatus(status: SyncStatus) {
        binding.topToolbar.root.findViewById<StatusBadgeWidget>(R.id.statusBadge).bind(status)
        val tvStatus = binding.topToolbar.root.findViewById<TextView>(R.id.tvStatus)
        when (status) {
            SyncStatus.UNKNOWN -> tvStatus.setText(R.string.sync_status_unknown)
            SyncStatus.FAILED -> tvStatus.setText(R.string.sync_status_failed)
            SyncStatus.OFFLINE -> tvStatus.setText(R.string.sync_status_offline)
            SyncStatus.SYNCING -> tvStatus.setText(R.string.sync_status_syncing)
            SyncStatus.SYNCED -> tvStatus.setText(R.string.sync_status_synced)
            SyncStatus.INCOMPATIBLE_VERSION -> tvStatus.setText(R.string.sync_status_incompatible)
        }
        topToolbarStatusContainer.setOnClickListener {
            when (status) {
                SyncStatus.UNKNOWN -> toast(getString(R.string.sync_status_toast_unknown))
                SyncStatus.FAILED -> toast(getString(R.string.sync_status_toast_failed))
                SyncStatus.OFFLINE -> toast(getString(R.string.sync_status_toast_offline))
                SyncStatus.SYNCING -> toast(getString(R.string.sync_status_toast_syncing))
                SyncStatus.SYNCED -> toast(getString(R.string.sync_status_toast_synced))
                SyncStatus.INCOMPATIBLE_VERSION -> toast(getString(R.string.sync_status_toast_incompatible))
                else -> {
                    Timber.i("Missed sync status")
                }
            }
        }
    }

    private fun setupDataViewViewState(state: DataViewViewState) {
        when (state) {
            DataViewViewState.Collection.NoView, DataViewViewState.Set.NoView -> {
                topToolbarThreeDotsButton.visible()
                topToolbarStatusContainer.visible()
                initView.gone()
                header.visible()
                dataViewHeader.gone()
                dataViewInfo.hide()
                toast(getString(R.string.set_collection_view_not_present))
                setViewer(viewer = null)
            }
            is DataViewViewState.Collection.NoItems -> {
                topToolbarThreeDotsButton.visible()
                topToolbarStatusContainer.visible()
                initView.gone()
                header.visible()
                dataViewHeader.visible()
                viewerTitle.isEnabled = true
                setupNewButtons(state.hasTemplates)
                customizeViewButton.isEnabled = true
                setCurrentViewerName(state.title)
                dataViewInfo.show(DataViewInfo.TYPE.COLLECTION_NO_ITEMS)
                setViewer(viewer = null)
            }
            is DataViewViewState.Collection.Default -> {
                topToolbarThreeDotsButton.visible()
                topToolbarStatusContainer.visible()
                header.visible()
                initView.gone()
                dataViewHeader.visible()
                viewerTitle.isEnabled = true
                setupNewButtons(state.hasTemplates)
                customizeViewButton.isEnabled = true
                setCurrentViewerName(state.viewer?.title)
                dataViewInfo.hide()
                setViewer(viewer = state.viewer)
            }
            DataViewViewState.Set.NoQuery -> {
                topToolbarThreeDotsButton.visible()
                topToolbarStatusContainer.visible()
                initView.gone()
                header.visible()
                dataViewHeader.visible()
                viewerTitle.isEnabled = false
                addNewButton.isEnabled = false
                customizeViewButton.isEnabled = false
                setCurrentViewerName(getString(R.string.viewer_default_title))
                dataViewInfo.show(type = DataViewInfo.TYPE.SET_NO_QUERY)
                setViewer(viewer = null)
            }
            is DataViewViewState.Set.NoItems -> {
                topToolbarThreeDotsButton.visible()
                topToolbarStatusContainer.visible()
                initView.gone()
                header.visible()
                dataViewHeader.visible()
                viewerTitle.isEnabled = true
                setupNewButtons(state.hasTemplates)
                customizeViewButton.isEnabled = true
                setCurrentViewerName(state.title)
                dataViewInfo.show(type = DataViewInfo.TYPE.SET_NO_ITEMS)
                setViewer(viewer = null)
            }
            is DataViewViewState.Set.Default -> {
                topToolbarThreeDotsButton.visible()
                topToolbarStatusContainer.visible()
                initView.gone()
                header.visible()
                dataViewHeader.visible()
                viewerTitle.isEnabled = true
                setupNewButtons(state.hasTemplates)
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
        }
    }

    private fun setCurrentViewerName(title: String?) {
        viewerTitle.text = if (title.isNullOrEmpty()) {
            getString(R.string.untitled)
        } else {
            title
        }
    }

    private fun setupNewButtons(isTemplatesAllowed: Boolean) {
        if (isTemplatesAllowed) {
            addNewButton.gone()
            addNewIconButton.visible()
        } else {
            addNewButton.visible()
            addNewButton.isEnabled = true
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
                    unsupportedViewError.visible()
                    unsupportedViewError.text = viewer.error
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
                }
            }
        }
    }

    private fun bindHeader(header: SetOrCollectionHeaderState.Default) {
        setupHeaderMargins(header)
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
                .onEach { vm.onIconClicked() }
                .launchIn(lifecycleScope)
        }

        binding.objectHeader.root.findViewById<View>(R.id.imageIcon).apply {
            if (header.title.image != null) visible() else gone()
            jobs += this.clicks()
                .throttleFirst()
                .onEach { vm.onIconClicked() }
                .launchIn(lifecycleScope)
        }

        binding.objectHeader.root.findViewById<ImageView>(R.id.emojiIcon)
            .setEmojiOrNull(header.title.emoji)

        if (header.title.image != null) {
            binding.objectHeader.root.findViewById<ImageView>(R.id.imageIcon).apply {
                Glide
                    .with(this)
                    .load(header.title.image)
                    .centerCrop()
                    .into(this)
            }
        } else {
            binding.objectHeader.root.findViewById<ImageView>(R.id.imageIcon).setImageDrawable(null)
        }

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
                    Glide
                        .with(this)
                        .load(coverImage)
                        .centerCrop()
                        .into(this)
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
            topToolbarStatusText.setTextColor(Color.WHITE)
            topToolbarThreeDotsIcon.apply {
                imageTintList = ColorStateList.valueOf(Color.WHITE)
            }
        }
    }

    private fun onCoverRemoved() {
        topToolbarThreeDotsButton.background = null
        topToolbarThreeDotsIcon.imageTintList = null
        topToolbarStatusContainer.background = null
        topToolbarStatusText.setTextColor(requireContext().getColor(R.color.default_status_text_color))
    }

    private fun observeCommands(command: ObjectSetCommand) {
        when (command) {
            is ObjectSetCommand.Modal.Menu -> {
                findNavController().safeNavigate(
                    R.id.objectSetScreen,
                    R.id.objectSetMainMenuScreen,
                    bundleOf(
                        ObjectMenuBaseFragment.CTX_KEY to command.ctx,
                        ObjectMenuBaseFragment.IS_ARCHIVED_KEY to command.isArchived,
                        ObjectMenuBaseFragment.IS_FAVORITE_KEY to command.isFavorite,
                        ObjectMenuBaseFragment.IS_PROFILE_KEY to false,
                        ObjectMenuBaseFragment.IS_LOCKED_KEY to false,
                        ObjectMenuBaseFragment.FROM_NAME to title.text.toString()
                    )
                )
            }
            is ObjectSetCommand.Modal.EditGridTextCell -> {
                val fr = RelationTextValueFragment.new(
                    ctx = ctx,
                    objectId = command.recordId,
                    flow = RelationTextValueFragment.FLOW_DATAVIEW,
                    relationKey = command.relationKey
                )
                fr.showChildFragment(EMPTY_TAG)
            }
            is ObjectSetCommand.Modal.EditIntrinsicTextRelation -> {
                val fr = RelationTextValueFragment.new(
                    ctx = ctx,
                    objectId = ctx,
                    flow = RelationTextValueFragment.FLOW_SET_OR_COLLECTION,
                    relationKey = command.relation
                )
                fr.showChildFragment(EMPTY_TAG)
            }
            is ObjectSetCommand.Modal.EditIntrinsicRelationValue -> {
                val fr = RelationValueDVFragment().apply {
                    arguments = RelationValueDVFragment.args(
                        ctx = command.ctx,
                        target = command.ctx,
                        isIntrinsic = true,
                        targetTypes = emptyList(),
                        relation = command.relation
                    )
                }
                fr.showChildFragment()
            }
            is ObjectSetCommand.Modal.EditGridDateCell -> {
                val fr = RelationDateValueFragment.new(
                    ctx = ctx,
                    objectId = command.objectId,
                    flow = RelationDateValueFragment.FLOW_DV,
                    relationKey = command.relationKey
                )
                fr.showChildFragment(EMPTY_TAG)
            }
            is ObjectSetCommand.Modal.EditRelationCell -> {
                findNavController().safeNavigate(
                    R.id.objectSetScreen,
                    R.id.dataViewRelationValueScreen,
                    bundleOf(
                        RelationValueBaseFragment.CTX_KEY to command.ctx,
                        RelationValueBaseFragment.TARGET_KEY to command.target,
                        RelationValueBaseFragment.DV_KEY to command.dataview,
                        RelationValueBaseFragment.RELATION_KEY to command.relationKey,
                        RelationValueBaseFragment.VIEWER_KEY to command.viewer,
                        RelationValueBaseFragment.TARGET_TYPES_KEY to command.targetObjectTypes,
                        RelationValueBaseFragment.IS_LOCKED_KEY to false
                    )
                )
            }
            is ObjectSetCommand.Modal.OpenSettings -> {
                val fr = ObjectSetSettingsFragment.new(
                    ctx = command.ctx,
                    dv = command.dv,
                    viewer = command.viewer
                )
                fr.showChildFragment(EMPTY_TAG)
            }
            is ObjectSetCommand.Modal.SetNameForCreatedObject -> {
                findNavController().safeNavigate(
                    R.id.objectSetScreen,
                    R.id.setNameForNewRecordScreen,
                    bundleOf(
                        SetObjectCreateRecordFragmentBase.CONTEXT_KEY to command.ctx,
                        SetObjectCreateRecordFragmentBase.TARGET_KEY to command.target
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
                        IconPickerFragmentBase.ARG_CONTEXT_ID_KEY to ctx,
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
                )
                fr.showChildFragment(EMPTY_TAG)
            }
            is ObjectSetCommand.Modal.ModifyViewerSorts -> {
                val fr = ViewerSortFragment.new(ctx = ctx, viewer = command.viewer)
                fr.showChildFragment(EMPTY_TAG)
            }
            is ObjectSetCommand.Modal.OpenCoverActionMenu -> {
                findNavController().safeNavigate(
                    R.id.objectSetScreen,
                    R.id.action_objectSetScreen_to_objectSetCoverScreen,
                    bundleOf(SelectCoverObjectSetFragment.CTX_KEY to command.ctx)
                )
            }
            is ObjectSetCommand.Modal.CreateBookmark -> {
                findNavController().safeNavigate(
                    R.id.objectSetScreen,
                    R.id.setUrlForNewBookmark,
                    bundleOf(SetObjectCreateRecordFragmentBase.CONTEXT_KEY to command.ctx))
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
            is ObjectSetCommand.Modal.CreateViewer -> {
                val fr = CreateDataViewViewerFragment.new(
                    ctx = command.ctx,
                    target = command.target
                )
                fr.showChildFragment(EMPTY_TAG)
            }
            is ObjectSetCommand.Modal.EditDataViewViewer -> {
                val fr = EditDataViewViewerFragment.new(
                    ctx = command.ctx,
                    viewer = command.viewer
                )
                fr.showChildFragment(EMPTY_TAG)
            }
            is ObjectSetCommand.Modal.ManageViewer -> {
                val fr = ManageViewerFragment.new(ctx = command.ctx, dv = command.dataview)
                fr.showChildFragment(EMPTY_TAG)
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
                topToolbarStatusText.animate().alpha(1f).setDuration(DEFAULT_ANIM_DURATION).start()
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
                        topToolbarStatusText.setTextColor(Color.WHITE)
                    }
                }
            }
            if (id == R.id.end) {
                title.pauseTextWatchers { title.enableReadMode() }
                topToolbarStatusText.animate().alpha(0f).setDuration(DEFAULT_ANIM_DURATION).start()
                topToolbarTitle.animate().alpha(1f).setDuration(DEFAULT_ANIM_DURATION).start()
                binding.topToolbar.root.findViewById<ImageView>(R.id.ivThreeDots).apply {
                    imageTintList = null
                }
                topToolbarThreeDotsButton.apply {
                    background?.alpha = DRAWABLE_ALPHA_ZERO
                }
                topToolbarStatusContainer.apply {
                    background?.alpha = DRAWABLE_ALPHA_ZERO
                    topToolbarStatusText.setTextColor(
                        context.getColor(R.color.default_status_text_color)
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        jobs += lifecycleScope.subscribe(vm.commands) { observeCommands(it) }
        jobs += lifecycleScope.subscribe(vm.header) { header ->
            when(header) {
                is SetOrCollectionHeaderState.Default -> {
                    bindHeader(header)
                }
                is SetOrCollectionHeaderState.None -> {

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
        vm.onStart(ctx)
    }

    override fun onStop() {
        super.onStop()
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
                vm.templatesWidgetState.value.showWidget -> vm.onDismissTemplatesWidget()
                vm.viewersWidgetState.value.showWidget -> handleViewersWidgetState()
                vm.viewerEditWidgetState.value.showWidget -> handleViewerEditWidgetState()
                else -> vm.onSystemBackPressed()
            }
        }
    }

    private fun handleViewersWidgetState() = when {
        vm.viewerEditWidgetState.value.showWidget -> handleViewerEditWidgetState()
        else -> vm.onViewersWidgetAction(ViewersWidgetUi.Action.Dismiss)
    }

    private fun handleViewerEditWidgetState() = when {
        vm.viewerLayoutWidgetState.value.showWidget -> vm.onViewerLayoutWidgetAction(
            ViewerLayoutWidgetUi.Action.Dismiss
        )
        else -> vm.onViewerEditWidgetAction(ViewerEditWidgetUi.Action.Dismiss)
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

    override fun onProceedWithSelectSource(id: Id) {
        vm.onObjectSetQueryPicked(query = id)
    }

    override fun injectDependencies() {
        componentManager().objectSetComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectSetComponent.release(ctx)
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentObjectSetBinding = FragmentObjectSetBinding.inflate(
        inflater, container, false
    )

    private fun observeSelectingTemplate() {
        val navController = findNavController()
        val navBackStackEntry = navController.getBackStackEntry(R.id.objectSetScreen)
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME
                && navBackStackEntry.savedStateHandle.contains(ARG_TEMPLATE_ID)) {
                val result = navBackStackEntry.savedStateHandle.get<String>(ARG_TEMPLATE_ID);
                if (!result.isNullOrBlank()) {
                    navBackStackEntry.savedStateHandle.remove<String>(ARG_TEMPLATE_ID)
                    vm.proceedWithDataViewObjectCreate(result)
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

    companion object {
        const val CONTEXT_ID_KEY = "arg.object_set.context"
        val EMPTY_TAG = null
        const val BOTTOM_PANEL_ANIM_DURATION = 150L
        const val DEFAULT_ANIM_DURATION = 300L
        const val DRAWABLE_ALPHA_FULL = 255
        const val DRAWABLE_ALPHA_ZERO = 0
    }
}