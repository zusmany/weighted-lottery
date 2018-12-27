package com.wl

import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.NoSuchElementException

class SimpleIntWeightedLotteryNoRepetitions(private val maxAttempts: Int = 5,
                                            private val weights: DoubleArray,
                                            private val hitRatioThreshold: Double = 0.7,
                                            private val random: () -> Random = { ThreadLocalRandom.current() }) : IntLottery {

    private var indexMapping = weights.mapIndexed { k, _ -> k }.toIntArray()
    private var delegate = SimpleIntWeightedLottery(maxAttempts, weights, random)
    private val selected = BooleanArray(weights.size)
    private var remaining = weights.size
    private var attempts = 0
    private var hits = 0.0

    override fun draw(): Int {
        attempts++
        val item = indexMapping[delegate.draw()]
        if (!selected[item]) {
            hits++
            selected[item] = true
            remaining--
            return item
        }
        if (hits / attempts < hitRatioThreshold) {
            // hit ratio dropped. better to reallocate the delegate

            val remaining = remaining()
            if (remaining == 0) {
                throw NoSuchElementException()
            }
            val nonSelectedWeights = DoubleArray(remaining)
            val nonSelectedIndexMapping = IntArray(remaining)
            var idx = 0
            (0 until selected.size).asSequence().filter { !selected[it] }.forEach {
                nonSelectedIndexMapping[idx] = it
                nonSelectedWeights[idx] = weights[it]
                ++idx
            }


            indexMapping = nonSelectedIndexMapping
            delegate = SimpleIntWeightedLottery(maxAttempts, nonSelectedWeights, random)
            hits = 0.0
            attempts = 0

        }
        return draw()
    }

    override fun empty() = remaining() == 0

    override fun remaining() = remaining
}
