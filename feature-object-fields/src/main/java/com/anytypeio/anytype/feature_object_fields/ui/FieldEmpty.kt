package com.anytypeio.anytype.feature_object_fields.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.Relations1
import com.anytypeio.anytype.feature_object_fields.R

data class Item(
    val title: String,
    val format: RelationFormat
)

@Composable
fun FieldEmpty(item: Item) {
    val defaultModifier = Modifier
        .fillMaxWidth()
        .border(
            width = 1.dp,
            color = colorResource(id = R.color.shape_secondary),
            shape = RoundedCornerShape(12.dp)
        )
    when (item.format) {
        Relation.Format.LONG_TEXT,
        Relation.Format.SHORT_TEXT,
        Relation.Format.URL -> {
            val emptyState = getEnterValueText(item.format)
            FieldVerticalEmpty(
                modifier = defaultModifier,
                title = item.title,
                emptyState = emptyState
            )
        }

        else -> {
            val emptyState = getEnterValueText(item.format)
            FieldHorizontalEmpty(
                modifier = defaultModifier,
                title = item.title,
                emptyState = emptyState
            )
        }
    }
}

@Composable
private fun FieldVerticalEmpty(
    modifier: Modifier = Modifier,
    title: String,
    emptyState: String,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = title,
            style = Relations1,
            color = colorResource(id = R.color.text_secondary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = emptyState,
            style = Relations1,
            color = colorResource(id = R.color.text_tertiary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FieldHorizontalEmpty(
    modifier: Modifier = Modifier,
    title: String,
    emptyState: String,
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val halfScreenWidth = screenWidth / 2 - 32.dp

    Row(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            modifier = Modifier.widthIn(max = halfScreenWidth),
            text = title,
            style = Relations1,
            color = colorResource(id = R.color.text_secondary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = emptyState,
            style = Relations1,
            color = colorResource(id = R.color.text_tertiary)
        )
    }
}

@Composable
private fun getEnterValueText(format: RelationFormat): String {
    return when (format) {
        Relation.Format.LONG_TEXT,
        Relation.Format.SHORT_TEXT -> stringResource(R.string.field_text_empty)

        Relation.Format.NUMBER -> stringResource(R.string.field_number_empty)
        Relation.Format.DATE -> stringResource(R.string.field_date_empty)
        Relation.Format.CHECKBOX -> ""
        Relation.Format.URL -> stringResource(R.string.field_url_empty)
        Relation.Format.EMAIL -> stringResource(R.string.field_email_empty)
        Relation.Format.PHONE -> stringResource(R.string.field_phone_empty)
        Relation.Format.OBJECT -> stringResource(R.string.field_object_empty)
        else -> ""
    }
}

@DefaultPreviews
@Composable
fun PreviewField() {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
        item {
            FieldEmpty(
                item = Item(
                    title = "Description",
                    format = Relation.Format.LONG_TEXT
                )
            )
        }
        item {
            FieldEmpty(
                item = Item(
                    title = "Some Number, very long long long long long fields name",
                    format = Relation.Format.NUMBER
                )
            )
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}