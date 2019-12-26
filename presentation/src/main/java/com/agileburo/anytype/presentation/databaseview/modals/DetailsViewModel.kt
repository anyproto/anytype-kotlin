package com.agileburo.anytype.presentation.databaseview.modals

import androidx.lifecycle.*
import com.agileburo.anytype.domain.database.interactor.GetDatabase
import com.agileburo.anytype.domain.database.model.DatabaseView
import com.agileburo.anytype.presentation.databaseview.mapper.toPresentation
import com.agileburo.anytype.presentation.databaseview.models.ColumnView
import timber.log.Timber

sealed class DetailsViewState {

    data class Init(val details: List<ColumnView>) : DetailsViewState()
    data class NavigateToCustomize(val id: String) : DetailsViewState()
    data class NavigateToEditDetail(val databaseId: String, val detailId: String) :
        DetailsViewState()
    data class NavigateToReorder(val databaseId: String): DetailsViewState()
}

class DetailsViewModel(
    private val databaseId: String,
    private val getDatabase: GetDatabase
) : ViewModel() {

    private val stateData = MutableLiveData<DetailsViewState>()
    val state: LiveData<DetailsViewState> = stateData

    fun onViewCreated() {
        getDatabase()
    }

    fun onBackClick() {
        stateData.value =
            DetailsViewState.NavigateToCustomize(
                databaseId
            )
    }

    fun onReorderClick() {
        stateData.value =
            DetailsViewState.NavigateToReorder(
                databaseId
            )
    }

    fun onDetailClick(detail: ColumnView) {
        when (detail) {
            is ColumnView.Title -> navigateToEdit(databaseId = databaseId, detailId = detail.id)
            is ColumnView.Text -> navigateToEdit(databaseId = databaseId, detailId = detail.id)
            is ColumnView.Number -> navigateToEdit(databaseId = databaseId, detailId = detail.id)
            is ColumnView.Date -> navigateToEdit(databaseId = databaseId, detailId = detail.id)
            is ColumnView.Select -> navigateToEdit(databaseId = databaseId, detailId = detail.id)
            is ColumnView.Multiple -> navigateToEdit(databaseId = databaseId, detailId = detail.id)
            is ColumnView.Person -> navigateToEdit(databaseId = databaseId, detailId = detail.id)
            is ColumnView.File -> navigateToEdit(databaseId = databaseId, detailId = detail.id)
            is ColumnView.Checkbox -> navigateToEdit(databaseId = databaseId, detailId = detail.id)
            is ColumnView.URL -> navigateToEdit(databaseId = databaseId, detailId = detail.id)
            is ColumnView.Email -> navigateToEdit(databaseId = databaseId, detailId = detail.id)
            is ColumnView.Phone -> navigateToEdit(databaseId = databaseId, detailId = detail.id)
            ColumnView.AddNew -> onAddNewClick()
        }
    }

    private fun onAddNewClick() {
        Timber.d("On add new click")
    }

    private fun navigateToEdit(databaseId: String, detailId: String) {
        stateData.value =
            DetailsViewState.NavigateToEditDetail(
                databaseId = databaseId,
                detailId = detailId
            )
    }

    private fun getDatabase() =
        getDatabase.invoke(viewModelScope, GetDatabase.Params(id = databaseId)) {
            it.either(
                fnL = this::onGetDatabaseError,
                fnR = this::onGetDatabaseSuccess
            )
        }

    private fun onGetDatabaseSuccess(databaseView: DatabaseView) {
        stateData.value =
            DetailsViewState.Init(
                databaseView.toPresentation().column.addNew()
            )
    }

    private fun onGetDatabaseError(e: Throwable) {
        Timber.e(e, "Error getting database")
    }
}

@Suppress("UNCHECKED_CAST")
class DetailsViewModelFactory(
    private val id: String,
    private val getDatabase: GetDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        DetailsViewModel(
            id,
            getDatabase
        ) as T
}

fun List<ColumnView>.addNew() = this.toMutableList().apply { add(ColumnView.AddNew) }.toList()