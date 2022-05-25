package com.anytypeio.anytype.presentation.sets.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.sets.model.Viewer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PickFilterConditionViewModel : BaseViewModel() {

    private val _views = MutableStateFlow(PickConditionScreenState())
    val views: StateFlow<PickConditionScreenState> = _views

    fun onStart(type: Viewer.Filter.Type, index: Int) {
        val conditions = arrayListOf<Viewer.Filter.Condition>()
        val inputCondition = when (type) {
            Viewer.Filter.Type.TEXT -> {
                conditions.addAll(Viewer.Filter.Condition.Text.textConditions())
                conditions[index]
            }
            Viewer.Filter.Type.NUMBER -> {
                conditions.addAll(Viewer.Filter.Condition.Number.numberConditions())
                conditions[index]
            }
            Viewer.Filter.Type.SELECTED -> {
                conditions.addAll(Viewer.Filter.Condition.Selected.selectConditions())
                conditions[index]
            }
            Viewer.Filter.Type.CHECKBOX -> {
                conditions.addAll(Viewer.Filter.Condition.Checkbox.checkboxConditions())
                conditions[index]
            }
        }
        _views.value = PickConditionScreenState(
            picked = inputCondition,
            conditions = conditions
        )
    }

    class Factory() : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PickFilterConditionViewModel() as T
        }
    }
}

data class PickConditionScreenState(
    val picked: Viewer.Filter.Condition? = null,
    val conditions: List<Viewer.Filter.Condition> = emptyList()
)