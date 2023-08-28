package com.anytypeio.anytype.ui.objects

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_ui.features.objects.ObjectTypeVerticalAdapter
import com.anytypeio.anytype.core_ui.reactive.textChanges
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetTextInputFragment
import com.anytypeio.anytype.databinding.FragmentObjectTypeChangeBinding
import com.anytypeio.anytype.presentation.objects.ObjectTypeChangeViewModel
import com.anytypeio.anytype.presentation.objects.ObjectTypeChangeViewModel.Command
import com.anytypeio.anytype.presentation.objects.ObjectTypeChangeViewModelFactory
import javax.inject.Inject

abstract class BaseObjectTypeChangeFragment :
    BaseBottomSheetTextInputFragment<FragmentObjectTypeChangeBinding>() {

    abstract fun setTitle()
    abstract fun startWithParams()
    abstract fun onItemClicked(id: Id, key: Key, name: String)

    @Inject
    lateinit var factory: ObjectTypeChangeViewModelFactory
    protected val vm by viewModels<ObjectTypeChangeViewModel> { factory }

    protected val excludeTypes: List<Id>
        get() = argOrNull<List<Id>>(ARG_EXCLUDE_TYPES) ?: emptyList()
    protected val selectedTypes: List<Id>
        get() = argOrNull<List<Id>>(ARG_SELECTED_TYPES) ?: emptyList()

    private val objectTypeAdapter by lazy {
        ObjectTypeVerticalAdapter(
            onItemClick = vm::onItemClicked,
            data = arrayListOf()
        )
    }

    override val textInput: EditText get() = binding.searchObjectTypeInput

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitle()
        binding.recycler.apply {
            adapter = objectTypeAdapter
            layoutManager = LinearLayoutManager(context)
        }
        skipCollapsed()
        setFullHeightSheet()
    }

    override fun onStart() {
        super.onStart()
        expand()
        with(lifecycleScope) {
            jobs += subscribe(vm.views) { objectTypeAdapter.update(it) }
            jobs += subscribe(binding.searchObjectTypeInput.textChanges()) {
                vm.onQueryChanged(it.toString())
            }
            jobs += subscribe(vm.toasts) { toast -> toast(toast) }
            jobs += subscribe(vm.commands) { command ->
                when (command) {
                    is Command.DispatchType -> {
                        onItemClicked(
                            id = command.id,
                            key = command.key,
                            name = command.name
                        )
                    }
                    is Command.TypeAdded -> {
                        toast(getString(R.string.type_added, command.type))
                    }
                }
            }
        }
        startWithParams()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentObjectTypeChangeBinding = FragmentObjectTypeChangeBinding.inflate(
        inflater, container, false
    )

    companion object {
        const val ARG_EXCLUDE_TYPES = "arg.object-type-change.exclude-types"
        const val ARG_SELECTED_TYPES = "arg.object-type-change.selected-types"
        const val OBJECT_TYPE_URL_KEY = "object-type-url.key"
        const val OBJECT_TYPE_REQUEST_KEY = "object-type.request"
    }
}