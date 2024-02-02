package com.anytypeio.anytype.ui.relations.value

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.relations.OptionWidget
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.argStringOrNull
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.presentation.relations.value.tagstatus.OptionWidgetAction
import com.anytypeio.anytype.presentation.relations.value.tagstatus.OptionWidgetViewState
import com.anytypeio.anytype.ui.settings.typography

class OptionFragment: BaseBottomSheetComposeFragment() {

    private val optionId get() = argStringOrNull(OPTION_ID_KEY)
    private val color get() = argString(COLOR_KEY)
    private val text get() = argString(TEXT_KEY)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(
                    typography = typography,
                    shapes = MaterialTheme.shapes.copy(medium = RoundedCornerShape(10.dp)),
                    colors = MaterialTheme.colors.copy(
                        surface = colorResource(id = R.color.context_menu_background)
                    )
                ) {
                    val color = ThemeColor.fromCode(color)
                    if (optionId == null) {
                        OptionWidget(
                            state = OptionWidgetViewState.Create(
                                color = color,
                                text = text
                            ),
                            action = { _, _ ->
                                     Log.d("Test1983", "OptionWidgetViewState.Create")
                            },
                        )
                    } else {
                        OptionWidget(
                            state = OptionWidgetViewState.Edit(
                                optionId = optionId!!,
                                color = color,
                                text = text
                            ),
                            action = { _, _ -> },
                        )
                    }
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        skipCollapsed()
        expand()
    }

    fun action(action: (OptionWidgetAction) -> Unit) {}

    companion object {
        val OPTION_ID_KEY = "arg.option.option_id"
        val COLOR_KEY = "arg.option.color"
        val TEXT_KEY = "arg.option.text"

        fun args(optionId: String?, color: String, text: String) = bundleOf(
            OPTION_ID_KEY to optionId,
            COLOR_KEY to color,
            TEXT_KEY to text
        )
    }
}