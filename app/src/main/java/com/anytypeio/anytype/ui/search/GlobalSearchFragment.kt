package com.anytypeio.anytype.ui.search

import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager

class GlobalSearchFragment : BaseBottomSheetComposeFragment() {

    override fun injectDependencies() {
        componentManager().globalSearchComponent.get().inject(this)
    }
    override fun releaseDependencies() {
        componentManager().globalSearchComponent.release()
    }
}