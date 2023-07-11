package com.anytypeio.anytype.sample.design_system

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.anytypeio.anytype.sample.R

class DesignSystemActivity : AppCompatActivity(), Navigate {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_design_sysytem)
        supportFragmentManager
            .beginTransaction()
            .add(R.id.root, NavigateFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun toContentStyle() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.root, ContentStyleFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun toContentStyleMultiline() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.root, ContentStyleMultiline())
            .addToBackStack(null)
            .commit()
    }

    override fun toUxStyle() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.root, UxStyleFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun toButtons() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.root, ButtonsPrimaryFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun toButtonsSecondary() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.root, ButtonsSecondaryFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun toButtonsWarning() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.root, ButtonsWarningFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun toButtonsCompose() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.root, ComposeButtonsPrimaryFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun toDialogsCompose() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.root, ComposeDialogFragment())
            .addToBackStack(null)
            .commit()
    }
}

interface Navigate {
    fun toContentStyle()
    fun toContentStyleMultiline()
    fun toUxStyle()
    fun toButtons()
    fun toButtonsSecondary()
    fun toButtonsWarning()
    fun toButtonsCompose()
    fun toDialogsCompose()
}