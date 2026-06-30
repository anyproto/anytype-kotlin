package com.anytypeio.anytype.core_ui.widgets.dv.board

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.light
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.relations.model.DefaultObjectRelationValueView
import com.anytypeio.anytype.presentation.sets.model.Viewer

/**
 * A single board card: object icon + name, with the object's visible relation
 * values rendered below as text / colored chips.
 */
@Composable
fun BoardCardItem(
    card: Viewer.Board.Card,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colorResource(id = R.color.background_primary))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!card.hideIcon && card.icon !is ObjectIcon.None) {
                ListWidgetObjectIcon(
                    icon = card.icon,
                    modifier = Modifier.size(18.dp),
                    iconSize = 18.dp,
                    iconWithoutBackgroundMaxSize = 18.dp
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = card.name,
                style = BodyRegular,
                color = colorResource(id = R.color.text_primary),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        val relations = card.relations.filterNot { it is DefaultObjectRelationValueView.Empty }
        relations.forEach { relation ->
            CardRelationValue(relation)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CardRelationValue(relation: DefaultObjectRelationValueView) {
    when (relation) {
        is DefaultObjectRelationValueView.Tag -> {
            if (relation.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    relation.tags.forEach { RelationChip(text = it.tag, colorCode = it.color) }
                }
            }
        }
        is DefaultObjectRelationValueView.Status -> {
            if (relation.status.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    relation.status.forEach { RelationChip(text = it.status, colorCode = it.color) }
                }
            }
        }
        is DefaultObjectRelationValueView.Object ->
            SimpleRelationText(relation.objects.joinToString { it.name })
        is DefaultObjectRelationValueView.File ->
            SimpleRelationText(relation.files.joinToString { it.name })
        is DefaultObjectRelationValueView.Text -> SimpleRelationText(relation.text)
        is DefaultObjectRelationValueView.Number -> SimpleRelationText(relation.number)
        is DefaultObjectRelationValueView.Url -> SimpleRelationText(relation.url)
        is DefaultObjectRelationValueView.Email -> SimpleRelationText(relation.email)
        is DefaultObjectRelationValueView.Phone -> SimpleRelationText(relation.phone)
        is DefaultObjectRelationValueView.Checkbox ->
            SimpleRelationText(if (relation.isChecked) "✓" else null)
        // Date relations need formatting that is out of scope for the read-only MVP.
        is DefaultObjectRelationValueView.Date -> Unit
        is DefaultObjectRelationValueView.Empty -> Unit
    }
}

@Composable
private fun SimpleRelationText(text: String?) {
    if (text.isNullOrBlank()) return
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = text,
        style = Relations2,
        color = colorResource(id = R.color.text_secondary),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun RelationChip(text: String, colorCode: String) {
    Text(
        text = text,
        style = Relations2,
        color = dark(colorCode),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(light(colorCode))
            .padding(horizontal = 6.dp, vertical = 1.dp)
    )
}
