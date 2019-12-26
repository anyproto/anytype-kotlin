package com.agileburo.anytype.presentation.databaseview

import androidx.lifecycle.*
import com.agileburo.anytype.core_utils.common.EventWrapper
import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.database.interactor.GetContact
import com.agileburo.anytype.domain.database.interactor.GetContacts
import com.agileburo.anytype.domain.database.ALL_ID
import com.agileburo.anytype.domain.database.interactor.AddFilter
import com.agileburo.anytype.domain.database.interactor.GetFilters
import com.agileburo.anytype.presentation.databaseview.mapper.toPresentation
import com.agileburo.anytype.presentation.databaseview.models.ListItem
import com.agileburo.anytype.presentation.databaseview.models.FilterState
import com.agileburo.anytype.presentation.databaseview.models.FilterView
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import timber.log.Timber

sealed class ListBoardViewState {

    sealed class ScreenState : ListBoardViewState() {
        object Loading : ScreenState()
        data class SetContacts(val contacts: List<ListItem>) : ScreenState()
        data class SetFilters(val filters: List<FilterView>) : ScreenState()
        object Error : ScreenState()
        object OpenBottomSheet : ScreenState()
    }

    sealed class UiEffect : ListBoardViewState() {
        object Init : UiEffect()
    }
}

class ListBoardViewModel(
    private val getContacts: GetContacts,
    private val getContact: GetContact,
    private val getFilters: GetFilters,
    private val addFilter: AddFilter
) : ViewModel(), SupportNavigation<EventWrapper<AppNavigation.Command>> {

    private val stateData = MutableLiveData<ListBoardViewState>()
    val state: LiveData<ListBoardViewState> = stateData

    override val navigation: MutableLiveData<EventWrapper<AppNavigation.Command>> = MutableLiveData()

    private var filterState =
        FilterState()

    fun onViewCreated() {
        stateData.postValue(ListBoardViewState.UiEffect.Init)
    }

    fun getFilters() {
        getFilters.invoke(viewModelScope, BaseUseCase.None) { results ->
            results.either(
                fnL = {
                    Timber.e(it, "Error while getting filters")
                },
                fnR = { filters ->
                    filters.map {
                        it.toPresentation()
                    }.let { filterViews ->
                        setFiltersState(filterViews).let {
                            stateData.postValue(
                                ListBoardViewState.ScreenState.SetFilters(
                                    it
                                )
                            )
                        }
                    }
                }
            )
        }
    }

    fun getContacts() {
        getContacts.invoke(viewModelScope, BaseUseCase.None) { results ->
            results.either(
                fnL = {
                    Timber.e(it, "Error while getting contacts")
                },
                fnR = { contacts ->
                    contacts.map { contact ->
                        contact.toPresentation()
                    }
                        .filter { contactView ->
                            filterContactsByFilterState(contactView)
                        }
                        .let {
                            stateData.postValue(
                                ListBoardViewState.ScreenState.SetContacts(
                                    it
                                )
                            )
                        }
                }
            )
        }
    }

    fun onFilterClick(filter: FilterView) {
        addFilterToState(filter)
        getFilters()

        if (filter.id == "888") {
            navigation.postValue(EventWrapper(AppNavigation.Command.OpenDatabaseViewAddView))
        }

        Timber.d("on filter click : ${filter.id}")
    }

    fun onContactClick(contactId: String) {
        getContact.invoke(viewModelScope, GetContact.Params(contactId = contactId)) { result ->
            result.either(
                fnL = {
                    Timber.e(it, "Error while getting contact by id:$contactId")
                },
                fnR = {
                    Timber.d("Get contact : $it")
                }
            )
        }
    }

    private fun setFiltersState(filters: List<FilterView>): List<FilterView> =
        if (filterState.filters.isEmpty()) {
            for (filter in filters) {
                if (filter.id == ALL_ID) {
                    filter.isChecked = true
                    break
                }
            }
            filters
        } else {
            checkFiltersInFilterState(filters)
        }

    private fun checkFiltersInFilterState(filters: List<FilterView>): List<FilterView> =
        filters.apply {
            this.forEach {
                if (filterState.filters.contains(it.id)) {
                    it.isChecked = true
                }
            }
        }

    private fun addFilterToState(filter: FilterView) {
        filterState.filters.clear()
        if (filter.id != ALL_ID) {
            filterState.filters.add(filter.id)
        }
    }

    fun onEditDatabaseClick() {
        navigation.postValue(EventWrapper(AppNavigation.Command.OpenEditDatabase))
    }

    fun onCustomizeClick() {
        //navigation.postValue(Event(AppNavigation.Command.OpenCustomizeDisplayView))
        stateData.postValue(ListBoardViewState.ScreenState.OpenBottomSheet)
    }

    private fun filterContactsByFilterState(contact: ListItem): Boolean =
        if (filterState.filters.isNotEmpty()) {
            contact.tags.any {
                filterState.filters.contains(it.id)
            }
        } else {
            true
        }
}

@Suppress("UNCHECKED_CAST")
class ListBoardViewModelFactory(
    private val getContacts: GetContacts,
    private val getContact: GetContact,
    private val getFilters: GetFilters,
    private val addFilter: AddFilter
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ListBoardViewModel(
            getContacts = getContacts,
            getContact = getContact,
            addFilter = addFilter,
            getFilters = getFilters
        ) as T
    }
}