package com.anytypeio.anytype.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.presentation.library.delegates.LibraryRelationsDelegate
import com.anytypeio.anytype.presentation.library.delegates.LibraryTypesDelegate
import com.anytypeio.anytype.presentation.library.delegates.MyRelationsDelegate
import com.anytypeio.anytype.presentation.library.delegates.MyTypesDelegate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val myTypesDelegate: MyTypesDelegate,
    private val libraryTypesDelegate: LibraryTypesDelegate,
    private val myRelationsDelegate: MyRelationsDelegate,
    private val libraryRelationsDelegate: LibraryRelationsDelegate
) : ViewModel() {

    private val uiEvents = MutableStateFlow<LibraryEvent>(LibraryEvent.Query.MyTypes(""))

    init {
        viewModelScope.launch {
            uiEvents.collect {
                when (it) {
                    is LibraryEvent.Query.MyTypes -> {
                        myTypesDelegate.onQueryMyTypes(it.query)
                    }
                    is LibraryEvent.Query.LibraryTypes -> {
                        libraryTypesDelegate.onQueryLibTypes(it.query)
                    }
                    is LibraryEvent.Query.MyRelations -> {
                        myRelationsDelegate.onQueryMyRelations(it.query)
                    }
                    is LibraryEvent.Query.LibraryRelations -> {
                        libraryRelationsDelegate.onQueryLibRelations(it.query)
                    }
                }
            }
        }
    }

    fun eventStream(event: LibraryEvent) {
        uiEvents.value = event
    }

    val uiState: StateFlow<LibraryScreenState> = combine(
        myTypesDelegate.itemsFlow,
        libraryTypesDelegate.itemsFlow,
        myRelationsDelegate.itemsFlow,
        libraryRelationsDelegate.itemsFlow
    ) { myTypes, libTypes, myRel, libRel ->
        LibraryScreenState(
            types = LibraryScreenState.Tabs.Types(myTypes, libTypes),
            relations = LibraryScreenState.Tabs.Relations(myRel, libRel)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_SUBSCRIPTION_TIMEOUT),
        initialValue = LibraryScreenState(
            types = LibraryScreenState.Tabs.Types(
                myTypes = LibraryScreenState.Tabs.TabData(),
                libTypes = LibraryScreenState.Tabs.TabData()
            ),
            relations = LibraryScreenState.Tabs.Relations(
                myRelations = LibraryScreenState.Tabs.TabData(),
                libRelations = LibraryScreenState.Tabs.TabData()
            )
        )
    )

    class Factory @Inject constructor(
        private val myTypesDelegate: MyTypesDelegate,
        private val libraryTypesDelegate: LibraryTypesDelegate,
        private val myRelationsDelegate: MyRelationsDelegate,
        private val libraryRelationsDelegate: LibraryRelationsDelegate
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LibraryViewModel(
                myTypesDelegate,
                libraryTypesDelegate,
                myRelationsDelegate,
                libraryRelationsDelegate
            ) as T
        }
    }

}

private const val STOP_SUBSCRIPTION_TIMEOUT = 1_000L