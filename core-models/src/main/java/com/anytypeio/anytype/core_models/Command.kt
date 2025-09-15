package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_models.membership.NameServiceNameType
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey

sealed class Command {

    data class AccountCreate(
        val name: String,
        val avatarPath: String?,
        val icon: Int,
        val networkMode: NetworkMode = NetworkMode.DEFAULT,
        val networkConfigFilePath: String? = null
    ) : Command()

    data class AccountSelect(
        val id: String,
        val path: String,
        val networkMode: NetworkMode = NetworkMode.DEFAULT,
        val networkConfigFilePath: String? = null,
        val preferYamuxTransport: Boolean? = null
    ) : Command()

    data class SetInitialParams(
        val version: String,
        val platform: String,
        val workDir: String,
        val defaultLogLevel: String
    ) : Command()


    class UploadFile(
        val space: SpaceId,
        val path: String,
        val type: Block.Content.File.Type?
    )

    class FileDrop(
        val space: SpaceId,
        val ctx: Id,
        val dropTarget: Id,
        val blockPosition: Position,
        val localFilePaths: List<String>
    )

    class DownloadFile(
        val path: String,
        val objectId: Id
    )

    /**
     * Command for turning simple blocks into documents
     * @property context id of the context
     * @property targets id of the targets
     */
    class TurnIntoDocument(
        val context: Id,
        val targets: List<Id>
    )

    /**
     * @property contextId context id
     * @property blockId target block id
     * @property text updated text
     * @property marks marks of the updated text
     */
    class UpdateText(
        val contextId: Id,
        val blockId: Id,
        val text: String,
        val marks: List<Block.Content.Text.Mark>
    ) : Command()

    /**
     * Commands for updating document's title
     * @property context id of the context
     * @property title new title for the document
     */
    class UpdateTitle(
        val context: Id,
        val title: String
    )

    /**
     * Command for replacing target block by a new block (created from prototype)
     * @property context id of the context
     * @property target id of the block, which we need to replace
     * @property prototype prototype of the new block
     */
    data class Replace(
        val context: Id,
        val target: Id,
        val prototype: Block.Prototype
    )

    /**
     * Command for updating the whole block's text color.
     * @property context context id
     * @property targets id of the target blocks, whose color we need to update.
     * @property color new color (hex)
     */
    data class UpdateTextColor(
        val context: Id,
        val targets: List<Id>,
        val color: String
    )

    /**
     * Command for updating background color for the whole block.
     * @property context context id
     * @property targets id of the target blocks, whose background color we need to update.
     * @property color new color (hex)
     */
    data class UpdateBackgroundColor(
        val context: Id,
        val targets: List<Id>,
        val color: String
    )

    data class UpdateBlocksMark(
        val context: Id,
        val targets: List<Id>,
        val mark: Block.Content.Text.Mark
    )

    /**
     * Command for updating alignment for the block.
     * @property context context id
     * @property targets id of the target blocks, whose alignments we need to update.
     * @property alignment new alignment
     */
    data class UpdateAlignment(
        val context: Id,
        val targets: List<Id>,
        val alignment: Block.Align
    )

    /**
     * @property context context id
     * @property target id of the target checkbox block
     * @property isChecked new checked/unchecked state for this checkbox block
     */
    class UpdateCheckbox(
        val context: Id,
        val target: Id,
        val isChecked: Boolean
    )

    /**
     * Command for updating style for one textual block.
     * @property context context id
     * @property targets id of the target blocks, whose style we need to update.
     * @property style new style for the target block.
     */
    data class UpdateStyle(
        val context: Id,
        val targets: List<Id>,
        val style: Block.Content.Text.Style
    )

    /**
     * Command for set icon to text block
     * @property context context id
     * @property blockId text block id
     * @property icon
     */
    data class SetTextIcon(
        val context: Id,
        val blockId: Id,
        val icon: Icon,
    ) {
        sealed interface Icon {
            object None : Icon
            data class Emoji(val unicode: String) : Icon
            data class Image(val hash: Hash) : Icon
        }
    }

    /**
     * Command for creating a block
     * @property context id of the context of the block (i.e. page, dashboard or something else)
     * @property target id of the block associated with the block we need to create
     * @property position position of the block that we need to create in relation with the target block
     * @property prototype a prototype of the block we would like to create
     */
    class Create(
        val context: Id,
        val target: Id,
        val position: Position,
        val prototype: Block.Prototype
    )

    /**
     * Command for creating a new object with linked block
     * @property [context] id of the context of the block (i.e. page, dashboard or something else)
     * @property [target] id of the block associated with the block we need to create
     * @property [position] position of the block that we need to create in relation with the target block
     * @property [template] id of the template for this object (optional)
     * @property [prefilled] new object details
     * @property [internalFlags] flags responsible for the object creation logic
     */
    data class CreateBlockLinkWithObject(
        val context: Id,
        val type: TypeKey,
        val target: Id,
        val position: Position,
        val template: Id?,
        val prefilled: Struct,
        val internalFlags: List<InternalFlags>,
        val space: Id
    )

    /**
     * Command for creating a new object
     * @property [prefilled] new object details
     * @property [template] id of the template for this object (optional)
     * @property [internalFlags] flags responsible for the object creation logic
     */
    data class CreateObject(
        val prefilled: Struct,
        val template: Id?,
        val internalFlags: List<InternalFlags>,
        val space: SpaceId,
        val typeKey: TypeKey
    )

    class Move(
        val ctx: Id,
        val targetId: Id,
        val targetContextId: Id,
        val blockIds: List<String>,
        val position: Position
    )

    /**
     * Command for block duplication
     * @property context context id
     * @property blocks id of the target blocks, which we need to duplicate
     */
    class Duplicate(
        val context: Id,
        val target: Id,
        val blocks: List<Id>
    )

    /**
     * Command for unlinking a set of blocks from its context (i.e. page)
     * @property context context id
     * @property targets ids of the blocks, which we need to unlink from its [context]
     */
    class Unlink(
        val context: Id,
        val targets: List<Id>
    )

    /**
     * Command for merging two blocks into one block
     * @property context context id
     * @property pair pair of the blocks, which we need to merge
     */
    data class Merge(
        val context: Id,
        val pair: Pair<Id, Id>
    )

    /**
     * Command for splitting one block into two blocks
     * @property context context id
     * @property target id of the target block, which we need to split
     * @property style target block text style
     * @property index index or cursor position
     */
    data class Split(
        val context: Id,
        val target: Id,
        val style: Block.Content.Text.Style,
        val range: IntRange,
        val mode: BlockSplitMode
    )

    /**
     * Command for uploading media or file block
     * @property contextId context id
     * @property blockId id of the block
     * @property url valid url
     * @property filePath file uri
     */
    data class UploadBlock(
        val contextId: Id,
        val blockId: Id,
        val url: String,
        val filePath: String
    )


    /**
     * Command for setting relation key for a block
     * @property contextId context id
     * @property blockId id of the block
     * @property key relation key
     */
    data class SetRelationKey(
        val contextId: Id,
        val blockId: Id,
        val key: Id
    )

    data class AddRelationToBlock(
        val contextId: Id,
        val blockId: Id,
        val relation: Relation
    )

    /**
     * Command for setting document's emoji icon
     * @property emoji emoji's unicode
     * @property context id of the context for this operation
     */
    data class SetDocumentEmojiIcon(
        val context: Id,
        val emoji: String
    )

    /**
     * Command for setting document's image icon
     * @property id image hash
     * @property context id of the context for this operation
     */
    data class SetDocumentImageIcon(
        val context: Id,
        val id: Hash
    )

    /**
     * Command for setting up a bookmark from [url]
     * @property context id of the context
     * @property target id of the target block (future bookmark block)
     * @property url bookmark url
     */
    data class SetupBookmark(
        val context: Id,
        val target: Id,
        val url: String
    )

    /**
     * Command for creating a bookmark block from [url]
     * @property context id of the context
     * @property target id of the target block (future bookmark block)
     * @property url bookmark url
     * @property [position] position relative to [target] block
     */
    data class CreateBookmark(
        val context: Id,
        val target: Id,
        val url: String,
        val position: Position
    )

    /**
     * Command for undoing latest changes in document
     * @property context id of the context
     */
    data class Undo(val context: Id)

    /**
     * Command for redoing latest changes in document
     * @property context id of the context
     */
    data class Redo(val context: Id)

    /**
     * Command for clipboard paste operation
     * @property context id of the context
     * @property focus id of the focused/target block
     * @property selected id of currently selected blocks
     * @property range selected text range
     * @property text plain text to paste
     * @property html optional html to paste
     * @property blocks blocks currently contained in clipboard
     */
    data class Paste(
        val context: Id,
        val focus: Id,
        val selected: List<Id>,
        val range: IntRange,
        val text: String,
        val html: String?,
        val blocks: List<Block>,
        val isPartOfBlock: Boolean? = null
    )

    /**
     * Command for clipboard copy operation.
     * @param context id of the context
     * @param range selected text range
     * @param blocks associated blocks
     */
    data class Copy(
        val context: Id,
        val range: IntRange?,
        val blocks: List<Block>
    )

    /**
     * Command for updating style for divider blocks.
     * @property context context id
     * @property targets id of the target blocks, whose style we need to update.
     * @property style new style for the target block.
     */
    data class UpdateDivider(
        val context: Id,
        val targets: List<Id>,
        val style: Block.Content.Divider.Style
    )

    data class SetFields(
        val context: Id,
        val fields: List<Pair<Id, Block.Fields>>
    )

    data class SetLinkAppearance(
        val contextId: String,
        val blockId: String,
        val content: Block.Content.Link
    )

    data class AddFilter(
        val ctx: Id,
        val dv: Id,
        val view: Id,
        val relationKey: String,
        val relationFormat: RelationFormat?,
        val operator: Block.Content.DataView.Filter.Operator,
        val condition: Block.Content.DataView.Filter.Condition,
        val quickOption: Block.Content.DataView.Filter.QuickOption,
        val value: Any? = null
    )

    data class ReplaceFilter(
        val ctx: Id,
        val dv: Id,
        val view: Id,
        val id: Id,
        val filter: DVFilter
    )

    data class RemoveFilter(val ctx: Id, val dv: Id, val view: Id, val ids: List<Id>)

    data class AddSort(
        val ctx: Id,
        val dv: Id,
        val view: Id,
        val relationKey: String,
        val relationFormat: RelationFormat? = null,
        val type: DVSortType,
        val customOrder: List<Any> = emptyList(),
        val includeTime: Boolean? = null
    )

    data class ReplaceSort(val ctx: Id, val dv: Id, val view: Id, val sort: DVSort)
    data class RemoveSort(val ctx: Id, val dv: Id, val view: Id, val ids: List<Id>)

    data class AddRelation(val ctx: Id, val dv: Id, val view: Id, val relation: DVViewerRelation)
    data class UpdateRelation(val ctx: Id, val dv: Id, val view: Id, val relation: DVViewerRelation)
    data class DeleteRelation(val ctx: Id, val dv: Id, val view: Id, val keys: List<Key>)
    data class SortRelations(val ctx: Id, val dv: Id, val view: Id, val keys: List<Key>)

    data class AddObjectToCollection(val ctx: Id, val afterId: Id, val ids: List<Id>)
    data class SetQueryToSet(val ctx: Id, val query: String)

    data class SetInternalFlags(val ctx: Id, val flags: List<InternalFlags>)

    data class CreateSpace(
        val details: Struct,
        val useCase: SpaceCreationUseCase
    ) {
        data class Result(
            val space: SpaceId,
            val startingObject: Id? = null
        )
    }

    data object ImportUseCase {
        data class Result(
            val startingObject: Id? = null
        )
    }

    data class CreateObjectType(
        val details: Struct,
        val spaceId: Id,
        val internalFlags: List<InternalFlags>
    )

    data class AddObjectToSpace(val space: Id, val objectId: Id)
    data class ApplyTemplate(val objectId: Id, val template: Id?)

    data class DeleteRelationOptions(val optionIds: List<Id>)

    data class RelationListWithValue(val space: SpaceId, val value: Any?)

    data class ObjectDateByTimestamp(val space: SpaceId, val timeInSeconds: TimeInSeconds)

    data class SendJoinSpaceRequest(
        val space: SpaceId,
        val network: Id?,
        val inviteContentId: Id,
        val inviteFileKey: String
    )

    data class SpaceChangeInvite(
        val space: SpaceId,
        val permissions: SpaceMemberPermissions
    )

    data class ApproveSpaceLeaveRequest(
        val space: SpaceId,
        val identities: List<Id>
    )

    data class DownloadGalleryManifest(val url: String)

    data class ImportExperience(
        val space: SpaceId,
        val url: String,
        val title: String,
        val isNewSpace: Boolean
    )

    sealed class Membership {
        data class GetStatus(val noCache: Boolean) : Membership()
        data class IsNameValid(
            val tier: Int,
            val name: String,
            val nameType: NameServiceNameType
        ) : Membership()

        data class ResolveName(
            val name: String,
            val nameType: NameServiceNameType
        ) : Membership()

        data class GetPaymentUrl(
            val tier: Int,
            val paymentMethod: MembershipPaymentMethod,
            val name: String,
            val nameType: NameServiceNameType
        ) : Membership()

        data class Finalize(val name: String, val nameType: NameServiceNameType) : Membership()
        data class GetVerificationEmail(
            val email: String,
            val subscribeToNewsletter: Boolean,
            val isFromOnboarding: Boolean
        ) : Membership()

        data class VerifyEmailCode(val code: String) : Membership()
        data class GetTiers(val noCache: Boolean, val locale: String) : Membership()
    }

    data class SearchWithMeta(
        val query: String = EMPTY_QUERY,
        val limit: Int = 0,
        val offset: Int = 0,
        val keys: List<Key>,
        val sorts: List<DVSort> = emptyList(),
        val filters: List<DVFilter>,
        val withMeta: Boolean = false,
        val withMetaRelationDetails: Boolean = false,
        val space: SpaceId
    ) {
        data class Result(
            val obj: Id,
            val wrapper: ObjectWrapper.Basic,
            val metas: List<Meta>
        ) {
            data class Meta(
                val source: Source,
                val highlight: String?,
                val ranges: List<IntRange>,
                val dependencies: List<ObjectWrapper.Basic>
            ) {
                sealed class Source {
                    data class Relation(val key: Key) : Source()
                    data class Block(val id: Id) : Source()
                }
            }
        }
    }

    data class ProcessCancel(
        val processId: Id
    ) : Command()

    sealed class VersionHistory {
        data class GetVersions(
            val objectId: Id,
            val lastVersion: Id?,
            val limit: Int
        ) : VersionHistory()

        data class ShowVersion(
            val objectId: Id,
            val versionId: Id,
            val traceId: Id
        ) : VersionHistory()

        data class SetVersion(
            val objectId: Id,
            val versionId: Id
        ) : VersionHistory()

        data class DiffVersions(
            val objectId: Id,
            val spaceId: Id,
            val currentVersion: Id,
            val previousVersion: Id
        ) : VersionHistory()
    }

    sealed class ChatCommand {

        data class ReadMessages(
            val chat: Id,
            val afterOrderId: Id? = null,
            val beforeOrderId: Id? = null,
            val lastStateId: Id? = null,
            val isMention: Boolean = false
        )

        data class AddMessage(
            val chat: Id,
            val message: Chat.Message,
        ) : ChatCommand()

        data class DeleteMessage(
            val chat: Id,
            val msg: Id
        ) : ChatCommand()

        data class EditMessage(
            val chat: Id,
            val message: Chat.Message
        ) : ChatCommand()

        /**
         * @property [includeBoundary] defines whether the message corresponding to the order ID
         * in [afterOrderId] or [beforeOrderId] should be included in results.
         */
        data class GetMessages(
            val chat: Id,
            val beforeOrderId: Id? = null,
            val afterOrderId: Id? = null,
            val limit: Int,
            val includeBoundary: Boolean = false
        ) : ChatCommand() {
            data class Response(
                val messages: List<Chat.Message>,
                val state: Chat.State? = null
            )
        }

        data class GetMessagesByIds(
            val chat: Id,
            val messages: List<Id>
        ) : ChatCommand()

        data class SubscribeLastMessages(
            val chat: Id,
            val limit: Int
        ) : ChatCommand() {
            data class Response(
                val messages: List<Chat.Message>,
                val messageCountBefore: Int,
                val chatState: Chat.State? = null
            )
        }

        data class ToggleMessageReaction(
            val chat: Id,
            val msg: Id,
            val emoji: String
        ) : ChatCommand()
    }

    /**
     * id of dataview block
     * id of active vi1ew
     */
    data class DataViewSetActiveView(
        val ctx: Id,
        val dataViewId: Id,
        val viewerId: Id
    )
    data class ObjectTypeConflictingFields(
        val spaceId: String,
        val objectTypeId: String
    ) : Command()

    data class ObjectTypeSetRecommendedHeaderFields(
        val objectTypeId: String,
        val fields: List<Id>
    ) : Command()

    data class ObjectTypeSetRecommendedFields(
        val objectTypeId: String,
        val fields: List<Id>
    ) : Command()

    data class SetDataViewProperties(
        val objectId: Id,
        val blockId: Id,
        val properties: List<Key>
    ) : Command()

    data class RegisterDeviceToken(
        val token: String
    ) : Command()

    sealed class Publishing : Command() {
        data class GetStatus(
            val space: SpaceId,
            val objectId: Id
        ) : Publishing()

        data class GetList(
            val space: SpaceId?
        ) : Publishing()

        data class Create(
            val space: SpaceId,
            val objectId: Id,
            val uri: String,
            val showJoinSpaceBanner: Boolean = false
        ) : Publishing()

        data class Remove(
            val space: SpaceId,
            val objectId: Id
        ) : Publishing()
    }
}
