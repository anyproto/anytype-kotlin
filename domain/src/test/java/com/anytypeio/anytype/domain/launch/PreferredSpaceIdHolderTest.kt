package com.anytypeio.anytype.domain.launch

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PreferredSpaceIdHolderTest {

    private val holder: PreferredSpaceIdHolder = PreferredSpaceIdHolder.Default

    @Test
    fun `consume returns null when nothing was set`() {
        holder.clear()
        assertNull(holder.consume())
    }

    @Test
    fun `consume returns the set value then clears it`() {
        holder.set("space-1")
        assertEquals("space-1", holder.consume())
        assertNull(holder.consume())
    }

    @Test
    fun `set overwrites previous value`() {
        holder.set("space-1")
        holder.set("space-2")
        assertEquals("space-2", holder.consume())
    }

    @Test
    fun `clear removes the value`() {
        holder.set("space-1")
        holder.clear()
        assertNull(holder.consume())
    }
}
