package com.anytypeio.anytype.presentation.objects.menu

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Block.Fields
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class ObjectMenuOptionsProviderImplTest {

    private val objectId: String = "objectId"
    private val details = MutableStateFlow<Map<Id, Fields>>(mapOf())
    private val restrictions = MutableStateFlow<List<ObjectRestriction>>(emptyList())
    private val provider = ObjectMenuOptionsProviderImpl(details, restrictions, mock())

    @Test
    fun `when layout note - options are layout, relations, history`() {
        details.value = mapOf(
            objectId to Fields(map = mapOf(Relations.LAYOUT to ObjectType.Layout.NOTE.code.toDouble()))
        )
        val expected = ObjectMenuOptionsProvider.Options(
            hasIcon = false,
            hasCover = false,
            hasLayout = true,
            hasRelations = true,
            hasDiagnosticsVisibility = false
        )

        assertOptions(
            expected = expected
        )
    }

    @Test
    fun `when layout task - options are layout, relations, history`() {
        details.value = mapOf(
            objectId to Fields(map = mapOf(Relations.LAYOUT to ObjectType.Layout.TODO.code.toDouble()))
        )
        val expected = ObjectMenuOptionsProvider.Options(
            hasIcon = false,
            hasCover = true,
            hasLayout = true,
            hasRelations = true,
            hasDiagnosticsVisibility = false
        )

        assertOptions(
            expected = expected
        )
    }

    @Test
    fun `when layout basic - all options are visible`() {
        details.value = mapOf(
            objectId to Fields(map = mapOf(Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()))
        )

        assertOptions(
            expected = ObjectMenuOptionsProvider.Options.ALL.copy(hasDiagnosticsVisibility = false)
        )
    }


    @Test
    fun `when layout null - all options are visible`() {
        details.value = mapOf(
            objectId to Fields(map = mapOf(Relations.LAYOUT to null))
        )

        assertOptions(
            expected = ObjectMenuOptionsProvider.Options.ALL.copy(hasDiagnosticsVisibility = false)
        )
    }

    @Test
    fun `when restricts layout_change - layout options is invisible`() {
        details.value = mapOf(
            objectId to Fields(map = mapOf(Relations.LAYOUT to null))
        )
        restrictions.value = listOf(ObjectRestriction.LAYOUT_CHANGE)

        assertOptions(
            expected = ObjectMenuOptionsProvider.Options.ALL.copy(
                hasLayout = false,
                hasDiagnosticsVisibility = false
            )
        )
    }

    @Test
    fun `when object is Locked - show relations and history`() {
        details.value = mapOf(
            objectId to Fields(map = mapOf(Relations.LAYOUT to null))
        )

        assertOptions(
            isLocked = true,
            expected = ObjectMenuOptionsProvider.Options(
                hasIcon = false,
                hasCover = false,
                hasLayout = false,
                hasRelations = true,
                hasDiagnosticsVisibility = false
            )
        )
    }

    private fun assertOptions(
        expected: ObjectMenuOptionsProvider.Options,
        isLocked: Boolean = false,
    ) {
        runTest {
            provider.provide(objectId, isLocked).test {
                assertEquals(
                    expected = expected,
                    actual = awaitItem()
                )
            }
        }
    }
}