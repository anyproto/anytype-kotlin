package com.anytypeio.anytype.middleware

import anytype.Event
import com.squareup.wire.Message
import org.junit.Test
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Iterates EVERY oneof message field of the generated [Event.Message] binding and asserts
 * [EventGroup.groupBits] routes it to the group implied by its family prefix. This auto-covers NEW
 * proto events: when a field is added to a routed family, this test fails until groupBits routes it
 * (and a field in no routed family must route nowhere). Keeps the routing table honest without a
 * hand-maintained list.
 */
class EventGroupReflectionTest {

    private fun expected(field: String): Set<EventGroup> = when {
        field.startsWith("objectDetails") -> setOf(EventGroup.EDITOR, EventGroup.SUBSCRIPTION)
        field.startsWith("objectRelations") -> setOf(EventGroup.EDITOR)
        field.startsWith("block") -> setOf(EventGroup.EDITOR)
        field.startsWith("subscription") -> setOf(EventGroup.SUBSCRIPTION)
        field.startsWith("chat") -> setOf(EventGroup.CHAT)
        field == "p2pStatusUpdate" || field == "spaceSyncStatusUpdate" -> setOf(EventGroup.SYNC_P2P)
        field.startsWith("process") -> setOf(EventGroup.PROCESS)
        field.startsWith("account") -> setOf(EventGroup.ACCOUNT)
        field.startsWith("file") -> setOf(EventGroup.FILE)
        field.startsWith("membership") -> setOf(EventGroup.MEMBERSHIP)
        field.startsWith("notification") -> setOf(EventGroup.NOTIFICATIONS)
        else -> emptySet() // no consumer -> intentionally unrouted
    }

    @Test
    fun `groupBits routes every generated Event_Message field per its family prefix`() {
        val ctor = Event.Message::class.primaryConstructor!!
        val messageFields = Event.Message::class.declaredMemberProperties.filter { p ->
            (p.returnType.classifier as? KClass<*>)?.isSubclassOf(Message::class) == true
        }
        assertTrue(messageFields.size > 20, "reflection found too few oneof fields: ${messageFields.size}")

        val mismatches = messageFields.mapNotNull { p ->
            val fieldClass = p.returnType.classifier as KClass<*>
            val param = ctor.parameters.first { it.name == p.name }
            val message = ctor.callBy(mapOf(param to fieldClass.createInstance()))
            val bits = EventGroup.groupBits(message)
            val routed = EventGroup.values().filter { (bits and it.bit) != 0 }.toSet()
            if (routed != expected(p.name)) "${p.name}: expected ${expected(p.name)}, routed $routed" else null
        }

        assertEquals(emptyList(), mismatches, "groupBits routing diverges from family-prefix contract")
    }
}
