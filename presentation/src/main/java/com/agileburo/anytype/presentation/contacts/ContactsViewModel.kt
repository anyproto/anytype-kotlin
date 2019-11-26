package com.agileburo.anytype.presentation.contacts

import androidx.lifecycle.*
import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.contacts.Contact
import com.agileburo.anytype.domain.contacts.GetContact
import com.agileburo.anytype.domain.contacts.GetContacts
import com.agileburo.anytype.domain.database.ALL_ID
import com.agileburo.anytype.domain.database.interactor.AddFilter
import com.agileburo.anytype.domain.database.interactor.GetFilters
import com.agileburo.anytype.presentation.contacts.model.FilterState
import com.agileburo.anytype.presentation.databaseview.mapper.toPresentation
import com.agileburo.anytype.presentation.filters.model.FilterView
import timber.log.Timber

class ContactsViewModel(
    private val getContacts: GetContacts,
    private val getContact: GetContact,
    private val getFilters: GetFilters,
    private val addFilter: AddFilter
) : ViewModel() {

    private val stateData = MutableLiveData<ContactsViewState>()
    val state: LiveData<ContactsViewState> = stateData

    private var filterState = FilterState()

    fun onViewCreated() {
        stateData.postValue(ContactsViewState.UiEffect.Init)
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
                    }.let {
                        it.forEach { filter ->
                            if (filter.id == ALL_ID) {
                                filter.isChecked = true
                            }
                        }
                        stateData.postValue(ContactsViewState.ScreenState.Filters(it))
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
                    }.let {
                        stateData.postValue(ContactsViewState.ScreenState.Contacts(it))
                    }
                }
            )
        }
    }

    fun onAddFilterClick() {
    }

    fun onFilterClick(filterId: String) {
        Timber.d("on filter click : $filterId")
    }

    fun onContactClick(contactId: String) {
        getContact.invoke(viewModelScope, GetContact.Params(contactId = contactId)) { result ->
            result.either(
                fnL = {
                    Timber.e(it, "Error while getting contact by id:$contactId")
                },
                fnR = { contact: Contact ->
                    Timber.d("Get contact : $contact")
                }
            )
        }
    }

    private fun addFilterToState(filter: FilterView) {
        //todo
    }
}

@Suppress("UNCHECKED_CAST")
class ContactsViewModelFactory(
    private val getContacts: GetContacts,
    private val getContact: GetContact,
    private val getFilters: GetFilters,
    private val addFilter: AddFilter
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ContactsViewModel(
            getContacts = getContacts,
            getContact = getContact,
            addFilter = addFilter,
            getFilters = getFilters
        ) as T
    }
}