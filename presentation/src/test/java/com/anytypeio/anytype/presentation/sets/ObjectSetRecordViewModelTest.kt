package com.anytypeio.anytype.presentation.sets

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
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

class ObjectSetRecordViewModelTest {

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var setObjectDetails: SetObjectDetails

    private val defaultSpace = MockDataFactory.randomUuid()
    private val obj = ObjectWrapper.Basic(
        mapOf(
            Relations.ID to MockDataFactory.randomUuid(),
            Relations.SPACE_ID to defaultSpace
        )
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

        val params = SetObjectDetails.Params(
            ctx = obj.id,
            details = mapOf(
                Relations.NAME to input
            )
        )

        stubSetObjectDetails(params)

        // TESTING

        vm.onActionDone(
            target = obj.id,
            input = input,
            space = requireNotNull(obj.spaceId)
        )

        verifyBlocking(setObjectDetails, times(1)) {
            async(params)
        }
    }

    @Test
    fun `should update record name before opening this object`() = runTest {

        // SETUP

        val input = MockDataFactory.randomString()

        val params = SetObjectDetails.Params(
            ctx = obj.id,
            details = mapOf(
                Relations.NAME to input
            )
        )

        stubSetObjectDetails(params)

        val vm = buildViewModel()

        // TESTING

        vm.commands.test {
            vm.onButtonClicked(
                target = obj.id,
                input = input,
                space = requireNotNull(obj.spaceId)
            )
            assertEquals(
                expected = ObjectSetRecordViewModel.Command.OpenObject(
                    ctx = obj.id,
                    space = requireNotNull(obj.spaceId)
                ),
                actual = awaitItem()
            )
        }

        verifyBlocking(setObjectDetails, times(1)) {
            async(params)
        }
    }

    @Test
    fun `should not update record name before opening this object if user input is empty`() = runTest {

        // SETUP

        val emptyInput = ""

        val params = SetObjectDetails.Params(
            ctx = obj.id,
            details = mapOf(
                Relations.NAME to emptyInput
            )
        )

        stubSetObjectDetails(params)

        val vm = buildViewModel()

        // TESTING

        vm.commands.test {
            vm.onButtonClicked(
                target = obj.id,
                input = emptyInput,
                space = requireNotNull(obj.spaceId)
            )
            assertEquals(
                expected = ObjectSetRecordViewModel.Command.OpenObject(
                    ctx = obj.id,
                    space = requireNotNull(obj.spaceId)
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
        params: SetObjectDetails.Params,
        payload: Payload = Payload(
            context = MockDataFactory.randomUuid(),
            events = emptyList()
        )
    ) {
        setObjectDetails.stub {
            onBlocking {
                async(params)
            } doReturn Resultat.Success(payload)
        }
    }
}