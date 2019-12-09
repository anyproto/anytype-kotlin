package com.agileburo.anytype.presentation.auth.congratulation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.agileburo.anytype.core_utils.common.EventWrapper
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation

class CongratulationViewModel : ViewModel(),
    SupportNavigation<EventWrapper<AppNavigation.Command>> {

    override val navigation: MutableLiveData<EventWrapper<AppNavigation.Command>> =
        MutableLiveData()

    fun onStartClicked() {
        navigation.postValue(EventWrapper(AppNavigation.Command.StartDesktopFromLogin))
    }
}