package com.agileburo.anytype

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.agileburo.anytype.di.app.MainScreenComponent
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

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
        setSupportActionBar(toolbar)
        NavigationUI.setupWithNavController(
            bottomNavigationView,
            Navigation.findNavController(this, R.id.fragment)
        )
    }
}
