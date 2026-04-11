package com.anytypeio.anytype.ui.oswidgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_os_widgets.presentation.DataViewWidgetConfigViewModel
import com.anytypeio.anytype.feature_os_widgets.ui.config.ObjectSelectionScreen
import com.anytypeio.anytype.feature_os_widgets.ui.config.SpaceSelectionScreen
import com.anytypeio.anytype.feature_os_widgets.ui.config.ViewerSelectionScreen
import com.anytypeio.anytype.feature_os_widgets.ui.config.WidgetConfigTheme
import javax.inject.Inject

/**
 * Configuration Activity for the Data View widget.
 * Three-step flow: Select Space -> Select Set/Collection -> Select View
 */
class DataViewWidgetConfigActivity : AppCompatActivity() {

    @Inject
    lateinit var factory: DataViewWidgetConfigViewModel.Factory

    @Inject
    lateinit var urlBuilder: UrlBuilder

    private lateinit var vm: DataViewWidgetConfigViewModel

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

        componentManager().dataViewWidgetConfigComponent.get().inject(this)
        vm = ViewModelProvider(this, factory.create(appWidgetId))[DataViewWidgetConfigViewModel::class.java]

        setContentView(
            ComposeView(this).apply {
                setContent {
                    val screenState by vm.screenState.collectAsState()
                    val spaces by vm.spaces.collectAsState()
                    val objectItems by vm.objectItems.collectAsState()
                    val isLoading by vm.isLoading.collectAsState()
                    val searchQuery by vm.searchQuery.collectAsState()
                    val selectedSpace by vm.selectedSpace.collectAsState()
                    val selectedObject by vm.selectedObject.collectAsState()
                    val viewers by vm.viewers.collectAsState()

                    WidgetConfigTheme {
                        when (screenState) {
                            DataViewWidgetConfigViewModel.ScreenState.SpaceSelection -> {
                                SpaceSelectionScreen(
                                    spaces = spaces,
                                    urlBuilder = urlBuilder,
                                    onSpaceSelected = { space ->
                                        vm.onSpaceSelected(space)
                                    },
                                    onCancel = { finish() }
                                )
                            }
                            DataViewWidgetConfigViewModel.ScreenState.ObjectSelection -> {
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
                            DataViewWidgetConfigViewModel.ScreenState.ViewerSelection -> {
                                ViewerSelectionScreen(
                                    objectName = selectedObject?.obj?.name.orEmpty(),
                                    viewers = viewers,
                                    onViewerSelected = { viewer ->
                                        vm.onViewerSelected(viewer)
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
                                is DataViewWidgetConfigViewModel.Command.FinishWithSuccess -> {
                                    setResult(
                                        Activity.RESULT_OK,
                                        Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, command.appWidgetId)
                                    )
                                    finish()
                                }
                                is DataViewWidgetConfigViewModel.Command.ShowError -> {
                                    Toast.makeText(
                                        this@DataViewWidgetConfigActivity,
                                        "Error: ${command.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                is DataViewWidgetConfigViewModel.Command.FinishWithFailure -> {
                                    Toast.makeText(
                                        this@DataViewWidgetConfigActivity,
                                        "Error: ${command.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    setResult(Activity.RESULT_CANCELED)
                                    finish()
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
        componentManager().dataViewWidgetConfigComponent.release()
    }
}
