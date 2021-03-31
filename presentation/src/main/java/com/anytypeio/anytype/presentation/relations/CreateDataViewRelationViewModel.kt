package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import com.anytypeio.anytype.core_models.Relation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CreateDataViewRelationViewModel : ViewModel() {

    private val _formats = MutableStateFlow<List<RelationFormatView>>(emptyList())
    val formats: StateFlow<List<RelationFormatView>> = _formats

    init {
        _formats.value = Relation.Format.values().mapIndexed { index, format ->
            RelationFormatView(
                format = format,
                isSelected = index == 0
            )
        }
    }

    fun onFormatClicked(view: RelationFormatView) {
        _formats.value = formats.value.map { v ->
            if (v.format == view.format)
                v.copy(isSelected = true)
            else
                v.copy(isSelected = false)
        }
    }
}