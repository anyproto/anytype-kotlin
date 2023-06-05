package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.ext.process
import com.anytypeio.anytype.core_utils.ext.replace
import com.anytypeio.anytype.domain.objects.ObjectWatcher
import javax.inject.Inject
import timber.log.Timber

class DefaultObjectViewReducer @Inject constructor() : ObjectWatcher.Reducer {
    override fun invoke(given: ObjectView, events: List<Event>): ObjectView {
        var curr = given
        events.forEach { e ->
            when (e) {
                is Event.Command.AddBlock -> {
                    curr = curr.copy(blocks = curr.blocks + e.blocks)
                }
                is Event.Command.DeleteBlock -> {
                    curr = curr.copy(
                        blocks = curr.blocks.filter { !e.targets.contains(it.id) }
                    )
                }
                is Event.Command.UpdateStructure -> {
                    curr = curr.copy(
                        blocks = curr.blocks.replace(
                            replacement = { target ->
                                target.copy(children = e.children)
                            },
                            target = { block -> block.id == e.id }
                        )
                    )
                }
                is Event.Command.Details -> {
                    curr = curr.copy(details = curr.details.process(e))
                }
                else -> {
                    Timber.d("Skipping event: $e")
                }
            }
        }
        return curr
    }
}