package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.presentation.relations.NumberParser
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class RelationTextValueViewModel(
    private val relations: ObjectRelationProvider,
    private val values: ObjectValueProvider
) : ViewModel() {

    val views = MutableStateFlow<List<RelationTextValueView>>(emptyList())
    val title = MutableStateFlow("")

    private val jobs = mutableListOf<Job>()

    fun onStart(relationId: Id, recordId: String) {
        jobs += viewModelScope.launch {
            val pipeline = combine(
                relations.subscribe(relationId),
                values.subscribe(recordId)
            ) { relation, values ->
                title.value = relation.name
                views.value = listOf(
                    when (relation.format) {
                        Relation.Format.SHORT_TEXT -> {
                            RelationTextValueView.TextShort(value = values[relationId] as? String)
                        }
                        Relation.Format.LONG_TEXT -> {
                            RelationTextValueView.Text(value = values[relationId] as? String)
                        }
                        Relation.Format.NUMBER -> {
                            val value = values[relationId]
                            RelationTextValueView.Number(
                                value = NumberParser.parse(value)
                            )
                        }
                        Relation.Format.URL -> {
                            RelationTextValueView.Url(value = values[relationId] as? String)
                        }
                        Relation.Format.EMAIL -> {
                            RelationTextValueView.Email(value = values[relationId] as? String)
                        }
                        Relation.Format.PHONE -> {
                            RelationTextValueView.Phone(value = values[relationId] as? String)
                        }
                        else -> throw  IllegalArgumentException("Wrong format:${relation.format}")
                    }
                )
            }
            pipeline.collect()
        }
    }

    fun onStop() {
        jobs.cancel()
    }

    class Factory(
        private val relations: ObjectRelationProvider,
        private val values: ObjectValueProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RelationTextValueViewModel(relations, values) as T
        }
    }
}

sealed class RelationTextValueView {
    data class Text(val value: String?) : RelationTextValueView()
    data class TextShort(val value: String?) : RelationTextValueView()
    data class Phone(val value: String?) : RelationTextValueView()
    data class Url(val value: String?) : RelationTextValueView()
    data class Email(val value: String?) : RelationTextValueView()
    data class Number(val value: String?) : RelationTextValueView()
}

sealed class EditGridCellAction {
    data class Url(val url: String) : EditGridCellAction()
    data class Email(val email: String) : EditGridCellAction()
    data class Phone(val phone: String) : EditGridCellAction()
}
