package com.anytypeio.anytype.ui.main

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.navigation.Navigator
import com.anytypeio.anytype.presentation.main.MainViewModel
import com.anytypeio.anytype.presentation.main.MainViewModelFactory
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import javax.inject.Inject

class MainActivity : AppCompatActivity(R.layout.activity_main), AppNavigation.Provider {

    private val vm by viewModels<MainViewModel> { factory }

    private val navigator by lazy { Navigator() }

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var factory: MainViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject()
        if (savedInstanceState != null) vm.onRestore()
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
