package com.agileburo.anytype.presentation.page.toggle

import com.agileburo.anytype.domain.common.Id

interface ToggleStateHolder {

    fun isToggled(target: Id): Boolean
    fun onToggleChanged(target: Id)

    class Default : ToggleStateHolder {

        private val memory = mutableMapOf<Id, Boolean>()

        override fun isToggled(target: Id) = memory[target] ?: false

        override fun onToggleChanged(target: Id) {
            val value = memory[target]
            if (value != null)
                memory[target] = !value
            else
                memory[target] = true
        }
    }
}