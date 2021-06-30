package com.anytypeio.anytype.presentation.`object`

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.domain.dataview.interactor.GetCompatibleObjectTypes
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.sets.toObjectTypeViews
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectTypeChangeViewModel(
    private val getCompatibleObjectTypes: GetCompatibleObjectTypes
) : BaseViewModel() {

    val views = MutableStateFlow<List<ObjectTypeView.Item>>(emptyList())

    fun onStart() {
        viewModelScope.launch {
            getCompatibleObjectTypes.invoke(
                GetCompatibleObjectTypes.Params(
                    smartBlockType = SmartBlockType.PAGE
                )
            ).proceed(
                failure = { Timber.e(it, "Error while getting object types") },
                success = { objectTypes ->
                    views.emit(objectTypes.toObjectTypeViews())
                }
            )
        }
    }

}