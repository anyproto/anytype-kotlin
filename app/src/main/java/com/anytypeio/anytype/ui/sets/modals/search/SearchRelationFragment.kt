package com.anytypeio.anytype.ui.sets.modals.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.sets.SearchRelationAdapter
import com.anytypeio.anytype.core_ui.reactive.textChanges
import com.anytypeio.anytype.core_utils.ext.drawable
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetTextInputFragment
import com.anytypeio.anytype.databinding.FragmentSelectSortOrFilterRelationBinding
import com.anytypeio.anytype.presentation.sets.SearchRelationViewModel
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView

abstract class SearchRelationFragment :
    BaseBottomSheetTextInputFragment<FragmentSelectSortOrFilterRelationBinding>() {

    abstract val ctx: String
    abstract val vm: SearchRelationViewModel

    private val searchRelationAdapter by lazy {
        SearchRelationAdapter { relation -> onRelationClicked(ctx = ctx, relation = relation) }
    }

    override val textInput: EditText get() = binding.searchBar.root.findViewById(R.id.filterInputField)

    lateinit var searchRelationInput: EditText
    lateinit var clearSearchText: View

    abstract fun onRelationClicked(ctx: Id, relation: SimpleRelationView)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchRelationInput = binding.searchBar.root.findViewById(R.id.filterInputField)
        searchRelationInput.apply {
            hint = getString(R.string.choose_relation_to_filter)
        }
        clearSearchText = binding.searchBar.root.findViewById(R.id.clearSearchText)
        clearSearchText.setOnClickListener {
            searchRelationInput.setText("")
            clearSearchText.invisible()

        }
        binding.searchRelationRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchRelationAdapter
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                    setDrawable(drawable(R.drawable.divider_sort_or_filter_relation))
                }
            )
        }
        with(lifecycleScope) {
            subscribe(searchRelationInput.textChanges()) {
                vm.onSearchQueryChanged(it.toString())
                if (it.isEmpty()) {
                    clearSearchText.invisible()
                } else {
                    clearSearchText.visible()
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(lifecycleScope) {
            subscribe(vm.views) { searchRelationAdapter.update(it) }
            subscribe(vm.isDismissed) { isDismissed -> if (isDismissed) dismiss() }
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSelectSortOrFilterRelationBinding =
        FragmentSelectSortOrFilterRelationBinding.inflate(
            inflater, container, false
        )
}