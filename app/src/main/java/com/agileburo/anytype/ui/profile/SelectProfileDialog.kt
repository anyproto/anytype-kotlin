package com.agileburo.anytype.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.agileburo.anytype.R
import com.agileburo.anytype.ui.profile.SelectProfileAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_select_profile.*

class SelectProfileDialog : BottomSheetDialogFragment() {

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

    companion object {
        fun newInstance(): SelectProfileDialog = SelectProfileDialog()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_select_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectProfileRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = selectProfileAdapter
        }
    }
}