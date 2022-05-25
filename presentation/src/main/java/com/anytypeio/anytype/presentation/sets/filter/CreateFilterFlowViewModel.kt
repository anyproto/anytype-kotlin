package com.anytypeio.anytype.presentation.sets.filter

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.sets.model.ColumnView.Format
import kotlinx.coroutines.flow.MutableStateFlow

class CreateFilterFlowViewModel : BaseViewModel() {

    val step = MutableStateFlow<Step>(Step.SelectRelation)

    fun onRelationSelected(ctx: Id, relation: Id, format: Format) {
        step.value = Step.CreateFilter(
            ctx = ctx,
            relation = relation,
            type = when (format) {
                Format.SHORT_TEXT,
                Format.LONG_TEXT,
                Format.NUMBER,
                Format.EMAIL,
                Format.PHONE,
                Format.URL -> {
                    Step.CreateFilter.Type.INPUT_FIELD
                }
                else -> Step.CreateFilter.Type.UNDEFINED
            }
        )
    }

    sealed class Step {
        object SelectRelation : Step()
        data class CreateFilter(
            val ctx: Id,
            val relation: Id,
            val type: Type
        ) : Step() {
            enum class Type {
                INPUT_FIELD, UNDEFINED
            }
        }
    }
}