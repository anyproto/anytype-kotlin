package com.anytypeio.anytype.core_ui.features.fields

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.Relations1

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FieldEmpty(
    modifier: Modifier = Modifier,
    title: String,
    fieldFormat: RelationFormat,
    isLocal: Boolean,
    onFieldClick: () -> Unit,
    onAddToCurrentTypeClick: () -> Unit,
    onRemoveFromObjectClick: () -> Unit,
) {
    val defaultModifier = modifier
        .fillMaxWidth()
        .border(
            width = 1.dp,
            color = colorResource(id = R.color.shape_secondary),
            shape = RoundedCornerShape(12.dp)
        )
    when (fieldFormat) {
        Relation.Format.LONG_TEXT,
        Relation.Format.SHORT_TEXT,
        Relation.Format.URL -> {
            val emptyState = getEnterValueText(fieldFormat)
            FieldVerticalEmpty(
                modifier = defaultModifier,
                title = title,
                emptyState = emptyState,
                isLocal = isLocal,
                onFieldClick = onFieldClick,
                onAddToCurrentTypeClick = onAddToCurrentTypeClick,
                onRemoveFromObjectClick = onRemoveFromObjectClick
            )
        }

        else -> {
            val emptyState = getEnterValueText(fieldFormat)
            FieldHorizontalEmpty(
                modifier = defaultModifier,
                title = title,
                emptyState = emptyState,
                isLocal = isLocal,
                onFieldClick = onFieldClick,
                onAddToCurrentTypeClick = onAddToCurrentTypeClick,
                onRemoveFromObjectClick = onRemoveFromObjectClick
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FieldVerticalEmpty(
    modifier: Modifier = Modifier,
    title: String,
    emptyState: String,
    isLocal: Boolean,
    onFieldClick: () -> Unit,
    onAddToCurrentTypeClick: () -> Unit,
    onRemoveFromObjectClick: () -> Unit,
) {
    val isMenuExpanded = remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .combinedClickable(
                onClick = { onFieldClick()},
                onLongClick = {
                    if (isLocal) isMenuExpanded.value = true
                }
            )
            .padding(horizontal = 16.dp, vertical = 16.dp)
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
        ItemDropDownMenu(
            showMenu = isMenuExpanded.value,
            onDismissRequest = {
                isMenuExpanded.value = false
            },
            onAddToCurrentTypeClick = {
                isMenuExpanded.value = false
                onAddToCurrentTypeClick()
            },
            onRemoveFromObjectClick = {
                isMenuExpanded.value = false
                onRemoveFromObjectClick()
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FieldHorizontalEmpty(
    modifier: Modifier = Modifier,
    title: String,
    emptyState: String,
    isLocal: Boolean,
    onFieldClick: () -> Unit,
    onAddToCurrentTypeClick: () -> Unit,
    onRemoveFromObjectClick: () -> Unit,
) {
    val isMenuExpanded = remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val halfScreenWidth = screenWidth / 2 - 32.dp

    Row(
        modifier = modifier
            .combinedClickable(
                onClick = onFieldClick,
                onLongClick = {
                    if (isLocal) isMenuExpanded.value = true
                }
            )
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
        ItemDropDownMenu(
            showMenu = isMenuExpanded.value,
            onDismissRequest = {
                isMenuExpanded.value = false
            },
            onAddToCurrentTypeClick = {
                isMenuExpanded.value = false
                onAddToCurrentTypeClick()
            },
            onRemoveFromObjectClick = {
                isMenuExpanded.value = false
                onRemoveFromObjectClick()
            }
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
                title = "Description",
                fieldFormat = Relation.Format.LONG_TEXT,
                isLocal = true,
                onFieldClick = {},
                onAddToCurrentTypeClick = {},
                onRemoveFromObjectClick = {}
            )
        }
        item {
            FieldEmpty(
                title = "Some Number, very long long long long long fields name",
                fieldFormat = Relation.Format.NUMBER,
                isLocal = true,
                onFieldClick = {},
                onAddToCurrentTypeClick = {},
                onRemoveFromObjectClick = {}
            )
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}