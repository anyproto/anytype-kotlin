package com.anytypeio.anytype.ui.main

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.OpenableColumns
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.app.DefaultAppActionManager
import com.anytypeio.anytype.core_models.ThemeMode
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.core_utils.ext.parseImagePath
import com.anytypeio.anytype.core_utils.ext.parsePath
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.theme.GetTheme
import com.anytypeio.anytype.middleware.discovery.MDNSProvider
import com.anytypeio.anytype.navigation.Navigator
import com.anytypeio.anytype.other.DEEP_LINK_PATTERN
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient
import com.anytypeio.anytype.presentation.main.MainViewModel
import com.anytypeio.anytype.presentation.main.MainViewModel.Command
import com.anytypeio.anytype.presentation.main.MainViewModelFactory
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.wallpaper.WallpaperColor
import com.anytypeio.anytype.ui.editor.CreateObjectFragment
import com.anytypeio.anytype.ui.sharing.SharingFragment
import com.anytypeio.anytype.ui_settings.appearance.ThemeApplicator
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.UpdateFrom
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class MainActivity : AppCompatActivity(R.layout.activity_main), AppNavigation.Provider {

    var deepLink: String? = null

    private val vm by viewModels<MainViewModel> { factory }

    private val navigator by lazy { Navigator() }

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var factory: MainViewModelFactory

    @Inject
    lateinit var getTheme: GetTheme

    @Inject
    lateinit var themeApplicator: ThemeApplicator

    @Inject
    lateinit var mdnsProvider: MDNSProvider

    @Inject
    lateinit var featureToggles: FeatureToggles

    val container: FragmentContainerView get() = findViewById(R.id.fragment)

    override fun onCreate(savedInstanceState: Bundle?) {
        setupWindowInsets()
        inject()
        setupTheme()
        super.onCreate(savedInstanceState)

        startAppUpdater()

        if (savedInstanceState != null) vm.onRestore()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.wallpaper.collect { setWallpaper(it) }
                }
                launch {
                    vm.toasts.collect { toast(it) }
                }
                launch {
                    vm.commands.collect { command ->
                        when (command) {
                            is Command.ShowDeletedAccountScreen -> {
                                navigator.deletedAccountScreen(
                                    deadline = command.deadline
                                )
                            }
                            is Command.LogoutDueToAccountDeletion -> {
                                navigator.logout()
                            }
                            is Command.OpenCreateNewType -> {
                                findNavController(R.id.fragment)
                                    .navigate(
                                        R.id.action_global_createObjectFragment,
                                        bundleOf(
                                            CreateObjectFragment.TYPE_KEY to command.type
                                        )
                                    )
                            }
                            is Command.Sharing.Text -> {
                                SharingFragment.text(command.data).show(
                                    supportFragmentManager,
                                    SHARE_DIALOG_LABEL
                                )
                            }
                            is Command.Sharing.Image -> {
                                SharingFragment.image(command.path).show(
                                    supportFragmentManager,
                                    SHARE_DIALOG_LABEL
                                )
                            }
                            is Command.Sharing.Images -> {
                                toast("TODO")
                            }
                            is Command.Error -> {
                                toast(command.msg)
                            }
                        }
                    }
                }
            }
        }
        if (savedInstanceState == null && intent.action == Intent.ACTION_SEND) {
            proceedWithShareIntent(intent)
        }
    }

    private fun startAppUpdater() {
        if (featureToggles.isAutoUpdateEnabled) {
            AppUpdater(this)
                .setUpdateFrom(UpdateFrom.JSON)
                .setUpdateJSON(AUTO_UPDATE_URL)
                .setButtonDoNotShowAgain("")
                .start()
        }
    }

    private fun setupWindowInsets() {
        if (BuildConfig.USE_NEW_WINDOW_INSET_API && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }
    }

    private fun setupTheme() {
        runBlocking {
            getTheme(BaseUseCase.None).proceed(
                success = {
                    setTheme(it)
                },
                failure = {
                    Timber.e(it, "Error while setting current app theme")
                },
            )
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            when(intent.action) {
                Intent.ACTION_VIEW -> {
                    val data = intent.dataString
                    if (data != null && data.contains(DEEP_LINK_PATTERN)) {
                        deepLink = data
                    } else {
                        intent.extras?.getString(DefaultAppActionManager.ACTION_CREATE_NEW_TYPE_KEY)?.let {
                            vm.onIntentCreateObject(it)
                        }
                    }
                }
                Intent.ACTION_SEND -> {
                    proceedWithShareIntent(intent)
                }
                Intent.ACTION_SEND_MULTIPLE -> {
                    proceedWithShareIntent(intent)
                }
            }
        }
        if (BuildConfig.DEBUG) {
            Timber.d("on NewIntent: $intent")
        }
    }

    private fun proceedWithShareIntent(intent: Intent) {
        Timber.d("Got intent: $intent")
        when {
            intent.type == "text/plain" -> {
                vm.onIntentTextShare(intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty())
            }
            intent.type?.startsWith("image/") == true -> {
                if (intent.action == Intent.ACTION_SEND_MULTIPLE) {
                    // TODO
                } else {
                    proceedWithReceivingImage(intent)
                }
            }
            intent.type?.startsWith("application/") == true -> {
                if (intent.action == Intent.ACTION_SEND_MULTIPLE) {
                    // TODO
                } else {
                    proceedWithReceivingFile(intent)
                }
            }
        }
    }

    private fun proceedWithReceivingFile(intent: Intent) {
        val extra = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM)
        if (extra is Uri) {
            val name = if (extra.scheme == "content") {
                contentResolver.query(
                    extra,
                    null,
                    null,
                    null,
                    null
                ).use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) {
                        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx != -1) {
                            cursor.getString(idx)
                        } else {
                            "Untitled"
                        }
                    } else {
                        "Untitled"
                    }
                }
            } else {
                Timber.w("Not content scheme")
                extra.path!!.substring(extra.path!!.lastIndexOf("/"))
            }
            val inputStream = contentResolver.openInputStream(extra)
            var path = ""
            inputStream?.use { input ->
                val newFile = File(cacheDir?.path + "/" + name);
                FileOutputStream(newFile).use { output ->
                    val buffer = ByteArray(1024)
                    var read: Int = input.read(buffer)
                    while (read != -1) {
                        output.write(buffer, 0, read)
                        read = input.read(buffer)
                    }
                }
                path = newFile.path
            }

            val parsed = runCatching { path }
            Timber.d("Parsed APPLICATION uri: ${parsed.getOrNull()} for path: ${extra.path} with scheme: ${extra.scheme}")
            parsed.fold(
                onSuccess = {
                    vm.onIntentImageShare(it)
                },
                onFailure = {
                    Timber.e(it, "Error while parsing path")
                }
            )
        }
    }

    private fun proceedWithReceivingImage(intent: Intent) {
        val extra = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM)
        if (extra is Uri) {
            Timber.d("Uri path: ${extra.path}")
            val name = if (extra.scheme == "content") {
                contentResolver.query(
                    extra,
                    null,
                    null,
                    null,
                    null
                ).use { cursor ->
                    val result = if (cursor != null && cursor.moveToFirst()) {
                        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx != -1) {
                            cursor.getString(idx)
                        } else {
                            "Untitled"
                        }
                    } else {
                        "Untitled"
                    }
                    cursor?.close()
                    result
                }
            } else {
                extra.path!!.substring(extra.path!!.lastIndexOf("/"))
            }
            Timber.d("Extracted name: $name")
            val inputStream = contentResolver.openInputStream(extra)
            var path = ""
            inputStream?.use { input ->
                val newFile = File(cacheDir?.path + "/" + name);
                FileOutputStream(newFile).use { output ->
                    val buffer = ByteArray(1024)
                    var read: Int = input.read(buffer)
                    while (read != -1) {
                        output.write(buffer, 0, read)
                        read = input.read(buffer)
                    }
                }
                path = newFile.path
            }

            val parsed = runCatching { path }
            Timber.d("Parse IMAGE uri: ${parsed.getOrNull()} for path: ${extra.path} with scheme: ${extra.scheme}")
            parsed.fold(
                onSuccess = {
                    vm.onIntentImageShare(it)
                },
                onFailure = {
                    Timber.e(it, "Error while parsing path")
                }
            )
        } else {
            Timber.w("URI not found")
        }
    }

    private fun setTheme(themeMode: ThemeMode) {
        themeApplicator.apply(themeMode)
    }

    private fun setWallpaper(wallpaper: Wallpaper) {
        when (wallpaper) {
            is Wallpaper.Gradient -> {
                when (wallpaper.code) {
                    CoverGradient.YELLOW -> container.setBackgroundResource(R.drawable.cover_gradient_yellow)
                    CoverGradient.RED -> container.setBackgroundResource(R.drawable.cover_gradient_red)
                    CoverGradient.BLUE -> container.setBackgroundResource(R.drawable.cover_gradient_blue)
                    CoverGradient.TEAL -> container.setBackgroundResource(R.drawable.cover_gradient_teal)
                    CoverGradient.PINK_ORANGE -> container.setBackgroundResource(R.drawable.wallpaper_gradient_1)
                    CoverGradient.BLUE_PINK -> container.setBackgroundResource(R.drawable.wallpaper_gradient_2)
                    CoverGradient.GREEN_ORANGE -> container.setBackgroundResource(R.drawable.wallpaper_gradient_3)
                    CoverGradient.SKY -> container.setBackgroundResource(R.drawable.wallpaper_gradient_4)
                }
            }
            is Wallpaper.Default -> {
                container.setBackgroundResource(R.drawable.cover_gradient_default)
            }
            is Wallpaper.Color -> {
                val color = WallpaperColor.values().find { it.code == wallpaper.code }
                if (color != null) {
                    container.setBackgroundColor(Color.parseColor(color.hex))
                }
            }
            is Wallpaper.Image -> {
                container.setBackgroundResource(R.color.default_dashboard_background_color)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mdnsProvider.start()
        navigator.bind(findNavController(R.id.fragment))
    }

    override fun onPause() {
        super.onPause()
        mdnsProvider.stop()
        navigator.unbind()
    }

    override fun onDestroy() {
        super.onDestroy()
        mdnsProvider.stop()
        release()
    }

    override fun nav(): AppNavigation = navigator

    fun inject() {
        componentManager().mainEntryComponent.get().inject(this)
    }

    fun release() {
        componentManager().mainEntryComponent.release()
    }

    companion object {
        const val AUTO_UPDATE_URL = "https://fra1.digitaloceanspaces.com/anytype-release/latest-android.json"
        const val SHARE_DIALOG_LABEL = "anytype.dialog.share.label"
    }
}
