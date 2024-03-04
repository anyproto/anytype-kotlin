package com.anytypeio.anytype.viewmodel

sealed class PaymentsState {
    object Loading : PaymentsState()
    object Error : PaymentsState()
    object Success : PaymentsState()
}