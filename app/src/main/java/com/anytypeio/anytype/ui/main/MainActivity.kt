package com.anytypeio.anytype.ui.main

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.app.DefaultAppActionManager
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.navigation.Navigator
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient
import com.anytypeio.anytype.presentation.main.MainViewModel
import com.anytypeio.anytype.presentation.main.MainViewModelFactory
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.wallpaper.WallpaperColor
import com.anytypeio.anytype.ui.editor.CreateObjectFragment
import javax.inject.Inject

class MainActivity : AppCompatActivity(R.layout.activity_main), AppNavigation.Provider {

    private val vm by viewModels<MainViewModel> { factory }

    private val navigator by lazy { Navigator() }

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var factory: MainViewModelFactory

    val container : FragmentContainerView get() = findViewById(R.id.fragment)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject()
        if (savedInstanceState != null) vm.onRestore()
        with(lifecycleScope) {
            subscribe(vm.wallpaper) { wallpaper -> setWallpaper(wallpaper) }
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
                container.setBackgroundResource(R.color.default_dashboard_background_color)
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
