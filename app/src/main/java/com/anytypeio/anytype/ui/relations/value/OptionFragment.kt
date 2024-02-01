package com.anytypeio.anytype.ui.relations.value

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.relations.OptionWidget
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.presentation.relations.value.tagstatus.OptionWidgetAction
import com.anytypeio.anytype.presentation.relations.value.tagstatus.OptionWidgetViewState
import com.anytypeio.anytype.ui.settings.typography

class OptionFragment: BaseBottomSheetComposeFragment() {

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
                    OptionWidget(
                        state = OptionWidgetViewState.Create(
                            optionId = "",
                            color = ThemeColor.BLUE,
                            text = "text1983",
                        ),
                        action = {},
                        scope = lifecycleScope,
                    )
                }
            }
        }
    }

    fun action(action: (OptionWidgetAction) -> Unit) {}
}