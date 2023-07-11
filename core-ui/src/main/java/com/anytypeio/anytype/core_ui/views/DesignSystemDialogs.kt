package com.anytypeio.anytype.core_ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@ExperimentalMaterial3Api
@Composable
fun BaseAlertDialog(
    dialogText: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    onDismissRequest: () -> Unit
): Unit {
    val modifier = Modifier
        .shadow(
            elevation = 40.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000)
        )
        .wrapContentWidth()
        .wrapContentHeight()
        .background(
            color = Color(0xFF1F1E1D), shape = RoundedCornerShape(size = 8.dp)
        )
        .padding(start = 32.dp, top = 32.dp, end = 32.dp, bottom = 32.dp)

    AlertDialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation,
            color = Color.Transparent
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Child views.
                Text(
                    text = dialogText,
                    style = UXBody,
                    textAlign = TextAlign.Center,
                    color = Color(0xFFFFFFFF),
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                Spacer(modifier = Modifier.height(18.dp))
                ButtonPrimaryDarkTheme(
                    text = buttonText,
                    onClick = onButtonClick,
                    size = ButtonSize.Large,
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}