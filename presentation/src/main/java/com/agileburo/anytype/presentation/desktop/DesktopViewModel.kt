package com.agileburo.anytype.presentation.desktop

import androidx.lifecycle.MutableLiveData
import com.agileburo.anytype.core_utils.common.Event
import com.agileburo.anytype.core_utils.ui.ViewState
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation

class DesktopViewModel : ViewStateViewModel<ViewState<List<DesktopView>>>(),
    SupportNavigation<Event<AppNavigation.Command>> {

    override val navigation = MutableLiveData<Event<AppNavigation.Command>>()

    fun onViewCreated() {
        stateData.postValue(ViewState.Init)
    }

    fun onAddNewDocumentClicked() {
        stateData.postValue(
            ViewState.Success(
                listOf(
                    DesktopView.Document(
                        id = "1",
                        title = "Document"
                    )
                )
            )
        )
    }

    fun onDocumentClicked() {
        navigation.postValue(Event(AppNavigation.Command.OpenDocument("")))
    }

    fun onProfileClicked() {
        navigation.postValue(Event(AppNavigation.Command.OpenProfile))
    }

}