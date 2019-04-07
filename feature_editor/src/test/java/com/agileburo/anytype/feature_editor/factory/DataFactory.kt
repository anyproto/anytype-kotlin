package com.agileburo.anytype.feature_editor.factory

import java.util.*
import java.util.concurrent.ThreadLocalRandom

object DataFactory {

    fun randomUuid(): String {
        return UUID.randomUUID().toString()
    }

    fun randomString(): String {
        return randomUuid()
    }

    fun randomInt(): Int {
        return ThreadLocalRandom.current().nextInt(0, 1000 + 1)
    }

    fun randomInt(min : Int = 0, max: Int): Int {
        return ThreadLocalRandom.current().nextInt(min, max)
    }

    fun randomLong(): Long {
        return randomInt().toLong()
    }

    fun randomFloat(): Float {
        return randomInt().toFloat()
    }

    fun randomDouble(): Double {
        return randomInt().toDouble()
    }

    fun randomBoolean(): Boolean {
        return Math.random() < 0.5
    }

    fun makeStringList(count: Int): List<String> {
        val items = mutableListOf<String>()
        repeat(count) {
            items.add(randomUuid())
        }
        return items
    }

}