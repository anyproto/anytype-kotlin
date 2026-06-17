package com.anytypeio.anytype.middleware

import anytype.Event

/**
 * Demux key for the top-level per-event-type buffered fan-out (see
 * docs/superpowers/specs/2026-06-17-event-fanout-per-type-buffer-design.md).
 *
 * One proto [Event] carries a LIST of messages that may span several groups; `objectDetails*`
 * belongs to BOTH [EDITOR] and [SUBSCRIPTION], so routing is fan-out (a message can set >1 bit),
 * NOT a partition.
 *
 * Routing is intentionally FAIL-OPEN: [groupBits] returns a SUPERSET of the bits any consumer needs,
 * so a newly-added proto message type is over-delivered (every consumer re-filters by message field)
 * rather than silently unrouted. Under-routing loses events; over-routing only costs a `trySend`.
 * The contract test (EventGroupTest) asserts every message type each consumer filter accepts sets the
 * expected bit — so a forgotten arm here is a CI failure, not a production drop.
 *
 * IMPORTANT: when a new proto message field is added to a consumer's filter, add it to the matching
 * arm below AND to EventGroupTest. Cross-referenced consumers per group:
 *   EDITOR        -> MiddlewareEventChannel.isAccepted
 *   SUBSCRIPTION  -> MiddlewareSubscriptionEventChannel
 *   CHAT          -> ChatEventMiddlewareChannel
 *   SYNC_P2P      -> SyncAndP2PStatusEventsStoreImpl (SyncAndP2PChannelContainer)
 *   PROCESS       -> EventProcessDropFiles/Import/Migration MiddlewareChannel
 *   ACCOUNT       -> AuthMiddleware.observeAccounts (accountShow) + AccountStatusMiddlewareChannel (accountUpdate)
 *   FILE          -> FileLimitsMiddlewareChannel
 *   MEMBERSHIP    -> MembershipMiddlewareChannel
 *   NOTIFICATIONS -> NotificationsMiddlewareChannel
 */
enum class EventGroup {
    EDITOR,
    SUBSCRIPTION,
    CHAT,
    SYNC_P2P,
    PROCESS,
    ACCOUNT,
    FILE,
    MEMBERSHIP,
    NOTIFICATIONS;

    val bit: Int get() = 1 shl ordinal

    companion object {
        /** OR of every group a single message belongs to (fan-out; objectDetails* -> EDITOR|SUBSCRIPTION). */
        fun groupBits(message: Event.Message): Int {
            var mask = 0
            // objectDetails* -> BOTH editor and subscription
            if (message.objectDetailsSet != null ||
                message.objectDetailsAmend != null ||
                message.objectDetailsUnset != null
            ) {
                mask = mask or EDITOR.bit or SUBSCRIPTION.bit
            }
            // editor: block* / blockDataview* / objectRelations*
            if (message.blockAdd != null ||
                message.blockSetText != null ||
                message.blockSetChildrenIds != null ||
                message.blockSetBackgroundColor != null ||
                message.blockDelete != null ||
                message.blockSetLink != null ||
                message.blockSetFile != null ||
                message.blockSetFields != null ||
                message.blockSetBookmark != null ||
                message.blockSetAlign != null ||
                message.blockSetDiv != null ||
                message.blockSetRelation != null ||
                message.blockSetWidget != null ||
                message.blockDataviewRelationSet != null ||
                message.blockDataviewRelationDelete != null ||
                message.blockDataviewViewSet != null ||
                message.blockDataviewViewDelete != null ||
                message.blockDataviewViewOrder != null ||
                message.blockDataviewViewUpdate != null ||
                message.blockDataviewTargetObjectIdSet != null ||
                message.blockDataviewIsCollectionSet != null ||
                message.objectRelationsAmend != null ||
                message.objectRelationsRemove != null
            ) {
                mask = mask or EDITOR.bit
            }
            // subscription-only (no objectDetails) -> subscription
            if (message.subscriptionAdd != null ||
                message.subscriptionRemove != null ||
                message.subscriptionPosition != null ||
                message.subscriptionCounters != null
            ) {
                mask = mask or SUBSCRIPTION.bit
            }
            // chat
            if (message.chatAdd != null ||
                message.chatUpdate != null ||
                message.chatDelete != null ||
                message.chatStateUpdate != null ||
                message.chatUpdateReactions != null ||
                message.chatUpdateMessageReadStatus != null ||
                message.chatUpdateMentionReadStatus != null ||
                message.chatUpdateMessageSyncStatus != null
            ) {
                mask = mask or CHAT.bit
            }
            // sync / p2p (two prefix-disjoint types)
            if (message.p2pStatusUpdate != null || message.spaceSyncStatusUpdate != null) {
                mask = mask or SYNC_P2P.bit
            }
            // long-running process
            if (message.processNew != null || message.processUpdate != null || message.processDone != null) {
                mask = mask or PROCESS.bit
            }
            // account (accountShow REQUIRED — AuthMiddleware/login)
            if (message.accountShow != null || message.accountUpdate != null) {
                mask = mask or ACCOUNT.bit
            }
            // file limits / usage
            if (message.fileSpaceUsage != null ||
                message.fileLocalUsage != null ||
                message.fileLimitReached != null ||
                message.fileLimitUpdated != null
            ) {
                mask = mask or FILE.bit
            }
            // membership
            if (message.membershipUpdate != null || message.membershipTiersUpdate != null) {
                mask = mask or MEMBERSHIP.bit
            }
            // notifications
            if (message.notificationUpdate != null || message.notificationSend != null) {
                mask = mask or NOTIFICATIONS.bit
            }
            return mask
        }

        /** OR of [groupBits] over all messages in the payload. */
        fun groupBits(event: Event): Int {
            var mask = 0
            for (message in event.messages) mask = mask or groupBits(message)
            return mask
        }
    }
}
