package com.anytypeio.anytype.ui.sets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.features.sets.CreateObjectTypeAdapter
import com.anytypeio.anytype.core_utils.ext.hideKeyboard
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.CreateObjectTypeView
import com.anytypeio.anytype.presentation.sets.CreateObjectTypeViewModel
import com.anytypeio.anytype.presentation.sets.CreateObjectTypeViewState
import kotlinx.android.synthetic.main.fragment_create_object_type.*
import javax.inject.Inject

class CreateObjectTypeFragment : BaseBottomSheetFragment() {

    @Inject
    lateinit var factory: CreateObjectTypeViewModel.Factory
    private val vm by viewModels<CreateObjectTypeViewModel> { factory }
    private val typesAdapter by lazy { CreateObjectTypeAdapter { vm.onSelectType(it) } }
    private val types: List<CreateObjectTypeView>
        get() {
            val args = requireArguments()
            val list =
                args.getParcelableArrayList<CreateObjectTypeView>(ARG_TYPES)
            checkNotNull(list)
            return list
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_create_object_type, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnCreate.setOnClickListener { vm.onCreateClicked(edtTypeName.text.toString()) }
        vm.state.observe(viewLifecycleOwner) { observeData(it) }
        rvTypes.layoutManager = LinearLayoutManager(requireContext())
        rvTypes.adapter = typesAdapter
        vm.init(types)
    }

    private fun observeData(state: CreateObjectTypeViewState) {
        when (state) {
            CreateObjectTypeViewState.Loading -> TODO()
            is CreateObjectTypeViewState.Success -> {
                typesAdapter.types = state.data
                typesAdapter.notifyDataSetChanged()
            }
            is CreateObjectTypeViewState.Exit -> {
                (parentFragment as CreateObjectTypeCallback).onCreateObjectTypeClicked(
                    state.type,
                    state.name
                )
                edtTypeName.hideKeyboard()
                dismiss()
            }
        }
    }

    override fun injectDependencies() {
        componentManager().createObjectTypeComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().createObjectTypeComponent.release()
    }

    companion object {

        private const val ARG_TYPES = "arg.create.object.types"

        fun newInstance(types: ArrayList<CreateObjectTypeView>): CreateObjectTypeFragment =
            CreateObjectTypeFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_TYPES, types)
                }
            }
    }
}