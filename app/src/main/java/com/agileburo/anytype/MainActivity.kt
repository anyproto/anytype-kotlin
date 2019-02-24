package com.agileburo.anytype

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.agileburo.anytype.di.app.MainScreenComponent
import com.ebolo.krichtexteditor.fragments.kRichEditorFragment
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
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
        btnART.setOnClickListener {
            val editorFragment = kRichEditorFragment{
            }
            supportFragmentManager.beginTransaction()
                .add(R.id.container, editorFragment)
                .commit()

        }
        Timber.d("Get context:$context")
    }
}
