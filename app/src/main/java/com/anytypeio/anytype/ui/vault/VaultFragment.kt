package com.anytypeio.anytype.ui.vault

import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager

class VaultFragment : BaseComposeFragment() {

    override fun injectDependencies() {
        componentManager().vaultComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().vaultComponent.release()
    }
}