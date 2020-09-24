package com.anytypeio.anytype.presentation.databaseview.modals

import androidx.lifecycle.*
import com.anytypeio.anytype.domain.database.interactor.GetDatabase
import com.anytypeio.anytype.domain.database.interactor.SwapDetails
import com.anytypeio.anytype.domain.database.model.DatabaseView
import com.anytypeio.anytype.presentation.databaseview.mapper.toPresentation
import com.anytypeio.anytype.presentation.databaseview.models.ColumnView
import com.anytypeio.anytype.presentation.databaseview.models.Swap
import timber.log.Timber

sealed class DetailsReorderViewState {

    data class Init(val details: List<ColumnView>) : DetailsReorderViewState()
    data class NavigateToDetails(val databaseId: String) :
        DetailsReorderViewState()

    data class Error(val msg: String) : DetailsReorderViewState()
}

class DetailsReorderViewModel(
    private val databaseId: String,
    private val getDatabase: GetDatabase,
    private val swapDetails: SwapDetails
) : ViewModel() {

    private val stateData = MutableLiveData<DetailsReorderViewState>()
    val state: LiveData<DetailsReorderViewState> = stateData

    fun onViewCreated() {
        getDatabase()
    }

    fun onDoneClick() {
        stateData.value =
            DetailsReorderViewState.NavigateToDetails(
                databaseId = databaseId
            )
    }

    fun onSwapDetails(swap: Swap) =
        swapDetails.invoke(
            viewModelScope,
            SwapDetails.Params(from = swap.fromPos, to = swap.toPos, databaseId = databaseId)
        ) {
            it.either(
                fnL = this::onSwapDetailsError,
                fnR = this::onSwapDetailsSuccess
            )
        }

    fun onDetailClick(detail: ColumnView) = Unit

    private fun getDatabase() =
        getDatabase.invoke(viewModelScope, GetDatabase.Params(id = databaseId)) {
            it.either(
                fnL = this::onGetDatabaseError,
                fnR = this::onGetDatabaseSuccess
            )
        }

    private fun onGetDatabaseSuccess(databaseView: DatabaseView) {
        stateData.value =
            DetailsReorderViewState.Init(
                databaseView.toPresentation().column.addNew()
            )
    }

    private fun onGetDatabaseError(e: Throwable) {
        Timber.e(e, "Error getting database")
    }

    private fun onSwapDetailsSuccess(msg: Unit) {
        Timber.d("Swapping details success")
    }

    private fun onSwapDetailsError(e: Throwable) {
        Timber.e(e, "Error swapping details")
    }

}

@Suppress("UNCHECKED_CAST")
class DetailsReorderViewModelFactory(
    private val databaseId: String,
    private val getDatabase: GetDatabase,
    private val swapDetails: SwapDetails
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        DetailsReorderViewModel(
            databaseId,
            getDatabase,
            swapDetails
        ) as T
}