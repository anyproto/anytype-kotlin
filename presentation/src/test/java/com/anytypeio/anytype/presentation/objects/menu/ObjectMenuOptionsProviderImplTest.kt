package com.anytypeio.anytype.presentation.objects.menu

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.Relations
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class ObjectMenuOptionsProviderImplTest {

    private val objectId: String = "objectId"
    private val details = MutableStateFlow<ObjectViewDetails>(ObjectViewDetails.EMPTY)
    private val hasObjectLayoutConflict = MutableStateFlow(false)
    private val personalFavoriteTargets = MutableStateFlow<Set<String>>(emptySet())
    private val canToggleChannelPin = MutableStateFlow(false)
    private val provider = ObjectMenuOptionsProviderImpl(
        objectViewDetailsFlow = details,
        hasObjectLayoutConflict = hasObjectLayoutConflict,
        personalFavoriteTargets = personalFavoriteTargets,
        canToggleChannelPin = canToggleChannelPin
    )

    @Test
    fun `when layout note - options are layout, relations, history`() {
        details.value = ObjectViewDetails(
            mapOf(
                objectId to mapOf(
                    Relations.ID to objectId,
                    Relations.LAYOUT to ObjectType.Layout.NOTE.code.toDouble()
                )
            )
        )
        val expected = ObjectMenuOptionsProvider.Options(
            hasIcon = false,
            hasCover = false,
            hasDescriptionShow = true,
            hasRelations = true,
            hasDiagnosticsVisibility = true,
            hasHistory = true,
            hasObjectLayoutConflict = false
        )

        assertOptions(
            expected = expected
        )
    }

    @Test
    fun `when layout task - options are layout, relations, history`() {
        details.value = ObjectViewDetails(
            mapOf(
                objectId to mapOf(
                    Relations.ID to objectId,
                    Relations.LAYOUT to ObjectType.Layout.TODO.code.toDouble()
                )
            )
        )
        val expected = ObjectMenuOptionsProvider.Options(
            hasIcon = false,
            hasCover = true,
            hasRelations = true,
            hasDiagnosticsVisibility = true,
            hasHistory = true,
            hasDescriptionShow = true,
            hasObjectLayoutConflict = false,
            hasTemplateNamePrefill = true,
            isTemplateNamePrefillEnabled = false
        )

        assertOptions(
            expected = expected
        )
    }

    @Test
    fun `when layout basic - all options are visible`() {
        details.value = ObjectViewDetails(
            mapOf(
                objectId to mapOf(
                    Relations.ID to objectId,
                    Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
                )
            )
        )

        assertOptions(
            expected = ObjectMenuOptionsProvider.Options.ALL.copy(
                hasDiagnosticsVisibility = true,
                hasTemplateNamePrefill = true,
                isTemplateNamePrefillEnabled = false
            )
        )
    }


    @Test
    fun `when layout null - all options are not visible`() {
        details.value = ObjectViewDetails(
            mapOf(
                objectId to mapOf(
                    Relations.ID to objectId
                )
            )
        )

        assertOptions(
            expected = ObjectMenuOptionsProvider.Options.NONE.copy(hasDiagnosticsVisibility = true)
        )
    }

    @Test
    fun `when object is Locked - show only relations`() {
        details.value = ObjectViewDetails(
            mapOf(
                objectId to mapOf(
                    Relations.ID to objectId,
                    Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
                )
            )
        )

        assertOptions(
            isLocked = true,
            expected = ObjectMenuOptionsProvider.Options(
                hasIcon = false,
                hasCover = false,
                hasRelations = true,
                hasDiagnosticsVisibility = true,
                hasHistory = false,
                hasDescriptionShow = true,
                hasObjectLayoutConflict = false,
                hasTemplateNamePrefill = true,
                isTemplateNamePrefillEnabled = false
            )
        )
    }

    @Test
    fun `when description is already in featured - show hide description`() {
        details.value = ObjectViewDetails(
            mapOf(
                objectId to mapOf(
                    Relations.ID to objectId,
                    Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
                    Relations.FEATURED_RELATIONS to listOf(Relations.DESCRIPTION)
                )
            )
        )

        assertOptions(
            isLocked = true,
            expected = ObjectMenuOptionsProvider.Options(
                hasIcon = false,
                hasCover = false,
                hasRelations = true,
                hasDiagnosticsVisibility = true,
                hasHistory = false,
                hasDescriptionShow = false,
                hasObjectLayoutConflict = false,
                hasTemplateNamePrefill = true,
                isTemplateNamePrefillEnabled = false
            )
        )
    }

    @Test
    fun `isFavorited is true when object id is in personal favorites`() = runTest {
        details.value = basicDetails()
        personalFavoriteTargets.value = setOf(objectId)

        provider.provide(objectId, isLocked = false, isReadOnly = false).test {
            val options = awaitItem()
            assertEquals(true, options.isFavorited)
            assertEquals(false, options.canToggleChannelPin)
        }
    }

    @Test
    fun `isFavorited is false when object id is absent from personal favorites`() = runTest {
        details.value = basicDetails()
        personalFavoriteTargets.value = setOf("someOtherId")

        provider.provide(objectId, isLocked = false, isReadOnly = false).test {
            assertEquals(false, awaitItem().isFavorited)
        }
    }

    @Test
    fun `canToggleChannelPin reflects user role flag`() = runTest {
        details.value = basicDetails()
        canToggleChannelPin.value = true

        provider.provide(objectId, isLocked = false, isReadOnly = false).test {
            assertEquals(true, awaitItem().canToggleChannelPin)
        }
    }

    @Test
    fun `new flags default to false when no flows are wired`() = runTest {
        val plainProvider = ObjectMenuOptionsProviderImpl(
            objectViewDetailsFlow = details,
            hasObjectLayoutConflict = hasObjectLayoutConflict
        )
        details.value = basicDetails()

        plainProvider.provide(objectId, isLocked = false, isReadOnly = false).test {
            val options = awaitItem()
            assertEquals(false, options.isFavorited)
            assertEquals(false, options.canToggleChannelPin)
        }
    }

    private fun basicDetails() = ObjectViewDetails(
        mapOf(
            objectId to mapOf(
                Relations.ID to objectId,
                Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
            )
        )
    )

    private fun assertOptions(
        expected: ObjectMenuOptionsProvider.Options,
        isLocked: Boolean = false,
        isReadOnly: Boolean = false
    ) {
        runTest {
            provider.provide(
                objectId,
                isLocked = isLocked,
                isReadOnly = isReadOnly
            ).test {
                assertEquals(
                    expected = expected,
                    actual = awaitItem()
                )
            }
        }
    }
}