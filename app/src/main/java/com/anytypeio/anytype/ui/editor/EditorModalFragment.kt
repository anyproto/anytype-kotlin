package com.anytypeio.anytype.ui.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.MyFragmentContainerBinding

class EditorModalFragment : BaseBottomSheetFragment<MyFragmentContainerBinding>() {

    private val ctx get() = arg<Id>(ARG_ID)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppBottomSecondarySheetDialogTheme)
        val editorFragment = EditorFragment.newInstance(ctx, false)
        childFragmentManager.beginTransaction()
            .add(R.id.fragment_container_view, editorFragment)
            .commit()
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
    }
}