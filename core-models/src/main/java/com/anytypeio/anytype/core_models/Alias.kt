package com.anytypeio.anytype.core_models

typealias Id = String
typealias Url = String
typealias Hash = String

typealias Document = List<Block>

typealias TextStyle = Block.Content.Text.Style
typealias CBTextStyle = Block.Content.Text.Style

typealias DV = Block.Content.DataView
typealias DVViewer = Block.Content.DataView.Viewer
typealias DVViewerType = Block.Content.DataView.Viewer.Type
typealias DVFilter = Block.Content.DataView.Filter
typealias DVFilterCondition = Block.Content.DataView.Filter.Condition
typealias DVFilterConditionType = Block.Content.DataView.Filter.ConditionType
typealias DVFilterOperator = Block.Content.DataView.Filter.Operator
typealias DVSort = Block.Content.DataView.Sort
typealias DVSortType = Block.Content.DataView.Sort.Type
typealias DVDateFormat = Block.Content.DataView.DateFormat
typealias DVTimeFormat = Block.Content.DataView.TimeFormat
typealias DVViewerRelation = Block.Content.DataView.Viewer.ViewerRelation

typealias DVRecord = Map<String, Any?>