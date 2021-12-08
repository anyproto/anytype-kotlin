package com.anytypeio.anytype.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.base.updateUserProperties
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.analytics.props.UserProperty
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.domain.auth.interactor.LaunchAccount
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.wallpaper.ObserveWallpaper
import com.anytypeio.anytype.domain.wallpaper.RestoreWallpaper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(
    private val launchAccount: LaunchAccount,
    private val observeWallpaper: ObserveWallpaper,
    private val restoreWallpaper: RestoreWallpaper,
    private val analytics: Analytics
) : ViewModel() {

    val wallpaper = MutableStateFlow<Wallpaper>(Wallpaper.Default)

    init {
        viewModelScope.launch { restoreWallpaper(BaseUseCase.None) }
        viewModelScope.launch {
            observeWallpaper.build(BaseUseCase.None).collect {
                wallpaper.value = it
            }
        }
    }

    fun onRestore() {
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            launchAccount(BaseUseCase.None).either(
                fnR = { id ->
                    updateUserProperties(
                        analytics = analytics,
                        userProperty = UserProperty.AccountId(id)
                    )
                    Timber.d("Restored account after activity recreation")
                    sendAuthEvent(startTime, id)
                },
                fnL = { error ->
                    Timber.e(error, "Error while launching account after activity recreation")
                }
            )
        }
    }

    private fun sendAuthEvent(start: Long, id: String) {
        val middle = System.currentTimeMillis()
        viewModelScope.sendEvent(
            analytics = analytics,
            startTime = start,
            middleTime = middle,
            renderTime = middle,
            eventName = EventsDictionary.ACCOUNT_SELECT,
            props = Props(mapOf(EventsDictionary.PROP_ACCOUNT_ID to id))
        )
    }
}