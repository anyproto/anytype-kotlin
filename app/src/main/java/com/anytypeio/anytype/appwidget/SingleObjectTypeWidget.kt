package com.anytypeio.anytype.appwidget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
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
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
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
import com.anytypeio.anytype.core_models.Relations
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
                Content(taskViews)
            }
        }
    }

    @Composable
    private fun Content(taskViews: List<TaskWidgetView>) {
        Scaffold(
            backgroundColor = ColorProvider(Color.White),
            titleBar = {
                TitleBar(
                    startIcon = ImageProvider(R.mipmap.ic_launcher),
                    title = "Tasks",
                    iconColor = ColorProvider(Color.Transparent)
                )
            }
        ) {
            AppWidgetContent(taskViews)
        }
    }


    @Composable
    private fun AppWidgetContent(taskViews: List<TaskWidgetView>) {
        LazyColumn(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color.White))
        ) {

            if (taskViews.isEmpty()) {
                item {
                    Text(
                        text = "No open tasks",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = ColorProvider(Color.LightGray)
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
    private fun WidgetHeader() =
        IconTextRow(
            iconRes = R.drawable.ic_gallery_view_task_checked,
            iconSize = 25.dp,
            text = "Tasks",
            textStyle = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = ColorProvider(Color.Black)
            ),
            modifier = GlanceModifier.padding(6.dp)
        )

    @Composable
    private fun TaskViewItem(task: TaskWidgetView) {
        val context = LocalContext.current
        val name = task.name.takeUnless { it.isNullOrEmpty() } ?: "Untitled"
        val textColor = if (task.name.isNullOrEmpty()) {
            ColorProvider(Color.LightGray)
        } else {
            ColorProvider(Color.Black)
        }
        IconTextRow(
            iconRes = R.drawable.ic_gallery_view_task_unchecked,
            text = name,
            textStyle = TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = textColor
            ),
            modifier = GlanceModifier.padding(6.dp),
            onClick = actionStartActivity(
                Intent(
                    WIDGET_ACTION_OPEN_TASK,
                    null,
                    context,
                    MainActivity::class.java
                ).apply {
                    putExtra(WIDGET_ACTION_TASK_ID, task.id)
                    putExtra(Relations.SPACE_ID, task.spaceId)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            ),
            maxLines = 1
        )
    }

    @Composable
    private fun IconTextRow(
        iconRes: Int?,
        iconSize: Dp = 20.dp,
        text: String,
        modifier: GlanceModifier = GlanceModifier,
        textStyle: TextStyle,
        onClick: androidx.glance.action.Action? = null,
        maxLines: Int = Int.MAX_VALUE
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null)
                        GlanceModifier.clickable(onClick)
                    else
                        GlanceModifier
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            iconRes?.let {
                Image(
                    provider = ImageProvider(it),
                    contentDescription = null,
                    modifier = GlanceModifier.size(iconSize)
                )
                Spacer(GlanceModifier.width(12.dp))
            }

            Text(text = text, style = textStyle, maxLines = maxLines)
        }
    }

    companion object {
        const val WIDGET_ACTION_OPEN_TASK = "com.anytype.WIDGET_ACTION_OPEN_TASK"
        const val WIDGET_ACTION_TASK_ID = "com.anytype.WIDGET_ACTION_TASK_ID"
    }
}


