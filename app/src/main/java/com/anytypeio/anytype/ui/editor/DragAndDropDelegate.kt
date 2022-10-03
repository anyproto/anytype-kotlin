package com.anytypeio.anytype.ui.editor

import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Point
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_ui.features.editor.BlockAdapter
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.DefaultEditorDragShadow
import com.anytypeio.anytype.core_ui.features.editor.DragAndDropConfig
import com.anytypeio.anytype.core_ui.features.editor.EditorDragAndDropListener
import com.anytypeio.anytype.core_ui.features.editor.SupportNesting
import com.anytypeio.anytype.core_ui.features.editor.TextInputDragShadow
import com.anytypeio.anytype.core_ui.features.editor.holders.media.Video
import com.anytypeio.anytype.core_ui.features.editor.holders.other.Code
import com.anytypeio.anytype.core_ui.features.editor.holders.other.Title
import com.anytypeio.anytype.core_ui.features.editor.holders.relations.FeaturedRelationListViewHolder
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Text
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.screen
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.databinding.FragmentEditorBinding
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.editor.BlockDimensions
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber

class DragAndDropDelegate {

    private lateinit var binding: FragmentEditorBinding
    private lateinit var blockAdapter: BlockAdapter
    private lateinit var vm: EditorViewModel
    private lateinit var fragment: EditorFragment

    private val screen: Point by lazy { fragment.screen() }
    private var dndTargetPos = NO_POSITION
    private var dndTargetPrevious: Pair<Float, Int>? = null

    private var dndTargetLineAnimator: ViewPropertyAnimator? = null

    private var scrollDownJob: Job? = null
    private var scrollUpJob: Job? = null

    private var operationProcessed = false

    fun init(
        blockAdapter: BlockAdapter,
        vm: EditorViewModel,
        editorFragment: EditorFragment
    ) {
        this.binding = editorFragment.binding
        this.blockAdapter = blockAdapter
        this.vm = vm
        this.fragment = editorFragment

        binding.recycler.setOnDragListener(dndListener)
    }

    val dndListener: EditorDragAndDropListener by lazy {
        EditorDragAndDropListener(
            onDragLocation = { target, ratio ->
                handleDragging(target, ratio)
            },
            onDrop = { target, event ->
                binding.recycler.itemAnimator = DefaultItemAnimator()
                proceedWithDropping(target, event)
                binding.recycler.postDelayed({
                    binding.recycler.itemAnimator = null
                }, RECYCLER_DND_ANIMATION_RELAXATION_TIME)
            },
            onDragExited = {
                it.isSelected = false
            },
            onDragEnded = { _, isMoved ->
                binding.dndTargetLine.invisible()
                blockAdapter.unSelectDraggedViewHolder()
                blockAdapter.notifyItemChanged(dndTargetPos)
                if (!operationProcessed && !isMoved && dndTargetPos != NO_POSITION) {
                    val block = blockAdapter.views[dndTargetPos]
                    if (block is BlockView.Selectable)
                        vm.onClickListener(ListenerType.LongClick(block.id))
                    operationProcessed = true
                }
                stopScrollDownJob()
                stopScrollUpJob()
            },
            onDragStart = {
                operationProcessed = false
            }
        )
    }

    private fun stopScrollDownJob() {
        scrollDownJob?.cancel()
        scrollDownJob = null
    }

    private fun stopScrollUpJob() {
        scrollUpJob?.cancel()
        scrollUpJob = null
    }

    fun handleDragAndDropTrigger(
        vh: RecyclerView.ViewHolder,
        event: MotionEvent?
    ): Boolean {
        if (vm.mode is Editor.Mode.Edit) {
            if (vh is BlockViewHolder.DragAndDropHolder && binding.recycler.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                dndTargetPos = vh.bindingAdapterPosition
                if (vh is Video) {
                    vh.pause()
                }

                val item = ClipData.Item(EditorFragment.EMPTY_TEXT)

                val dragData = ClipData(
                    EditorFragment.DRAG_AND_DROP_LABEL,
                    arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                    item
                )

                val shadow = when (vh) {
                    is Text<*> -> TextInputDragShadow(vh.content.id, vh.itemView, event)
                    is Code -> TextInputDragShadow(vh.content.id, vh.itemView, event)
                    else -> DefaultEditorDragShadow(vh.itemView, event)
                }
                vh.itemView.startDragAndDrop(
                    dragData,
                    shadow,
                    null,
                    0
                )
                blockAdapter.selectDraggedViewHolder(dndTargetPos)
                blockAdapter.notifyItemChanged(dndTargetPos)
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
                if (vh is SupportNesting) {
                    when (ratio) {
                        in DragAndDropConfig.topRange -> {
                            target.isSelected = false
                            if (handleDragAbove(vh, ratio))
                                return
                        }
                        in DragAndDropConfig.middleRange -> {
                            target.isSelected = true
                            handleDragInside(vh)
                        }
                        in DragAndDropConfig.bottomRange -> {
                            target.isSelected = false
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
        scrollDownJob = fragment.lifecycleScope.launch {
            while (isActive) {
                binding.recycler.smoothScrollBy(0, 350)
                delay(60)
            }
        }
    }

    private fun startScrollingUp() {
        scrollUpJob = fragment.lifecycleScope.launch {
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
            indent = block.indent * fragment.dimen(R.dimen.indent)
        }

        if (binding.dndTargetLine.isVisible) {
            dndTargetLineAnimator?.cancel()
            dndTargetLineAnimator = binding.dndTargetLine
                .animate()
                .setInterpolator(DecelerateInterpolator())
                .translationY(vh.itemView.bottom.toFloat())
                .translationX(indent.toFloat())
                .setDuration(75)
            dndTargetLineAnimator?.start()
        } else {
            binding.dndTargetLine.translationY = vh.itemView.bottom.toFloat()
            binding.dndTargetLine.translationX = indent.toFloat()
            binding.dndTargetLine.visible()
        }

        return false
    }

    private fun handleDragInside(vh: RecyclerView.ViewHolder) {
        dndTargetLineAnimator?.cancel()
        binding.dndTargetLine.invisible()
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
            indent = block.indent * fragment.dimen(R.dimen.indent)
        }

        if (binding.dndTargetLine.isVisible) {
            dndTargetLineAnimator?.cancel()
            dndTargetLineAnimator = binding.dndTargetLine
                .animate()
                .setInterpolator(DecelerateInterpolator())
                .translationY(vh.itemView.top.toFloat())
                .translationX(indent.toFloat())
                .setDuration(75)
            dndTargetLineAnimator?.start()
        } else {
            binding.dndTargetLine.translationY = vh.itemView.top.toFloat()
            binding.dndTargetLine.translationX = indent.toFloat()
            binding.dndTargetLine.visible()
        }

        return false
    }

    private class DropContainer(
        val vh: RecyclerView.ViewHolder?,
        val ratio: Float
    )

    private fun checkIfDroppedBeforeFirstVisibleItem(
        manager: LinearLayoutManager,
        touchY: Float
    ): DropContainer? {
        manager.findFirstCompletelyVisibleItemPosition().let { first ->
            if (first != RecyclerView.NO_POSITION) {
                manager.findViewByPosition(first)?.let { view ->
                    val point = IntArray(2)
                    view.getLocationOnScreen(point)
                    if (touchY < point[1]) {
                        return DropContainer(
                            binding.recycler.findContainingViewHolder(view),
                            TOP_RATIO
                        )
                    }
                }
            }
        }
        return null
    }

    private fun checkIfDroppedAfterLastVisibleItem(
        manager: LinearLayoutManager,
        touchY: Float
    ): DropContainer? {
        manager.findLastCompletelyVisibleItemPosition().let { last ->
            if (last != RecyclerView.NO_POSITION) {
                manager.findViewByPosition(last)?.let { view ->
                    val point = IntArray(2)
                    view.getLocationOnScreen(point)
                    if (touchY > point[1]) {
                        return DropContainer(
                            binding.recycler.findContainingViewHolder(view),
                            BOTTOM_RATIO
                        )
                    }
                }
            }
        }
        return null
    }

    private fun calculateBottomClosestView(
        manager: LinearLayoutManager,
        start: Int,
        end: Int,
        touchY: Float
    ): View? {
        var closestBottomView: View? = null
        var closestBottomViewDistance = Int.MAX_VALUE

        for (i in start..end) {
            manager.findViewByPosition(i)?.let { view ->
                val point = IntArray(2)
                view.getLocationOnScreen(point)
                val height = view.height
                if (touchY <= point[1] + height) {
                    val newLastDiff = (point[1] - touchY).toInt()
                    if (newLastDiff < closestBottomViewDistance) {
                        closestBottomViewDistance = newLastDiff
                        closestBottomView = view
                    }
                }
            }
        }
        return closestBottomView
    }

    private fun calculateTopClosestView(
        manager: LinearLayoutManager,
        start: Int,
        end: Int,
        touchY: Float
    ): View? {
        var closestTopView: View? = null
        var closestTopViewDistance = Int.MAX_VALUE
        for (i in start..end) {
            manager.findViewByPosition(i)?.let { view ->
                val point = IntArray(2)
                view.getLocationOnScreen(point)
                val height = view.height
                if (touchY > point[1] + height) {
                    val newLastDiff = (touchY - point[1] - height).toInt()
                    if (newLastDiff < closestTopViewDistance) {
                        closestTopViewDistance = newLastDiff
                        closestTopView = view
                    }
                }
            }
        }
        return closestTopView
    }

    private fun calculateDropContainer(touchY: Float): DropContainer {
        val point = IntArray(2)
        binding.recycler.getLocationOnScreen(point)
        val touchY = point[1] + touchY

        val manager = (binding.recycler.layoutManager as LinearLayoutManager)
        checkIfDroppedBeforeFirstVisibleItem(manager, touchY)?.let {
            return it
        }
        checkIfDroppedAfterLastVisibleItem(manager, touchY)?.let {
            return it
        }

        val start = manager.findFirstCompletelyVisibleItemPosition()
        val end = manager.findLastCompletelyVisibleItemPosition()

        val bottomClosestView =
            calculateBottomClosestView(manager, start, end, touchY) ?: return DropContainer(
                null,
                0f
            )
        val topClosestView = calculateTopClosestView(manager, start, end, touchY)
            ?: return DropContainer(null, 0f)

        return getClosestViewToLine(topClosestView, bottomClosestView)
    }

    private fun getClosestViewToLine(
        topView: View,
        bottomView: View
    ): DropContainer {

        val dndMiddle = kotlin.run {
            val point = IntArray(2)
            binding.dndTargetLine.getLocationOnScreen(point)
            point[1] + binding.dndTargetLine.height / 2f
        }

        val topViewDistance = kotlin.run {
            val point = IntArray(2)
            topView.getLocationOnScreen(point)
            dndMiddle - point[1] - topView.height
        }

        val bottomViewDistance = kotlin.run {
            val point = IntArray(2)
            bottomView.getLocationOnScreen(point)
            point[1] - dndMiddle
        }

        return if (topViewDistance > bottomViewDistance) {
            DropContainer(binding.recycler.findContainingViewHolder(bottomView), TOP_RATIO)
        } else {
            DropContainer(binding.recycler.findContainingViewHolder(topView), BOTTOM_RATIO)
        }
    }

    private fun resolveDropContainer(target: View, event: DragEvent): DropContainer {
        return if (target == binding.recycler) {
            calculateDropContainer(event.y)
        } else {
            val vh = binding.recycler.findContainingViewHolder(target)
            if (vh != null) {
                DropContainer(vh, event.y / vh.itemView.height)
            } else {
                DropContainer(null, 0f)
            }
        }
    }

    private fun proceedWithDropping(target: View, event: DragEvent) {
        binding.dndTargetLine.invisible()

        val dropContainer = resolveDropContainer(target, event)
        val vh = dropContainer.vh
        val ratio = dropContainer.ratio

        if (vh != null) {
            if (vh.bindingAdapterPosition != dndTargetPos) {
                target.isSelected = false
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
                                fragment.toast("Failed to drop. Please, try again later.")
                            }
                        }
                        else -> fragment.toast("drop skipped, scenario 1")
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
                        else -> fragment.toast("drop skipped, scenario 2")
                    }
                }
            }
        } else {
            fragment.toast("view holder not found")
        }
    }
}

private const val NO_POSITION = -1
private const val RECYCLER_DND_ANIMATION_RELAXATION_TIME = 500L
private const val TOP_RATIO = 0.1f
private const val BOTTOM_RATIO = 0.9f