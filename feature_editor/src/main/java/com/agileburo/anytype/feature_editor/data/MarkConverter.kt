package com.agileburo.anytype.feature_editor.data

import android.net.MacAddress
import com.agileburo.anytype.feature_editor.domain.Mark

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 03.04.2019.
 */
interface MarkConverter {

    fun modelToDomain(model: MarkModel): Mark
    fun domainToModel(domain: Mark): MarkModel
}

class MarkConverterImpl : MarkConverter {

    override fun modelToDomain(model: MarkModel): Mark =
        Mark(type = getType(model.type),
            start = model.start,
            end = model.end,
            param = model.param)

    override fun domainToModel(domain: Mark): MarkModel {
        throw UnsupportedOperationException("not implemented")
    }

    private fun getType(type: String) =
        when (type) {
            "b" -> Mark.MarkType.BOLD
            "i" -> Mark.MarkType.ITALIC
            "s" -> Mark.MarkType.STRIKE_THROUGH
            "kbd" -> Mark.MarkType.CODE
            "a" -> Mark.MarkType.HYPERTEXT
            else -> Mark.MarkType.UNDEFINED
        }
}