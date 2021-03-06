package com.example.bcsinvest.data

import kotlinx.serialization.Contextual
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate


//data class SecId(
//    val type: String,
//    val bytes: Int,
//    val max_size: Int
//)
//
//
//data class Metadata(
//    @SerialName("SECID")
//    val secId: SecId
//)
//
//
//data class Securities(
//    val metadata: Metadata,
//    val columns: List<String>,
//    val data: List<List<Any>>,
//)
//
//
//data class MainData(
//    val securities: Securities
//)

data class Security(
    val secId: String? = null,
    val boardId: String?,
    val shortName: String?,
    val prevWAPrice: Double?,
    val yieldAtPrevWAPrice: Double?,
    val couponValue: Double?,
    val nextCoupon: String?,
    val accRuEdInt: Double?,
    val prevPrice: Double?,
    val lotSize: Int?,
    val faceValue: Double?,
    val boardName: String?,
    val status: String?,
    val matDate: String?,
    val decimals: Int?,
    val couponPeriod: Int?,
    val issueSize: Int?,
    val prevLegalClosePrice: Double?,
    val prevAdmittedQuote: Double?,
    val prevDate: String?,
    val secName: String?,
    val remarks: String?,
    val marketCode: String?,
    val instrId: String?,
    val sectorId: String?,
    val minStep: Double?,
    val faceUnit: String?,
    val buyBackPrice: Double?,
    val buyBackDate: String?,
    val isIn: String?,
    val latName: String?,
    val regNumber: String?,
    val currencyId: String?,
    val issueSizePlaced: Int?,
    val listLevel: Int?,
    val secType: String?,
    val couponPercent: Double?,
    val offerDate: String?,
    val settleDate: String?,
    val lotValue: Double?,
)

enum class InvestCurrency {
    RUR, USD
}

sealed class BillType(val value: String) {
    class IIS() : BillType("ИИС")
    class Simple() : BillType("Обычный счет")
}

data class CalculateSecurity(
    var sumAfter: Long,
    var onePrice: Double,
    var count: Int,
    var period: Long,
    var security: Security,
    var rateForCouponTime: Int,
    var rateProc: Double,
    var matDate: LocalDate,
    var isClosed: Boolean,
    var checked: Boolean = false
)

data class InvestResult(
    var sum: Long,
    var rate: Long,
    var afterSum: Long,
    var rateProc: Double
)

data class BagResult(
    val yearsAndResults: MutableMap<Int, InvestResult>,
    val bag: Map<String, Long>,
    val firstAfterSum: Long
)

