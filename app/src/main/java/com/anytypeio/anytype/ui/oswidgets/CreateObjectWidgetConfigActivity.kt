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
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_os_widgets.presentation.CreateObjectWidgetConfigViewModel
import com.anytypeio.anytype.feature_os_widgets.ui.config.SpaceSelectionScreen
import com.anytypeio.anytype.feature_os_widgets.ui.config.WidgetConfigTheme
import com.anytypeio.anytype.ui.objects.creation.ObjectTypeSelectionFragment
import com.anytypeio.anytype.ui.objects.types.pickers.ObjectTypeSelectionListener
import javax.inject.Inject

/**
 * Configuration Activity for the Create Object widget.
 * Two-step flow: Select Space -> Select Type (using ObjectTypeSelectionFragment)
 */
class CreateObjectWidgetConfigActivity : AppCompatActivity(), ObjectTypeSelectionListener {

    @Inject
    lateinit var factory: CreateObjectWidgetConfigViewModel.Factory

    @Inject
    lateinit var urlBuilder: UrlBuilder

    private val vm by viewModels<CreateObjectWidgetConfigViewModel> { factory }

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
                                is CreateObjectWidgetConfigViewModel.Command.ShowTypeSelection -> {
                                    showTypeSelectionDialog(command.spaceId)
                                }
                                is CreateObjectWidgetConfigViewModel.Command.FinishWithSuccess -> {
                                    setResult(
                                        Activity.RESULT_OK,
                                        Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, command.appWidgetId)
                                    )
                                    finish()
                                }
                                is CreateObjectWidgetConfigViewModel.Command.ShowError -> {
                                    Toast.makeText(this@CreateObjectWidgetConfigActivity, "Error: ${command.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                }
            }
        )
    }

    private fun showTypeSelectionDialog(spaceId: String) {
        val dialog = ObjectTypeSelectionFragment.new(space = spaceId)
        dialog.show(supportFragmentManager, "object-type-selection")
    }

    override fun onSelectObjectType(objType: ObjectWrapper.Type) {
        vm.onTypeSelected(objType)
    }

    override fun onDestroy() {
        super.onDestroy()
        componentManager().createObjectWidgetConfigComponent.release()
    }
}
