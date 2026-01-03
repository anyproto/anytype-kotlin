package com.anytypeio.anytype.appwidget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.anytypeio.anytype.R
import com.anytypeio.anytype.app.AndroidApplication
import com.anytypeio.anytype.ui.main.MainActivity

class SingleObjectTypeWidgetReceiver: GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SingleObjectTypeWidget()
}

class SingleObjectTypeWidget: GlanceAppWidget() {

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val app = context.applicationContext as AndroidApplication
        val widgetDataProvider = app.componentManager.appWidgetComponent.get().widgetDataProvider()

        val taskViews = try {
            widgetDataProvider.getTasks()
        } catch (e: Exception) {
            emptyList()
        }

        provideContent {
            GlanceTheme {
                AppWidgetContent(taskViews)
            }
        }
    }


    @Composable
    private fun AppWidgetContent(taskViews: List<TaskWidgetView>) {
        LazyColumn(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color.White))
                .padding(12.dp)
        ) {
            item {
                Text(
                    text = "Tasks",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GlanceTheme.colors.onSurface
                    ),
                    modifier = GlanceModifier.padding(bottom = 8.dp)
                )
            }

            if (taskViews.isEmpty()) {
                item {
                    Text(
                        text = "No open tasks",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                }
            } else {
                items(taskViews) { item ->
                    TaskViewItem(item)
                }
            }
        }
    }

    @Composable
    private fun TaskViewItem(taskViewItem: TaskWidgetView) {
        val context = LocalContext.current
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clickable(
                    actionStartActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("anytype://object/${taskViewItem.id}"),
                            context,
                            MainActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_checkbox_unchecked),
                contentDescription = null,
                modifier = GlanceModifier.size(20.dp)
            )
            Spacer(modifier = GlanceModifier.width(12.dp))
            Text(
                text = taskViewItem.name,
                style = TextStyle(
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = GlanceTheme.colors.onSurface
                ),
                maxLines = 1
            )
        }
    }
}


