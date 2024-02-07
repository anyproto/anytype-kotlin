package com.anytypeio.anytype.presentation.sets

import android.util.Log
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.DateType
import com.anytypeio.anytype.presentation.relations.providers.FakeObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.FakeObjectValueProvider
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.presentation.widgets.collection.DateProviderImpl
import java.time.Duration
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
        viewModel.setState(994)

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
        viewModel.setState(615815721)

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
        viewModel.setState(1941191721)

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

        val yesterdayTimestamp = dateProvider.getTimestampForYesterdayAtStartOfDay()

        viewModel.setState(yesterdayTimestamp)

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

        val yesterdayTimestamp = dateProvider.getTimestampForYesterdayAtStartOfDay() + 43200
        Timber.d("yesterdayTimestamp: $yesterdayTimestamp")

        viewModel.setState(yesterdayTimestamp)

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

        val yesterdayTimestamp = dateProvider.getTimestampForYesterdayAtStartOfDay() + 86399
        Timber.d("yesterdayTimestamp: $yesterdayTimestamp")

        viewModel.setState(yesterdayTimestamp)

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

        val todayTimestamp = dateProvider.getTimestampForTodayAtStartOfDay()

        viewModel.setState(todayTimestamp)

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

        val todayTimestamp = dateProvider.getTimestampForTodayAtStartOfDay() + 43200

        viewModel.setState(todayTimestamp)

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

        val todayTimestamp = dateProvider.getTimestampForTodayAtStartOfDay() + 86399

        viewModel.setState(todayTimestamp)

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

        val tomorrowTimestamp = dateProvider.getTimestampForTomorrowAtStartOfDay()

        viewModel.setState(tomorrowTimestamp)

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

        val tomorrowTimestamp = dateProvider.getTimestampForTomorrowAtStartOfDay() + 43200

        viewModel.setState(tomorrowTimestamp)

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

        val tomorrowTimestamp = dateProvider.getTimestampForTomorrowAtStartOfDay() + 86399

        viewModel.setState(tomorrowTimestamp)

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
    fun `should be within previous seven days`() {

        val today = dateProvider.getTimestampForTodayAtStartOfDay()

        val twoDaysAgo = today - Duration.ofDays(2).toSeconds()
        val threeDaysAgo = today - Duration.ofDays(3).toSeconds()
        val fourDaysAgo = today - Duration.ofDays(4).toSeconds()
        val fiveDaysAgo = today - Duration.ofDays(5).toSeconds()
        val sixDaysAgo = today - Duration.ofDays(6).toSeconds()
        val sevenDaysAgo = today - Duration.ofDays(7).toSeconds()

        assertEquals(
            expected = DateType.PREVIOUS_SEVEN_DAYS,
            actual = dateProvider.calculateDateType(twoDaysAgo)
        )

        assertEquals(
            expected = DateType.PREVIOUS_SEVEN_DAYS,
            actual = dateProvider.calculateDateType(threeDaysAgo)
        )

        assertEquals(
            expected = DateType.PREVIOUS_SEVEN_DAYS,
            actual = dateProvider.calculateDateType(fourDaysAgo)
        )

        assertEquals(
            expected = DateType.PREVIOUS_SEVEN_DAYS,
            actual = dateProvider.calculateDateType(fiveDaysAgo)
        )

        assertEquals(
            expected = DateType.PREVIOUS_SEVEN_DAYS,
            actual = dateProvider.calculateDateType(sixDaysAgo)
        )

        assertEquals(
            expected = DateType.PREVIOUS_SEVEN_DAYS,
            actual = dateProvider.calculateDateType(sevenDaysAgo)
        )
    }

    @Test
    fun `should be within previous thirty days`() {

        val today = dateProvider.getTimestampForTodayAtStartOfDay()

        val heightDaysAgo = today - Duration.ofDays(8).toSeconds()
        val nineDaysAgo = today - Duration.ofDays(9).toSeconds()
        val tenDaysAgo = today - Duration.ofDays(10).toSeconds()
        val thirtyDaysAgo = today - Duration.ofDays(30).toSeconds()

        assertEquals(
            expected = DateType.PREVIOUS_THIRTY_DAYS,
            actual = dateProvider.calculateDateType(heightDaysAgo)
        )

        assertEquals(
            expected = DateType.PREVIOUS_THIRTY_DAYS,
            actual = dateProvider.calculateDateType(nineDaysAgo)
        )

        assertEquals(
            expected = DateType.PREVIOUS_THIRTY_DAYS,
            actual = dateProvider.calculateDateType(tenDaysAgo)
        )

        assertEquals(
            expected = DateType.PREVIOUS_THIRTY_DAYS,
            actual = dateProvider.calculateDateType(thirtyDaysAgo)
        )
    }

    @Test
    fun `should be older than previous thirty days`() {

        val today = dateProvider.getTimestampForTodayAtStartOfDay()

        val thirtyOneDaysAgo = today - Duration.ofDays(31).toSeconds()
        val thirtyTwoDaysAgo = today - Duration.ofDays(32).toSeconds()

        assertEquals(
            expected = DateType.OLDER,
            actual = dateProvider.calculateDateType(thirtyOneDaysAgo)
        )

        assertEquals(
            expected = DateType.OLDER,
            actual = dateProvider.calculateDateType(thirtyTwoDaysAgo)
        )
    }
}