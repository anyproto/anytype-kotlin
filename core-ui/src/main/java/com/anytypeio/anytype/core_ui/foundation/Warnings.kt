package com.anytypeio.anytype.core_ui.foundation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun UpdateAppWarning() {
    Column {
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier.padding(horizontal = 28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .padding(vertical = 16.dp)
            ) {
                // TODO add image
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // TODO add title widget
        Spacer(modifier = Modifier.height(8.dp))
        // TODO add text widget
        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.height(68.dp)) {
            // Add buttons
        }
    }
}

sealed class Config {
    //
}

