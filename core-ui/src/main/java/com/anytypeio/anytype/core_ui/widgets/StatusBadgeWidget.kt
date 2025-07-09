package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncError
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncUpdate
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible


class StatusBadgeWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    private var animatedDrawable: AnimatedVectorDrawableCompat? =
        AnimatedVectorDrawableCompat.create(context, R.drawable.animated_pulsing_circle)

    fun bind(status: SpaceSyncAndP2PStatusState?) {
        when (status) {
            is SpaceSyncAndP2PStatusState.Error -> {
                visible()
                setImageResource(R.drawable.ic_sync_error_8)
            }
            SpaceSyncAndP2PStatusState.Init -> {
                gone()
            }
            is SpaceSyncAndP2PStatusState.Success -> {
                when (val spaceSyncUpdate = status.spaceSyncUpdate) {
                    is SpaceSyncUpdate.Update -> {
                        val error = spaceSyncUpdate.error
                        if (error != SpaceSyncError.NULL) {
                            visible()
                            setImageResource(R.drawable.ic_sync_error_8)
                        } else {
                            return when (spaceSyncUpdate.status) {
                                SpaceSyncStatus.SYNCED -> {
                                    visible()
                                    setImageResource(R.drawable.ic_synced_8)
                                }
                                SpaceSyncStatus.SYNCING -> {
                                    visible()
                                    setImageDrawable(animatedDrawable)
                                    animatedDrawable?.start()
                                    Unit
                                }
                                SpaceSyncStatus.ERROR -> {
                                    visible()
                                    setImageResource(R.drawable.ic_sync_error_8)
                                }
                                SpaceSyncStatus.OFFLINE -> {
                                    visible()
                                    setImageResource(R.drawable.ic_sync_grey_8)
                                }
                                SpaceSyncStatus.NETWORK_UPDATE_NEEDED -> {
                                    visible()
                                    setImageResource(R.drawable.ic_sync_slow_8)
                                }
                            }
                        }
                    }

                    SpaceSyncUpdate.Initial -> {
                        gone()
                    }
                }
            }

            null -> {
                gone()
            }
        }
    }
}