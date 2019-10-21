package com.agileburo.anytype.ui.main

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.agileburo.anytype.R
import com.agileburo.anytype.navigation.Navigator
import com.agileburo.anytype.presentation.navigation.AppNavigation
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), AppNavigation.Provider {

    private val navigator by lazy { Navigator() }

    @Inject
    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
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

    override fun nav(): AppNavigation = navigator
}
