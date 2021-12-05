package com.example.bcsinvest.screen.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bcsinvest.data.*
import com.example.bcsinvest.gate.Gate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalStateException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.nextDown
import kotlin.math.roundToInt

class SettingsViewModel : ViewModel() {

    val investPeriod = MutableLiveData(36f)
    val investSum = MutableLiveData(100000f)
    val currency = MutableLiveData(InvestCurrency.RUR)
    val billType = MutableLiveData<BillType>(BillType.IIS())
    val isRegularUp = MutableLiveData(false)
    val regularSum = MutableLiveData(0f)
//    val gate = Gate.getInstance()
//    val curState = MutableLiveData<State>()
//    var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")


//    fun getData(sum: Int, period: Int, monthlyRepay: Int, currency: InvestCurrency) {
//        viewModelScope.launch(Dispatchers.IO) {
//            val list = gate.getMainDataList(currency)
//            val map = mutableMapOf<Double, Security>()
////            val sum = 156700
////            val period = 730
////            val monthlyRepay = 10000
//            val filterList = list.filter {
//                ChronoUnit.DAYS.between(
//                    LocalDate.now(),
//                    LocalDate.parse(it.matDate)
//                ) > (period + 365)
//            }
//            if (filterList.isNotEmpty()) {
//                filterList.forEach {
//                    val d = parseSecurity(it, period * 30, LocalDate.now())
//                    if (d > 0) {
//                        map[d] = it
//                    }
//                }
//
//            }
//
//            val sortDs = map.keys.sortedBy { it.nextDown() }.reversed()
//            val bag =
//                calculateListSecurity(sortDs, map, sum, monthRepay = monthlyRepay, months = period)
//
//
//        }
//    }
//
//    fun parseSecurity(security: Security, days: Int, today: LocalDate = LocalDate.now()): Double {
//        if (security.faceValue != null) {
//
//            if (security.couponPercent != null) {
//
//
//                val matDate = LocalDate.parse(security.matDate)
//
//                val couponPeriodLast = ChronoUnit.DAYS.between(today, matDate)
//
//                val f = security.faceValue - (((security.prevPrice
//                    ?: 100.0) / 100.0).times(security.faceValue))
//                val needPeriod = if ((couponPeriodLast) > days) days else (couponPeriodLast)
//
//
//                val s = needPeriod.toDouble() / 365.0
//                val th = (security.faceValue.times(security.couponPercent.div(100.0)))
//                val fth = (security.faceValue + (((security.prevPrice ?: 100.0) / 100.0).times(
//                    security.faceValue
//                ))) / 2.0
//                return ((f / s + th) / fth) * 100.0
//            } else return 0.0
//        } else {
//            throw IllegalStateException("Просроченный купон")
//        }
//
//    }
//
//    fun calculateListSecurity(
//        list: List<Double>,
//        map: MutableMap<Double, Security>,
//        sum: Int,
//        months: Int,
//        monthRepay: Int
//    ): BagResult {
//        var mainS = sum
//        var s = sum
//        val bag = mutableListOf<CalculateSecurity>()
//        val now = LocalDate.now()
//        list.forEach {
//            if (s > map[it]!!.faceValue!!) {
//                val calculateResult = calculateSecurity(map[it]!!, s, it)
//                s = calculateResult.sumAfter
//                bag.add(calculateResult)
//            }
//        }
//        val yearsAndResults = mutableMapOf<Int, InvestResult>()
//        var yearsCount = 0
//
//        var rate: Double
//
//        val firstBag = mutableMapOf<String, Int>()
//        bag.forEach {
//            firstBag[it.security.shortName ?: it.security.secName!!] = it.count
//        }
//
//        val firstAfterSum = s
//
//        for (i in 1..months) {
//
//            bag.forEach { cs ->
//                if (i % (cs.period/30) == 0) {
//                    s += (cs.rateForCouponTime * cs.count)
//                }
//            }
//
//            s += monthRepay
//            mainS += monthRepay
//            bag.forEach { cs ->
//                if (s > cs.onePrice) {
//                    val newD = parseSecurity(
//                        cs.security,
//                        today = now.plusMonths(i.toLong()),
//                        days = (60 - i) * 30
//                    )
//                    cs.rateProc = newD
//                    val sCs = calculateSecurity(cs.security, s, cs.rateProc)
//                    s = sCs.sumAfter
//                    cs.count += sCs.count
//                }
//            }
//
//
//
//            if (i % 12 == 0) {
//                yearsCount++
//                rate = bag.sumOf { it.rateProc } / bag.count()
//                val rateC = (mainS / 100 * rate).roundToInt()
//                val res = InvestResult(sum = mainS, rateC, afterSum = s, rate)
//                mainS += rateC
//                yearsAndResults[yearsCount] = res
//            }
//
//        }
//
//
//
//
//        return BagResult(yearsAndResults = yearsAndResults, firstBag, s)
//    }
//
//    fun calculateSecurity(security: Security, sum: Int, rate: Double): CalculateSecurity {
//
//        if (security.faceValue != null) {
//
//            val period = security.couponPeriod
//            val forCouponTime = security.faceValue / 100 * (security.couponPercent!!)
//            val rateForCouponTime = ((365 / period!!) * forCouponTime).roundToInt()
//            val onePrice = ((security.faceValue * ((security.prevPrice
//                ?: 100.0) / 100.0)) + (security.accRuEdInt ?: 0.0))
//            val countCanBuy =
//                ((sum / (onePrice * (security.lotSize
//                    ?: 1))) / (security.lotSize ?: 1)).toInt()
//
//            val sumAfter = (sum - (countCanBuy * onePrice).roundToInt())
//
//
//
//
//            return CalculateSecurity(
//                sumAfter = sumAfter,
//                onePrice = onePrice,
//                count = countCanBuy,
//                period = period,
//                security = security,
//                rateForCouponTime = rateForCouponTime,
//                rateProc = rate
//            )
////                }
//        } else {
//            throw IllegalStateException("Не известен номинал")
//        }
//
//
//    }



}


