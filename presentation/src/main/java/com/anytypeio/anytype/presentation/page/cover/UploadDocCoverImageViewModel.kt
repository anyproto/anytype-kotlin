package com.anytypeio.anytype.presentation.page.cover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.presentation.util.Dispatcher

class UploadDocCoverImageViewModel(
    private val setDocCoverImage: SetDocCoverImage,
    private val payloadDispatcher: Dispatcher<Payload>
) : ViewModel() {


    class Factory(
        private val setDocCoverImage: SetDocCoverImage,
        private val payloadDispatcher: Dispatcher<Payload>,
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