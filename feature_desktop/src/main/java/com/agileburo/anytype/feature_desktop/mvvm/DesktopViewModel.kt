package com.agileburo.anytype.feature_desktop.mvvm

import androidx.lifecycle.MutableLiveData
import com.agileburo.anytype.core_utils.common.Event
import com.agileburo.anytype.core_utils.navigation.SupportNavigation
import com.agileburo.anytype.core_utils.ui.ViewState
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.feature_desktop.navigation.DesktopNavigation

class DesktopViewModel : ViewStateViewModel<ViewState<List<DesktopView>>>(),
    SupportNavigation<Event<DesktopNavigation.Command>> {

    override val navigation = MutableLiveData<Event<DesktopNavigation.Command>>()

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
        navigation.postValue(Event(DesktopNavigation.Command.OpenDocument("")))
    }

    fun onProfileClicked() {
        navigation.postValue(Event(DesktopNavigation.Command.OpenProfile))
    }

}