package com.agileburo.anytype.presentation.databaseview.modals

import androidx.lifecycle.*
import com.agileburo.anytype.domain.database.interactor.DeleteDetail
import com.agileburo.anytype.domain.database.interactor.DuplicateDetail
import com.agileburo.anytype.domain.database.interactor.GetDatabase
import com.agileburo.anytype.domain.database.interactor.HideDetail
import com.agileburo.anytype.domain.database.model.DatabaseView
import com.agileburo.anytype.domain.database.model.Detail
import com.agileburo.anytype.presentation.databaseview.mapper.toPresentation
import com.agileburo.anytype.presentation.databaseview.models.ColumnView
import timber.log.Timber

sealed class DetailEditViewState {

    data class Init(val detail: ColumnView) : DetailEditViewState()
    data class NavigateToDetails(val databaseId: String) :
        DetailEditViewState()

    data class Error(val msg: String) : DetailEditViewState()
}

class DetailEditViewModel(
    private val databaseId: String,
    private val detailId: String,
    private val getDatabase: GetDatabase,
    private val deleteDetail: DeleteDetail,
    private val hideDetail: HideDetail,
    private val duplicateDetail: DuplicateDetail
) : ViewModel() {

    private val stateData = MutableLiveData<DetailEditViewState>()
    val state: LiveData<DetailEditViewState> = stateData

    fun onViewCreated() {
        getDatabase()
    }

    fun onBackClick() {
        stateData.value =
            DetailEditViewState.NavigateToDetails(
                databaseId
            )
    }

    fun updatePropertyName(name: String) {

    }

    fun onPropertyTypeClick() {

    }

    fun onHideClick() {
        hideDetail()
    }

    fun onDuplicateClick() {
        duplicateDetail()
    }

    fun onDeleteClick() {
        deleteDetail()
    }

    private fun duplicateDetail() =
        duplicateDetail.invoke(
            viewModelScope,
            DuplicateDetail.Params(detailId = detailId, databaseId = databaseId)
        ) {
            it.either(
                fnL = this::onDeleteDetailError,
                fnR = this::onDuplicateDetailSuccess
            )
        }

    private fun hideDetail() =
        hideDetail.invoke(
            viewModelScope,
            HideDetail.Params(detailId = detailId, databaseId = databaseId)
        ) {
            it.either(
                fnL = this::onHideDetailError,
                fnR = this::onHideDetailSuccess
            )
        }

    private fun deleteDetail() =
        deleteDetail.invoke(
            viewModelScope,
            DeleteDetail.Params(detailId = detailId, databaseId = databaseId)
        ) {
            it.either(
                fnL = this::onDeleteDetailError,
                fnR = this::onDeleteDetailSuccess
            )
        }

    private fun getDatabase() =
        getDatabase.invoke(viewModelScope, GetDatabase.Params(id = databaseId)) {
            it.either(
                fnL = this::onGetDatabaseError,
                fnR = this::onGetDatabaseSuccess
            )
        }

    private fun onDuplicateDetailSuccess(unit: Unit) {
        stateData.value =
            DetailEditViewState.NavigateToDetails(
                databaseId = databaseId
            )
    }

    private fun onDuplicateDetailError(throwable: Throwable) {
        stateData.value =
            DetailEditViewState.Error(
                "Detail duplicate error"
            )
    }

    private fun onHideDetailSuccess(unit: Unit) {
        stateData.value =
            DetailEditViewState.NavigateToDetails(
                databaseId = databaseId
            )
    }

    private fun onHideDetailError(throwable: Throwable) {
        stateData.value =
            DetailEditViewState.Error(
                "Detail hide error"
            )
    }

    private fun onDeleteDetailSuccess(unit: Unit) {
        stateData.value =
            DetailEditViewState.NavigateToDetails(
                databaseId = databaseId
            )
    }

    private fun onDeleteDetailError(throwable: Throwable) {
        stateData.value =
            DetailEditViewState.Error(
                "Detail delete error"
            )
    }

    private fun onGetDatabaseSuccess(databaseView: DatabaseView) {
        parseResult(databaseView.content.details)
    }

    private fun onGetDatabaseError(e: Throwable) {
        Timber.e(e, "Error getting database")
    }

    private fun parseResult(data: List<Detail>) {
        val property = getColumnViewById(id = detailId, data = data)
        if (property == null) {
            stateData.value =
                DetailEditViewState.Error(
                    "Can't find property with id=$detailId"
                )
        } else {
            Timber.d("Get Detail to show : $property")
            stateData.value =
                DetailEditViewState.Init(
                    property
                )
        }
    }

    private fun getColumnViewById(id: String, data: List<Detail>): ColumnView? =
        data.find { it.id == id }?.toPresentation()
}

@Suppress("UNCHECKED_CAST")
class DetailEditViewModelFactory(
    private val databaseId: String,
    private val detailId: String,
    private val getDatabase: GetDatabase,
    private val deleteDetail: DeleteDetail,
    private val hideDetail: HideDetail,
    private val duplicateDetail: DuplicateDetail
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        DetailEditViewModel(
            databaseId = databaseId,
            detailId = detailId,
            getDatabase = getDatabase,
            deleteDetail = deleteDetail,
            hideDetail = hideDetail,
            duplicateDetail = duplicateDetail
        ) as T
}
