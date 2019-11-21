package com.agileburo.anytype.presentation.desktop

sealed class DashboardView {
    data class Document(val id: String, val title: String) : DashboardView()
}