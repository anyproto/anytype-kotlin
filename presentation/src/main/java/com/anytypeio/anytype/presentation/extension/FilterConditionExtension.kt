package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.presentation.sets.filter.DVFilterConditionCategory
import com.anytypeio.anytype.presentation.sets.model.Viewer
import timber.log.Timber

fun Viewer.Filter.Condition.index(): Int = this.getConditions().indexOf(this)

fun Viewer.Filter.Condition.getConditions(): List<Viewer.Filter.Condition> = when (this) {
    is Viewer.Filter.Condition.Checkbox -> Viewer.Filter.Condition.Checkbox.checkboxConditions()
    is Viewer.Filter.Condition.Number -> Viewer.Filter.Condition.Number.numberConditions()
    is Viewer.Filter.Condition.Selected -> Viewer.Filter.Condition.Selected.selectConditions()
    is Viewer.Filter.Condition.Text -> Viewer.Filter.Condition.Text.textConditions()
}

fun Viewer.Filter.Condition.type(): Viewer.Filter.Type = when (this) {
    is Viewer.Filter.Condition.Checkbox -> Viewer.Filter.Type.CHECKBOX
    is Viewer.Filter.Condition.Number -> Viewer.Filter.Type.NUMBER
    is Viewer.Filter.Condition.Selected -> Viewer.Filter.Type.SELECTED
    is Viewer.Filter.Condition.Text -> Viewer.Filter.Type.TEXT
}

fun Relation.toConditionView(condition: DVFilterCondition?): Viewer.Filter.Condition =
    when (this.format) {
        Relation.Format.SHORT_TEXT,
        Relation.Format.LONG_TEXT,
        Relation.Format.URL,
        Relation.Format.EMAIL,
        Relation.Format.PHONE -> {
            condition?.toView(category = DVFilterConditionCategory.TEXT)
                ?: Viewer.Filter.Condition.Text.textConditions().first()
        }
        Relation.Format.NUMBER,
        Relation.Format.DATE -> {
            condition?.toView(category = DVFilterConditionCategory.NUMBER)
                ?: Viewer.Filter.Condition.Number.numberConditions().first()
        }
        Relation.Format.STATUS,
        Relation.Format.TAG,
        Relation.Format.OBJECT -> {
            condition?.toView(category = DVFilterConditionCategory.SELECT)
                ?: Viewer.Filter.Condition.Selected.selectConditions().first()
        }
        Relation.Format.CHECKBOX -> {
            condition?.toView(category = DVFilterConditionCategory.CHECKBOX)
                ?: Viewer.Filter.Condition.Checkbox.checkboxConditions().first()
        }
        else -> throw UnsupportedOperationException("Unsupported relation format:${format}")
    }

private fun DVFilterCondition.toView(
    category: DVFilterConditionCategory
): Viewer.Filter.Condition? = when (this) {
    DVFilterCondition.EQUAL -> {
        when (category) {
            DVFilterConditionCategory.TEXT -> Viewer.Filter.Condition.Text.Equal()
            DVFilterConditionCategory.NUMBER -> Viewer.Filter.Condition.Number.Equal()
            DVFilterConditionCategory.SELECT -> Viewer.Filter.Condition.Selected.Equal()
            DVFilterConditionCategory.CHECKBOX -> Viewer.Filter.Condition.Checkbox.Equal()
        }
    }
    DVFilterCondition.NOT_EQUAL -> {
        when (category) {
            DVFilterConditionCategory.TEXT -> Viewer.Filter.Condition.Text.NotEqual()
            DVFilterConditionCategory.NUMBER -> Viewer.Filter.Condition.Number.NotEqual()
            DVFilterConditionCategory.CHECKBOX -> Viewer.Filter.Condition.Checkbox.NotEqual()
            else -> throw IllegalArgumentException("Condition ${this.name} is not present in $category")
        }
    }
    DVFilterCondition.GREATER -> {
        when (category) {
            DVFilterConditionCategory.NUMBER -> Viewer.Filter.Condition.Number.Greater()
            else -> throw IllegalArgumentException("Condition ${this.name} is not present in $category")
        }
    }
    DVFilterCondition.LESS -> {
        when (category) {
            DVFilterConditionCategory.NUMBER -> Viewer.Filter.Condition.Number.Less()
            else -> throw IllegalArgumentException("Condition ${this.name} is not present in $category")
        }
    }
    DVFilterCondition.GREATER_OR_EQUAL -> {
        when (category) {
            DVFilterConditionCategory.NUMBER -> Viewer.Filter.Condition.Number.GreaterOrEqual()
            else -> throw IllegalArgumentException("Condition ${this.name} is not present in $category")
        }
    }
    DVFilterCondition.LESS_OR_EQUAL -> {
        when (category) {
            DVFilterConditionCategory.NUMBER -> Viewer.Filter.Condition.Number.LessOrEqual()
            else -> throw IllegalArgumentException("Condition ${this.name} is not present in $category")
        }
    }
    DVFilterCondition.LIKE -> {
        when (category) {
            DVFilterConditionCategory.TEXT -> Viewer.Filter.Condition.Text.Like()
            else -> throw IllegalArgumentException("Condition ${this.name} is not present in $category")
        }
    }
    DVFilterCondition.NOT_LIKE -> {
        when (category) {
            DVFilterConditionCategory.TEXT -> Viewer.Filter.Condition.Text.NotLike()
            else -> throw IllegalArgumentException("Condition ${this.name} is not present in $category")
        }
    }
    DVFilterCondition.IN -> {
        when (category) {
            DVFilterConditionCategory.SELECT -> Viewer.Filter.Condition.Selected.In()
            else -> throw IllegalArgumentException("Condition ${this.name} is not present in $category")
        }
    }
    DVFilterCondition.NOT_IN -> {
        when (category) {
            DVFilterConditionCategory.SELECT -> Viewer.Filter.Condition.Selected.NotIn()
            else -> throw IllegalArgumentException("Condition ${this.name} is not present in $category")
        }
    }
    DVFilterCondition.EMPTY -> {
        when (category) {
            DVFilterConditionCategory.TEXT -> Viewer.Filter.Condition.Text.Empty()
            DVFilterConditionCategory.SELECT -> Viewer.Filter.Condition.Selected.Empty()
            DVFilterConditionCategory.NUMBER -> Viewer.Filter.Condition.Number.Empty()
            else -> throw IllegalArgumentException("Condition ${this.name} is not present in $category")
        }
    }
    DVFilterCondition.NOT_EMPTY -> {
        when (category) {
            DVFilterConditionCategory.TEXT -> Viewer.Filter.Condition.Text.NotEmpty()
            DVFilterConditionCategory.SELECT -> Viewer.Filter.Condition.Selected.NotEmpty()
            DVFilterConditionCategory.NUMBER -> Viewer.Filter.Condition.Number.NotEmpty()
            else -> throw IllegalArgumentException("Condition ${this.name} is not present in $category")
        }
    }
    DVFilterCondition.ALL_IN -> {
        when (category) {
            DVFilterConditionCategory.SELECT -> Viewer.Filter.Condition.Selected.AllIn()
            else -> throw IllegalArgumentException("Condition ${this.name} is not present in $category")
        }
    }
    DVFilterCondition.NOT_ALL_IN -> {
        throw IllegalArgumentException("Condition ${this.name} is not present in $category")
    }
    DVFilterCondition.NONE -> {
        when (category) {
            DVFilterConditionCategory.TEXT -> Viewer.Filter.Condition.Text.None()
            DVFilterConditionCategory.NUMBER -> Viewer.Filter.Condition.Number.None()
            DVFilterConditionCategory.SELECT -> Viewer.Filter.Condition.Selected.None()
            DVFilterConditionCategory.CHECKBOX -> Viewer.Filter.Condition.Checkbox.None()
        }
    }
    DVFilterCondition.EXACT_IN -> {
        Timber.w("Unexpected filter condition: EXACT IN")
        null
    }
    DVFilterCondition.NOT_EXACT_IN -> {
        Timber.w("Unexpected filter condition: NOT EXACT IN")
        null
    }
}