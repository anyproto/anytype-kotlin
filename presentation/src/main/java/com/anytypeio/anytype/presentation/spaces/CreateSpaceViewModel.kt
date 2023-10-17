package com.anytypeio.anytype.presentation.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.spaces.CreateSpace
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class CreateSpaceViewModel(
    private val createSpace: CreateSpace,
    private val spaceGradientProvider: SpaceGradientProvider,
    private val spaceManager: SpaceManager
) : BaseViewModel() {

    val isInProgress = MutableStateFlow(false)

    private var spaceGradientId = spaceGradientProvider.randomId()

    val spaceGradient : MutableStateFlow<SpaceIconView.Gradient>

    init {
        val gradient = spaceGradientProvider.get(spaceGradientId.toDouble())
        val view = SpaceIconView.Gradient(
            from = gradient.from,
            to = gradient.to
        )
        spaceGradient = MutableStateFlow(view)
    }

    val isDismissed = MutableStateFlow(false)

    fun onCreateSpace(name: String) {
        if (isDismissed.value) {
            return
        }
        if (isInProgress.value) {
            sendToast("Please wait...")
            return
        }
        if (name.isEmpty()) {
            sendToast("Name should not be empty")
            return
        }
        viewModelScope.launch {
            createSpace.stream(
                CreateSpace.Params(
                    details = mapOf(
                        Relations.NAME to name,
                        Relations.ICON_OPTION to spaceGradientId.toDouble()
                    )
                )
            ).collect { result ->
                result.fold(
                    onLoading = { isInProgress.value = true },
                    onSuccess = { space: Id ->
                        setNewSpaceAsCurrentSpace(space)
                        Timber.d("Successfully created space: $space").also {
                            isDismissed.value = true
                            isInProgress.value = false
                        }
                    },
                    onFailure = {
                        Timber.e(it, "Error while creating space").also {
                            sendToast("Error while creating space, please try again.")
                            isInProgress.value = false
                        }
                    }
                )
            }
        }
    }

    private suspend fun setNewSpaceAsCurrentSpace(space: Id) {
        spaceManager.set(space)
    }

    fun onSpaceIconClicked() {
        proceedWithResettingRandomSpaceGradient()
    }

    private fun proceedWithResettingRandomSpaceGradient() {
        spaceGradientId = spaceGradientProvider.randomId()
        val gradient = spaceGradientProvider.get(spaceGradientId.toDouble())
        val view = SpaceIconView.Gradient(
            from = gradient.from,
            to = gradient.to
        )
        spaceGradient.value = view
    }

    class Factory @Inject constructor(
        private val createSpace: CreateSpace,
        private val spaceGradientProvider: SpaceGradientProvider,
        private val spaceManager: SpaceManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ) = CreateSpaceViewModel(
            createSpace = createSpace,
            spaceGradientProvider = spaceGradientProvider,
            spaceManager = spaceManager
        ) as T
    }
}