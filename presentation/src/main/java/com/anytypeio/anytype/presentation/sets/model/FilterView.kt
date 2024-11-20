package com.anytypeio.anytype.presentation.sets.model

import com.anytypeio.anytype.core_models.DVFilterQuickOption
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.RelativeDate
import com.anytypeio.anytype.core_utils.ui.ViewType

sealed class FilterView : ViewType {

    sealed class Expression : FilterView() {
        abstract val id: Id
        abstract val relation: Key
        abstract val title: String
        abstract val operator: Viewer.FilterOperator
        abstract val condition: Viewer.Filter.Condition
        open val quickOption: DVFilterQuickOption = DVFilterQuickOption.EXACT_DATE
        abstract val filterValue: FilterValue
        abstract val format: ColumnView.Format
        abstract val isValueRequired: Boolean
        abstract val isInEditMode:Boolean

        data class Text(
            override val id: Id,
            override val relation: Key,
            override val title: String,
            override val operator: Viewer.FilterOperator,
            override val condition: Viewer.Filter.Condition.Text,
            override val filterValue: FilterValue.Text,
            override val format: ColumnView.Format,
            override val isValueRequired: Boolean,
            override val isInEditMode: Boolean
        ) : Expression(), ViewType {
            override fun getViewType(): Int = HOLDER_TEXT
        }

        data class TextShort(
            override val id: Id,
            override val relation: Key,
            override val title: String,
            override val operator: Viewer.FilterOperator,
            override val condition: Viewer.Filter.Condition.Text,
            override val filterValue: FilterValue.TextShort,
            override val format: ColumnView.Format,
            override val isValueRequired: Boolean,
            override val isInEditMode: Boolean
        ) : Expression(), ViewType {
            override fun getViewType(): Int = HOLDER_TEXT_SHORT
        }

        data class Url(
            override val id: Id,
            override val relation: Key,
            override val title: String,
            override val operator: Viewer.FilterOperator,
            override val condition: Viewer.Filter.Condition.Text,
            override val filterValue: FilterValue.Url,
            override val format: ColumnView.Format,
            override val isValueRequired: Boolean,
            override val isInEditMode: Boolean
        ) : Expression(), ViewType {
            override fun getViewType(): Int = HOLDER_URL
        }

        data class Email(
            override val id: Id,
            override val relation: Key,
            override val title: String,
            override val operator: Viewer.FilterOperator,
            override val condition: Viewer.Filter.Condition.Text,
            override val filterValue: FilterValue.Email,
            override val format: ColumnView.Format,
            override val isValueRequired: Boolean,
            override val isInEditMode: Boolean
        ) : Expression(), ViewType {
            override fun getViewType(): Int = HOLDER_EMAIL
        }

        data class Phone(
            override val id: Id,
            override val relation: Key,
            override val title: String,
            override val operator: Viewer.FilterOperator,
            override val condition: Viewer.Filter.Condition.Text,
            override val filterValue: FilterValue.Phone,
            override val format: ColumnView.Format,
            override val isValueRequired: Boolean,
            override val isInEditMode: Boolean
        ) : Expression(), ViewType {
            override fun getViewType(): Int = HOLDER_PHONE
        }

        data class Number(
            override val id: Id,
            override val relation: Key,
            override val title: String,
            override val operator: Viewer.FilterOperator,
            override val condition: Viewer.Filter.Condition.Number,
            override val filterValue: FilterValue.Number,
            override val format: ColumnView.Format,
            override val isValueRequired: Boolean,
            override val isInEditMode: Boolean
        ) : Expression(), ViewType {
            override fun getViewType(): Int = HOLDER_NUMBER
        }

        data class Status(
            override val id: Id,
            override val relation: Key,
            override val title: String,
            override val operator: Viewer.FilterOperator,
            override val condition: Viewer.Filter.Condition.Selected,
            override val filterValue: FilterValue.Status,
            override val format: ColumnView.Format,
            override val isValueRequired: Boolean,
            override val isInEditMode: Boolean
        ) : Expression(), ViewType {
            override fun getViewType(): Int = HOLDER_STATUS
        }

        data class Tag(
            override val id: Id,
            override val relation: Key,
            override val title: String,
            override val operator: Viewer.FilterOperator,
            override val condition: Viewer.Filter.Condition.Selected,
            override val filterValue: FilterValue.Tag,
            override val format: ColumnView.Format,
            override val isValueRequired: Boolean,
            override val isInEditMode: Boolean
        ) : Expression(), ViewType {
            override fun getViewType(): Int = HOLDER_TAG
        }

        data class Date(
            override val id: Id,
            override val relation: Key,
            override val title: String,
            override val operator: Viewer.FilterOperator,
            override val condition: Viewer.Filter.Condition.Date,
            override val quickOption: DVFilterQuickOption,
            override val filterValue: FilterValue.Date,
            override val format: ColumnView.Format,
            override val isValueRequired: Boolean,
            override val isInEditMode: Boolean,
            val relativeDate: RelativeDate?
        ) : Expression(), ViewType {
            override fun getViewType(): Int = HOLDER_DATE
        }

        data class Object(
            override val id: Id,
            override val relation: Key,
            override val title: String,
            override val operator: Viewer.FilterOperator,
            override val condition: Viewer.Filter.Condition.Selected,
            override val filterValue: FilterValue.Object,
            override val format: ColumnView.Format,
            override val isValueRequired: Boolean,
            override val isInEditMode: Boolean
        ) : Expression(), ViewType {
            override fun getViewType(): Int = HOLDER_OBJECT
        }

        data class Checkbox(
            override val id: Id,
            override val relation: Key,
            override val title: String,
            override val operator: Viewer.FilterOperator,
            override val condition: Viewer.Filter.Condition.Checkbox,
            override val filterValue: FilterValue.Check,
            override val format: ColumnView.Format,
            override val isValueRequired: Boolean,
            override val isInEditMode: Boolean
        ) : Expression(), ViewType {
            override fun getViewType(): Int = HOLDER_CHECKBOX
        }
    }

    companion object {
        const val HOLDER_TEXT = 1
        const val HOLDER_TEXT_SHORT = 2
        const val HOLDER_URL = 3
        const val HOLDER_EMAIL = 4
        const val HOLDER_PHONE = 5
        const val HOLDER_NUMBER = 6
        const val HOLDER_DATE = 7
        const val HOLDER_STATUS = 8
        const val HOLDER_TAG = 9
        const val HOLDER_OBJECT = 10
        const val HOLDER_CHECKBOX = 12
    }
}