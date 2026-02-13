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
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetCreateObjectEntity
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetsDataStore
import com.anytypeio.anytype.feature_os_widgets.ui.OsCreateObjectWidgetUpdater
import com.anytypeio.anytype.feature_os_widgets.ui.config.SpaceSelectionScreen
import com.anytypeio.anytype.feature_os_widgets.ui.config.WidgetConfigTheme
import com.anytypeio.anytype.ui.objects.creation.ObjectTypeSelectionFragment
import com.anytypeio.anytype.ui.objects.types.pickers.ObjectTypeSelectionListener
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Configuration Activity for the Create Object widget.
 * Two-step flow: Select Space -> Select Type (using ObjectTypeSelectionFragment)
 */
class CreateObjectWidgetConfigActivity : AppCompatActivity(), ObjectTypeSelectionListener {

    @Inject
    lateinit var spaceViews: SpaceViewSubscriptionContainer

    @Inject
    lateinit var urlBuilder: UrlBuilder

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    
    // Store selected space for use when type is selected
    private var selectedSpace: ObjectWrapper.SpaceView? = null

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

        // Inject dependencies
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
                                selectedSpace = space
                                showTypeSelectionDialog(space)
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

    private fun showTypeSelectionDialog(space: ObjectWrapper.SpaceView) {
        val spaceId = space.targetSpaceId.orEmpty()
        val dialog = ObjectTypeSelectionFragment.new(space = spaceId)
        dialog.show(supportFragmentManager, "object-type-selection")
    }

    override fun onSelectObjectType(objType: ObjectWrapper.Type) {
        val space = selectedSpace ?: run {
            Toast.makeText(this, "Error: No space selected", Toast.LENGTH_SHORT).show()
            return
        }
        
        val config = OsWidgetCreateObjectEntity(
            appWidgetId = appWidgetId,
            spaceId = space.targetSpaceId.orEmpty(),
            typeKey = objType.uniqueKey.orEmpty(),
            typeName = objType.name.orEmpty(),
            typeIconEmoji = objType.iconEmoji,
            typeIconName = objType.iconName,
            typeIconOption = objType.iconOption?.toInt(),
            spaceName = space.name.orEmpty()
        )
        completeConfiguration(config)
    }

    private fun completeConfiguration(config: OsWidgetCreateObjectEntity) {
        lifecycleScope.launch {
            try {
                // Save the configuration
                OsWidgetsDataStore(applicationContext).saveCreateObjectConfig(config)

                // Trigger widget update so it picks up the saved config
                OsCreateObjectWidgetUpdater.update(applicationContext, appWidgetId)

                // Return success and finish
                setResult(
                    Activity.RESULT_OK,
                    Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                )
                finish()
            } catch (e: Exception) {
                Timber.e(e, "Error saving widget config")
                Toast.makeText(this@CreateObjectWidgetConfigActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        componentManager().createObjectWidgetConfigComponent.release()
    }
}
