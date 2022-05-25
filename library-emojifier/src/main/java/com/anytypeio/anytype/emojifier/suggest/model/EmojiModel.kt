package com.anytypeio.anytype.emojifier.suggest.model


import com.google.gson.annotations.SerializedName

data class EmojiModel(
    @SerializedName("category")
    override val category: String,
    @SerializedName("char")
    override val emoji: String,
    @SerializedName("name")
    override val name: String
) : EmojiSuggest