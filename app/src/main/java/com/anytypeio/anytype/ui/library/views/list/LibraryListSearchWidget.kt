package com.anytypeio.anytype.ui.library.views.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.presentation.library.LibraryEvent
import com.anytypeio.anytype.ui.library.LibraryListConfig

@Composable
fun LibraryListSearchWidget(
    modifier: Modifier = Modifier,
    vmEventStream: (LibraryEvent) -> Unit,
    config: LibraryListConfig
) {
    val input = remember { mutableStateOf(String()) }
    OutlinedTextField(
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
            .padding(
                start = 20.dp, end = 20.dp
            ),
        textStyle = TextStyle(
            fontSize = 17.sp
        ),
        placeholder = { Text(text = "Search") },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = colorResource(id = R.color.black),
            backgroundColor = colorResource(id = R.color.light_grayish),
            disabledBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        ),
        singleLine = true,
        maxLines = 1,
        leadingIcon = {
            Image(painterResource(id = R.drawable.ic_search), "")
        },
    )
}

private fun LibraryListConfig.toEvent(query: String): LibraryEvent.Query = when (this) {
    LibraryListConfig.Relations -> LibraryEvent.Query.MyRelations(query)
    LibraryListConfig.RelationsLibrary -> LibraryEvent.Query.LibraryRelations(query)
    LibraryListConfig.Types -> LibraryEvent.Query.MyTypes(query)
    LibraryListConfig.TypesLibrary -> LibraryEvent.Query.LibraryTypes(query)
}