package com.anytypeio.anytype.presentation.editor.editor.mention

import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.ext.stripPlaceholdersAndRecalculate
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random
import kotlin.system.measureNanoTime

class StripPlaceholdersPerformanceTest {

    private fun generateLargeTextWithPlaceholders(
        length: Int,
        placeholderProbability: Double
    ): String {
        // We'll randomly choose letters a-z plus \u200B if random < placeholderProbability
        val sb = StringBuilder(length)
        val rng = Random(System.currentTimeMillis())

        for (i in 0 until length) {
            val randVal = rng.nextDouble()
            if (randVal < placeholderProbability) {
                // Insert zero-width space
                sb.append('\u200B')
            } else {
                // Insert a random letter or space
                sb.append(('a'..'z').random(rng))
            }
        }

        return sb.toString()
    }

    private fun generateRandomMarks(
        textLength: Int,
        markCount: Int
    ): List<Markup.Mark> {
        // Just create some random Mark ranges
        // We'll ensure from < to
        val rng = Random(System.currentTimeMillis())
        return List(markCount) {
            val start = rng.nextInt(0, textLength - 1)
            val end = rng.nextInt(start + 1, textLength)
            Markup.Mark.Mention.WithEmoji(
                from = start,
                to = end,
                param = "id-$it",
                emoji = ":smiley:",
                isArchived = false
            )
        }
    }

    @Test
    fun testStripPlaceholdersPerformanceOn100000() {
        // 1) Generate a large string with random placeholders
        val largeTextSize = 100_000      // 100K characters
        val placeholderProbability = 0.05  // ~5% chance of zero-width space
        val largeText = generateLargeTextWithPlaceholders(largeTextSize, placeholderProbability)

        // 2) Generate ~50 marks
        val markCount = 50
        val marks = generateRandomMarks(largeText.length, markCount)

        // 3) Measure how long it takes to strip placeholders + recalc
        val nanoTime = measureNanoTime {
            val (cleanedText, cleanedMarks) = stripPlaceholdersAndRecalculate(largeText, marks)
            // Basic sanity checks
            assertTrue(cleanedText.length <= largeText.length)
            assertEquals(markCount, cleanedMarks.size)
        }

        val millis = nanoTime / 1_000_000.0
        println("stripPlaceholdersAndRecalculate took $millis ms for $largeTextSize chars and $markCount marks")
    }

    @Test
    fun testStripPlaceholdersPerformanceOn10000() {
        // 1) Generate a large string with random placeholders
        val largeTextSize = 10000      // 10K characters
        val placeholderProbability = 0.05  // ~5% chance of zero-width space
        val largeText = generateLargeTextWithPlaceholders(largeTextSize, placeholderProbability)

        // 2) Generate ~20 marks
        val markCount = 20
        val marks = generateRandomMarks(largeText.length, markCount)

        // 3) Measure how long it takes to strip placeholders + recalc
        val nanoTime = measureNanoTime {
            val (cleanedText, cleanedMarks) = stripPlaceholdersAndRecalculate(largeText, marks)
            // Basic sanity checks
            assertTrue(cleanedText.length <= largeText.length)
            assertEquals(markCount, cleanedMarks.size)
        }

        val millis = nanoTime / 1_000_000.0
        println("stripPlaceholdersAndRecalculate took $millis ms for $largeTextSize chars and $markCount marks")
    }

    @Test
    fun testStripPlaceholdersPerformanceOn1000() {
        // 1) Generate a large string with random placeholders
        val largeTextSize = 1_000      // 1K characters
        val placeholderProbability = 0.05  // ~5% chance of zero-width space
        val largeText = generateLargeTextWithPlaceholders(largeTextSize, placeholderProbability)

        // 2) Generate ~5 marks
        val markCount = 5
        val marks = generateRandomMarks(largeText.length, markCount)

        // 3) Measure how long it takes to strip placeholders + recalc
        val nanoTime = measureNanoTime {
            val (cleanedText, cleanedMarks) = stripPlaceholdersAndRecalculate(largeText, marks)
            // Basic sanity checks
            assertTrue(cleanedText.length <= largeText.length)
            assertEquals(markCount, cleanedMarks.size)
        }

        val millis = nanoTime / 1_000_000.0
        println("stripPlaceholdersAndRecalculate took $millis ms for $largeTextSize chars and $markCount marks")
    }
}