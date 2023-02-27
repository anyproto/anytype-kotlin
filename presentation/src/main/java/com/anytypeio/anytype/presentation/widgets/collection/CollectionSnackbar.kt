package com.anytypeio.anytype.presentation.widgets.collection

data class CollectionSnackbar(
    val message: String,
    val action: () -> Unit = { }
)