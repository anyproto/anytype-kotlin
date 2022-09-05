package com.anytypeio.anytype.ui.objects

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.core_ui.features.objects.ObjectTypeVerticalAdapter
import com.anytypeio.anytype.core_ui.reactive.textChanges
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ext.hideKeyboard
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetTextInputFragment
import com.anytypeio.anytype.databinding.FragmentObjectTypeChangeBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.objects.ObjectTypeChangeViewModel
import com.anytypeio.anytype.presentation.objects.ObjectTypeChangeViewModelFactory
import com.anytypeio.anytype.presentation.objects.ObjectTypeView
import javax.inject.Inject

class ObjectTypeChangeFragment :
    BaseBottomSheetTextInputFragment<FragmentObjectTypeChangeBinding>() {

    private val smartBlockType: SmartBlockType get() = arg(ARG_SMART_BLOCK_TYPE)
    private val excludedTypes: List<Id>
        get() = argOrNull<List<Id>>(ARG_EXCLUDED_TYPES) ?: emptyList()

    private val vm by viewModels<ObjectTypeChangeViewModel> { factory }

    private val isDraft: Boolean get() = argOrNull<Boolean>(OBJECT_IS_DRAFT_KEY) ?: false

    @Inject
    lateinit var factory: ObjectTypeChangeViewModelFactory

    private val objectTypeAdapter by lazy {
        ObjectTypeVerticalAdapter(
            onItemClick = ::onItemClicked,
            data = arrayListOf()
        )
    }

    override val textInput: EditText get() = binding.searchObjectTypeInput

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recycler.apply {
            adapter = objectTypeAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeViews(views: List<ObjectTypeView.Item>) {
        objectTypeAdapter.update(views)
    }

    private fun onItemClicked(id: String, name: String) {
        val bundle = bundleOf(
            OBJECT_TYPE_URL_KEY to id,
            OBJECT_TYPE_NAME_KEY to name,
            OBJECT_IS_DRAFT_KEY to isDraft
        )
        setFragmentResult(OBJECT_TYPE_REQUEST_KEY, bundle)
        view?.rootView?.hideKeyboard()
        dismiss()
    }

    override fun onStart() {
        with(lifecycleScope) {
            jobs += subscribe(vm.results) { observeViews(it) }
            jobs += subscribe(binding.searchObjectTypeInput.textChanges()) {
                vm.onQueryChanged(it.toString())
            }
        }
        super.onStart()
        vm.onStart(
            smartBlockType = smartBlockType,
            excludedTypes = excludedTypes,
            isDraft = isDraft
        )
    }

    override fun injectDependencies() {
        componentManager().objectTypeChangeComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectTypeChangeComponent.release()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentObjectTypeChangeBinding = FragmentObjectTypeChangeBinding.inflate(
        inflater, container, false
    )

    companion object {
        const val ARG_SMART_BLOCK_TYPE = "arg.object-type.smart-block-type"
        const val ARG_EXCLUDED_TYPES = "arg.object-type.excluded-types"
        const val OBJECT_TYPE_URL_KEY = "object-type-url.key"
        const val OBJECT_TYPE_NAME_KEY = "object-type-name.key"
        const val OBJECT_TYPE_REQUEST_KEY = "object-type.request"
        const val OBJECT_IS_DRAFT_KEY = "arg.object-type-change.isDraft"
    }
}