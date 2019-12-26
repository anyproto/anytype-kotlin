package com.agileburo.anytype.ui.database.kanban

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.layout.SpacingItemDecoration
import com.agileburo.anytype.core_utils.ext.disposedBy
import com.agileburo.anytype.core_utils.ext.px
import com.agileburo.anytype.core_utils.ext.toast
import com.agileburo.anytype.core_utils.ui.ViewState
import com.agileburo.anytype.presentation.databaseview.KanbanBoardViewModel
import com.agileburo.anytype.ui.database.kanban.adapter.KanbanColumnAdapter
import com.agileburo.anytype.ui.database.kanban.helpers.KanbanBoardCallback
import com.agileburo.anytype.ui.database.kanban.helpers.KanbanBoardListener
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_kanban.*

const val COLUMN_WIDTH = 1.46 // На эту величину делим ширину экрана

class KanbanBoardFragment : Fragment(R.layout.fragment_kanban) {

    private val subscriptions by lazy { CompositeDisposable() }

    private val vm by lazy {
        ViewModelProviders.of(this).get(KanbanBoardViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setKanbanRecycler()
        setToolbarListeners()
    }

    private fun setKanbanRecycler() {
        with(mBoardView) {
            isDragEnabled = true
            setColumnWidth(218.px)
            setSnapToColumnInLandscape(false)
            setSnapToColumnsWhenScrolling(true)
            setSnapToColumnWhenDragging(true)
            setSnapDragItemToTouch(true)

            setBoardListener(KanbanBoardListener())
            setBoardCallback(KanbanBoardCallback())
        }
    }

    private fun setToolbarListeners() {
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        startObservingViewModel()
    }

    private fun startObservingViewModel() {
        vm.observeKanbanBoard().subscribe { state ->
            when (state) {
                is ViewState.Success -> {
                    state.data.forEachIndexed { index, kanbanColumnView ->
                        val header =
                            View.inflate(
                                requireContext(), R.layout.item_kanban_column_header, null
                            )
                                .apply {
                                    findViewById<TextView>(R.id.columnName).apply {
                                        text = kanbanColumnView.name
                                        (layoutParams as ConstraintLayout.LayoutParams).run {
                                            if (index != 0) this.marginStart = 10.px
                                        }
                                    }

                                }
                        var startSpace = 0
                        val space = 10.px
                        if (index != 0) {
                            startSpace = 10.px
                        }

                        mBoardView.addColumn(
                            KanbanColumnAdapter(
                                kanbanColumnView.rows
                            ),
                            header,
                            header,
                            true,
                            LinearLayoutManager(requireContext())
                        )
                            .apply {
                                addItemDecoration(
                                    SpacingItemDecoration(
                                        spacingStart = startSpace,
                                        spacingTop = space,
                                        spacingEnd = space,
                                        spacingBottom = space
                                    )
                                )
                            }
                    }
                }

                is ViewState.Error -> {
                    requireActivity().toast(state.error)
                }

                is ViewState.Loading -> {
                    TODO()
                }
            }
        }.disposedBy(subscriptions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        subscriptions.clear()
    }
}
