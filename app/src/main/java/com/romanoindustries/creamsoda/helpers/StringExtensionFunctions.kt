package com.romanoindustries.creamsoda.helpers

fun String.splitWithEmptyRemoved(delimiter: String): List<String> {
    val split = ArrayList(split(delimiter))
    val resultArray = arrayListOf<String>()
    split.forEach {
        val trimmed = it.trim()
        if (trimmed.isNotBlank()) resultArray.add(trimmed)
    }
    return resultArray
}