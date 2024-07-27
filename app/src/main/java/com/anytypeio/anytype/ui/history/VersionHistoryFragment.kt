package com.anytypeio.anytype.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.gallery_experience.viewmodel.GalleryInstallationViewModel
import com.anytypeio.anytype.gallery_experience.viewmodel.GalleryInstallationViewModelFactory
import com.anytypeio.anytype.presentation.history.VersionHistoryVMFactory
import com.anytypeio.anytype.presentation.history.VersionHistoryViewModel
import javax.inject.Inject

class VersionHistoryFragment : BaseBottomSheetComposeFragment() {

    private val ctx get() = argString(CTX_ARG)
    private val spaceId get() = argString(SPACE_ID_ARG)

    @Inject
    lateinit var factory: VersionHistoryVMFactory
    private val vm by viewModels<VersionHistoryViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        vm.onStart()
    }

    override fun injectDependencies() {
        val vmParams = VersionHistoryViewModel.VmParams(
            objectId = ctx,
            spaceId = spaceId
        )
        componentManager().versionHistoryComponent.get(vmParams).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().versionHistoryComponent.release()
    }

    companion object {
        const val CTX_ARG = "anytype.ui.history.ctx_arg"
        const val SPACE_ID_ARG = "anytype.ui.history.space_id_arg"

        fun args(ctx: Id, spaceId: Id) = bundleOf(
            CTX_ARG to ctx,
            SPACE_ID_ARG to spaceId
        )
    }
}