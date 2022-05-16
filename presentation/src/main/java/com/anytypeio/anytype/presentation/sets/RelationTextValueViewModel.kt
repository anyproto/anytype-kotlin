package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.presentation.number.NumberParser
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

    fun onStart(
        relationId: Id,
        recordId: String,
        isLocked: Boolean = false
    ) {
        jobs += viewModelScope.launch {
            val pipeline = combine(
                relations.subscribe(relationId),
                values.subscribe(recordId)
            ) { relation, values ->
                title.value = relation.name
                val value = values[relationId] as? String
                val isValueReadOnly = values[Relations.IS_READ_ONLY] as? Boolean ?: false
                val isValueEditable = !(isValueReadOnly || isLocked)
                views.value = listOf(
                    when (relation.format) {
                        Relation.Format.SHORT_TEXT -> {
                            RelationTextValueView.TextShort(
                                value = value,
                                isEditable = isValueEditable
                            )
                        }
                        Relation.Format.LONG_TEXT -> {
                            RelationTextValueView.Text(
                                value = value,
                                isEditable = isValueEditable
                            )
                        }
                        Relation.Format.NUMBER -> {
                            RelationTextValueView.Number(
                                value = NumberParser.parse(value),
                                isEditable = isValueEditable
                            )
                        }
                        Relation.Format.URL -> {
                            RelationTextValueView.Url(
                                value = value,
                                isEditable = isValueEditable
                            )
                        }
                        Relation.Format.EMAIL -> {
                            RelationTextValueView.Email(
                                value = value,
                                isEditable = isValueEditable
                            )
                        }
                        Relation.Format.PHONE -> {
                            RelationTextValueView.Phone(
                                value = value,
                                isEditable = isValueEditable
                            )
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
    abstract val value: String?
    abstract val isEditable: Boolean

    data class Text(
        override val value: String? = null,
        override val isEditable: Boolean = true
    ) : RelationTextValueView()

    data class TextShort(
        override val value: String? = null,
        override val isEditable: Boolean = true
    ) : RelationTextValueView()

    data class Phone(
        override val value: String? = null,
        override val isEditable: Boolean = true
    ) : RelationTextValueView()

    data class Url(
        override val value: String? = null,
        override val isEditable: Boolean = true
    ) : RelationTextValueView()

    data class Email(
        override val value: String? = null,
        override val isEditable: Boolean = true
    ) : RelationTextValueView()

    data class Number(
        override val value: String? = null,
        override val isEditable: Boolean = true
    ) : RelationTextValueView()
}

sealed class EditGridCellAction {
    data class Url(val url: String) : EditGridCellAction()
    data class Email(val email: String) : EditGridCellAction()
    data class Phone(val phone: String) : EditGridCellAction()
}
