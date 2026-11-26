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
import com.anytypeio.anytype.domain.objects.StoreOfRelations
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
    private val values: ObjectValueProvider,
    private val dateProvider: DateProvider,
    private val storeOfRelations: StoreOfRelations
) : ViewModel() {

    val commands = MutableSharedFlow<DateValueCommand>(0)
    private val _views = MutableStateFlow(DateValueView())
    val views: StateFlow<DateValueView> = _views

    private val jobs = mutableListOf<Job>()
    private var isEditableRelation = false
    private var includeTime = false

    fun onStart(
        ctx: Id,
        relationKey: Key,
        objectId: String,
        isLocked: Boolean
    ) {
        Timber.d("onStart: ctx:[$ctx], relationKey:[$relationKey], objectId:[$objectId]")
        jobs += viewModelScope.launch {
            val pipeline = combine(
                storeOfRelations.trackChanges(),
                values.subscribe(ctx = ctx, target = objectId)
            ) { _, value ->
                val relation = storeOfRelations.getByKey(key = relationKey)
                setupIsRelationNotEditable(isLocked, relation)
                setName(relation?.name)
                setIncludeTime(relation?.includeTime == true)
                setDate(timeInSeconds = DateParser.parse(value[relationKey]))
            }
            pipeline.collect()
        }
    }

    fun onStop() {
        jobs.cancel()
    }

    fun onTodayClicked() {
        val timeInSeconds = withSelectedTime(dateProvider.getTimestampForTodayAtStartOfDay())
        setDate(timeInSeconds = timeInSeconds)
        viewModelScope.launch {
            commands.emit(
                DateValueCommand.DispatchResult(
                    timeInSeconds = timeInSeconds.toDouble(),
                    dismiss = true
                )
            )
        }
    }

    fun onTomorrowClicked() {
        val timeInSeconds = withSelectedTime(dateProvider.getTimestampForTomorrowAtStartOfDay())
        viewModelScope.launch {
            commands.emit(
                DateValueCommand.DispatchResult(
                    timeInSeconds = timeInSeconds.toDouble(),
                    dismiss = true
                )
            )
        }
    }

    fun openSelectedDateClicked(timeInMillis: TimeInMillis) {
        viewModelScope.launch {
            commands.emit(
                DateValueCommand.OpenDateObject(timeInMillis)
            )
        }
    }

    fun onYesterdayClicked() {
        setDate(timeInSeconds = dateProvider.getTimestampForYesterdayAtStartOfDay())
    }

    fun onDateSelected(selectedDate: TimeInMillis?) {
        if (selectedDate != null) {
            val view = views.value
            val properDate = if (includeTime) {
                dateProvider.combineDateAndTimeInUserTimeZone(
                    pickerDateMillis = selectedDate,
                    hour = view.hour ?: 0,
                    minute = view.minute ?: 0
                )
            } else {
                dateProvider.adjustFromStartOfDayInUserTimeZoneToUTC(
                    timeInMillis = selectedDate
                )
            }
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

    fun onTimeSelected(hour: Int, minute: Int) {
        val date = views.value.timeInMillis
            ?: dateProvider.adjustToStartOfDayInUserTimeZone(
                dateProvider.getTimestampForTodayAtStartOfDay()
            )
        val timeInSeconds = dateProvider.combineDateAndTimeInUserTimeZone(
            pickerDateMillis = date,
            hour = hour,
            minute = minute
        )
        setDate(timeInSeconds = timeInSeconds)
        viewModelScope.launch {
            commands.emit(
                DateValueCommand.DispatchResult(timeInSeconds = timeInSeconds.toDouble())
            )
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

    private fun setIncludeTime(includeTime: Boolean) {
        this.includeTime = includeTime
        _views.value = views.value.copy(includeTime = includeTime)
    }

    private fun setDate(timeInSeconds: Long?) {
        if (timeInSeconds != null) {
            val (hour, minute) = if (includeTime) {
                dateProvider.getLocalHourAndMinute(timeInSeconds)
            } else {
                null to null
            }
            _views.value = _views.value.copy(
                timeInMillis = dateProvider.adjustToStartOfDayInUserTimeZone(timeInSeconds),
                hour = hour,
                minute = minute
            )
        } else {
            _views.value = _views.value.copy(
                timeInMillis = null,
                hour = null,
                minute = null
            )
        }
    }

    /**
     * Moves the time of day already set to the day of [startOfDay], when the relation includes
     * time. Otherwise returns [startOfDay] unchanged.
     */
    private fun withSelectedTime(startOfDay: Long): Long {
        val view = views.value
        val hour = view.hour
        val minute = view.minute
        if (!includeTime || hour == null || minute == null) return startOfDay
        return dateProvider.combineDateAndTimeInUserTimeZone(
            pickerDateMillis = dateProvider.adjustToStartOfDayInUserTimeZone(startOfDay),
            hour = hour,
            minute = minute
        )
    }

    private fun setupIsRelationNotEditable(isLocked: Boolean, relation: ObjectWrapper.Relation?) {
        if (relation == null) {
            _views.value = views.value.copy(
                isEditable = false,
                title = null,
                timeInMillis = null
            )
            return
        }
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
        private val values: ObjectValueProvider,
        private val dateProvider: DateProvider,
        private val storeOfRelations: StoreOfRelations
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RelationDateValueViewModel(values, dateProvider, storeOfRelations) as T
        }
    }
}

sealed class DateValueCommand {
    data class DispatchResult(val timeInSeconds: Double?, val dismiss: Boolean = false) : DateValueCommand()
    data class OpenDatePicker(val timeInSeconds: Long?) : DateValueCommand()
    data class OpenDateObject(val timeInMillis: TimeInMillis) : DateValueCommand()
}

data class DateValueView(
    val title: String? = null,
    val timeInMillis: TimeInMillis? = null,
    val isEditable: Boolean = true,
    val includeTime: Boolean = false,
    val hour: Int? = null,
    val minute: Int? = null
)