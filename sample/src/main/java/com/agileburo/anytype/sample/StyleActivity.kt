package com.agileburo.anytype.sample

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.agileburo.anytype.core_ui.common.Alignment
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.features.page.styling.StylingType
import com.agileburo.anytype.core_ui.model.StyleConfig
import com.agileburo.anytype.core_ui.state.ControlPanelState
import kotlinx.android.synthetic.main.activity_style.*

class StyleActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_style)

        findViewById<ImageView>(R.id.close).setOnClickListener {
            styleToolbar.hideWithAnimation()
        }

        button.setOnClickListener {
            styleToolbar.update(
                config = StyleConfig(
                    visibleTypes = listOf(
                        StylingType.STYLE,
                        StylingType.TEXT_COLOR,
                        StylingType.BACKGROUND
                    ),
                    enabledMarkup = listOf(
                        Markup.Type.BOLD,
                        Markup.Type.ITALIC,
                        Markup.Type.STRIKETHROUGH,
                        Markup.Type.KEYBOARD,
                        Markup.Type.LINK
                    ),
                    enabledAlignment = listOf(Alignment.START, Alignment.END)
                ),
                props = ControlPanelState.Toolbar.Styling.Props(
                    isBold = false,
                    isItalic = false,
                    isStrikethrough = true,
                    isCode = false,
                    isLinked = false,
                    color = null,
                    background = null,
                    alignment = null
                )
            )
            styleToolbar.showWithAnimation()

        }
    }
}