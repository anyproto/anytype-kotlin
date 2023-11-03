package com.anytypeio.anytype.ui.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.argInt
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.MyFragmentContainerBinding
import com.anytypeio.anytype.ui.templates.EditorTemplateFragment

class EditorModalFragment : BaseBottomSheetFragment<MyFragmentContainerBinding>() {

    private val template get() = arg<Id>(ARG_TEMPLATE_ID)
    private val templateTypeId get() = arg<Id>(ARG_TEMPLATE_TYPE_ID)
    private val templateTypeKey get() = arg<Id>(ARG_TEMPLATE_TYPE_KEY)
    private val screenType get() = argInt(ARG_SCREEN_TYPE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppBottomSecondarySheetDialogTheme)
        val editorFragment = EditorTemplateFragment.newInstance(
            id = template,
            targetTypeId = templateTypeId,
            targetTypeKey = templateTypeKey,
            type = screenType
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
        const val ARG_TEMPLATE_ID = "arg_template_id"
        const val ARG_TEMPLATE_TYPE_ID = "arg_template_object_type"
        const val ARG_TEMPLATE_TYPE_KEY = "arg_template_object_type_key"
        const val ARG_SCREEN_TYPE = "arg_screen_type"
    }
}