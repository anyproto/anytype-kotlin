package com.anytypeio.anytype.middleware

import anytype.Event
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Contract test for the fail-open routing. Routing is fail-CLOSED on omission: any message field a
 * consumer accepts but [EventGroup.groupBits] does not route is a silent production drop after the
 * consumers migrate to flow(group). This test asserts EXACT routing per accepted field, so a missing
 * or wrong arm is a CI failure.
 *
 * Coverage is exhaustive per accepted field for every consumer EXCEPT EDITOR's block* family (all of
 * which route to EDITOR via a single arm, audited field-by-field against MiddlewareEventChannel.
 * isAccepted; a representative block field + objectRelations are covered here). Any NEW accepted field
 * must be added to BOTH EventGroup.groupBits AND this list.
 */
class EventGroupTest {

    private fun routed(message: Event.Message): Set<EventGroup> {
        val bits = EventGroup.groupBits(message)
        return EventGroup.values().filter { (bits and it.bit) != 0 }.toSet()
    }

    private data class Case(val name: String, val message: Event.Message, val groups: Set<EventGroup>)

    private val cases = listOf(
        // objectDetails* -> EDITOR + SUBSCRIPTION (fan-out, not partition)
        Case("objectDetailsSet", Event.Message(objectDetailsSet = Event.Object.Details.Set()), setOf(EventGroup.EDITOR, EventGroup.SUBSCRIPTION)),
        Case("objectDetailsAmend", Event.Message(objectDetailsAmend = Event.Object.Details.Amend()), setOf(EventGroup.EDITOR, EventGroup.SUBSCRIPTION)),
        Case("objectDetailsUnset", Event.Message(objectDetailsUnset = Event.Object.Details.Unset()), setOf(EventGroup.EDITOR, EventGroup.SUBSCRIPTION)),
        // subscription-only -> SUBSCRIPTION (must route without objectDetails)
        Case("subscriptionAdd", Event.Message(subscriptionAdd = Event.Object.Subscription.Add()), setOf(EventGroup.SUBSCRIPTION)),
        Case("subscriptionRemove", Event.Message(subscriptionRemove = Event.Object.Subscription.Remove()), setOf(EventGroup.SUBSCRIPTION)),
        Case("subscriptionPosition", Event.Message(subscriptionPosition = Event.Object.Subscription.Position()), setOf(EventGroup.SUBSCRIPTION)),
        Case("subscriptionCounters", Event.Message(subscriptionCounters = Event.Object.Subscription.Counters()), setOf(EventGroup.SUBSCRIPTION)),
        // editor (representative block + objectRelations; block* family audited in groupBits)
        Case("blockSetText", Event.Message(blockSetText = Event.Block.Set.Text()), setOf(EventGroup.EDITOR)),
        Case("objectRelationsAmend", Event.Message(objectRelationsAmend = Event.Object.Relations.Amend()), setOf(EventGroup.EDITOR)),
        Case("objectRelationsRemove", Event.Message(objectRelationsRemove = Event.Object.Relations.Remove()), setOf(EventGroup.EDITOR)),
        // chat (all 8)
        Case("chatAdd", Event.Message(chatAdd = Event.Chat.Add()), setOf(EventGroup.CHAT)),
        Case("chatUpdate", Event.Message(chatUpdate = Event.Chat.Update()), setOf(EventGroup.CHAT)),
        Case("chatDelete", Event.Message(chatDelete = Event.Chat.Delete()), setOf(EventGroup.CHAT)),
        Case("chatStateUpdate", Event.Message(chatStateUpdate = Event.Chat.UpdateState()), setOf(EventGroup.CHAT)),
        Case("chatUpdateReactions", Event.Message(chatUpdateReactions = Event.Chat.UpdateReactions()), setOf(EventGroup.CHAT)),
        Case("chatUpdateMessageReadStatus", Event.Message(chatUpdateMessageReadStatus = Event.Chat.UpdateMessageReadStatus()), setOf(EventGroup.CHAT)),
        Case("chatUpdateMentionReadStatus", Event.Message(chatUpdateMentionReadStatus = Event.Chat.UpdateMentionReadStatus()), setOf(EventGroup.CHAT)),
        Case("chatUpdateMessageSyncStatus", Event.Message(chatUpdateMessageSyncStatus = Event.Chat.UpdateMessageSyncStatus()), setOf(EventGroup.CHAT)),
        // sync / p2p (two prefix-disjoint types)
        Case("p2pStatusUpdate", Event.Message(p2pStatusUpdate = Event.P2PStatus.Update()), setOf(EventGroup.SYNC_P2P)),
        Case("spaceSyncStatusUpdate", Event.Message(spaceSyncStatusUpdate = Event.Space.SyncStatus.Update()), setOf(EventGroup.SYNC_P2P)),
        // process (all 3)
        Case("processNew", Event.Message(processNew = Event.Process.New()), setOf(EventGroup.PROCESS)),
        Case("processUpdate", Event.Message(processUpdate = Event.Process.Update()), setOf(EventGroup.PROCESS)),
        Case("processDone", Event.Message(processDone = Event.Process.Done()), setOf(EventGroup.PROCESS)),
        // account (both; accountShow is the login path)
        Case("accountShow", Event.Message(accountShow = Event.Account.Show()), setOf(EventGroup.ACCOUNT)),
        Case("accountUpdate", Event.Message(accountUpdate = Event.Account.Update()), setOf(EventGroup.ACCOUNT)),
        // file (all 4 — fileLimitReached is the C1 regression guard)
        Case("fileSpaceUsage", Event.Message(fileSpaceUsage = Event.File.SpaceUsage()), setOf(EventGroup.FILE)),
        Case("fileLocalUsage", Event.Message(fileLocalUsage = Event.File.LocalUsage()), setOf(EventGroup.FILE)),
        Case("fileLimitReached", Event.Message(fileLimitReached = Event.File.LimitReached()), setOf(EventGroup.FILE)),
        Case("fileLimitUpdated", Event.Message(fileLimitUpdated = Event.File.LimitUpdated()), setOf(EventGroup.FILE)),
        // membership (both)
        Case("membershipUpdate", Event.Message(membershipUpdate = Event.Membership.Update()), setOf(EventGroup.MEMBERSHIP)),
        Case("membershipTiersUpdate", Event.Message(membershipTiersUpdate = Event.Membership.TiersUpdate()), setOf(EventGroup.MEMBERSHIP)),
        // notifications (both)
        Case("notificationUpdate", Event.Message(notificationUpdate = Event.Notification.Update()), setOf(EventGroup.NOTIFICATIONS)),
        Case("notificationSend", Event.Message(notificationSend = Event.Notification.Send()), setOf(EventGroup.NOTIFICATIONS)),
    )

    @Test
    fun `every accepted field routes to exactly its expected groups`() {
        for (c in cases) {
            assertEquals(c.groups, routed(c.message), "routing for ${c.name}")
        }
    }

    @Test
    fun `empty message routes nowhere`() {
        assertEquals(emptySet(), routed(Event.Message()))
    }

    @Test
    fun `event mask is the OR over its messages`() {
        val event = Event(
            messages = listOf(
                Event.Message(p2pStatusUpdate = Event.P2PStatus.Update()),
                Event.Message(chatAdd = Event.Chat.Add())
            )
        )
        val bits = EventGroup.groupBits(event)
        assertEquals(
            setOf(EventGroup.SYNC_P2P, EventGroup.CHAT),
            EventGroup.values().filter { (bits and it.bit) != 0 }.toSet()
        )
    }
}
