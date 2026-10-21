package com.anytypeio.anytype.features.sets.dv

import com.anytypeio.anytype.presentation.sets.ObjectSetViewModelFactory
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import com.anytypeio.anytype.feature_create_object.presentation.CreateObjectViewModelFactory

class TestObjectSetFragment : ObjectSetFragment() {
    init {
        factory = testVmFactory
    }

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        // FragmentScenario's EmptyFragmentActivity omits the production MainActivity policy.
        requireActivity().window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun injectDependencies() {
        // The real fragment subscribes to creation upload events onStart, even when this
        // rendering fixture never opens the creation sheet. Supply its isolated factory.
        ObjectSetFragment::class.java.getDeclaredField("createObjectFactory").apply {
            isAccessible = true
        }.set(this, testCreateObjectFactory)
    }
    override fun releaseDependencies() {}

    companion object {
        lateinit var testVmFactory: ObjectSetViewModelFactory
        lateinit var testCreateObjectFactory: CreateObjectViewModelFactory
    }
}
