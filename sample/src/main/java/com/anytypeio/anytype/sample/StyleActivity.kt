package com.anytypeio.anytype.sample

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_style.*

class StyleActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_style)

        findViewById<ImageView>(R.id.close).setOnClickListener {
            //styleToolbar.hideWithAnimation()
        }

        button.setOnClickListener {
        }
    }
}