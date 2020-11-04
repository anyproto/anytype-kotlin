package com.anytypeio.anytype.sample

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.anytypeio.anytype.presentation.page.editor.Markup
import com.anytypeio.anytype.presentation.page.editor.model.Alignment
import com.anytypeio.anytype.presentation.page.editor.styling.StyleConfig
import com.anytypeio.anytype.presentation.page.editor.styling.StylingType
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
                    enabledAlignment = listOf(
                        Alignment.START,
                        Alignment.END
                    )
                ),
                props = com.anytypeio.anytype.presentation.page.editor.control.ControlPanelState.Toolbar.Styling.Props(
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