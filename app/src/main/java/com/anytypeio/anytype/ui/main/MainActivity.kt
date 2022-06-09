package com.anytypeio.anytype.ui.main

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
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
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.theme.GetTheme
import com.anytypeio.anytype.navigation.Navigator
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient
import com.anytypeio.anytype.presentation.main.MainViewModel
import com.anytypeio.anytype.presentation.main.MainViewModel.Command
import com.anytypeio.anytype.presentation.main.MainViewModelFactory
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.wallpaper.WallpaperColor
import com.anytypeio.anytype.ui.editor.CreateObjectFragment
import com.anytypeio.anytype.ui_settings.appearance.ThemeApplicator
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

class MainActivity : AppCompatActivity(R.layout.activity_main), AppNavigation.Provider {

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

    val container: FragmentContainerView get() = findViewById(R.id.fragment)

    override fun onCreate(savedInstanceState: Bundle?) {
        setupWindowInsets()
        inject()
        setupTheme()
        super.onCreate(savedInstanceState)
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
                        when(command) {
                            is Command.ShowDeletedAccountScreen -> {
                                navigator.deletedAccountScreen(
                                    deadline = command.deadline
                                )
                            }
                            is Command.LogoutDueToAccountDeletion -> {
                                navigator.logout()
                            }
                        }
                    }
                }
            }
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
        if (intent != null && intent.action == Intent.ACTION_VIEW) {
            val bundle = intent.extras
            if (bundle != null) {
                val type = bundle.getString(DefaultAppActionManager.ACTION_CREATE_NEW_TYPE_KEY)
                if (type != null) {
                    findNavController(R.id.fragment)
                        .navigate(
                            R.id.action_global_createObjectFragment,
                            bundleOf(CreateObjectFragment.TYPE_KEY to type)
                        )
                }
            }
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
        navigator.bind(findNavController(R.id.fragment))
    }

    override fun onPause() {
        super.onPause()
        navigator.unbind()
    }

    override fun onDestroy() {
        super.onDestroy()
        release()
    }

    override fun nav(): AppNavigation = navigator

    fun inject() {
        componentManager().mainEntryComponent.get().inject(this)
    }

    fun release() {
        componentManager().mainEntryComponent.release()
    }
}
