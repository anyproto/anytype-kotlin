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
import com.anytypeio.anytype.feature_os_widgets.presentation.ObjectShortcutWidgetConfigViewModel
import com.anytypeio.anytype.feature_os_widgets.ui.config.ObjectSelectionScreen
import com.anytypeio.anytype.feature_os_widgets.ui.config.SpaceSelectionScreen
import com.anytypeio.anytype.feature_os_widgets.ui.config.WidgetConfigTheme
import javax.inject.Inject

/**
 * Configuration Activity for the Object Shortcut widget.
 * Two-step flow: Select Space -> Select Object (with search)
 */
class ObjectShortcutWidgetConfigActivity : AppCompatActivity() {

    @Inject
    lateinit var factory: ObjectShortcutWidgetConfigViewModel.Factory

    @Inject
    lateinit var urlBuilder: UrlBuilder

    private val vm by viewModels<ObjectShortcutWidgetConfigViewModel> { factory }

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        componentManager().objectShortcutWidgetConfigComponent.get().inject(this)
        factory.appWidgetId = appWidgetId

        setContentView(
            ComposeView(this).apply {
                setContent {
                    val screenState by vm.screenState.collectAsState()
                    val spaces by vm.spaces.collectAsState()
                    val objectItems by vm.objectItems.collectAsState()
                    val isLoading by vm.isLoading.collectAsState()
                    val searchQuery by vm.searchQuery.collectAsState()
                    val selectedSpace by vm.selectedSpace.collectAsState()

                    WidgetConfigTheme {
                        when (screenState) {
                            ObjectShortcutWidgetConfigViewModel.ScreenState.SpaceSelection -> {
                                SpaceSelectionScreen(
                                    spaces = spaces,
                                    urlBuilder = urlBuilder,
                                    onSpaceSelected = { space ->
                                        vm.onSpaceSelected(space)
                                    },
                                    onCancel = { finish() }
                                )
                            }
                            ObjectShortcutWidgetConfigViewModel.ScreenState.ObjectSelection -> {
                                ObjectSelectionScreen(
                                    spaceName = selectedSpace?.name.orEmpty(),
                                    objectItems = objectItems,
                                    isLoading = isLoading,
                                    searchQuery = searchQuery,
                                    onSearchQueryChanged = { query ->
                                        vm.onSearchQueryChanged(query)
                                    },
                                    onObjectSelected = { item ->
                                        vm.onObjectSelected(item)
                                    },
                                    onBack = {
                                        vm.onBack()
                                    }
                                )
                            }
                        }
                    }

                    LaunchedEffect(Unit) {
                        vm.commands.collect { command ->
                            when (command) {
                                is ObjectShortcutWidgetConfigViewModel.Command.FinishWithSuccess -> {
                                    setResult(
                                        Activity.RESULT_OK,
                                        Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, command.appWidgetId)
                                    )
                                    finish()
                                }
                                is ObjectShortcutWidgetConfigViewModel.Command.ShowError -> {
                                    Toast.makeText(this@ObjectShortcutWidgetConfigActivity, "Error: ${command.message}", Toast.LENGTH_LONG).show()
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
        componentManager().objectShortcutWidgetConfigComponent.release()
    }
}

