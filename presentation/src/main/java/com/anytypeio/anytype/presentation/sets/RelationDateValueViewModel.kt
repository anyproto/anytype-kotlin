package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_utils.const.DateConst.DEFAULT_DATE_FORMAT
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.core_utils.ext.formatTimeInMillis
import com.anytypeio.anytype.core_models.ext.DateParser
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.DateType
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

    fun onStart(
        ctx: Id,
        relationKey: Key,
        objectId: String
    ) {
        Timber.d("onStart: ctx:[$ctx], relationKey:[$relationKey], objectId:[$objectId]")
        jobs += viewModelScope.launch {
            val pipeline = combine(
                relations.observe(relationKey),
                values.subscribe(ctx = ctx, target = objectId)
            ) { relation, value ->
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
    }

    fun onTomorrowClicked() {
        setDate(timeInSeconds = dateProvider.getTimestampForTomorrowAtStartOfDay())
    }

    fun onYesterdayClicked() {
        setDate(timeInSeconds = dateProvider.getTimestampForYesterdayAtStartOfDay())
    }

    fun onExactDayClicked() {
//        viewModelScope.launch {
//            commands.emit(
//                DateValueCommand.OpenDatePicker(
//                    timeInSeconds = views.value.timeInSeconds
//                )
//            )
//        }
    }

    fun onNoDateClicked() {
        setDate(timeInSeconds = null)
    }

    fun onActionClicked() {
//        viewModelScope.launch {
//            commands.emit(
//                DateValueCommand.DispatchResult(
//                    timeInSeconds = views.value.timeInSeconds?.toDouble()
//                )
//            )
//        }
    }

    private fun setName(name: String?) {
        _views.value = views.value.copy(
            title = name
        )
    }

    fun setDate(timeInSeconds: Long?) {
        if (timeInSeconds != null) {
            val dateType = dateProvider.calculateDateType(timeInSeconds)
            val isToday = dateType == DateType.TODAY
            val isTomorrow = dateType == DateType.TOMORROW
            val isYesterday = dateType == DateType.YESTERDAY

            var exactDayFormat: String? = null
            if (!isToday && !isTomorrow && !isYesterday) {
                exactDayFormat = (timeInSeconds * 1000).formatTimeInMillis(DEFAULT_DATE_FORMAT)
            }
            _views.value = views.value.copy(
                isToday = isToday,
                isYesterday = isYesterday,
                isTomorrow = isTomorrow,
                exactDayFormat = exactDayFormat,
                timeInSeconds = dateProvider.adjustToStartOfDayInUserTimeZone(timeInSeconds)
            )
        } else {
            _views.value = views.value.copy(
                isToday = false,
                isYesterday = false,
                isTomorrow = false,
                exactDayFormat = null,
                timeInSeconds = null
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
    data class DispatchResult(val timeInSeconds: Double?) : DateValueCommand()
    data class OpenDatePicker(val timeInSeconds: Long?) : DateValueCommand()
}

data class DateValueView(
    val title: String? = null,
    val isToday: Boolean = false,
    val isYesterday: Boolean = false,
    val isTomorrow: Boolean = false,
    val exactDayFormat: String? = null,
    val timeInSeconds: Long? = null
)