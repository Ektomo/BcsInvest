package com.example.bcsinvest.screen.graph

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bcsinvest.data.*
import com.example.bcsinvest.gate.Gate
import hu.ma.charts.bars.data.HorizontalBarsData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalStateException
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.nextDown
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class GraphViewModel : ViewModel() {
    val date = LocalDate.now()
    val gate = Gate.getInstance()
    val investPeriod = MutableLiveData(36f)
    val investSum = MutableLiveData(100000f)
    val currency = MutableLiveData(InvestCurrency.RUR)
    val billType = MutableLiveData<BillType>(BillType.IIS())
    val isRegularUp = MutableLiveData(false)
    val regularSum = MutableLiveData(0f)
    val needLoad = MutableLiveData(false)
    val curState = MutableLiveData<State>(State.Default)
    val mapBars = mutableMapOf<BagResult, List<HorizontalBarsData>>()


//    fun getBarsBy(bagResult: BagResult): List<HorizontalBarsData>{
//
//        if (mapBars[bagResult] != null){
//            return mapBars[bagResult]!!
//        }else{
//            bagResult.yearsAndResults.forEach { (t, u) ->
//                bars.add(
//                    HorizontalBarsData(
//                        bars = createBars(
//                            mapOf(
//                                "${date.year + t - 1}" to listOf(
//                                    u.sum,
//                                    u.rate,
//                                    u.afterSum
//                                )
//                            ), nameList1
//                        )
//                    )
//                )
//            }
//        }
//
//    }


    fun getData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                curState.postValue(State.Loading)
                val sum = investSum.value!!.roundToLong()
                val period = investPeriod.value!!.toLong()
                val map = mutableMapOf<Pair<Double,Double>, Security>()
                val monthlyRepay = if (isRegularUp.value!!) regularSum.value!!.toInt() else 0
                val curr = currency.value!!


                val list = gate.getMainDataList(curr)
                val filterList = list.filter {
                    it.matDate != "0000-00-00" &&

                            (ChronoUnit.DAYS.between(
                                LocalDate.now(),
                                LocalDate.parse(it.matDate)
                            )) > ((period * 30) - 365)
                }
                if (filterList.isNotEmpty()) {
                    filterList.forEach {
                        val d = parseSecurity(it, period * 30, LocalDate.now())
                        if (d.first > 0) {
                            map[d] = it
                        }
                    }

                }

                val sortDs = map.keys.sortedBy { it.first.nextDown() }.reversed()
                val bag =
                    calculateListSecurity(
                        sortDs,
                        map,
                        sum,
                        monthRepay = monthlyRepay,
                        months = period
                    )
                curState.postValue(State.Data(bag))
            } catch (e: Exception) {
                curState.postValue(State.Error(e))
            }


        }
    }

    fun parseSecurity(security: Security, days: Long, today: LocalDate = LocalDate.now()): Pair<Double, Double> {
        if (security.faceValue != null) {

            if (security.couponPercent != null) {

                val now = LocalDate.now()
                val isCalculate: Boolean = now == today
                val matDate = LocalDate.parse(security.matDate)
                val couponPeriod = security.couponPeriod
                val couponPeriodLast = ChronoUnit.DAYS.between(today, matDate)
                val needPeriod: Long =
                    if (couponPeriodLast > days){
                    days
                }else{
                    couponPeriodLast
                }

                val periodCounts = (needPeriod.toDouble()/couponPeriod!!.toDouble()).roundToInt()
                val timeToPayCoupon = (360.0 / security.couponPeriod).roundToInt()
                val couponPayment = (security.faceValue / 100 * security.couponPercent)/timeToPayCoupon
                val allCouponPayments = couponPayment * periodCounts

                val secPrice = ((security.faceValue * ((security.prevPrice
                    ?: 100.0) / 100.0)) + (security.accRuEdInt ?: 0.0))
                val nomMP = security.faceValue - secPrice
                val result = ((nomMP + allCouponPayments)/secPrice) * (365.0/needPeriod.toDouble()*100)
                val yearRate = (result / (needPeriod.toDouble() / 365))



//                val f = security.faceValue - (((security.prevPrice
//                    ?: 100.0) / 100.0).times(security.faceValue))
//
//
//
//                val s = needPeriod.toDouble() / 365.0
//                val th = (security.faceValue.times(security.couponPercent.div(100.0)))
//                val fth = (security.faceValue + (((security.prevPrice ?: 100.0) / 100.0).times(
//                    security.faceValue
//                ))) / 2.0
//                val d = ((f / s + th) / fth) * 100.0
                return Pair(result, yearRate)
            } else return Pair(0.0, 0.0)
        } else {
            throw IllegalStateException("Просроченный купон")
        }

    }

    fun calculateListSecurity(
        list: List<Pair<Double, Double>>,
        map: MutableMap<Pair<Double, Double>, Security>,
        sum: Long,
        months: Long,
        monthRepay: Int
    ): BagResult {
        var mainS = sum
        var s = sum
        val bag = mutableListOf<CalculateSecurity>()
        val now = LocalDate.now()
        list.forEach {
            if (s > map[it]!!.faceValue!!) {
                val calculateResult = calculateSecurity(map[it]!!, s, it.second, now)
                s = calculateResult.sumAfter
                bag.add(calculateResult)
            }
        }
        val yearsAndResults = mutableMapOf<Int, InvestResult>()
        var yearsCount = 0

        var rate: Double

        val firstBag = mutableMapOf<String, Long>()
        bag.forEach {
            firstBag[it.security.shortName ?: it.security.secName!!] = it.count.toLong()
        }

        val firstAfterSum = s

        for (i in 1..months) {

            bag.forEach { cs ->
                if (i % (cs.period / 30) == 0L) {
                    s += (cs.rateForCouponTime * cs.count)
                }
            }

            s += monthRepay
            mainS += monthRepay
            bag.forEach { cs ->
                if (s > cs.onePrice) {
//                    val newD = parseSecurity(
//                        cs.security,
//                        today = now.plusMonths(i),
//                        days = (60 - i) * 30
//                    )
//                    cs.rateProc = newD.second
                    val sCs = calculateSecurity(cs.security, s, cs.rateProc, today = now.plusMonths(i))
                    s = sCs.sumAfter
                    cs.rateForCouponTime = sCs.rateForCouponTime
                    cs.count += sCs.count
                }

            }

            if (i == months && i % 12 != 0L) {
                yearsCount++
                rate = bag.sumOf { it.rateProc } / bag.count()
                val rateC = (mainS / 100 * rate).roundToLong()
                val res = InvestResult(sum = mainS, rateC, afterSum = s, rate)
                mainS += rateC
                yearsAndResults[yearsCount] = res
            }



            if (i % 12 == 0L) {
                yearsCount++
                rate = bag.sumOf { it.rateProc } / bag.count()
                val rateC = (mainS / 100 * rate).roundToLong()
                val res = InvestResult(sum = mainS, rateC, afterSum = s, rate)
                mainS += rateC
                yearsAndResults[yearsCount] = res
            }

        }




        return BagResult(yearsAndResults = yearsAndResults, firstBag, firstAfterSum)
    }

    fun calculateSecurity(security: Security, sum: Long, rate: Double, today: LocalDate): CalculateSecurity {

        if (security.faceValue != null) {


            val onePrice = ((security.faceValue * ((security.prevPrice
                ?: 100.0) / 100.0)) + (security.accRuEdInt ?: 0.0))



            if(today > LocalDate.parse(security.matDate)){
                return CalculateSecurity(
                    sumAfter = sum,
                    onePrice = onePrice,
                    count = 0,
                    period = 0,
                    security = security,
                    rateForCouponTime = 0,
                    rateProc = rate
                )
            }


            val period = security.couponPeriod!!.toLong()
            val timeToPayCoupon = (360.0 / security.couponPeriod).roundToInt()
            val couponPayment = (security.faceValue / 100 * security.couponPercent!!)/timeToPayCoupon
            val rateForCouponTime = ((365 / period) * couponPayment).roundToInt()

            val countCanBuy =
                ((sum / (onePrice * (security.lotSize
                    ?: 1))) / (security.lotSize ?: 1)).toInt()

            val sumAfter = (sum - (countCanBuy * onePrice).roundToInt())




            return CalculateSecurity(
                sumAfter = sumAfter,
                onePrice = onePrice,
                count = countCanBuy,
                period = period,
                security = security,
                rateForCouponTime = rateForCouponTime,
                rateProc = rate
            )
//                }
        } else {
            throw IllegalStateException("Не известен номинал")
        }


    }


    sealed class State() {
        object Loading : State()
        object Default : State()
        class Error(val e: Exception) : State()
        class Data(val data: BagResult) : State()
    }


}