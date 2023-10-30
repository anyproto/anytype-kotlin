package com.anytypeio.anytype.ui.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.MyFragmentContainerBinding
import com.anytypeio.anytype.ui.templates.EditorTemplateFragment

class EditorModalFragment : BaseBottomSheetFragment<MyFragmentContainerBinding>() {

    private val ctx get() = arg<Id>(ARG_ID)
    private val targetTypeId get() = arg<Id>(ARG_TARGET_TYPE_ID)
    private val targetTypeKey get() = arg<Id>(ARG_TARGET_TYPE_KEY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppBottomSecondarySheetDialogTheme)
        val editorFragment = EditorTemplateFragment.newInstance(
            id = ctx,
            targetTypeId = targetTypeId,
            targetTypeKey = targetTypeKey,
            type = EditorTemplateFragment.TYPE_SINGLE
        )
        childFragmentManager.beginTransaction()
            .add(R.id.fragment_container_view, editorFragment)
            .commit()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        skipCollapsed()
        setFullHeightSheet()
    }

    override fun onStart() {
        super.onStart()
        expand()
    }

    override fun injectDependencies() {}

    override fun releaseDependencies() {}

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): MyFragmentContainerBinding = MyFragmentContainerBinding.inflate(
        inflater, container, false
    )

    companion object {
        const val ARG_ID = "arg_id"
        const val ARG_TARGET_TYPE_ID = "arg_target_object_type"
        const val ARG_TARGET_TYPE_KEY = "arg_target_object_type_key"
    }
}