package com.anytypeio.anytype.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.DialogSelectProfileBinding

class SelectProfileDialog : BaseBottomSheetFragment<DialogSelectProfileBinding>() {

    private val selectProfileAdapter by lazy {
        SelectProfileAdapter(
            models = mutableListOf(
                SelectProfileAdapter.Model.Profile(
                    name = "Konstantin Ivanov",
                    id = "id",
                    status = "20/100 peers",
                    active = true
                ),
                SelectProfileAdapter.Model.Profile(
                    name = "Evgenii Kozlov",
                    id = "id",
                    status = "20/100 peers"
                ),
                SelectProfileAdapter.Model.AddProfile
            ),
            onProfileClicked = {},
            onAddProfileClicked = {}
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.selectProfileRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = selectProfileAdapter
        }
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogSelectProfileBinding = DialogSelectProfileBinding.inflate(
        inflater, container, false
    )
}