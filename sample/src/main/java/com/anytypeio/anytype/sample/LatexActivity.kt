package com.anytypeio.anytype.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class LatexActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latex)
//        mathview.fontSize = 40f
//        mathview.latex = "x = \\frac{-b \\pm \\sqrt{b^2-4ac}}{2a}"
    }
}