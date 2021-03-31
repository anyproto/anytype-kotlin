package com.anytypeio.anytype.ui.sets

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.features.sets.CreateSetHeaderAdapter
import com.anytypeio.anytype.core_ui.features.sets.CreateSetObjectTypeAdapter
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.CreateObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.CreateObjectTypeView
import com.anytypeio.anytype.presentation.sets.CreateSetViewState
import com.anytypeio.anytype.ui.base.NavigationFragment
import kotlinx.android.synthetic.main.fragment_create_set.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

class CreateObjectSetFragment : NavigationFragment(R.layout.fragment_create_set),
    CreateObjectTypeCallback {

    private val ctx: String get() = argString(CONTEXT_ID_KEY)

    private val vm: CreateObjectSetViewModel by viewModels { factory }

    @Inject
    lateinit var factory: CreateObjectSetViewModel.Factory

    private val headerAdapter by lazy {
        CreateSetHeaderAdapter { vm.onCreateNewObjectType() }
    }

    private val typeAdapter by lazy {
        CreateSetObjectTypeAdapter { type -> vm.onObjectTypeSelected(type, ctx) }
    }

    private val createSetAdapter by lazy {
        ConcatAdapter(headerAdapter, typeAdapter)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.state.observe(viewLifecycleOwner) { observe(it) }
        recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = createSetAdapter
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm.objectTypeViews.onEach {
            Timber.d("Receiving new views: $it")
            typeAdapter.views = it
            typeAdapter.notifyDataSetChanged()
            createSetAdapter.notifyDataSetChanged()
        }.launchIn(lifecycleScope)
    }

    override fun onCreateObjectTypeClicked(type: CreateObjectTypeView, name: String) {
        vm.onCreateObjectTypeClicked(type, name)
        Timber.d("CreateObjectType:$type, $name")
    }

    override fun injectDependencies() {
        componentManager().createSetComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().createSetComponent.release()
    }

    private fun observe(state: CreateSetViewState) {
        when (state) {
            is CreateSetViewState.AddObjectType -> {
                CreateObjectTypeFragment.newInstance(state.data).show(childFragmentManager, null)
            }
        }
    }

    companion object {
        const val CONTEXT_ID_KEY = "arg.create_object_set.context"
    }
}

interface CreateObjectTypeCallback {
    fun onCreateObjectTypeClicked(type: CreateObjectTypeView, name: String)
}