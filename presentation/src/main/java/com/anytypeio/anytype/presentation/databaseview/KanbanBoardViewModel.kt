package com.anytypeio.anytype.presentation.databaseview

import androidx.lifecycle.ViewModel
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable

class KanbanBoardViewModel : ViewModel() {

    private val state by lazy { BehaviorRelay.create<ViewState<KanbanColumns>>() }

    fun observeKanbanBoard(): Observable<ViewState<KanbanColumns>> {
        return state.startWith(
            ViewState.Success(data = MockFactory.makeKanbanBoard())
        )
    }
}