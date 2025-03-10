package com.anytypeio.anytype.ui.primitives

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_properties.EditTypePropertiesViewModel
import com.anytypeio.anytype.feature_properties.EditTypePropertiesViewModelFactory
import com.anytypeio.anytype.feature_properties.add.EditTypePropertiesVmParams
import javax.inject.Inject

class EditTypePropertiesFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var viewModelFactory: EditTypePropertiesViewModelFactory
    private val vm by viewModels<EditTypePropertiesViewModel> { viewModelFactory }
    private val space get() = argString(ARG_SPACE)
    private val typeId get() = argString(ARG_OBJECT_ID)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        MaterialTheme {
        }
    }

    override fun injectDependencies() {
        val params = EditTypePropertiesVmParams(
            objectTypeId = typeId,
            spaceId = SpaceId(space)

        )
        componentManager().editTypePropertiesComponent.get(params).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().editTypePropertiesComponent.release()
    }

    companion object {

        fun args(objectId: Id, space: Id) = bundleOf(
            ARG_OBJECT_ID to objectId,
            ARG_SPACE to space
        )

        const val ARG_OBJECT_ID = "arg.primitives.edit.type.property.object.id"
        const val ARG_SPACE = "arg.primitives.edit.type.property.space"

        const val DEFAULT_PADDING_TOP = 10
    }
}