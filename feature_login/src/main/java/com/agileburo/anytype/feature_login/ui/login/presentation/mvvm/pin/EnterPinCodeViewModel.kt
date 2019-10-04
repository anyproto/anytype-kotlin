package com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.pin

import com.agileburo.anytype.core_utils.ext.disposedBy
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.congratulation.ViewState
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.common.ViewStateViewModel
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

class EnterPinCodeViewModel : ViewStateViewModel<ViewState<Boolean>>() {

    private val reducer = BiFunction<PinCodeState, PinCodeAction, PinCodeState> { prev, action ->
        when (action) {
            is PinCodeAction.AddDigit -> {
                if (prev.completed)
                    prev.copy()
                else {
                    prev.copy(
                        digits = mutableListOf<Int>().apply {
                            addAll(prev.digits)
                            add(action.digit)
                        }
                    )
                }
            }
            is PinCodeAction.Removed -> {
                if (prev.digits.isNotEmpty())
                    prev.copy(digits = prev.digits.dropLast(1))
                else
                    prev.copy()
            }
        }
    }

    private val actions = PublishRelay.create<PinCodeAction>()

    val pin: Observable<PinCodeState> = actions
        .scan<PinCodeState>(
            PinCodeState(digits = emptyList()),
            reducer
        )

    init {
        pin.map { state -> ViewState.Success(state.completed) }
            .subscribe(state)
            .disposedBy(subscriptions)
    }

    fun onNumPadClicked(entry: String) {
        actions.accept(PinCodeAction.AddDigit(entry.toInt()))
    }

    fun onRemovedDigitClicked() {
        actions.accept(PinCodeAction.Removed)
    }

    sealed class PinCodeAction {
        data class AddDigit(val digit: Int) : PinCodeAction()
        object Removed : PinCodeAction()
    }

}