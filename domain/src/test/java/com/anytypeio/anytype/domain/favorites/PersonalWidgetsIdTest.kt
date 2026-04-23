package com.anytypeio.anytype.domain.favorites

import com.anytypeio.anytype.core_models.primitives.SpaceId
import org.junit.Assert.assertEquals
import org.junit.Test

class PersonalWidgetsIdTest {

    @Test
    fun `replaces the first dot with an underscore and prepends the prefix`() {
        val result = personalWidgetsId(SpaceId("bafyreig5abc.2f7dexample"))
        assertEquals("_personalWidgets_bafyreig5abc_2f7dexample", result)
    }

    @Test
    fun `only replaces the first dot when multiple are present`() {
        val result = personalWidgetsId(SpaceId("aaa.bbb.ccc"))
        assertEquals("_personalWidgets_aaa_bbb.ccc", result)
    }

    @Test
    fun `returns prefix plus raw id when spaceId has no dot`() {
        val result = personalWidgetsId(SpaceId("nodot"))
        assertEquals("_personalWidgets_nodot", result)
    }
}
