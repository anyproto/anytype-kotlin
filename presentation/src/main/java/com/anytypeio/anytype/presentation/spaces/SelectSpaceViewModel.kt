package com.anytypeio.anytype.presentation.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class SelectSpaceViewModel @Inject constructor(
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer
) : BaseViewModel() {

    val spaces = MutableStateFlow<List<ObjectWrapper.Basic>>(emptyList())

    init {
        viewModelScope.launch {
            storelessSubscriptionContainer.subscribe(
                StoreSearchParams(
                    subscription = "test",
                    filters = listOf(
                        DVFilter(
                            relation = Relations.LAYOUT,
                            value = ObjectType.Layout.SPACE.code.toDouble(),
                            condition = DVFilterCondition.EQUAL
                        )
                    )
                )
            ).collect {
                Timber.d("Got spaces: $it")
                spaces.value = it
            }
        }
    }

    class Factory @Inject constructor(
        private val storelessSubscriptionContainer: StorelessSubscriptionContainer
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ) = SelectSpaceViewModel(
            storelessSubscriptionContainer = storelessSubscriptionContainer
        ) as T
    }
}