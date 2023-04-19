package com.anytypeio.anytype.ui.auth.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.FloatTweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.auth.account.DeletedAccountViewModel
import com.anytypeio.anytype.presentation.auth.account.DeletedAccountViewModel.DeletionDate
import com.anytypeio.anytype.ui.auth.account.DeletedAccountFragment.Companion.DISPLAYED_PROGRESS_MAX_VALUE
import com.anytypeio.anytype.ui.auth.account.DeletedAccountFragment.Companion.DISPLAYED_PROGRESS_MIN_VALUE
import com.anytypeio.anytype.ui.settings.typography
import com.anytypeio.anytype.ui_settings.account.Action
import com.anytypeio.anytype.ui_settings.account.ActionWithProgressBar
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DeletedAccountFragment : BaseComposeFragment() {

    private val deadline: Long get() = arg(DEADLINE_KEY)

    @Inject
    lateinit var factory: DeletedAccountViewModel.Factory
    private val vm by viewModels<DeletedAccountViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DeletedAccountScreen(
                    progress = vm.progress.collectAsState().value,
                    date = vm.date.collectAsState().value,
                    onLogoutAndClearDataClicked = { vm.onLogoutAndClearDataClicked() },
                    onCancelDeletionClicked = { vm.cancelDeletionClicked() },
                    isLoggingOutInProgress = vm.isLoggingOut.collectAsState().value
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.toasts.collect { toast(it) }
                }
                launch {
                    vm.commands.collect { command ->
                        when (command) {
                            DeletedAccountViewModel.Command.Resume -> {
                                findNavController().apply {
                                    popBackStack()
                                    navigate(R.id.main_navigation)
                                }
                            }
                            DeletedAccountViewModel.Command.Logout -> {
                                findNavController().navigate(R.id.main_navigation)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        vm.onStart(
            nowInMillis = System.currentTimeMillis(),
            deadlineInMillis = TimeUnit.SECONDS.toMillis(deadline)
        )
    }

    override fun injectDependencies() {
        componentManager().deletedAccountComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().deletedAccountComponent.release()
    }

    companion object {
        const val DEADLINE_KEY = "arg.deleted-account.date"
        const val DISPLAYED_PROGRESS_MIN_VALUE = 0.10f
        const val DISPLAYED_PROGRESS_MAX_VALUE = 0.90f
    }
}

@Composable
fun DeletedAccountScreen(
    progress: Float,
    date: DeletionDate,
    onCancelDeletionClicked: () -> Unit,
    onLogoutAndClearDataClicked: () -> Unit,
    isLoggingOutInProgress: Boolean
) {
    MaterialTheme(typography = typography) {
        Box(contentAlignment = Alignment.BottomCenter) {
            Card(
                modifier = Modifier.padding(
                    start = 10.dp,
                    end = 10.dp,
                    bottom = 16.dp
                ),
                shape = RoundedCornerShape(16.dp),
                backgroundColor = colorResource(R.color.background_secondary)
            ) {
                Column {
                    Chart(
                        chartColor = colorResource(R.color.palette_system_red),
                        actualProgress = progress
                    )
                    Text(
                        text = when (date) {
                            is DeletionDate.Later -> {
                                stringResource(
                                    R.string.planned_for_deletion_in_n_days,
                                    date.days
                                )
                            }
                            DeletionDate.Today -> {
                                stringResource(
                                    R.string.planned_for_deletion_today
                                )
                            }
                            DeletionDate.Tomorrow -> {
                                stringResource(
                                    R.string.planned_for_deletion_tomorrow
                                )
                            }
                            DeletionDate.Unknown -> {
                                stringResource(
                                    R.string.planned_for_deletion_unknown
                                )
                            }
                            DeletionDate.Deleted -> {
                                stringResource(
                                    R.string.your_account_deleted
                                )
                            }
                        },
                        color = colorResource(R.color.text_primary),
                        style = HeadlineHeading,
                        modifier = Modifier.padding(
                            start = 20.dp,
                            end = 20.dp
                        )
                    )
                    Text(
                        text = stringResource(R.string.deleted_account_msg),
                        color = colorResource(R.color.text_primary),
                        style = BodyCalloutRegular,
                        modifier = Modifier.padding(
                            top = 12.dp,
                            start = 20.dp,
                            end = 20.dp,
                            bottom = 14.dp
                        )
                    )
                    if (date != DeletionDate.Deleted) {
                        Action(
                            name = stringResource(R.string.cancel_deletion),
                            color = colorResource(R.color.palette_dark_red),
                            onClick = onCancelDeletionClicked
                        )
                        Divider()
                    }
                    ActionWithProgressBar(
                        name = stringResource(R.string.logout_and_clear_local_data),
                        color = colorResource(R.color.palette_dark_red),
                        onClick = onLogoutAndClearDataClicked,
                        isInProgress = isLoggingOutInProgress
                    )
                    Divider()
                    Spacer(Modifier.height(22.dp))
                }
            }
        }
    }
}

@Composable
fun Chart(
    chartColor: Color,
    actualProgress: Float = 0.0f
) {
    val displayedProgress = when {
        actualProgress < DISPLAYED_PROGRESS_MIN_VALUE -> DISPLAYED_PROGRESS_MIN_VALUE
        actualProgress > DISPLAYED_PROGRESS_MAX_VALUE -> DISPLAYED_PROGRESS_MAX_VALUE
        else -> actualProgress
    }
    val sweepAngleValueAnimation by animateFloatAsState(
        targetValue = 360f * displayedProgress,
        animationSpec = FloatTweenSpec(duration = 300)
    )
    Box(
        modifier = Modifier
            .padding(
                start = 20.dp,
                top = 20.dp,
                bottom = 20.dp
            )
            .height(52.dp)
            .width(52.dp)
            .border(
                shape = CircleShape,
                width = 1.5.dp,
                color = colorResource(R.color.shape_primary)
            )
    ) {
        Canvas(
            modifier = Modifier
                .align(Alignment.Center)
                .height(36.dp)
                .width(36.dp)
        ) {
            drawArc(
                startAngle = -90f,
                sweepAngle = sweepAngleValueAnimation,
                color = chartColor,
                useCenter = true
            )
        }
    }
}