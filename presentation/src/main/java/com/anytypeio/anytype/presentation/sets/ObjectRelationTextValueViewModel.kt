package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import kotlinx.coroutines.flow.MutableStateFlow

class ObjectRelationTextValueViewModel(
    private val relations: ObjectRelationProvider,
    private val values: ObjectValueProvider
) : ViewModel() {

    val views = MutableStateFlow<List<ObjectRelationTextValueView>>(emptyList())
    val title = MutableStateFlow("")

    fun onStart(relationId: Id, recordId: String) {
        val relation = relations.get(relationId)
        val values = values.get(recordId)
        title.value = relation.name
        views.value = listOf(
            when (relation.format) {
                Relation.Format.SHORT_TEXT -> ObjectRelationTextValueView.Text(value = values[relationId] as? String)
                Relation.Format.LONG_TEXT -> ObjectRelationTextValueView.Text(value = values[relationId] as? String)
                Relation.Format.NUMBER -> ObjectRelationTextValueView.Number(
                    value = values[relationId]?.let { value ->
                        when (value) {
                            is String -> value.toIntOrNull().toString()
                            is Number -> value.toInt().toString()
                            else -> null
                        }
                    }
                )
                Relation.Format.URL -> ObjectRelationTextValueView.Url(value = values[relationId] as? String)
                Relation.Format.EMAIL -> ObjectRelationTextValueView.Email(value = values[relationId] as? String)
                Relation.Format.PHONE -> ObjectRelationTextValueView.Phone(value = values[relationId] as? String)
                else -> throw  IllegalArgumentException("Wrong format:${relation.format}")
            }
        )
    }

    class Factory(
        private val relations: ObjectRelationProvider,
        private val values: ObjectValueProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ObjectRelationTextValueViewModel(relations, values) as T
        }
    }
}

sealed class ObjectRelationTextValueView {
    data class Text(val value: String?) : ObjectRelationTextValueView()
    data class Phone(val value: String?) : ObjectRelationTextValueView()
    data class Url(val value: String?) : ObjectRelationTextValueView()
    data class Email(val value: String?) : ObjectRelationTextValueView()
    data class Number(val value: String?) : ObjectRelationTextValueView()
}

sealed class EditGridCellAction {
    data class Url(val url: String) : EditGridCellAction()
    data class Email(val email: String) : EditGridCellAction()
    data class Phone(val phone: String) : EditGridCellAction()
}
