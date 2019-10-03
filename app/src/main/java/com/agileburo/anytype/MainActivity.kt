package com.agileburo.anytype

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.agileburo.anytype.di.app.MainScreenComponent
import com.agileburo.anytype.feature_desktop.navigation.DesktopNavigationProvider
import com.agileburo.anytype.feature_login.ui.login.presentation.navigation.AuthNavigation
import com.agileburo.anytype.feature_login.ui.login.presentation.navigation.AuthNavigationProvider
import com.agileburo.anytype.navigation.Navigator
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), AuthNavigationProvider, DesktopNavigationProvider {

    private val navigator by lazy { Navigator() }

    @Inject
    lateinit var context: Context

    private val applicationComponent by lazy {
        (application as AndroidApplication).applicationComponent
    }

    private val mainScreenComponent: MainScreenComponent by lazy {
        applicationComponent.mainScreenComponent()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mainScreenComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        NavigationUI.setupWithNavController(
            bottomNavigationView,
            Navigation.findNavController(this, R.id.fragment)
        )
    }

    override fun onResume() {
        super.onResume()
        navigator.bind(findNavController(R.id.fragment))
    }

    override fun onPause() {
        super.onPause()
        navigator.unbind()
    }

    override fun provideNavigation(): AuthNavigation = navigator
    override fun provideDesktopNavigation() = navigator
}
