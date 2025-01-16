package com.anytypeio.anytype.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.core.os.bundleOf
import androidx.fragment.compose.content
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.features.profile.ProfileScreen
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.profile.ParticipantViewModel

class ParticipantFragment: BaseBottomSheetComposeFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        MaterialTheme {
            ProfileScreen()
        }
    }

    override fun injectDependencies() {
        val vmParams = ParticipantViewModel.VmParams(
            objectId = argString(ARG_OBJECT_ID),
            space = SpaceId(argString(ARG_SPACE))
        )
        componentManager().participantScreenComponent.get(vmParams).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().participantScreenComponent.release()
    }

    companion object ProfileScreenNavigation {
        const val ARG_SPACE = "arg.profile.screen.space"
        const val ARG_OBJECT_ID = "arg.profile.screen.object_id"

        fun args(space: Id, objectId: Id) = bundleOf(
            ARG_SPACE to space,
            ARG_OBJECT_ID to objectId
        )
    }
}