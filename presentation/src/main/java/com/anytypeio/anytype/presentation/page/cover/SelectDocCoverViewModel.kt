package com.anytypeio.anytype.presentation.page.cover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.domain.common.Id
import com.anytypeio.anytype.domain.event.model.Payload
import com.anytypeio.anytype.domain.page.SetDocCoverColor
import com.anytypeio.anytype.presentation.util.Bridge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class SelectDocCoverViewModel(
    private val setDocCoverColor: SetDocCoverColor,
    private val payloadDispatcher: Bridge<Payload>
) : ViewModel() {

    val views = MutableStateFlow(
        listOf(
            DocCaverGalleryView.Header("Color solid")
        ) + CoverColor.values().map {
            DocCaverGalleryView.Color(it)
        }
    )

    fun onSolidColorSelected(
        ctx: Id,
        color: CoverColor
    ) {
        viewModelScope.launch {
            setDocCoverColor(
                SetDocCoverColor.Params(
                    ctx = ctx,
                    color = color.code
                )
            ).proceed(
                failure = { Timber.e(it, "Error while updating document's cover color") },
                success = { payloadDispatcher.send(it) }
            )
        }
    }

    class Factory(
        private val setDocCoverColor: SetDocCoverColor,
        private val payloadDispatcher: Bridge<Payload>
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SelectDocCoverViewModel(
                setDocCoverColor = setDocCoverColor,
                payloadDispatcher = payloadDispatcher
            ) as T
        }
    }
}