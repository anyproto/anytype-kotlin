package com.agileburo.anytype.presentation.auth.congratulation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.agileburo.anytype.core_utils.common.Event
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation

class CongratulationViewModel : ViewModel(), SupportNavigation<Event<AppNavigation.Command>> {

    override val navigation: MutableLiveData<Event<AppNavigation.Command>> = MutableLiveData()

    fun onStartClicked() {
        navigation.postValue(Event(AppNavigation.Command.StartDesktopFromLogin))
    }
}