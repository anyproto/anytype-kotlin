package com.anytypeio.anytype.domain.primitives

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.objects.GetDateObjectByTimestamp
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FieldParserGetObjectTypeIdAndNameTest {

    private val dateProvider: DateProvider = mock()
    private val logger: Logger = mock()
    private val getDateObjectByTimestamp: GetDateObjectByTimestamp = mock()
    private val stringResourceProvider: StringResourceProvider = mock()

    private val fieldParser = FieldParserImpl(
        dateProvider = dateProvider,
        logger = logger,
        getDateObjectByTimestamp = getDateObjectByTimestamp,
        stringResourceProvider = stringResourceProvider
    )

    companion object {
        private const val DELETED_TYPE_NAME = "Deleted type"
        private const val UNTITLED = "Untitled"
    }

    @Test
    fun `should return deleted type name when object has no type id`() {
        // Given
        whenever(stringResourceProvider.getDeletedTypeName()).thenReturn(DELETED_TYPE_NAME)

        val objectWrapper = ObjectWrapper.Basic(
            map = mapOf(
                Relations.ID to MockDataFactory.randomUuid(),
                Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
                // No TYPE relation - simulating deleted type
            )
        )
        val types = emptyList<ObjectWrapper.Type>()

        // When
        val result = fieldParser.getObjectTypeIdAndName(objectWrapper, types)

        // Then
        assertEquals(null to DELETED_TYPE_NAME, result)
    }

    @Test
    fun `should return deleted type name when type not found in store`() {
        // Given
        whenever(stringResourceProvider.getDeletedTypeName()).thenReturn(DELETED_TYPE_NAME)

        val typeId = MockDataFactory.randomUuid()
        val objectWrapper = ObjectWrapper.Basic(
            map = mapOf(
                Relations.ID to MockDataFactory.randomUuid(),
                Relations.TYPE to listOf(typeId),
                Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
            )
        )
        // Empty types list - type not in store
        val types = emptyList<ObjectWrapper.Type>()

        // When
        val result = fieldParser.getObjectTypeIdAndName(objectWrapper, types)

        // Then
        assertEquals(typeId to DELETED_TYPE_NAME, result)
    }

    @Test
    fun `should return deleted type name when type is deleted`() {
        // Given
        whenever(stringResourceProvider.getDeletedTypeName()).thenReturn(DELETED_TYPE_NAME)

        val typeId = MockDataFactory.randomUuid()
        val typeName = "My Type"
        val objectWrapper = ObjectWrapper.Basic(
            map = mapOf(
                Relations.ID to MockDataFactory.randomUuid(),
                Relations.TYPE to listOf(typeId),
                Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
            )
        )
        val types = listOf(
            ObjectWrapper.Type(
                map = mapOf(
                    Relations.ID to typeId,
                    Relations.UNIQUE_KEY to MockDataFactory.randomUuid(),
                    Relations.NAME to typeName,
                    Relations.IS_DELETED to true
                )
            )
        )

        // When
        val result = fieldParser.getObjectTypeIdAndName(objectWrapper, types)

        // Then
        assertEquals(typeId to DELETED_TYPE_NAME, result)
    }

    @Test
    fun `should return untitled when type exists but name is empty`() {
        // Given
        whenever(stringResourceProvider.getUntitledObjectTitle()).thenReturn(UNTITLED)

        val typeId = MockDataFactory.randomUuid()
        val objectWrapper = ObjectWrapper.Basic(
            map = mapOf(
                Relations.ID to MockDataFactory.randomUuid(),
                Relations.TYPE to listOf(typeId),
                Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
            )
        )
        val types = listOf(
            ObjectWrapper.Type(
                map = mapOf(
                    Relations.ID to typeId,
                    Relations.UNIQUE_KEY to MockDataFactory.randomUuid(),
                    Relations.NAME to ""
                )
            )
        )

        // When
        val result = fieldParser.getObjectTypeIdAndName(objectWrapper, types)

        // Then
        assertEquals(typeId to UNTITLED, result)
    }

    @Test
    fun `should return untitled when type exists but name is blank`() {
        // Given
        whenever(stringResourceProvider.getUntitledObjectTitle()).thenReturn(UNTITLED)

        val typeId = MockDataFactory.randomUuid()
        val objectWrapper = ObjectWrapper.Basic(
            map = mapOf(
                Relations.ID to MockDataFactory.randomUuid(),
                Relations.TYPE to listOf(typeId),
                Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
            )
        )
        val types = listOf(
            ObjectWrapper.Type(
                map = mapOf(
                    Relations.ID to typeId,
                    Relations.UNIQUE_KEY to MockDataFactory.randomUuid(),
                    Relations.NAME to "   "
                )
            )
        )

        // When
        val result = fieldParser.getObjectTypeIdAndName(objectWrapper, types)

        // Then
        assertEquals(typeId to UNTITLED, result)
    }

    @Test
    fun `should return untitled when type exists but name is null`() {
        // Given
        whenever(stringResourceProvider.getUntitledObjectTitle()).thenReturn(UNTITLED)

        val typeId = MockDataFactory.randomUuid()
        val objectWrapper = ObjectWrapper.Basic(
            map = mapOf(
                Relations.ID to MockDataFactory.randomUuid(),
                Relations.TYPE to listOf(typeId),
                Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
            )
        )
        val types = listOf(
            ObjectWrapper.Type(
                map = mapOf(
                    Relations.ID to typeId,
                    Relations.UNIQUE_KEY to MockDataFactory.randomUuid()
                    // NAME not set - null
                )
            )
        )

        // When
        val result = fieldParser.getObjectTypeIdAndName(objectWrapper, types)

        // Then
        assertEquals(typeId to UNTITLED, result)
    }

    @Test
    fun `should return type name when type exists with valid name`() {
        // Given
        val typeId = MockDataFactory.randomUuid()
        val typeName = "My Custom Type"
        val objectWrapper = ObjectWrapper.Basic(
            map = mapOf(
                Relations.ID to MockDataFactory.randomUuid(),
                Relations.TYPE to listOf(typeId),
                Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
            )
        )
        val types = listOf(
            ObjectWrapper.Type(
                map = mapOf(
                    Relations.ID to typeId,
                    Relations.UNIQUE_KEY to MockDataFactory.randomUuid(),
                    Relations.NAME to typeName
                )
            )
        )

        // When
        val result = fieldParser.getObjectTypeIdAndName(objectWrapper, types)

        // Then
        assertEquals(typeId to typeName, result)
    }

    @Test
    fun `should return untitled when archived type has empty name`() {
        // Given
        whenever(stringResourceProvider.getUntitledObjectTitle()).thenReturn(UNTITLED)

        val typeId = MockDataFactory.randomUuid()
        val objectWrapper = ObjectWrapper.Basic(
            map = mapOf(
                Relations.ID to MockDataFactory.randomUuid(),
                Relations.TYPE to listOf(typeId),
                Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
            )
        )
        val types = listOf(
            ObjectWrapper.Type(
                map = mapOf(
                    Relations.ID to typeId,
                    Relations.UNIQUE_KEY to MockDataFactory.randomUuid(),
                    Relations.NAME to "",
                    Relations.IS_ARCHIVED to true
                )
            )
        )

        // When
        val result = fieldParser.getObjectTypeIdAndName(objectWrapper, types)

        // Then
        assertEquals(typeId to UNTITLED, result)
    }

    @Test
    fun `should return type name when archived type has valid name`() {
        // Given
        val typeId = MockDataFactory.randomUuid()
        val typeName = "Archived Type"
        val objectWrapper = ObjectWrapper.Basic(
            map = mapOf(
                Relations.ID to MockDataFactory.randomUuid(),
                Relations.TYPE to listOf(typeId),
                Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
            )
        )
        val types = listOf(
            ObjectWrapper.Type(
                map = mapOf(
                    Relations.ID to typeId,
                    Relations.UNIQUE_KEY to MockDataFactory.randomUuid(),
                    Relations.NAME to typeName,
                    Relations.IS_ARCHIVED to true
                )
            )
        )

        // When
        val result = fieldParser.getObjectTypeIdAndName(objectWrapper, types)

        // Then
        assertEquals(typeId to typeName, result)
    }

    @Test
    fun `should return DATE type for DATE layout objects`() {
        // Given
        val typeName = "Date"
        val objectWrapper = ObjectWrapper.Basic(
            map = mapOf(
                Relations.ID to MockDataFactory.randomUuid(),
                Relations.LAYOUT to ObjectType.Layout.DATE.code.toDouble()
                // No TYPE relation needed - should use ObjectTypeIds.DATE
            )
        )
        val types = listOf(
            ObjectWrapper.Type(
                map = mapOf(
                    Relations.ID to ObjectTypeIds.DATE,
                    Relations.UNIQUE_KEY to ObjectTypeIds.DATE,
                    Relations.NAME to typeName
                )
            )
        )

        // When
        val result = fieldParser.getObjectTypeIdAndName(objectWrapper, types)

        // Then
        assertEquals(ObjectTypeIds.DATE to typeName, result)
    }

    @Test
    fun `should return deleted type when type is marked as deleted even if archived`() {
        // Given - Type is both deleted and archived, deleted should take precedence
        whenever(stringResourceProvider.getDeletedTypeName()).thenReturn(DELETED_TYPE_NAME)

        val typeId = MockDataFactory.randomUuid()
        val typeName = "My Type"
        val objectWrapper = ObjectWrapper.Basic(
            map = mapOf(
                Relations.ID to MockDataFactory.randomUuid(),
                Relations.TYPE to listOf(typeId),
                Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
            )
        )
        val types = listOf(
            ObjectWrapper.Type(
                map = mapOf(
                    Relations.ID to typeId,
                    Relations.UNIQUE_KEY to MockDataFactory.randomUuid(),
                    Relations.NAME to typeName,
                    Relations.IS_DELETED to true,
                    Relations.IS_ARCHIVED to true
                )
            )
        )

        // When
        val result = fieldParser.getObjectTypeIdAndName(objectWrapper, types)

        // Then
        assertEquals(typeId to DELETED_TYPE_NAME, result)
    }

    @Test
    fun `should return type name when type exists and not deleted with isDeleted false`() {
        // Given - Explicitly testing when isDeleted is false
        val typeId = MockDataFactory.randomUuid()
        val typeName = "Active Type"
        val objectWrapper = ObjectWrapper.Basic(
            map = mapOf(
                Relations.ID to MockDataFactory.randomUuid(),
                Relations.TYPE to listOf(typeId),
                Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
            )
        )
        val types = listOf(
            ObjectWrapper.Type(
                map = mapOf(
                    Relations.ID to typeId,
                    Relations.UNIQUE_KEY to MockDataFactory.randomUuid(),
                    Relations.NAME to typeName,
                    Relations.IS_DELETED to false
                )
            )
        )

        // When
        val result = fieldParser.getObjectTypeIdAndName(objectWrapper, types)

        // Then
        assertEquals(typeId to typeName, result)
    }
}
