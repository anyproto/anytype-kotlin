package com.agileburo.anytype.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_keyboard.*

class KeyboardActivity : AppCompatActivity(), UpdateValues {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keyboard)

        tvFirst.text = "111"
        tvSecond.text = "222"
        tvThree.text = "333"

        btnTest.setOnClickListener {
            AboveDialogFragment().show(supportFragmentManager, null)
        }
    }

    override fun update1() {
        tvFirst.text = incText(tvFirst.text.toString())
    }

    override fun update2() {
        tvSecond.text = incText(tvSecond.text.toString())
    }

    override fun update3() {
        tvThree.text = incText(tvThree.text.toString())
    }

    fun incText(value: String): String =
        value.toInt().inc().toString()
}

interface UpdateValues {
    fun update1()
    fun update2()
    fun update3()
}