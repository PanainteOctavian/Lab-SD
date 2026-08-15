package com.sd.laborator.model

data class CartesianPayload(
    val a: Set<Int>,
    val b: Set<Int>
)

data class UnionPayload(
    val a: Set<Int>,
    val b: Set<Int>,
    val p1: Set<Pair<Int, Int>>,
    val p2: Set<Pair<Int, Int>>
)