package com.anytypeio.anytype.data

import java.util.*
import java.util.concurrent.ThreadLocalRandom

object MockDataFactory {

    fun randomUuid(): String {
        return UUID.randomUUID().toString()
    }

    fun randomString(): String {
        return randomUuid()
    }


    fun randomInt(): Int {
        return ThreadLocalRandom.current().nextInt(0, 1000 + 1)
    }

    fun randomInt(max: Int): Int {
        return ThreadLocalRandom.current().nextInt(0, max)
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

    fun makeIntList(count: Int): List<Int> {
        val items = mutableListOf<Int>()
        repeat(count) {
            items.add(randomInt())
        }
        return items
    }

    fun makeStringList(count: Int): List<String> {
        val items = mutableListOf<String>()
        repeat(count) {
            items.add(randomUuid())
        }
        return items
    }

    fun makeDoubleList(count: Int): List<Double> {
        val items = mutableListOf<Double>()
        repeat(count) {
            items.add(randomDouble())
        }
        return items
    }
}