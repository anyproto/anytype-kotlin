package com.anytypeio.anytype.presentation.sets

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

/**
 * The fake [DateProvider] works in UTC, so a day starts at a multiple of [DAY].
 */
class RelationDateValueViewModelTest {

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    private val ctx = MockDataFactory.randomUuid()
    private val relationKey = MockDataFactory.randomString()

    private val storeOfRelations = DefaultStoreOfRelations()
    private val values: ObjectValueProvider = mock()
    private val dateProvider: DateProvider = mock()

    @Before
    fun setup() {
        whenever(dateProvider.adjustToStartOfDayInUserTimeZone(any())).doAnswer {
            val seconds = it.getArgument<Long>(0)
            (seconds - seconds % DAY) * 1000
        }
        whenever(dateProvider.adjustFromStartOfDayInUserTimeZoneToUTC(any())).doAnswer {
            it.getArgument<Long>(0) / 1000
        }
        whenever(dateProvider.getLocalHourAndMinute(any())).doAnswer {
            val seconds = it.getArgument<Long>(0) % DAY
            (seconds / HOUR).toInt() to ((seconds % HOUR) / 60).toInt()
        }
        whenever(dateProvider.combineDateAndTimeInUserTimeZone(any(), any(), any())).doAnswer {
            it.getArgument<Long>(0) / 1000 +
                    it.getArgument<Int>(1) * HOUR +
                    it.getArgument<Int>(2) * 60
        }
        whenever(dateProvider.getTimestampForTodayAtStartOfDay()).thenReturn(TODAY)
        whenever(dateProvider.getTimestampForTomorrowAtStartOfDay()).thenReturn(TODAY + DAY)
    }

    @Test
    fun `without include time, picking a date dispatches the start of the day`() = runTest {
        val vm = start(includeTime = false, value = OTHER_DAY + 14 * HOUR + 30 * 60)

        vm.commands.test {
            vm.onDateSelected(TODAY * 1000)
            assertEquals(
                DateValueCommand.DispatchResult(timeInSeconds = TODAY.toDouble()),
                awaitItem()
            )
        }
        assertEquals(false, vm.views.value.includeTime)
        assertEquals(null, vm.views.value.hour)
    }

    @Test
    fun `with include time, the view shows the time of the value`() = runTest {
        val vm = start(includeTime = true, value = OTHER_DAY + 14 * HOUR + 30 * 60)

        val view = vm.views.value
        assertEquals(true, view.includeTime)
        assertEquals(OTHER_DAY * 1000, view.timeInMillis)
        assertEquals(14, view.hour)
        assertEquals(30, view.minute)
    }

    @Test
    fun `with include time, picking a date keeps the time`() = runTest {
        val vm = start(includeTime = true, value = OTHER_DAY + 14 * HOUR + 30 * 60)

        vm.commands.test {
            vm.onDateSelected(TODAY * 1000)
            assertEquals(
                DateValueCommand.DispatchResult(
                    timeInSeconds = (TODAY + 14 * HOUR + 30 * 60).toDouble()
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `picking a time keeps the date`() = runTest {
        val vm = start(includeTime = true, value = OTHER_DAY + 14 * HOUR + 30 * 60)

        vm.commands.test {
            vm.onTimeSelected(hour = 9, minute = 5)
            assertEquals(
                DateValueCommand.DispatchResult(
                    timeInSeconds = (OTHER_DAY + 9 * HOUR + 5 * 60).toDouble()
                ),
                awaitItem()
            )
        }
        assertEquals(9, vm.views.value.hour)
        assertEquals(5, vm.views.value.minute)
    }

    @Test
    fun `picking a time without a date uses today`() = runTest {
        val vm = start(includeTime = true, value = null)

        vm.commands.test {
            vm.onTimeSelected(hour = 9, minute = 5)
            assertEquals(
                DateValueCommand.DispatchResult(
                    timeInSeconds = (TODAY + 9 * HOUR + 5 * 60).toDouble()
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `with include time, tomorrow keeps the time`() = runTest {
        val vm = start(includeTime = true, value = OTHER_DAY + 14 * HOUR + 30 * 60)

        vm.commands.test {
            vm.onTomorrowClicked()
            assertEquals(
                DateValueCommand.DispatchResult(
                    timeInSeconds = (TODAY + DAY + 14 * HOUR + 30 * 60).toDouble(),
                    dismiss = true
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `without include time, today dispatches the start of the day`() = runTest {
        val vm = start(includeTime = false, value = OTHER_DAY + 14 * HOUR + 30 * 60)

        vm.commands.test {
            vm.onTodayClicked()
            assertEquals(
                DateValueCommand.DispatchResult(
                    timeInSeconds = TODAY.toDouble(),
                    dismiss = true
                ),
                awaitItem()
            )
        }
    }

    private suspend fun start(includeTime: Boolean, value: Long?): RelationDateValueViewModel {
        val relation = StubRelationObject(key = relationKey, format = Relation.Format.DATE)
        storeOfRelations.merge(
            listOf(
                ObjectWrapper.Relation(
                    relation.map + (Relations.RELATION_FORMAT_INCLUDE_TIME to includeTime)
                )
            )
        )
        whenever(values.subscribe(any(), anyOrNull())).thenReturn(
            flowOf(mapOf(relationKey to value?.toDouble()))
        )
        val vm = RelationDateValueViewModel(
            values = values,
            dateProvider = dateProvider,
            storeOfRelations = storeOfRelations
        )
        vm.onStart(ctx = ctx, relationKey = relationKey, objectId = ctx, isLocked = false)
        coroutineTestRule.advanceUntilIdle()
        return vm
    }

    companion object {
        const val HOUR = 3600L
        const val DAY = 24 * HOUR
        const val TODAY = 20_000 * DAY
        const val OTHER_DAY = 19_990 * DAY
    }
}
