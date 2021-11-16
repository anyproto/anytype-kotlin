package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVFilterConditionType
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.presentation.sets.model.Viewer

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
            condition?.toView(conditionType = DVFilterConditionType.TEXT)
                ?: Viewer.Filter.Condition.Text.textConditions().first()
        }
        Relation.Format.NUMBER,
        Relation.Format.DATE -> {
            condition?.toView(conditionType = DVFilterConditionType.NUMBER)
                ?: Viewer.Filter.Condition.Number.numberConditions().first()
        }
        Relation.Format.STATUS,
        Relation.Format.TAG,
        Relation.Format.OBJECT -> {
            condition?.toView(conditionType = DVFilterConditionType.SELECT)
                ?: Viewer.Filter.Condition.Selected.selectConditions().first()
        }
        Relation.Format.CHECKBOX -> {
            condition?.toView(conditionType = DVFilterConditionType.CHECKBOX)
                ?: Viewer.Filter.Condition.Checkbox.checkboxConditions().first()
        }
        else -> throw UnsupportedOperationException("Unsupported relation format:${format}")
    }

private fun DVFilterCondition.toView(
    conditionType: DVFilterConditionType
): Viewer.Filter.Condition = when (this) {
    DVFilterCondition.EQUAL -> {
        when (conditionType) {
            DVFilterConditionType.TEXT -> Viewer.Filter.Condition.Text.Equal()
            DVFilterConditionType.NUMBER -> Viewer.Filter.Condition.Number.Equal()
            DVFilterConditionType.SELECT -> Viewer.Filter.Condition.Selected.Equal()
            DVFilterConditionType.CHECKBOX -> Viewer.Filter.Condition.Checkbox.Equal()
        }
    }
    DVFilterCondition.NOT_EQUAL -> {
        when (conditionType) {
            DVFilterConditionType.TEXT -> Viewer.Filter.Condition.Text.NotEqual()
            DVFilterConditionType.NUMBER -> Viewer.Filter.Condition.Number.NotEqual()
            DVFilterConditionType.CHECKBOX -> Viewer.Filter.Condition.Checkbox.NotEqual()
            else -> throw IllegalArgumentException("Condition ${this.name} is not present in $conditionType")
        }
    }
    DVFilterCondition.GREATER -> {
        when (conditionType) {
            DVFilterConditionType.NUMBER -> Viewer.Filter.Condition.Number.Greater()
            else -> throw IllegalArgumentException("Condition ${this.name} is not present in $conditionType")
        }
    }
    DVFilterCondition.LESS -> {
        when (conditionType) {
            DVFilterConditionType.NUMBER -> Viewer.Filter.Condition.Number.Less()
            else -> throw IllegalArgumentException("Condition ${this.name} is not present in $conditionType")
        }
    }
    DVFilterCondition.GREATER_OR_EQUAL -> {
        when (conditionType) {
            DVFilterConditionType.NUMBER -> Viewer.Filter.Condition.Number.GreaterOrEqual()
            else -> throw IllegalArgumentException("Condition ${this.name} is not present in $conditionType")
        }
    }
    DVFilterCondition.LESS_OR_EQUAL -> {
        when (conditionType) {
            DVFilterConditionType.NUMBER -> Viewer.Filter.Condition.Number.LessOrEqual()
            else -> throw IllegalArgumentException("Condition ${this.name} is not present in $conditionType")
        }
    }
    DVFilterCondition.LIKE -> {
        when (conditionType) {
            DVFilterConditionType.TEXT -> Viewer.Filter.Condition.Text.Like()
            else -> throw IllegalArgumentException("Condition ${this.name} is not present in $conditionType")
        }
    }
    DVFilterCondition.NOT_LIKE -> {
        when (conditionType) {
            DVFilterConditionType.TEXT -> Viewer.Filter.Condition.Text.NotLike()
            else -> throw IllegalArgumentException("Condition ${this.name} is not present in $conditionType")
        }
    }
    DVFilterCondition.IN -> {
        when (conditionType) {
            DVFilterConditionType.SELECT -> Viewer.Filter.Condition.Selected.In()
            else -> throw IllegalArgumentException("Condition ${this.name} is not present in $conditionType")
        }
    }
    DVFilterCondition.NOT_IN -> {
        when (conditionType) {
            DVFilterConditionType.SELECT -> Viewer.Filter.Condition.Selected.NotIn()
            else -> throw IllegalArgumentException("Condition ${this.name} is not present in $conditionType")
        }
    }
    DVFilterCondition.EMPTY -> {
        when (conditionType) {
            DVFilterConditionType.TEXT -> Viewer.Filter.Condition.Text.Empty()
            DVFilterConditionType.SELECT -> Viewer.Filter.Condition.Selected.Empty()
            else -> throw IllegalArgumentException("Condition ${this.name} is not present in $conditionType")
        }
    }
    DVFilterCondition.NOT_EMPTY -> {
        when (conditionType) {
            DVFilterConditionType.TEXT -> Viewer.Filter.Condition.Text.NotEmpty()
            DVFilterConditionType.SELECT -> Viewer.Filter.Condition.Selected.NotEmpty()
            else -> throw IllegalArgumentException("Condition ${this.name} is not present in $conditionType")
        }
    }
    DVFilterCondition.ALL_IN -> {
        when (conditionType) {
            DVFilterConditionType.SELECT -> Viewer.Filter.Condition.Selected.AllIn()
            else -> throw IllegalArgumentException("Condition ${this.name} is not present in $conditionType")
        }
    }
    DVFilterCondition.NOT_ALL_IN -> {
        throw IllegalArgumentException("Condition ${this.name} is not present in $conditionType")
    }
    DVFilterCondition.NONE -> {
        when (conditionType) {
            DVFilterConditionType.TEXT -> Viewer.Filter.Condition.Text.None()
            DVFilterConditionType.NUMBER -> Viewer.Filter.Condition.Number.None()
            DVFilterConditionType.SELECT -> Viewer.Filter.Condition.Selected.None()
            DVFilterConditionType.CHECKBOX -> Viewer.Filter.Condition.Checkbox.None()
        }
    }
}