package com.agileburo.anytype.presentation.navigation

import androidx.lifecycle.MutableLiveData
import com.agileburo.anytype.core_utils.common.EventWrapper
import com.agileburo.anytype.core_utils.ui.ViewState
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.domain.page.navigation.GetListPages
import com.agileburo.anytype.domain.page.navigation.GetPageInfoWithLinks

class PageNavigationViewModel(
    private val getPageInfoWithLinks: GetPageInfoWithLinks,
    private val getListPages: GetListPages
) :
    ViewStateViewModel<ViewState<PageNavigationView>>(),
    SupportNavigation<EventWrapper<AppNavigation.Command>> {

    override val navigation: MutableLiveData<EventWrapper<AppNavigation.Command>> =
        MutableLiveData()

    fun onViewCreated() {
        stateData.postValue(ViewState.Init)
    }
}