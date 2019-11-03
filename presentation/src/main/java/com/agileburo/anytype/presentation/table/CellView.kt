package com.agileburo.anytype.presentation.table

import java.util.*

sealed class CellView

data class CellNameView(val name: String, val surname: String): CellView()
data class CellDateView(val date: Date): CellView()