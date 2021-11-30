package com.anytypeio.anytype.presentation.auth.pin

import com.anytypeio.anytype.core_utils.ui.ViewStateViewModel
import com.anytypeio.anytype.presentation.common.ViewState

class EnterPinCodeViewModel : ViewStateViewModel<ViewState<Boolean>>() {

//    private val reducer = BiFunction<PinCodeState, PinCodeAction, PinCodeState> { prev, action ->
//        when (action) {
//            is PinCodeAction.AddDigit -> {
//                if (prev.completed)
//                    prev.copy()
//                else {
//                    prev.copy(
//                        digits = mutableListOf<Int>().apply {
//                            addAll(prev.digits)
//                            add(action.digit)
//                        }
//                    )
//                }
//            }
//            is PinCodeAction.Removed -> {
//                if (prev.digits.isNotEmpty())
//                    prev.copy(digits = prev.digits.dropLast(1))
//                else
//                    prev.copy()
//            }
//        }
//    }

    //private val actions = PublishRelay.create<PinCodeAction>()

//    val pin: Observable<PinCodeState> = actions
//        .scan<PinCodeState>(
//            PinCodeState(digits = emptyList()),
//            reducer
//        )

    init {
        //pin.map { state -> ViewState.Success(state.completed) }
        //.subscribe(state)
        //.disposedBy(subscriptions)
    }

    fun onNumPadClicked(entry: String) {
        //actions.accept(PinCodeAction.AddDigit(entry.toInt()))
    }

    fun onRemovedDigitClicked() {
        //actions.accept(PinCodeAction.Removed)
    }

    sealed class PinCodeAction {
        data class AddDigit(val digit: Int) : PinCodeAction()
        object Removed : PinCodeAction()
    }

}