package com.anytypeio.anytype.ui.sets

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.GestureDetector
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.EditorInfo.IME_ACTION_GO
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.marginBottom
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_ui.extensions.setImageOrNull
import com.anytypeio.anytype.core_ui.features.dataview.*
import com.anytypeio.anytype.core_ui.reactive.*
import com.anytypeio.anytype.core_utils.OnSwipeListener
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.hideKeyboard
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.NonScrollLinearLayoutManager
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import com.anytypeio.anytype.presentation.sets.ObjectSetCommand
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModelFactory
import com.anytypeio.anytype.presentation.sets.model.FilterExpression
import com.anytypeio.anytype.presentation.sets.model.SortingExpression
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.ui.base.NavigationFragment
import com.anytypeio.anytype.ui.database.modals.ObjectSetObjectRelationValueFragment
import com.anytypeio.anytype.ui.relations.CreateDataViewRelationFragment
import com.anytypeio.anytype.ui.relations.CreateDataViewRelationFragment.OnAddDataViewRelationRequestReceiver
import com.anytypeio.anytype.ui.relations.ObjectRelationDateValueFragment
import com.anytypeio.anytype.ui.relations.ObjectRelationDateValueFragment.EditObjectRelationDateValueReceiver
import com.anytypeio.anytype.ui.relations.ObjectRelationTextValueFragment
import com.anytypeio.anytype.ui.relations.ObjectRelationTextValueFragment.EditObjectRelationTextValueReceiver
import com.anytypeio.anytype.ui.sets.modals.*
import com.anytypeio.anytype.ui.sets.modals.sort.ViewerSortFragment
import kotlinx.android.synthetic.main.fragment_object_set.*
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

open class ObjectSetFragment :
    NavigationFragment(R.layout.fragment_object_set),
    OnAddDataViewRelationRequestReceiver,
    EditObjectRelationTextValueReceiver,
    EditObjectRelationDateValueReceiver{

    private val bottomPanelTranslationDelta: Float
        get() = (bottomPanel.height + bottomPanel.marginBottom).toFloat()

    private val actionHandler: (Int) -> Boolean = { action ->
        action == IME_ACTION_GO || action == IME_ACTION_DONE
    }

    private val swipeListener = object : OnSwipeListener() {
        override fun onSwipe(direction: Direction?): Boolean {
            if (direction == Direction.DOWN) hideBottomPanel()
            return true
        }
    }

    private val swipeDetector by lazy {
        GestureDetector(context, swipeListener)
    }

    private val viewerTabAdapter by lazy {
        ViewerTabItemAdapter(
            onAddNewViewerClicked = { vm.onCreateNewViewerClicked() },
            onViewerTabClicked = { vm.onViewerTabClicked(it) }
        )
    }
    private val viewerGridHeaderAdapter by lazy {
        ViewerGridHeaderAdapter(
            onCreateNewColumnClicked = { vm.onAddNewDataViewRelation() }
        )
    }
    private val viewerGridAdapter by lazy {
        ViewerGridAdapter(
            onCellClicked = vm::onGridCellClicked,
            onCellAction = vm::onCellAction,
            onObjectHeaderClicked = vm::onObjectHeaderClicked
        )
    }
    private val viewerListAdapter by lazy { ViewerListAdapter() }
    private val viewerTypeAdapter by lazy {
        ViewerTypeAdapter(
            gridHeaderAdapter = viewerGridHeaderAdapter,
            gridAdapter = viewerGridAdapter,
            listAdapter = viewerListAdapter
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
        topRecycler.apply {
            layoutManager = NonScrollLinearLayoutManager(requireContext())
            adapter = viewerTypeAdapter
            setHasFixedSize(true)
        }
        title.clearFocus()
        root.setTransitionListener(transitionListener)
        with(lifecycleScope) {
            subscribe(addNewButton.clicks()) { vm.onCreateNewRecord() }
            subscribe(title.afterTextChanges()) { vm.onTitleChanged(it.toString()) }
            subscribe(title.editorActionEvents(actionHandler)) {
                title.apply {
                    hideKeyboard()
                    clearFocus()
                }
            }
            subscribe(objectSetIcon.clicks()) { vm.onIconClicked() }
            subscribe(customizeViewButton.clicks()) {
                showBottomPanel()
            }
            subscribe(icBack.clicks()) { vm.onBackButtonPressed() }
            subscribe(tvCurrentViewerName.clicks()) { vm.onExpandViewerMenuClicked() }
            subscribe(bottomPanel.findViewById<FrameLayout>(R.id.btnFilter).clicks()) {
                vm.onViewerFiltersClicked()
            }
            subscribe(bottomPanel.findViewById<FrameLayout>(R.id.btnRelations).clicks()) {
                vm.onViewerRelationsClicked()
            }
            subscribe(bottomPanel.findViewById<FrameLayout>(R.id.btnSort).clicks()) {
                val fr = ViewerSortFragment.new(ctx)
                fr.show(childFragmentManager, EMPTY_TAG)
            }
            subscribe(bottomPanel.findViewById<FrameLayout>(R.id.btnGroup).clicks()) {
                toast(getString(R.string.coming_soon))
            }

            subscribe(bottomPanel.touches()) { swipeDetector.onTouchEvent(it) }
        }
    }

    private fun showBottomPanel() {
        val animation = bottomPanel.animate().translationY(-bottomPanelTranslationDelta).apply {
            duration = BOTTOM_PANEL_ANIM_DURATION
            interpolator = AccelerateDecelerateInterpolator()
        }
        animation.start()
    }

    private fun hideBottomPanel() {
        val animation = bottomPanel.animate().translationY(0f).apply {
            duration = BOTTOM_PANEL_ANIM_DURATION
            interpolator = AccelerateDecelerateInterpolator()
        }
        animation.start()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm.navigation.observe(viewLifecycleOwner, navObserver)
        lifecycleScope.subscribe(vm.toasts.stream()) { toast(it) }
    }

    private fun observeGrid(viewer: Viewer) {
        tvCurrentViewerName.text = viewer.title
        when (viewer) {
            is Viewer.GridView -> {
                viewerTypeAdapter.update(viewer)
            }
            is Viewer.ListView -> {
                // TODO
            }
        }
    }

    private fun observeHeader(title: BlockView.Title.Document) {
        checkMainThread()
        this.title.setText(title.text)
        this.titleSmall.text = title.text
        if (title.emoji != null) {
            objectSetIcon.scaleType = ImageView.ScaleType.CENTER
            objectSetIcon.setEmoji(unicode = title.emoji)
        } else {
            objectSetIcon.scaleType = ImageView.ScaleType.CENTER_CROP
            objectSetIcon.setImageOrNull(image = title.image)
        }
    }

    private fun observeCommands(command: ObjectSetCommand) {
        when (command) {
            is ObjectSetCommand.Modal.CreateDataViewRelation -> {
                val fr = CreateDataViewRelationFragment.new(
                    ctx = command.ctx,
                    target = command.target
                )
                fr.show(childFragmentManager, EMPTY_TAG)
            }
            is ObjectSetCommand.Modal.EditGridTextCell -> {
                val fr = ObjectRelationTextValueFragment.new(
                    ctx = ctx,
                    relationId = command.relationId,
                    objectId = command.recordId,
                    flow = ObjectRelationTextValueFragment.FLOW_DATAVIEW
                )
                fr.show(childFragmentManager, EMPTY_TAG)
            }
            is ObjectSetCommand.Modal.EditGridDateCell -> {
                val fr = ObjectRelationDateValueFragment.new(
                    ctx = ctx,
                    relationId = command.relationId,
                    objectId = command.objectId,
                    flow = ObjectRelationDateValueFragment.FLOW_DATAVIEW
                )
                fr.show(childFragmentManager, EMPTY_TAG)
            }
            is ObjectSetCommand.Modal.EditRelationCell -> {
                val fr = ObjectSetObjectRelationValueFragment.new(
                    ctx = command.ctx,
                    target = command.target,
                    dataview = command.dataview,
                    relation = command.relation,
                    viewer = command.viewer
                )
                fr.show(childFragmentManager, EMPTY_TAG)
            }
            is ObjectSetCommand.Modal.ViewerCustomizeScreen -> {
                val fr = ViewerBottomSheetRootFragment.new(
                    ctx = command.ctx,
                    viewer = command.viewer
                )
                fr.show(childFragmentManager, EMPTY_TAG)
            }
            is ObjectSetCommand.Modal.CreateViewer -> {
                val fr = CreateDataViewViewerFragment.new(
                    ctx = command.ctx,
                    target = command.target
                )
                fr.show(childFragmentManager, EMPTY_TAG)
            }
            is ObjectSetCommand.Modal.EditDataViewViewer -> {
                val fr = EditDataViewViewerFragment.new(
                    ctx = command.ctx,
                    viewer = command.viewer,
                    name = command.name
                )
                fr.show(childFragmentManager, EMPTY_TAG)
            }
            is ObjectSetCommand.Modal.ManageViewer -> {
                val fr = ManageViewerFragment.new(ctx = command.ctx, dataview = command.dataview)
                fr.show(childFragmentManager, EMPTY_TAG)
            }
            is ObjectSetCommand.Modal.ModifyViewerRelationOrder -> {
                val fr = ViewerRelationsFragment.new(
                    ctx = command.ctx
                )
                fr.show(childFragmentManager, EMPTY_TAG)
            }
            is ObjectSetCommand.Modal.SetNameForCreatedRecord -> {
                val dialog = SetObjectSetRecordNameFragment.new(command.ctx)
                dialog.show(childFragmentManager, EMPTY_TAG)
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
                //todo show edit icon widget
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
                    ctx = command.ctx
                )
                fr.show(childFragmentManager, EMPTY_TAG)
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
                title.enableEditMode()
            }
            if (id == R.id.end) {
                title.enableReadMode()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        jobs += lifecycleScope.subscribe(vm.commands) { observeCommands(it) }
        jobs += lifecycleScope.subscribe(vm.header.filterNotNull()) { observeHeader(it) }
        jobs += lifecycleScope.subscribe(vm.viewerTabs) { viewerTabAdapter.update(it) }
        jobs += lifecycleScope.subscribe(vm.viewerGrid) { observeGrid(it) }
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
            if (childFragmentManager.backStackEntryCount > 0) {
                childFragmentManager.popBackStack()
            } else {
                vm.onSystemBackPressed()
            }
        }
    }

    override fun onAddDataViewRelationRequest(
        context: String,
        target: Id,
        name: String,
        format: Relation.Format
    ) = vm.onRelationPrototypeCreated(
        context = context,
        target = target,
        name = name,
        format = format
    )

    fun onViewerNewSortsRequest(sorts: List<SortingExpression>) {
        vm.onUpdateViewerSorting(sorts)
    }

    fun onViewerNewFiltersRequest(filters: List<FilterExpression>) {
        vm.onUpdateViewerFilters(filters)
    }

    override fun onRelationTextValueChanged(
        ctx: String,
        text: String,
        objectId: String,
        relationId: String
    ) = vm.onRelationTextValueChanged(
        ctx = ctx,
        value = text,
        objectId = objectId,
        relationKey = relationId
    )

    override fun onRelationTextNumberValueChanged(
        ctx: Id,
        number: Number,
        objectId: Id,
        relationId: Id
    ) = vm.onRelationTextValueChanged(
        ctx = ctx,
        value = number,
        objectId = objectId,
        relationKey = relationId
    )

    override fun onRelationDateValueChanged(
        ctx: Id,
        timeInSeconds: Number?,
        objectId: Id,
        relationId: Id
    ) {
        vm.onRelationTextValueChanged(
            ctx = ctx,
            value = timeInSeconds,
            objectId = objectId,
            relationKey = relationId
        )
    }

    override fun injectDependencies() {
        componentManager().objectSetComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectSetComponent.release(ctx)
    }

    companion object {
        const val CONTEXT_ID_KEY = "arg.object_set.context"
        val EMPTY_TAG = null
        const val BOTTOM_PANEL_ANIM_DURATION = 150L
    }
}