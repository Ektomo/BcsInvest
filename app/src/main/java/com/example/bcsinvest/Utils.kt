package com.example.bcsinvest

import java.text.DecimalFormat

fun Float.format(digits: Int) = "%.${digits}f".format(this)

fun String.formatByNumber(separator: String) = this.reversed().chunked(3).joinToString(separator).reversed()

