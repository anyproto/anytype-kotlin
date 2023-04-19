package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.core_models.Block.Content.Text
import com.anytypeio.anytype.core_models.restrictions.DataViewRestrictions
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction

sealed class Event {

    abstract val context: Id

    sealed class Command : Event() {

        sealed class BlockEvent : Command() {
            data class SetRelation(
                override val context: String,
                val id: Id,
                val key: String?
            ) : BlockEvent()
        }

        data class ShowObject(
            override val context: Id,
            val root: Id,
            val details: Block.Details = Block.Details(emptyMap()),
            val blocks: List<Block>,
            val relations: List<Relation> = emptyList(),
            val relationLinks: List<RelationLink> = emptyList(),
            val objectRestrictions: List<ObjectRestriction> = emptyList(),
            val dataViewRestrictions: List<DataViewRestrictions> = emptyList()
        ) : Command()

        data class AddBlock(
            override val context: String,
            val blocks: List<Block>,
            val details: Block.Details = Block.Details(emptyMap())
        ) : Command()

        /**
         * Command to delete blocks
         * @property context id of the context
         * @property targets id of the target blocks, which we need to delete
         */
        data class DeleteBlock(
            override val context: String,
            val targets: List<Id>
        ) : Command()

        data class UpdateBlockText(
            override val context: String,
            val id: Id,
            val text: String
        ) : Command()

        /**
         * Command to update block's text content.
         * @property id id of the target block
         * @property text new text (considered updated if not null)
         * @property style new style (considered updated if not null)
         * @property color new color of the whole block (considered updated if not null)
         * @property backgroundColor background color of the whole block (considered updated if not null)
         */
        data class GranularChange(
            override val context: String,
            val id: Id,
            val text: String? = null,
            val style: Text.Style? = null,
            val color: String? = null,
            val backgroundColor: String? = null,
            val marks: List<Text.Mark>? = null,
            val alignment: Block.Align? = null,
            val checked: Boolean? = null,
            val emojiIcon: String? = null,
            val imageIcon: String? = null,
        ) : Command()

        /**
         * Command to update link.
         * @property context update's context
         * @property id id of the link
         * @property target id of the linked block
         */
        data class LinkGranularChange(
            override val context: String,
            val id: Id,
            val target: Id,
            val iconSize: Block.Content.Link.IconSize?,
            val cardStyle: Block.Content.Link.CardStyle?,
            val description: Block.Content.Link.Description?,
            val relations: Set<Key>?,
        ) : Command()

        /**
         * Command to update bookmark
         * @property context id of the context
         * @property target id of the bookmark block
         * @property url bookmark's url (considered updated if not null)
         * @property title bookmark's title (considered updated if not null)
         * @property description bookmark's description (considered updated if not null)
         * @property image bookmark's image hash (considered updated if not null)
         * @property favicon bookmark's favicon hash (considered updated if not null)
         */
        data class BookmarkGranularChange(
            override val context: Id,
            val target: Id,
            val url: Url?,
            val title: String?,
            val description: String?,
            val image: Hash?,
            val favicon: Hash?,
            val targetObjectId: Id?,
            val state: Block.Content.Bookmark.State?
        ) : Command()

        /**
         * Command to update a block structure.
         * @property context context id for this command (i.e page id, dashboard id, etc.)
         * @property id id of the block whose structure we need to update
         * @property children list of children ids for this block [id]
         */
        data class UpdateStructure(
            override val context: String,
            val id: Id,
            val children: List<Id>
        ) : Command()

        data class UpdateFields(
            override val context: Id,
            val target: Id,
            val fields: Block.Fields
        ) : Command()


        sealed class Details : Command() {
            /**
             * Command to set details (metadata) of the target block.
             * Overwrites existing state.
             * @property context id of the context
             * @property target id of the target block, whose details we need to update
             * @property details details of the target block
             */
            data class Set(
                override val context: Id,
                val target: Id,
                val details: Block.Fields
            ) : Details()

            /**
             * Command to amend details (metadata) of the target block.
             * Amend existing state.
             * @property context id of the context
             * @property target id of the target block, whose details we need to update
             * @property details slide of details of the target block
             */
            data class Amend(
                override val context: Id,
                val target: Id,
                val details: Map<Id, Any?>
            ) : Details()

            /**
             * Command to unset details (metadata) of the target block.
             * Unset existing detail keys.
             * @property context id of the context
             * @property target id of the target block, whose details we need to update
             * @property keys
             */
            data class Unset(
                override val context: Id,
                val target: Id,
                val keys: List<Id>
            ) : Details()
        }


        /**
         * Command to update file block content
         */
        data class UpdateFileBlock(
            override val context: String,
            val id: Id,
            val state: Block.Content.File.State? = null,
            val type: Block.Content.File.Type? = null,
            val name: String? = null,
            val hash: String? = null,
            val mime: String? = null,
            val size: Long? = null
        ) : Command()

        data class UpdateDividerBlock(
            override val context: String,
            val id: Id,
            val style: Block.Content.Divider.Style
        ) : Command()

        sealed class ObjectRelationLinks : Command() {
            data class Amend(
                override val context: String,
                val id: Id,
                val relationLinks: List<RelationLink>
            ) : ObjectRelationLinks()

            data class Remove(
                override val context: String,
                val id: Id,
                val keys: List<Key>
            ) : ObjectRelationLinks()
        }

        sealed class ObjectRelation : Command() {
            data class Set(
                override val context: String,
                val id: Id,
                val key: Id,
                val relations: List<Relation>
            ) : ObjectRelation()

            data class Remove(
                override val context: String,
                val id: Id,
                val key: Id
            ) : ObjectRelation()
        }

        sealed class DataView : Command() {

            /**
             * Sent when a data-view's view has been changed or added.
             * @property [target] data view's block id
             */
            data class SetView(
                override val context: Id,
                val target: Id,
                val viewerId: Id,
                val viewer: DVViewer
            ) : DataView()

            data class DeleteView(
                override val context: String,
                val target: String,
                val viewer: String
            ) : DataView()

            data class SetTargetObjectId(
                override val context: String,
                val dv: String,
                val targetObjectId: String
            ) : DataView()

            /**
             * Sent when a data-view's relation has been changed or added.
             * @property [dv] data view's block id
             * @property [links] relations link for this data view
             */
            data class SetRelation(
                override val context: Id,
                val dv: Id,
                val links: List<RelationLink>
            ) : DataView()

            data class DeleteRelation(
                override val context: String,
                val dv: Id,
                val keys: List<Key>
            ) : DataView()

            data class SetIsCollection(
                override val context: Id,
                val dv: Id,
                val isCollection: Boolean
            ) : DataView()

            data class UpdateView(
                override val context: Id,
                val block: Id,
                val viewerId: Id,
                val filterUpdates: List<DVFilterUpdate> = emptyList(),
                val sortUpdates: List<DVSortUpdate> = emptyList(),
                val relationUpdates: List<DVViewerRelationUpdate> = emptyList(),
                val fields: DVViewerFields? = null
            ) : DataView() {

                sealed class DVFilterUpdate {
                    data class Add(val afterId: Id, val filters: List<DVFilter>) :
                        DVFilterUpdate()

                    data class Move(val afterId: Id, val ids: List<Id>) : DVFilterUpdate()
                    data class Remove(val ids: List<Id>) : DVFilterUpdate()
                    data class Update(val id: Id, val filter: DVFilter) : DVFilterUpdate()
                }

                sealed class DVSortUpdate {
                    data class Add(val afterId: Id, val sorts: List<DVSort>) : DVSortUpdate()
                    data class Move(val afterId: Id, val ids: List<Id>) : DVSortUpdate()
                    data class Remove(val ids: List<Id>) : DVSortUpdate()
                    data class Update(val id: Id, val sort: DVSort) : DVSortUpdate()
                }

                sealed class DVViewerRelationUpdate {
                    data class Add(val afterId: Id, val relations: List<DVViewerRelation>) :
                        DVViewerRelationUpdate()
                    data class Move(val afterId: Id, val ids: List<Id>) : DVViewerRelationUpdate()
                    data class Remove(val ids: List<Id>) : DVViewerRelationUpdate()
                    data class Update(val id: Id, val relation: DVViewerRelation) :
                        DVViewerRelationUpdate()
                }

                data class DVViewerFields(
                    val name: String,
                    val type: DVViewerType,
                    val coverRelationKey: String,
                    val hideIcon: Boolean,
                    val cardSize: DVViewerCardSize,
                    val coverFit: Boolean
                )
            }
        }
    }
}