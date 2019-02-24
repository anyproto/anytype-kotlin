package com.agileburo.anytype

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.ebolo.krichtexteditor.fragments.kRichEditorFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnART.setOnClickListener {

            val editorFragment = kRichEditorFragment{}

            supportFragmentManager.beginTransaction()
                .add(R.id.container, editorFragment)
                .commit()

        }

        dragAndDropButton.setOnClickListener {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.container, PageFragment.getInstance())
                .commit()
        }
    }
}
