package com.agileburo.anytype.domain.block.model

/**
 * Represents block as basic data structure.
 * @property id block's id
 * @property children block's children ids
 * @property fields block's fields
 */
data class Block(
    val id: String,
    val children: List<String>,
    val fields: Fields
) {

    /**
     * Block fields containing useful block properties.
     * @property map map containing fields
     */
    data class Fields(val map: Map<String, Any?>) {
        val name: String by map
        val icon: String by map
    }

}