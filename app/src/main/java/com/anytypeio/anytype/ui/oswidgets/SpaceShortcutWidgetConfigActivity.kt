package com.anytypeio.anytype.ui.oswidgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetIconCache
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetSpaceShortcutEntity
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetsDataStore
import com.anytypeio.anytype.feature_os_widgets.ui.OsSpaceShortcutWidgetUpdater
import com.anytypeio.anytype.feature_os_widgets.ui.config.SpaceSelectionScreen
import com.anytypeio.anytype.feature_os_widgets.ui.config.WidgetConfigTheme
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Configuration Activity for the Space Shortcut widget.
 * Single-step flow: Select Space -> Widget created
 */
class SpaceShortcutWidgetConfigActivity : AppCompatActivity() {

    @Inject
    lateinit var spaceViews: SpaceViewSubscriptionContainer

    @Inject
    lateinit var urlBuilder: UrlBuilder

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the result to CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED)

        // Get the appWidgetId from the intent
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // Inject dependencies - reuse the same component as CreateObjectWidgetConfigActivity
        componentManager().createObjectWidgetConfigComponent.get().inject(this)

        // Set up content with space selection screen
        setContentView(
            ComposeView(this).apply {
                setContent {
                    WidgetConfigTheme {
                        SpaceSelectionScreen(
                            spaceViews = spaceViews,
                            urlBuilder = urlBuilder,
                            onSpaceSelected = { space ->
                                completeConfiguration(space)
                            },
                            onCancel = {
                                finish()
                            }
                        )
                    }
                }
            }
        )
    }

    private fun completeConfiguration(space: ObjectWrapper.SpaceView) {
        lifecycleScope.launch {
            try {
                // Cache the icon image if available
                val iconCache = OsWidgetIconCache(applicationContext)
                val cachedIconPath = space.iconImage?.takeIf { it.isNotEmpty() }?.let { iconHash ->
                    val iconUrl = urlBuilder.thumbnail(iconHash)
                    iconCache.cacheShortcutIcon(
                        url = iconUrl,
                        widgetId = appWidgetId,
                        prefix = OsWidgetIconCache.PREFIX_SPACE
                    )
                }

                val config = OsWidgetSpaceShortcutEntity(
                    appWidgetId = appWidgetId,
                    spaceId = space.targetSpaceId.orEmpty(),
                    spaceName = space.name.orEmpty(),
                    spaceIconImage = space.iconImage,
                    spaceIconOption = space.iconOption?.toInt(),
                    cachedIconPath = cachedIconPath
                )

                // Save the configuration
                OsWidgetsDataStore(applicationContext).saveSpaceShortcutConfig(config)

                // Trigger widget update so it picks up the saved config
                OsSpaceShortcutWidgetUpdater.update(applicationContext, appWidgetId)

                // Return success and finish
                setResult(
                    Activity.RESULT_OK,
                    Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                )
                finish()
            } catch (e: Exception) {
                Timber.e(e, "Error saving widget config")
                Toast.makeText(this@SpaceShortcutWidgetConfigActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        componentManager().createObjectWidgetConfigComponent.release()
    }
}
