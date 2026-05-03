package com.example.lab2project3_2kislov

import kotlin.random.Random

class DefaultNumberGenerator : NumberGenerator {
    override fun nextInt(maxNumber: Int): Int = Random.nextInt(maxNumber) + 1
}
