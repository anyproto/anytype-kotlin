package com.anytypeio.anytype.presentation.sets

import android.util.Log
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.presentation.relations.providers.FakeObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.FakeObjectValueProvider
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.presentation.widgets.collection.DateProviderImpl
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import timber.log.Timber


class RelationDateValueViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val rule = DefaultCoroutineTestRule()

    @get:Rule
    val timberTestRule: TimberTestRule = TimberTestRule.builder()
        .minPriority(Log.DEBUG)
        .showThread(false)
        .showTimestamp(false)
        .onlyLogWhenTestFails(false)
        .build()

    lateinit var dateProvider: DateProvider

    lateinit var viewModel: RelationDateValueViewModel

    private val valuesProvider = FakeObjectValueProvider()
    private val relationsProvider = FakeObjectRelationProvider()

    @Before
    fun setUp() {
        dateProvider = DateProviderImpl()
        viewModel = RelationDateValueViewModel(
            relations = relationsProvider,
            values = valuesProvider,
            dateProvider = dateProvider
        )
    }

    @Test
    fun `should be 01-01-1970`() {
        viewModel.setDate(994)

        val result = viewModel.views.value

        assertEquals(
            expected = DateValueView(
                title = null,
                isToday = false,
                isYesterday = false,
                isTomorrow = false,
                exactDayFormat = "01 January 1970",
                timeInSeconds = 994
            ),
            actual = result
        )
    }

    @Test
    fun `should be 07-07-1989`() {
        viewModel.setDate(615815721)

        val result = viewModel.views.value

        assertEquals(
            expected = DateValueView(
                title = null,
                isToday = false,
                isYesterday = false,
                isTomorrow = false,
                exactDayFormat = "07 July 1989",
                timeInSeconds = 615815721
            ),
            actual = result
        )
    }

    @Test
    fun `should be 07-07-2031`() {
        viewModel.setDate(1941191721)

        val result = viewModel.views.value

        assertEquals(
            expected = DateValueView(
                title = null,
                isToday = false,
                isYesterday = false,
                isTomorrow = false,
                exactDayFormat = "07 July 2031",
                timeInSeconds = 1941191721
            ),
            actual = result
        )
    }

    @Test
    fun `should be yesterday`() {

        val yesterdayTimestamp = dateProvider.getTimestampForYesterday()

        viewModel.setDate(yesterdayTimestamp)

        val result = viewModel.views.value

        assertEquals(
            expected = DateValueView(
                title = null,
                isToday = false,
                isYesterday = true,
                isTomorrow = false,
                exactDayFormat = null,
                timeInSeconds = yesterdayTimestamp
            ),
            actual = result
        )
    }

    @Test
    fun `should be yesterday when it's 12 o'clock`() {

        val yesterdayTimestamp = dateProvider.getTimestampForYesterday() + 43200
        Timber.d("yesterdayTimestamp: $yesterdayTimestamp")

        viewModel.setDate(yesterdayTimestamp)

        val result = viewModel.views.value

        assertEquals(
            expected = DateValueView(
                title = null,
                isToday = false,
                isYesterday = true,
                isTomorrow = false,
                exactDayFormat = null,
                timeInSeconds = yesterdayTimestamp
            ),
            actual = result
        )
    }

    @Test
    fun `should be yesterday when it's 23 59 99 o'clock`() {

        val yesterdayTimestamp = dateProvider.getTimestampForYesterday() + 86399
        Timber.d("yesterdayTimestamp: $yesterdayTimestamp")

        viewModel.setDate(yesterdayTimestamp)

        val result = viewModel.views.value

        assertEquals(
            expected = DateValueView(
                title = null,
                isToday = false,
                isYesterday = true,
                isTomorrow = false,
                exactDayFormat = null,
                timeInSeconds = yesterdayTimestamp
            ),
            actual = result
        )
    }

    @Test
    fun `should be today`() {

        val todayTimestamp = dateProvider.getTimestampForToday()

        viewModel.setDate(todayTimestamp)

        val result = viewModel.views.value

        assertEquals(
            expected = DateValueView(
                title = null,
                isToday = true,
                isYesterday = false,
                isTomorrow = false,
                exactDayFormat = null,
                timeInSeconds = todayTimestamp
            ),
            actual = result
        )
    }

    @Test
    fun `should be today when it's 12 o'clock`() {

        val todayTimestamp = dateProvider.getTimestampForToday() + 43200

        viewModel.setDate(todayTimestamp)

        val result = viewModel.views.value

        assertEquals(
            expected = DateValueView(
                title = null,
                isToday = true,
                isYesterday = false,
                isTomorrow = false,
                exactDayFormat = null,
                timeInSeconds = todayTimestamp
            ),
            actual = result
        )
    }

    @Test
    fun `should be today when it's 23 59 59 o'clock`() {

        val todayTimestamp = dateProvider.getTimestampForToday() + 86399

        viewModel.setDate(todayTimestamp)

        val result = viewModel.views.value

        assertEquals(
            expected = DateValueView(
                title = null,
                isToday = true,
                isYesterday = false,
                isTomorrow = false,
                exactDayFormat = null,
                timeInSeconds = todayTimestamp
            ),
            actual = result
        )
    }

    @Test
    fun `should be tomorrow`() {

        val tomorrowTimestamp = dateProvider.getTimestampForTomorrow()

        viewModel.setDate(tomorrowTimestamp)

        val result = viewModel.views.value

        assertEquals(
            expected = DateValueView(
                title = null,
                isToday = false,
                isYesterday = false,
                isTomorrow = true,
                exactDayFormat = null,
                timeInSeconds = tomorrowTimestamp
            ),
            actual = result
        )
    }

    @Test
    fun `should be tomorrow when it's 12 o'clock`() {

        val tomorrowTimestamp = dateProvider.getTimestampForTomorrow() + 43200

        viewModel.setDate(tomorrowTimestamp)

        val result = viewModel.views.value

        assertEquals(
            expected = DateValueView(
                title = null,
                isToday = false,
                isYesterday = false,
                isTomorrow = true,
                exactDayFormat = null,
                timeInSeconds = tomorrowTimestamp
            ),
            actual = result
        )
    }

    @Test
    fun `should be tomorrow when it's 23 59 59 o'clock`() {

        val tomorrowTimestamp = dateProvider.getTimestampForTomorrow() + 86399

        viewModel.setDate(tomorrowTimestamp)

        val result = viewModel.views.value

        assertEquals(
            expected = DateValueView(
                title = null,
                isToday = false,
                isYesterday = false,
                isTomorrow = true,
                exactDayFormat = null,
                timeInSeconds = tomorrowTimestamp
            ),
            actual = result
        )
    }
}