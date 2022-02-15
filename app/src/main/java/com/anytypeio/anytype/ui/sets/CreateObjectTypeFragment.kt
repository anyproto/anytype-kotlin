package com.anytypeio.anytype.ui.sets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.core_ui.features.sets.CreateObjectTypeAdapter
import com.anytypeio.anytype.core_utils.ext.hideKeyboard
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentCreateObjectTypeBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.CreateObjectTypeView
import com.anytypeio.anytype.presentation.sets.CreateObjectTypeViewModel
import com.anytypeio.anytype.presentation.sets.CreateObjectTypeViewState
import javax.inject.Inject

class CreateObjectTypeFragment : BaseBottomSheetFragment<FragmentCreateObjectTypeBinding>() {

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnCreate.setOnClickListener { vm.onCreateClicked(binding.edtTypeName.text.toString()) }
        vm.state.observe(viewLifecycleOwner) { observeData(it) }
        binding.rvTypes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTypes.adapter = typesAdapter
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
                binding.edtTypeName.hideKeyboard()
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

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCreateObjectTypeBinding = FragmentCreateObjectTypeBinding.inflate(
        inflater, container, false
    )

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