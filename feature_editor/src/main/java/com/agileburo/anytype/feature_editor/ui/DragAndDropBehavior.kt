package com.agileburo.anytype.feature_editor.ui

import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.feature_editor.presentation.model.BlockView
import com.agileburo.anytype.feature_editor.presentation.util.DragDropAction
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.disposables.CompositeDisposable
import kotlin.math.abs

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 2019-08-01.
 */

interface ItemTouchHelperViewHolder {

    fun targetView()
    fun targetViewBottom()
    fun clearTargetView()
}

sealed class DragState {

    /** Send Shift action. */
    data class Shift(val from: RecyclerView.ViewHolder) : DragState()

    /** The opportunity to produce Shift action. Remember shiftView for the future. Render view. */
    data class ShiftRequest(val from: RecyclerView.ViewHolder, val to: RecyclerView.ViewHolder) : DragState()

    /** Delete shiftView */
    object DeleteShiftRequest : DragState()

    /** Send Consume action. */
    data class Consume(val target: RecyclerView.ViewHolder) : DragState()

    /** The opportunity to produce Consume action. Remember consumerView for the future. */
    data class ConsumeRequest(val target: Int, val consumerViewHolder: RecyclerView.ViewHolder?) : DragState()

    /** The user drag element. */
    data class Dragging(val target: Int) : DragState()

    /** The element is not controlled by user and simply animating back to its original state. */
    data class DraggingNotActive(val view: View, val alpha: Float) : DragState()

    /** The user interaction with an element is over and it also completed its animation. */
    object Idle : DragState()
}

const val CONSUME_INTERVAL = 7
const val DRAG_STATE_ALPHA = 0.3f
const val CONSUME_STATE_ALPHA = 0.0f
const val IDLE_STATE_ALPHA = 1.0f
const val POSITION_NONE = -1

class DragAndDropBehavior(
    private val onDragDropAction: (DragDropAction) -> Unit
) : ItemTouchHelper.Callback() {

    private val disposable = CompositeDisposable()
    private val subject: BehaviorRelay<DragState> = BehaviorRelay.createDefault(DragState.Idle)
    private var shiftView: RecyclerView.ViewHolder? = null
    private var consumerPosition: Int = POSITION_NONE

    fun init() = disposable.addAll(subject.subscribe { handleState(it) })
    fun destroy() = disposable.clear()

    override fun onSwiped(p0: RecyclerView.ViewHolder, p1: Int) {}
    override fun getMovementFlags(p0: RecyclerView, p1: RecyclerView.ViewHolder): Int =
        makeMovementFlags(UP or DOWN, 0)

    /**
     *  The user has removed the finger from the screen.
     *  Check for null viewHolders for Shift or Consume behavior.
     *  First check for consume action
     */
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        removeDraggableViewProperties(viewHolder.itemView)
        if (consumerPosition > POSITION_NONE) {
            subject.accept(DragState.Consume(target = viewHolder))
            return
        }
        if (shiftView != null) {
            subject.accept(DragState.Shift(from = viewHolder))
            return
        }
    }

    /**
     * Returns the fraction that the user should move the View to be considered as it is
     * dragged. After a view is moved this amount, ItemTouchHelper starts checking for Views
     * below it for a possible drop.
     *
     * @param viewHolder The ViewHolder that is being dragged.
     * @return A float value that denotes the fraction of the View size. Dragging value is
     * .5f .
     */
    override fun getMoveThreshold(viewHolder: RecyclerView.ViewHolder): Float = 0.1f

    override fun getAnimationDuration(
        recyclerView: RecyclerView,
        animationType: Int,
        animateDx: Float,
        animateDy: Float
    ): Long = 0

    override fun isLongPressDragEnabled(): Boolean = true
    override fun isItemViewSwipeEnabled(): Boolean = false

    override fun canDropOver(
        recyclerView: RecyclerView,
        current: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = target.adapterPosition != 0

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        subject.accept(DragState.ShiftRequest(viewHolder, target))
        return false
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        when (actionState) {
            ACTION_STATE_DRAG -> isCurrentlyActive(
                recyclerView = recyclerView, dY = dY,
                viewHolder = viewHolder, isCurrentlyActive = isCurrentlyActive
            )
            else -> Unit
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun chooseDropTarget(
        selected: RecyclerView.ViewHolder,
        dropTargets: MutableList<RecyclerView.ViewHolder>,
        curX: Int,
        curY: Int
    ): RecyclerView.ViewHolder? {
        var winner: RecyclerView.ViewHolder? = null
        var winnerScore = Int.MAX_VALUE
        val dy = curY - selected.itemView.top
        dropTargets.forEach { dropTarget ->
            with(dropTarget) {
                if (this is BlockView.Consumer) {
                    val selectedTargetCenter = getSelectedTargetCenter(curY, selected.itemView.height)
                    when {
                        dy < 0 -> {
                            val diff = getConsumeTopBorder(itemView.top, itemView.bottom) - selectedTargetCenter
                            if (diff > 0 && abs(diff) < winnerScore) {
                                winnerScore = abs(diff)
                                winner = this
                            }
                        }
                        dy > 0 -> {
                            val diff = getConsumeBottomBorder(itemView.top, itemView.bottom) - selectedTargetCenter
                            if (diff < 0 && abs(diff) < winnerScore) {
                                winnerScore = abs(diff)
                                winner = this
                            }
                        }
                        else -> Unit
                    }
                } else {
                    val diff = getDropTargetCenter(itemView.top, itemView.bottom) -
                            getSelectedTargetCenter(curY, selected.itemView.height)
                    when {
                        dy < 0 ->
                            if (diff > 0 && abs(diff) < winnerScore) {
                                winnerScore = abs(diff)
                                winner = this
                            } else Unit
                        dy > 0 ->
                            if (diff < 0 && abs(diff) < winnerScore) {
                                winnerScore = abs(diff)
                                winner = this
                            } else Unit
                        else -> Unit
                    }
                }
            }
        }
        if (winner == null) {
            subject.accept(DragState.DeleteShiftRequest)
        }
        return winner
    }

    private fun setDraggableViewProperties(itemView: View) = with(itemView) {
        setBackgroundColor(Color.LTGRAY)
        alpha = DRAG_STATE_ALPHA
    }

    private fun removeDraggableViewProperties(itemView: View) = with(itemView) {
        setBackgroundColor(0)
        alpha = IDLE_STATE_ALPHA
    }

    private fun isCurrentlyActive(
        isCurrentlyActive: Boolean,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dY: Float
    ) = when (isCurrentlyActive) {
        true -> itemDraggedActive(recyclerView, viewHolder, dY)
        false -> subject.accept(DragState.DraggingNotActive(viewHolder.itemView, IDLE_STATE_ALPHA))
    }

    private fun itemDraggedActive(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dY: Float
    ) = with(viewHolder.itemView) {
        if (alpha != DRAG_STATE_ALPHA) setDraggableViewProperties(this)
        for (x in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(x)
            if (child != this) {
                val consumeCenter = (child.top + child.bottom).div(2)
                val interval = child.height.div(CONSUME_INTERVAL)
                if (isDraggedItemCanBeConsumeByCenterOfDragged(
                        consumerCenter = consumeCenter,
                        interval = interval,
                        baselineY = getDraggedItemBaseline(this, dY).toInt()
                    )
                ) {
                    subject.accept(DragState.DeleteShiftRequest)
                    val consume = recyclerView.findContainingViewHolder(child)
                    subject.accept(DragState.ConsumeRequest(viewHolder.adapterPosition, consume))
                    break
                } else {
                    if (consumerPosition > POSITION_NONE) {
                        recyclerView.findViewHolderForAdapterPosition(consumerPosition)?.itemView?.setBackgroundColor(0)
                    }
                    subject.accept(DragState.Dragging(viewHolder.adapterPosition))
                }
            }
        }
    }

    private fun handleState(state: DragState) = when (state) {

        is DragState.Shift -> {
            shiftView?.let {
                consumerPosition = POSITION_NONE
                (it as? ItemTouchHelperViewHolder)?.clearTargetView()
                onDragDropAction.invoke(
                    DragDropAction.Shift(from = state.from.adapterPosition, to = it.adapterPosition)
                )
            }
        }

        is DragState.ShiftRequest -> {
            (shiftView as? ItemTouchHelperViewHolder)?.clearTargetView()
            shiftView = null

            if (state.from.adapterPosition > state.to.adapterPosition) {
                (state.to as? ItemTouchHelperViewHolder)?.targetView()
            } else {
                (state.to as? ItemTouchHelperViewHolder)?.targetViewBottom()
            }
            shiftView = state.to
        }

        is DragState.DeleteShiftRequest -> {
            (shiftView as? ItemTouchHelperViewHolder)?.clearTargetView()
            shiftView = null
        }

        is DragState.ConsumeRequest -> {
            subject.accept(DragState.DeleteShiftRequest)
            state.consumerViewHolder?.let { holder ->
                if (holder is BlockView.Consumer) {
                    holder.itemView.setBackgroundColor(Color.GRAY)
                    consumerPosition = holder.adapterPosition
                }
            }
        }

        is DragState.Consume -> {
            shiftView = null
            subject.accept(
                DragState.DraggingNotActive(view = state.target.itemView, alpha = CONSUME_STATE_ALPHA)
            )
            onDragDropAction.invoke(
                DragDropAction.Consume(target = state.target.adapterPosition, consumer = consumerPosition)
            )
        }

        is DragState.Dragging -> {
            /** shiftView не нужно обнулять, так как после DragState.ShiftRequest может
            быть DragState.Dragging и тогда в clearView не случится SwapRequest
             */
            consumerPosition = POSITION_NONE
        }

        is DragState.DraggingNotActive -> {
            if (state.alpha < IDLE_STATE_ALPHA) {
                state.view.alpha = state.alpha
            } else Unit
        }

        is DragState.Idle -> {
            subject.accept(DragState.DeleteShiftRequest)
            consumerPosition = POSITION_NONE
        }
    }

    private fun getDraggedItemBaseline(draggedView: View, dY: Float): Float =
        (draggedView.top + dY + (draggedView.height / 2))

    private fun isDraggedItemCanBeConsumeByCenterOfDragged(
        consumerCenter: Int,
        baselineY: Int,
        interval: Int
    ): Boolean = baselineY in consumerCenter.minus(interval)..consumerCenter.plus(interval)
}

fun getDropTargetCenter(top: Int, bottom: Int) = (top + bottom) / 2

fun getConsumeTopBorder(top: Int, bottom: Int): Int {
    val centerY = getDropTargetCenter(top = top, bottom = bottom)
    val height = bottom - top
    return centerY - height.div(CONSUME_INTERVAL)
}

fun getConsumeBottomBorder(top: Int, bottom: Int): Int {
    val centerY = getDropTargetCenter(top = top, bottom = bottom)
    val height = bottom - top
    return centerY + height.div(CONSUME_INTERVAL)
}

fun getSelectedTargetCenter(curY: Int, height: Int): Int = curY + height.div(2)