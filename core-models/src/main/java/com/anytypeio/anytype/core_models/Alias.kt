package com.anytypeio.anytype.core_models

typealias Id = String
typealias Key = String
typealias Url = String
typealias Hash = String
typealias Struct = Map<Id, Any?>

typealias Document = List<Block>

typealias TextBlock = Block.Content.Text
typealias TextStyle = Block.Content.Text.Style
typealias CBTextStyle = Block.Content.Text.Style

typealias DV = Block.Content.DataView
typealias DVViewer = Block.Content.DataView.Viewer
typealias DVViewerType = Block.Content.DataView.Viewer.Type
typealias DVViewerCardSize= Block.Content.DataView.Viewer.Size
typealias DVFilter = Block.Content.DataView.Filter
typealias DVFilterCondition = Block.Content.DataView.Filter.Condition
typealias Condition = Block.Content.DataView.Filter.Condition
typealias DVFilterQuickOption = Block.Content.DataView.Filter.QuickOption
typealias DVFilterOperator = Block.Content.DataView.Filter.Operator
typealias DVSort = Block.Content.DataView.Sort
typealias DVSortType = Block.Content.DataView.Sort.Type
typealias DVDateFormat = Block.Content.DataView.DateFormat
typealias DVTimeFormat = Block.Content.DataView.TimeFormat
typealias DVViewerRelation = Block.Content.DataView.Viewer.ViewerRelation

typealias RelationFormat = Relation.Format

typealias DVRecord = Map<String, Any?>