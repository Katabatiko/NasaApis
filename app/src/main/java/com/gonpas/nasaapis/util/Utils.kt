package com.gonpas.nasaapis.util

import android.util.Log

private val PUNCTUATION = listOf(", ", "; ", ": ", " ")

/**
 * Truncate long text with a preference for word boundaries and without trailing punctuation.
 */
fun String.smartTruncate(length: Int): String {
    val words = split(" ")
    var added = 0
    var hasMore = false
    val builder = StringBuilder()
    for (word in words) {
        if (builder.length > length) {
            hasMore = true
            break
        }
        builder.append(word)
        builder.append(" ")
        added += 1
    }

    PUNCTUATION.map {
        if (builder.endsWith(it)) {
            builder.replace(builder.length - it.length, builder.length, "")
        }
    }

    if (hasMore) {
        builder.append("...")
    }
    return builder.toString()
}

fun extractedDate(date: String, field: String): Int{
    val extractedDate = date.split(" ")[0].split("-")
    return when(field){
        "dia" -> extractedDate[2].toInt()
        "mes" -> extractedDate[1].toInt()
        "anno" -> extractedDate[0].toInt()
        else -> Log.e("xxU","Campo de fecha erróneo")
    }
}