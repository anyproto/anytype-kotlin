package com.anytypeio.anytype.core_ui.foundation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineHeading

@Preview
@Composable
fun UpdateAppWarning() {
    val title = "It's time to update"
    val description = "Some of your data was managed in a newer version of Anytype. Please update the app to work with all your docs and the latest features."
    Column {
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .blur(5.dp)
                    .clip(OvalCornerShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Gradients.Green.from,
                                Gradients.Green.to
                            ),
                            radius = 500f
                        )
                    )
                    .fillMaxWidth()
                    .height(104.dp)
            )
            Box(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .size(72.dp)
                    .align(Alignment.Center)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_alert_update),
                    contentDescription = "Alert icon"
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = HeadlineHeading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = BodyRegular,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            color = colorResource(id = R.color.text_primary)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier
                .height(68.dp)
                .padding(horizontal = 20.dp)
        ) {
            ButtonSecondary(
                text = "Cancel",
                onClick = { /*TODO*/ },
                size = ButtonSize.Large,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            ButtonPrimary(
                text = "Update",
                onClick = { /*TODO*/ },
                size = ButtonSize.Large,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

sealed class Config {
    //
}

object OvalCornerShape : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val rect = size.toRect()
        val path = Path().apply {
            addOval(rect)
        }
        return Outline.Generic(path)
    }
}

sealed class Gradients {
    abstract val from: Color
    abstract val to: Color
    object Green: Gradients() {
        override val from: Color = GREEN_FROM
        override val to: Color = GREEN_TO
    }
    companion object {
        val GREEN_FROM = Color(0xFFA9F496)
        val GREEN_TO = Color(0xFF00BCF2AF)
    }
}