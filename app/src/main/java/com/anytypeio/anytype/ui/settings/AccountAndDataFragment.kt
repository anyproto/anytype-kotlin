package com.anytypeio.anytype.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.Toolbar
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment

class AccountAndDataFragment : BaseBottomSheetComposeFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { accountAndDataScreen() }
        }
    }

    override fun injectDependencies() {
        // TODO in the next PR
    }

    override fun releaseDependencies() {
        // TODO in the next PR
    }
}

@Composable
fun accountAndDataScreen() {
    MaterialTheme(typography = typography) {
        Column {
            Box(Modifier.padding(vertical = 6.dp).align(Alignment.CenterHorizontally)) {
                Dragger()
            }
            Toolbar(stringResource(R.string.account_and_data))
            Section(stringResource(R.string.access))
            Keychain()
            Divider(paddingStart = 60.dp)
            Pincode()
            Divider(paddingStart = 60.dp)
            Section(stringResource(R.string.data))
            Action(stringResource(R.string.clear_file_cache))
            Divider()
            Section(stringResource(R.string.account))
            Action(
                name = stringResource(R.string.reset_account),
                color = colorResource(R.color.anytype_text_red)
            )
            Divider()
            Action(
                name = stringResource(R.string.delete_account),
                color = colorResource(R.color.anytype_text_red)
            )
            Divider()
            Box(Modifier.height(20.dp))
            Action(stringResource(R.string.log_out))
            Divider()
            Box(Modifier.height(54.dp))
        }
    }
}

@Composable
fun Section(name: String) {
    Box(
        modifier = Modifier.height(52.dp).fillMaxWidth(),
        contentAlignment = Alignment.BottomStart
    ) {
        Text(
            text = name,
            fontSize = 13.sp,
            modifier = Modifier.padding(
                start = 20.dp,
                bottom = 8.dp
            ),
            color = colorResource(R.color.text_secondary)
        )
    }
}

@Composable
fun Keychain() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(52.dp)
    ) {
        Image(
            painterResource(R.drawable.ic_key),
            contentDescription = "Key icon",
            modifier = Modifier.padding(
                start = 20.dp
            )
        )
        Text(
            text = stringResource(R.string.keychain_phrase),
            modifier = Modifier.padding(
                start = 12.dp
            )
        )
        Box(
            modifier = Modifier.weight(1.0f, true),
            contentAlignment = Alignment.CenterEnd
        ) {
            Arrow()
        }
    }
}

@Composable
fun Pincode() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(52.dp)
    ) {
        Image(
            painterResource(R.drawable.ic_pin_code),
            contentDescription = "Pincode icon",
            modifier = Modifier.padding(
                start = 20.dp
            )
        )
        Text(
            text = stringResource(R.string.pin_code),
            modifier = Modifier.padding(
                start = 12.dp
            )
        )
        Box(
            modifier = Modifier.weight(1.0f, true),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row {
                Text(
                    text = stringResource(R.string.off),
                    fontSize = 17.sp,
                    color = colorResource(R.color.text_secondary),
                    modifier = Modifier.padding(end = 10.dp)
                )
                Arrow()
            }
        }
    }
}

@Composable
fun Arrow() {
    Image(
        painterResource(R.drawable.ic_arrow_forward),
        contentDescription = "Arrow forward",
        modifier = Modifier.padding(
            end = 20.dp
        )
    )
}

@Composable
fun Action(
    name: String,
    color: Color = Color.Unspecified
) {
    Box(
        modifier = Modifier.height(52.dp).fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = name,
            color = color,
            fontSize = 17.sp,
            modifier = Modifier.padding(
                start = 20.dp
            )
        )
    }
}

