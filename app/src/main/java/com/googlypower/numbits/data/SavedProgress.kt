package com.googlypower.numbits.data

data class SavedProgress(
    val id: Int,
    val solved: Boolean,
    val timeToSolve: String,
    val startTime: Long,
    val lhs: String,
    val rhs: String,
    val reserves: String
)
