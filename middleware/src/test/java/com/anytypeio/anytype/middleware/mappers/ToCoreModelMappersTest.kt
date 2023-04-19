package com.anytypeio.anytype.middleware.mappers

import kotlin.test.assertEquals
import org.junit.Test

class ToCoreModelMappersTest {

    @Test
    fun `should convert middleware relation type models to core models`() {
        MRelationFormat.values().forEach { mwModel ->
            val coreModel = mwModel.format()
            assertEquals(
                expected = coreModel.code, actual = mwModel.value
            )
        }
    }

    @Test
    fun `should convert middleware object restrictions type models to core models`() {
        MObjectRestriction.values().forEach { mwModel ->
            val coreModel = mwModel.toCoreModel()
            assertEquals(
                expected = coreModel.code, actual = mwModel.value
            )
        }
    }
}