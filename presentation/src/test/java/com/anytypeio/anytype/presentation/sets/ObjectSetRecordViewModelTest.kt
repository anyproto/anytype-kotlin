package com.anytypeio.anytype.presentation.sets

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions
import kotlin.test.assertEquals

class ObjectSetRecordViewModelTest {

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var setObjectDetails: UpdateDetail

    private val ctx: Id = MockDataFactory.randomUuid()
    private val obj = ObjectWrapper.Basic(
        mapOf(Relations.ID to MockDataFactory.randomUuid())
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should proceed with updating record name based on user input on action done`() = runTest {

        // SETUP

        val vm = buildViewModel()

        val input = MockDataFactory.randomString()

        val params = UpdateDetail.Params(
            ctx = obj.id,
            key = Relations.NAME,
            value = input
        )

        stubSetObjectDetails(params)

        // TESTING

        vm.onActionDone(
            target = obj.id,
            input = input
        )

        verifyBlocking(setObjectDetails, times(1)) {
            invoke(params)
        }
    }

    @Test
    fun `should update record name before opening this object`() = runTest {

        // SETUP

        val input = MockDataFactory.randomString()

        val params = UpdateDetail.Params(
            ctx = obj.id,
            key = Relations.NAME,
            value = input
        )

        stubSetObjectDetails(params)

        val vm = buildViewModel()

        // TESTING

        vm.commands.test {
            vm.onButtonClicked(
                target = obj.id,
                input = input
            )
            assertEquals(
                expected = ObjectSetRecordViewModel.Command.OpenObject(
                    ctx = obj.id
                ),
                actual = awaitItem()
            )
        }

        verifyBlocking(setObjectDetails, times(1)) {
            invoke(params)
        }
    }

    @Test
    fun `should not update record name before opening this object if user input is empty`() = runTest {

        // SETUP

        val emptyInput = ""

        val params = UpdateDetail.Params(
            ctx = obj.id,
            key = Relations.NAME,
            value = emptyInput
        )

        stubSetObjectDetails(params)

        val vm = buildViewModel()

        // TESTING

        vm.commands.test {
            vm.onButtonClicked(
                target = obj.id,
                input = emptyInput
            )
            assertEquals(
                expected = ObjectSetRecordViewModel.Command.OpenObject(
                    ctx = obj.id
                ),
                actual = awaitItem()
            )
        }

        verifyNoInteractions(setObjectDetails)
    }

    fun buildViewModel() : ObjectSetRecordViewModel = ObjectSetRecordViewModel(
        setObjectDetails = setObjectDetails
    )

    private fun stubSetObjectDetails(
        params: UpdateDetail.Params,
        payload: Payload = Payload(
            context = MockDataFactory.randomUuid(),
            events = emptyList()
        )
    ) {
        setObjectDetails.stub {
            onBlocking {
                invoke(params)
            } doReturn Either.Right(payload)
        }
    }
}