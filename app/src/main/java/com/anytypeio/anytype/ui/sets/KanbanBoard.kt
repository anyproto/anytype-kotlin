package com.anytypeio.anytype.ui.sets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.RelativeDate
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.light
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.relations.model.DefaultObjectRelationValueView
import com.anytypeio.anytype.presentation.sets.model.Viewer

private val COLUMN_WIDTH = 240.dp
private val CARD_CORNER_RADIUS = 8.dp
private val COLUMN_CORNER_RADIUS = 12.dp
private val ICON_SIZE = 18.dp
private val ICON_BACKGROUND_SIZE = 24.dp
private val CHIP_CORNER_RADIUS = 4.dp

@Composable
fun KanbanBoard(
    viewer: Viewer.KanbanView,
    onCardClicked: (Id) -> Unit,
    onRelationClicked: (objectId: Id, relationKey: Key) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxHeight()
    ) {
        items(
            items = viewer.columns,
            key = { column -> "kanban-col-${column.groupId.ifEmpty { "__ungrouped__" }}" }
        ) { column ->
            KanbanColumn(
                column = column,
                groupRelationKey = viewer.groupRelationKey,
                onCardClicked = onCardClicked,
                onRelationClicked = onRelationClicked
            )
        }
    }
}

@Composable
private fun KanbanColumn(
    column: Viewer.KanbanView.Column,
    groupRelationKey: Key,
    onCardClicked: (Id) -> Unit,
    onRelationClicked: (objectId: Id, relationKey: Key) -> Unit
) {
    Column(
        modifier = Modifier
            .width(COLUMN_WIDTH)
            .fillMaxHeight()
            .clip(RoundedCornerShape(COLUMN_CORNER_RADIUS))
            .background(colorResource(R.color.shape_tertiary))
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        KanbanColumnHeader(column = column)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxHeight()
        ) {
            items(
                items = column.cards,
                key = { card -> card.objectId }
            ) { card ->
                KanbanCard(
                    card = card,
                    groupRelationKey = groupRelationKey,
                    onCardClicked = onCardClicked,
                    onRelationClicked = onRelationClicked
                )
            }
        }
    }
}

@Composable
private fun KanbanColumnHeader(column: Viewer.KanbanView.Column) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        column.color?.let { colorCode ->
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(dark(code = colorCode))
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = column.name,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = colorResource(R.color.text_secondary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "${column.cards.size}",
            fontSize = 12.sp,
            color = colorResource(R.color.text_secondary)
        )
    }
}

@Composable
private fun KanbanCard(
    card: Viewer.KanbanView.Card,
    groupRelationKey: Key,
    onCardClicked: (Id) -> Unit,
    onRelationClicked: (objectId: Id, relationKey: Key) -> Unit
) {
    // Ensure the group-key chip always renders. If the card has no value for the group
    // relation (so the user can't see "Set state" anywhere), we synthesize an Empty entry
    // so a placeholder "+ <RelationName>" chip is drawn and tap-to-set works.
    val hasGroupRelationEntry = card.relations.any { it.relationKey == groupRelationKey }
    val effectiveRelations = if (hasGroupRelationEntry) {
        card.relations
    } else {
        card.relations + DefaultObjectRelationValueView.Empty(
            objectId = card.objectId,
            relationKey = groupRelationKey
        )
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CARD_CORNER_RADIUS))
            .background(colorResource(R.color.background_primary))
            .border(
                width = 1.dp,
                color = colorResource(R.color.shape_primary),
                shape = RoundedCornerShape(CARD_CORNER_RADIUS)
            )
            .clickable { onCardClicked(card.objectId) }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        if (!card.hideName) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!card.hideIcon) {
                    ListWidgetObjectIcon(
                        icon = card.icon,
                        modifier = Modifier.size(ICON_BACKGROUND_SIZE),
                        iconSize = ICON_BACKGROUND_SIZE,
                        iconWithoutBackgroundMaxSize = ICON_SIZE
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = card.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(R.color.text_primary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (effectiveRelations.isNotEmpty()) {
            if (!card.hideName) {
                Spacer(modifier = Modifier.height(6.dp))
            }
            KanbanCardRelations(
                objectId = card.objectId,
                groupRelationKey = groupRelationKey,
                relations = effectiveRelations,
                onRelationClicked = onRelationClicked
            )
        }
    }
}

@Composable
private fun KanbanCardRelations(
    objectId: Id,
    groupRelationKey: Key,
    relations: List<DefaultObjectRelationValueView>,
    onRelationClicked: (objectId: Id, relationKey: Key) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        relations.forEach { relation ->
            KanbanRelationRow(
                objectId = objectId,
                isGroupRelation = relation.relationKey == groupRelationKey,
                relation = relation,
                onRelationClicked = onRelationClicked
            )
        }
    }
}

@Composable
private fun KanbanRelationRow(
    objectId: Id,
    isGroupRelation: Boolean,
    relation: DefaultObjectRelationValueView,
    onRelationClicked: (objectId: Id, relationKey: Key) -> Unit
) {
    when (relation) {
        is DefaultObjectRelationValueView.Status -> {
            // Skip self-referential entries: middleware sometimes resolves a relation's
            // value back to the card object itself (mis-configured STATUS relation with
            // no real options). Such entries always have id == card's objectId.
            val items = relation.status.filter { it.id != objectId && it.status.isNotEmpty() }
            if (items.isEmpty()) {
                // Group-relation chip must still render so the user has a tap target to
                // assign a value; for non-group relations, suppress when empty.
                if (isGroupRelation) {
                    EmptyStatusChip(
                        objectId = objectId,
                        relationKey = relation.relationKey,
                        onRelationClicked = onRelationClicked
                    )
                }
                return
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clickable {
                    onRelationClicked(objectId, relation.relationKey)
                }
            ) {
                items.forEach { status ->
                    StatusOrTagChip(label = status.status, colorCode = status.color)
                }
            }
        }
        is DefaultObjectRelationValueView.Tag -> {
            val items = relation.tags.filter { it.id != objectId && it.tag.isNotEmpty() }
            if (items.isEmpty()) {
                if (isGroupRelation) {
                    EmptyStatusChip(
                        objectId = objectId,
                        relationKey = relation.relationKey,
                        onRelationClicked = onRelationClicked
                    )
                }
                return
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clickable {
                    onRelationClicked(objectId, relation.relationKey)
                }
            ) {
                items.forEach { tag ->
                    StatusOrTagChip(label = tag.tag, colorCode = tag.color)
                }
            }
        }
        is DefaultObjectRelationValueView.Empty -> {
            if (isGroupRelation) {
                EmptyStatusChip(
                    objectId = objectId,
                    relationKey = relation.relationKey,
                    onRelationClicked = onRelationClicked
                )
            }
        }
        else -> {
            val label = relationLabelText(relation)
            if (label.isNotEmpty()) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = colorResource(R.color.text_secondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EmptyStatusChip(
    objectId: Id,
    relationKey: Key,
    onRelationClicked: (objectId: Id, relationKey: Key) -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(CHIP_CORNER_RADIUS))
            .border(
                width = 1.dp,
                color = colorResource(R.color.shape_secondary),
                shape = RoundedCornerShape(CHIP_CORNER_RADIUS)
            )
            .clickable { onRelationClicked(objectId, relationKey) }
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = stringResource(R.string.select_status),
            fontSize = 12.sp,
            color = colorResource(R.color.text_tertiary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatusOrTagChip(label: String, colorCode: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(CHIP_CORNER_RADIUS))
            .background(light(code = colorCode))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = dark(code = colorCode),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun relationLabelText(relation: DefaultObjectRelationValueView): String = when (relation) {
    is DefaultObjectRelationValueView.Text -> relation.text.orEmpty()
    is DefaultObjectRelationValueView.Number -> relation.number.orEmpty()
    is DefaultObjectRelationValueView.Date -> when (val d = relation.relativeDate) {
        is RelativeDate.Today -> stringResource(R.string.today)
        is RelativeDate.Tomorrow -> stringResource(R.string.tomorrow)
        is RelativeDate.Yesterday -> stringResource(R.string.yesterday)
        is RelativeDate.Other -> d.formattedDate
        else -> ""
    }
    is DefaultObjectRelationValueView.Checkbox -> if (relation.isChecked) "✓" else ""
    is DefaultObjectRelationValueView.Object -> relation.objects.firstOrNull()?.name.orEmpty()
    is DefaultObjectRelationValueView.File -> relation.files.firstOrNull()?.name.orEmpty()
    is DefaultObjectRelationValueView.Url -> relation.url.orEmpty()
    is DefaultObjectRelationValueView.Email -> relation.email.orEmpty()
    is DefaultObjectRelationValueView.Phone -> relation.phone.orEmpty()
    is DefaultObjectRelationValueView.Status -> "" // handled above
    is DefaultObjectRelationValueView.Tag -> "" // handled above
    is DefaultObjectRelationValueView.Empty -> ""
}
