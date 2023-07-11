package com.anytypeio.anytype.sample.design_system

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.anytypeio.anytype.sample.R

class NavigateFragment : Fragment(R.layout.fragment_navigate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.button2).setOnClickListener {
            (activity as Navigate).toContentStyle()
        }
        view.findViewById<TextView>(R.id.button3).setOnClickListener {
            (activity as Navigate).toContentStyleMultiline()
        }
        view.findViewById<TextView>(R.id.btnUxStyle).setOnClickListener {
            (activity as Navigate).toUxStyle()
        }
        view.findViewById<TextView>(R.id.button7).setOnClickListener {
            (activity as Navigate).toButtons()
        }
        view.findViewById<TextView>(R.id.button12).setOnClickListener {
            (activity as Navigate).toButtonsSecondary()
        }
        view.findViewById<TextView>(R.id.button20).setOnClickListener {
            (activity as Navigate).toButtonsWarning()
        }
        view.findViewById<TextView>(R.id.btnCompose).setOnClickListener {
            (activity as Navigate).toButtonsCompose()
        }
        view.findViewById<TextView>(R.id.btnDialogsCompose).setOnClickListener {
            (activity as Navigate).toDialogsCompose()
        }
        view.findViewById<Switch>(R.id.switch3).setOnClickListener {
            it as Switch
            if (it.isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }
}