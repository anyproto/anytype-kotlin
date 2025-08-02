package com.anytypeio.anytype.presentation.sets.filter

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

class CreateFilterFlowViewModel : BaseViewModel() {

    val step = MutableStateFlow<Step>(Step.SelectRelation)

    fun onRelationSelected(ctx: Id, relation: Id, format: RelationFormat) {
        Timber.d("onRelationSelected: ctx=$ctx, relation=$relation, format=$format")
        step.value = Step.CreateFilter(
            ctx = ctx,
            relation = relation,
            type = when (format) {
                RelationFormat.SHORT_TEXT,
                RelationFormat.LONG_TEXT,
                RelationFormat.NUMBER,
                RelationFormat.EMAIL,
                RelationFormat.PHONE,
                RelationFormat.URL -> {
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