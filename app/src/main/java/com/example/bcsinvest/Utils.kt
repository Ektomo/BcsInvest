package com.example.bcsinvest

fun Float.format(digits: Int) = "%.${digits}f".format(this)

fun String.formatByNumber(separator: String) = this.reversed().chunked(3).joinToString(separator).reversed()