package com.anytypeio.anytype.ui.library.views.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.presentation.library.LibraryEvent
import com.anytypeio.anytype.ui.library.LibraryListConfig
import com.anytypeio.anytype.ui.library.styles.SearchQueryTextStyle
import com.anytypeio.anytype.ui.library.views.LibraryTextField

@Composable
fun LibraryListSearchWidget(
    vmEventStream: (LibraryEvent) -> Unit,
    config: LibraryListConfig
) {
    val input = remember { mutableStateOf(String()) }
    LibraryTextField(
        value = input.value,
        onValueChange = {
            input.value = it
            vmEventStream.invoke(
                config.toEvent(input.value)
            )
        },
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .padding(
                start = 20.dp,
                end = 20.dp
            ),
        textStyle = SearchQueryTextStyle,
        placeholder = {
            Text(
                text = "Search"
            )
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = colorResource(id = R.color.text_primary),
            backgroundColor = colorResource(id = R.color.shape_transparent),
            disabledBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            placeholderColor = colorResource(id = R.color.glyph_active),
            cursorColor = colorResource(id = R.color.text_primary)
        ),
        singleLine = true,
        maxLines = 1,
        leadingIcon = {
            Image(
                painterResource(id = R.drawable.ic_search),
                "",
                modifier = Modifier.offset(x = 8.dp)
            )
        }
    )
}

private fun LibraryListConfig.toEvent(query: String): LibraryEvent.Query = when (this) {
    LibraryListConfig.Relations -> LibraryEvent.Query.MyRelations(query)
    LibraryListConfig.RelationsLibrary -> LibraryEvent.Query.LibraryRelations(query)
    LibraryListConfig.Types -> LibraryEvent.Query.MyTypes(query)
    LibraryListConfig.TypesLibrary -> LibraryEvent.Query.LibraryTypes(query)
}