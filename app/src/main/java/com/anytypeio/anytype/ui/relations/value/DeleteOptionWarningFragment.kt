package com.anytypeio.anytype.ui.relations.value

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.foundation.AlertConfig
import com.anytypeio.anytype.core_ui.foundation.AlertIcon
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.GRADIENT_TYPE_RED
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.ButtonWarningLoading
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_utils.ext.argInt
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.ui.settings.typography
import com.google.android.material.bottomsheet.BottomSheetDialog

class DeleteOptionWarningFragment : BaseBottomSheetComposeFragment() {

    private val optionId get() = argString(OPTION_IDS_KEY)
    private val descriptionString get() = argInt(DESCRIPTION_STRING_KEY)

    var onDeletionAccepted: (Id) -> Unit = {}
    var onDeletionCancelled: () -> Unit = {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            dialog?.setOnShowListener { dg ->
                val bottomSheet = (dg as? BottomSheetDialog)?.findViewById<FrameLayout>(
                    com.google.android.material.R.id.design_bottom_sheet
                )
                bottomSheet?.setBackgroundColor(requireContext().color(android.R.color.transparent))
            }
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(start = 8.dp, end = 8.dp, bottom = 15.dp)
                            .background(
                                color = colorResource(id = R.color.background_secondary),
                                shape = RoundedCornerShape(size = 16.dp)
                            ),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(bottom = 16.dp, start = 20.dp, end = 20.dp)
                        ) {
                            val icon = AlertConfig.Icon(
                                GRADIENT_TYPE_RED,
                                icon = R.drawable.ic_alert_question_warning
                            )
                            Box(
                                Modifier
                                    .padding(vertical = 6.dp)
                                    .align(Alignment.CenterHorizontally)
                            ) {
                                Dragger()
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            AlertIcon(icon)
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(id = R.string.options_delete_title),
                                style = HeadlineHeading,
                                textAlign = TextAlign.Center,
                                color = colorResource(id = R.color.text_primary)
                            )
                            val description = if (descriptionString != 0) {
                                stringResource(id = descriptionString)
                            } else {
                                stringResource(id = R.string.options_delete_description)
                            }
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = description,
                                style = BodyRegular,
                                textAlign = TextAlign.Center,
                                color = colorResource(id = R.color.text_primary)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .padding(
                                        top = 10.dp,
                                        start = 20.dp,
                                        end = 20.dp
                                    )
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ButtonSecondary(
                                    onClick = onDeletionCancelled,
                                    size = ButtonSize.LargeSecondary,
                                    text = stringResource(id = R.string.cancel),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                ButtonWarningLoading(
                                    onClick = {
                                        onDeletionAccepted(optionId)
                                    },
                                    size = ButtonSize.Large,
                                    text = stringResource(id = R.string.delete),
                                    modifierBox = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    modifierButton = Modifier.fillMaxWidth(),
                                    loading = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }


    override fun injectDependencies() {
        // Do nothing
    }

    override fun releaseDependencies() {
        // Do nothing
    }

    companion object {
        const val OPTION_IDS_KEY = "option.warning.optionIds"
        const val DESCRIPTION_STRING_KEY = "option.warning.descriptionString"
        fun new(optionId: Id, descriptionString: Int? = null) =
            DeleteOptionWarningFragment().apply {
                arguments = bundleOf(
                    OPTION_IDS_KEY to optionId,
                    DESCRIPTION_STRING_KEY to descriptionString
                )
            }
    }
}