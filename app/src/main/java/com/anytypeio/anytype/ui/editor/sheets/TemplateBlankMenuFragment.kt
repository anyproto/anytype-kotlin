package com.anytypeio.anytype.ui.editor.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.features.objects.ObjectActionAdapter
import com.anytypeio.anytype.core_ui.layout.SpacingItemDecoration
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentObjectMenuBinding
import com.anytypeio.anytype.presentation.objects.ObjectAction

class TemplateBlankMenuFragment : BaseBottomSheetFragment<FragmentObjectMenuBinding>() {

    private val actionAdapter by lazy {
        ObjectActionAdapter { _ ->
            dismiss()
            withParent<SetAsDefaultListener> { onSetAsDefaultClicked() }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.optionIcon.gone()
        binding.optionCover.gone()
        binding.optionLayout.gone()
        binding.optionRelations.gone()
        binding.optionHistory.gone()
        binding.iconDivider.gone()
        binding.coverDivider.gone()
        binding.layoutDivider.gone()
        binding.relationsDivider.gone()
        binding.historyDivider.gone()
        binding.objectDiagnostics.gone()
        binding.objectDiagnosticsDivider.gone()
        binding.rvActions.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = actionAdapter
            addItemDecoration(
                SpacingItemDecoration(
                    firstItemSpacingStart = resources.getDimension(R.dimen.dp_8).toInt(),
                    lastItemSpacingEnd = resources.getDimension(R.dimen.dp_8).toInt()
                )
            )
        }
        actionAdapter.submitList(listOf(ObjectAction.SET_AS_DEFAULT))
    }

    override fun injectDependencies() {}

    override fun releaseDependencies() {}

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentObjectMenuBinding {
        return FragmentObjectMenuBinding.inflate(
            inflater, container, false
        )
    }
}

interface SetAsDefaultListener {
    fun onSetAsDefaultClicked()
}