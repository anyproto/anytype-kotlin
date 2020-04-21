package com.agileburo.anytype.ui.main

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.agileburo.anytype.R
import com.agileburo.anytype.navigation.Navigator
import com.agileburo.anytype.presentation.navigation.AppNavigation
import javax.inject.Inject

class MainActivity : AppCompatActivity(R.layout.activity_main), AppNavigation.Provider {

    private val navigator by lazy { Navigator() }

    @Inject
    lateinit var context: Context

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
