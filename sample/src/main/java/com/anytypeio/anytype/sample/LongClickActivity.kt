package com.anytypeio.anytype.sample

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_long_clicked.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Emulate long click on text block, with following
 * Selection and Focus events
 *
 */
class LongClickActivity: AppCompatActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_long_clicked)

        findViewById<ImageView>(R.id.close).setOnClickListener {
            //styleToolbar.hideWithAnimation()
        }

        textInputWidget.enableEditMode()
        textInputWidget.setText("Hi, Konstantin")

        textInputWidget.setOnClickListener {
            Timber.d("Single clicked!")
        }

        textInputWidget.setOnLongClickListener {
            Timber.d("Long clicked!")
            lifecycleScope.launch {
                delay(500)
                Timber.d("Enabling read mode")
                textInputWidget.enableReadMode()
            }
            true
        }

        textInputWidget.selectionWatcher = {intRange: IntRange ->
            Timber.d("Selection changed : $intRange")
        }

        textInputWidget.setOnFocusChangeListener { v, hasFocus ->
            Timber.d("Focus changed:$hasFocus")
        }

        button.setOnClickListener {
            textInputWidget.enableEditMode()
        }
    }
}