package com.anytypeio.anytype.presentation.editor.toggle

import com.anytypeio.anytype.core_models.Id
import javax.inject.Inject

interface ToggleStateHolder {

    fun isToggled(target: Id): Boolean
    fun onToggleChanged(target: Id)

    class Default @Inject constructor() : ToggleStateHolder {

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