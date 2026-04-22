package com.anytypeio.anytype.feature_create_object.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.domain.device.FileSharer
import com.anytypeio.anytype.domain.media.UploadFile
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import javax.inject.Inject

/**
 * Factory for creating NewCreateObjectViewModel instances with dependency injection.
 *
 * @param storeOfObjectTypes Store for fetching object types
 * @param spaceViewContainer Container for observing space properties (needed for sort order)
 * @param uploadFile Use case for uploading a local file to the space
 * @param fileSharer Resolves content URIs into local paths the middleware can read
 * @param vmParams Parameters including the current space ID
 */
class CreateObjectViewModelFactory @Inject constructor(
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val spaceViewContainer: SpaceViewSubscriptionContainer,
    private val uploadFile: UploadFile,
    private val fileSharer: FileSharer,
    private val vmParams: NewCreateObjectViewModel.VmParams
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NewCreateObjectViewModel(
            storeOfObjectTypes = storeOfObjectTypes,
            spaceViewContainer = spaceViewContainer,
            uploadFile = uploadFile,
            fileSharer = fileSharer,
            vmParams = vmParams
        ) as T
    }
}
