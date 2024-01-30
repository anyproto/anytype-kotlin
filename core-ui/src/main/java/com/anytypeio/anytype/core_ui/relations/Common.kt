package com.anytypeio.anytype.core_ui.relations

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.presentation.relations.model.RelationsListItem

@Composable
fun CommonContainer(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 20.dp),
        content = content
    )
}

@Composable
fun CircleIcon(number: String? = null, isSelected: Boolean, modifier: Modifier) {
    if (isSelected && number != null) {
        Box(
            modifier = modifier
                .background(
                    color = colorResource(id = R.color.palette_system_sky),
                    shape = CircleShape
                )
        ) {
            Text(
                text = number,
                style = PreviewTitle2Medium,
                color = colorResource(id = R.color.text_white),
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )
        }
    } else {
        Box(
            modifier = modifier
                .border(
                    shape = CircleShape,
                    width = 1.5.dp,
                    color = colorResource(R.color.glyph_inactive)
                )
        )
    }
}

@Composable
fun CheckedIcon(isSelected: Boolean, modifier: Modifier) {
    if (isSelected) {
        Image(
            modifier = modifier,
            painter = painterResource(id = R.drawable.ic_checkbox_selected),
            contentDescription = "Selected"
        )
    } else {
        Box(modifier)
    }
}

@Composable
fun ItemTagOrStatusCreate(state: RelationsListItem.CreateItem) {
    CommonContainer(modifier = Modifier.padding(top = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 56.dp)
                .align(alignment = Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_default_plus),
                contentDescription = "Create new tag"
            )
            val text = stringResource(id = R.string.relation_value_create_new, state.text)
            Text(
                text = text,
                color = colorResource(id = R.color.text_primary),
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(start = 10.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                //style = Relations1
            )
        }
    }
}