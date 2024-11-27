package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.TimeInMillis
import com.anytypeio.anytype.core_models.ext.DateParser
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber

class RelationDateValueViewModel(
    private val relations: ObjectRelationProvider,
    private val values: ObjectValueProvider,
    private val dateProvider: DateProvider
) : ViewModel() {

    val commands = MutableSharedFlow<DateValueCommand>(0)
    private val _views = MutableStateFlow(DateValueView())
    val views: StateFlow<DateValueView> = _views

    private val jobs = mutableListOf<Job>()
    private var isEditableRelation = false

    fun onStart(
        ctx: Id,
        relationKey: Key,
        objectId: String,
        isLocked: Boolean
    ) {
        Timber.d("onStart: ctx:[$ctx], relationKey:[$relationKey], objectId:[$objectId]")
        jobs += viewModelScope.launch {
            val pipeline = combine(
                relations.observe(relationKey),
                values.subscribe(ctx = ctx, target = objectId)
            ) { relation, value ->
                setupIsRelationNotEditable(isLocked, relation)
                setName(relation.name)
                setDate(timeInSeconds = DateParser.parse(value[relationKey]))
            }
            pipeline.collect()
        }
    }

    fun onStop() {
        jobs.cancel()
    }

    fun onTodayClicked() {
        setDate(timeInSeconds = dateProvider.getTimestampForTodayAtStartOfDay())
        viewModelScope.launch {
            commands.emit(
                DateValueCommand.DispatchResult(
                    timeInSeconds = dateProvider.getTimestampForTodayAtStartOfDay().toDouble(),
                    dismiss = true
                )
            )
        }
    }

    fun onTomorrowClicked() {
        viewModelScope.launch {
            commands.emit(
                DateValueCommand.DispatchResult(
                    timeInSeconds = dateProvider.getTimestampForTomorrowAtStartOfDay().toDouble(),
                    dismiss = true
                )
            )
        }
    }

    fun onYesterdayClicked() {
        setDate(timeInSeconds = dateProvider.getTimestampForYesterdayAtStartOfDay())
    }

    fun onDateSelected(selectedDate: TimeInMillis?) {
        if (selectedDate != null) {
            val properDate = dateProvider.adjustFromStartOfDayInUserTimeZoneToUTC(
                timeInMillis = selectedDate
            )
            viewModelScope.launch {
                commands.emit(
                    DateValueCommand.DispatchResult(timeInSeconds = properDate.toDouble())
                )
            }
        } else {
            viewModelScope.launch {
                commands.emit(
                    DateValueCommand.DispatchResult(
                        timeInSeconds = null
                    )
                )
            }
        }
    }

    fun onClearClicked() {
        setDate(timeInSeconds = null)
        viewModelScope.launch {
            commands.emit(
                DateValueCommand.DispatchResult(
                    timeInSeconds = null,
                    dismiss = true
                )
            )
        }
    }

    private fun setName(name: String?) {
        _views.value = views.value.copy(
            title = name
        )
    }

    private fun setDate(timeInSeconds: Long?) {
        if (timeInSeconds != null) {
            _views.value = _views.value.copy(
                timeInMillis = dateProvider.adjustToStartOfDayInUserTimeZone(timeInSeconds)
            )
        } else {
            _views.value = _views.value.copy(
                timeInMillis = null
            )
        }
    }

    private fun setupIsRelationNotEditable(isLocked: Boolean, relation: ObjectWrapper.Relation) {
        if (isLocked
            || relation.isReadonlyValue
            || relation.isHidden == true
            || relation.isDeleted == true
            || relation.isArchived == true
            || !relation.isValid
        ) {
            _views.value = views.value.copy(
                isEditable = false
            )
        }
    }

    class Factory(
        private val relations: ObjectRelationProvider,
        private val values: ObjectValueProvider,
        private val dateProvider: DateProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RelationDateValueViewModel(relations, values, dateProvider) as T
        }
    }
}

sealed class DateValueCommand {
    data class DispatchResult(val timeInSeconds: Double?, val dismiss: Boolean = false) : DateValueCommand()
    data class OpenDatePicker(val timeInSeconds: Long?) : DateValueCommand()
}

data class DateValueView(
    val title: String? = null,
    val timeInMillis: TimeInMillis? = null,
    val isEditable: Boolean = true
)