package com.anytypeio.anytype.ui.oswidgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_os_widgets.presentation.SpaceShortcutWidgetConfigViewModel
import com.anytypeio.anytype.feature_os_widgets.ui.config.SpaceSelectionScreen
import com.anytypeio.anytype.feature_os_widgets.ui.config.WidgetConfigTheme
import javax.inject.Inject

/**
 * Configuration Activity for the Space Shortcut widget.
 * Single-step flow: Select Space -> Widget created
 */
class SpaceShortcutWidgetConfigActivity : AppCompatActivity() {

    @Inject
    lateinit var factory: SpaceShortcutWidgetConfigViewModel.Factory

    @Inject
    lateinit var urlBuilder: UrlBuilder

    private val vm by viewModels<SpaceShortcutWidgetConfigViewModel> { factory }

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

        // Inject dependencies
        componentManager().createObjectWidgetConfigComponent.get().inject(this)
        factory.appWidgetId = appWidgetId

        // Set up content with space selection screen
        setContentView(
            ComposeView(this).apply {
                setContent {
                    val spaces by vm.spaces.collectAsState()

                    WidgetConfigTheme {
                        SpaceSelectionScreen(
                            spaces = spaces,
                            urlBuilder = urlBuilder,
                            onSpaceSelected = { space ->
                                vm.onSpaceSelected(space)
                            },
                            onCancel = {
                                finish()
                            }
                        )
                    }

                    LaunchedEffect(Unit) {
                        vm.commands.collect { command ->
                            when (command) {
                                is SpaceShortcutWidgetConfigViewModel.Command.FinishWithSuccess -> {
                                    setResult(
                                        Activity.RESULT_OK,
                                        Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, command.appWidgetId)
                                    )
                                    finish()
                                }
                                is SpaceShortcutWidgetConfigViewModel.Command.ShowError -> {
                                    Toast.makeText(this@SpaceShortcutWidgetConfigActivity, "Error: ${command.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        componentManager().createObjectWidgetConfigComponent.release()
    }
}
