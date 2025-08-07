package com.anytypeio.anytype.presentation.publishtoweb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.domain.publishing.CreatePublishing
import com.anytypeio.anytype.domain.publishing.GetPublishingStatus
import com.anytypeio.anytype.domain.publishing.RemovePublishing
import javax.inject.Inject

class PublishToWebViewModel(
    private val publish: CreatePublishing,
    private val getStatus: GetPublishingStatus,
    private val removePublishing: RemovePublishing
) : ViewModel() {

    // Empty constructor - will add logic later

    class Factory @Inject constructor(
        private val publish: CreatePublishing,
        private val getStatus: GetPublishingStatus,
        private val removePublishing: RemovePublishing
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PublishToWebViewModel(
                publish = publish,
                getStatus = getStatus,
                removePublishing = removePublishing
            ) as T
        }
    }
}
