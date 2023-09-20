package com.anytypeio.anytype.presentation.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.spaces.CreateSpace
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class CreateSpaceViewModel @Inject constructor(
    private val createSpace: CreateSpace
) : BaseViewModel() {

    val isDismissed = MutableStateFlow(false)

    fun onCreateSpace(name: String) {
        viewModelScope.launch {
            createSpace.async(
                CreateSpace.Params(
                    details = mapOf(
                        Relations.NAME to name
                    )
                )
            ).fold(
                onSuccess = { space: Id ->
                    sendToast("Space created")
                    Timber.d("Successfully created space: $space").also {
                        isDismissed.value = true
                    }
                },
                onFailure = {
                    Timber.e(it, "Error while creating space").also {
                        sendToast("Error while creating space, please try again.")
                    }
                }
            )
        }
    }

    class Factory @Inject constructor(
        private val createSpace: CreateSpace
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ) = CreateSpaceViewModel(
            createSpace = createSpace
        ) as T
    }
}