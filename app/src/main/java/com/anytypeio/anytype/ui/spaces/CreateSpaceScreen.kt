package com.anytypeio.anytype.ui.spaces

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.views.Title2

@Preview
@Composable
fun CreateSpaceScreen(
//    onCreate: (Name) -> Unit
) {
    Column {
        Dragger(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.CenterHorizontally)
        )
        Header()
        Spacer(modifier = Modifier.height(16.dp))
        SpaceIcon()
        Spacer(modifier = Modifier.height(10.dp))
        SpaceNameInput()
        Section(title = "Type")
        TypeOfSpace()
        Section(title = "Start with")
        UseCase()
    }
}

@Composable
fun Header() {
    Box(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = stringResource(id = R.string.create_space),
            style = Title2,
            color = colorResource(id = R.color.text_primary)
        )
    }
}

@Composable
fun SpaceIcon() {
    Box(
        modifier = Modifier
            .size(96.dp)
            .background(Color.Red)
            .clip(CircleShape)
    ) {
        // TODO
    }
}

@Composable
fun SpaceNameInput() {
    Box(
        modifier = Modifier
            .height(72.dp)
            .fillMaxWidth()
    ) {

        var input by remember { mutableStateOf("") }
        TextField(
            value = input,
            onValueChange = { input = it },
            keyboardActions = KeyboardActions(
                onDone = {
//                    vm.onCreateSpace(name = input)
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart),
            maxLines = 1,
            singleLine = true,
            placeholder = {
                Text(
                    text = "Enter space name",
                    style = HeadlineHeading,
                    color = colorResource(id = R.color.text_tertiary)
                )
            },
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                textColor = colorResource(id = R.color.text_primary)
            ),
            textStyle = HeadlineHeading
        )
        Text(
            color = colorResource(id = R.color.text_secondary),
            style = Caption1Regular,
            modifier = Modifier.padding(
                start = 20.dp,
                top = 11.dp
            ),
            text = stringResource(id = R.string.space_name)
        )
    }
}

@Composable
fun Section(
    title: String
) {
    Box(modifier = Modifier.height(52.dp)) {
        Text(
            modifier = Modifier
                .padding(
                    start = 20.dp,
                    bottom = 8.dp
                )
                .align(Alignment.BottomStart),
            text = title,
            color = colorResource(id = R.color.text_secondary)
        )
    }
}

@Composable
fun TypeOfSpace() {
    Box(modifier = Modifier.height(0.dp))
}

@Composable
fun UseCase() {
    Box(modifier = Modifier.height(0.dp))
}