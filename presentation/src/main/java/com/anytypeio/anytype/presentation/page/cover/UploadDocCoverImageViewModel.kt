package com.anytypeio.anytype.presentation.page.cover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.event.model.Payload
import com.anytypeio.anytype.presentation.util.Bridge

class UploadDocCoverImageViewModel(
    private val setDocCoverImage: SetDocCoverImage,
    private val payloadDispatcher: Bridge<Payload>
) : ViewModel() {


    class Factory(
        private val setDocCoverImage: SetDocCoverImage,
        private val payloadDispatcher: Bridge<Payload>,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return UploadDocCoverImageViewModel(
                setDocCoverImage = setDocCoverImage,
                payloadDispatcher = payloadDispatcher,
            ) as T
        }
    }
}