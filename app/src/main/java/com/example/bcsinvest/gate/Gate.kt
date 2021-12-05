package com.example.bcsinvest.gate

import com.example.bcsinvest.data.InvestCurrency
import com.example.bcsinvest.data.Security
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.IllegalStateException
import kotlin.math.roundToInt

class Gate {
    private val format = Json {
        ignoreUnknownKeys = true
    }

    private val baseUrl = "https://iss.moex.com/iss/"
    private val okHttpClient = OkHttpClient()
    private val mediaType = "application/json".toMediaType()


    fun getMainDataList(investCurrency: InvestCurrency): List<Security> {


        val request = Request.Builder()
            .url(baseUrl + "engines/stock/markets/bonds/boards/${if (investCurrency == InvestCurrency.RUR) "TQOB" else "TQOD"}/securities.json")
            .get()
            .build()

        val response = okHttpClient.newCall(request).execute()

        return if (response.isSuccessful) {
            val res = Gson().fromJson<HashMap<Any, Any>>(response.body!!.string(), object : TypeToken<HashMap<Any, Any>>(){}.type)
            parseList(res)
        } else {
            throw IllegalStateException("Не удалось выполнить запрос")
        }

    }




    private fun parseList(r1: HashMap<Any, Any>): MutableList<Security> {
        val sec = r1["securities"] as LinkedTreeMap<String, *>
        //            val columns = sec["columns"] as List<String>
        val data = sec["data"] as List<List<Any?>>
        val r = mutableListOf<Security>()
        data.forEach { list ->
            val security = Security(
                secId = list[0] as String?,
                boardId = list[1] as String?,
                shortName = list[2] as String?,
                prevWAPrice = list[3] as Double?,
                yieldAtPrevWAPrice = list[4] as Double?,
                couponValue = list[5] as Double?,
                nextCoupon = list[6] as String?,
                accRuEdInt = list[7] as Double?,
                prevPrice = list[8] as Double?,
                lotSize = if (list[9] == null) null else (list[9] as Double).roundToInt(),
                faceValue = list[10] as Double?,
                boardName = list[11] as String?,
                status = list[12] as String?,
                matDate = list[13] as String?,
                decimals = if (list[14] == null) null else (list[14] as Double).roundToInt(),
                couponPeriod = if (list[15] == null) null else (list[15] as Double).roundToInt(),
                issueSize = if (list[16] == null) null else (list[16] as Double).roundToInt(),
                prevLegalClosePrice = list[17] as Double?,
                prevAdmittedQuote = list[18] as Double?,
                prevDate = list[19] as String?,
                secName = list[20] as String?,
                remarks = list[21] as String?,
                marketCode = list[22] as String?,
                instrId = list[23] as String?,
                sectorId = list[24] as String?,
                minStep = list[25] as Double?,
                faceUnit = list[26] as String?,
                buyBackPrice = list[27] as Double?,
                buyBackDate = list[28] as String?,
                isIn = list[29] as String?,
                latName = list[30] as String?,
                regNumber = list[31] as String?,
                currencyId = list[32] as String?,
                issueSizePlaced = if (list[33] == null) null else (list[33] as Double).roundToInt(),
                listLevel = if (list[34] == null) null else (list[34] as Double).roundToInt(),
                secType = list[35] as String?,
                couponPercent = list[36] as Double?,
                offerDate = list[37] as String?,
                settleDate = list[38] as String?,
                lotValue = list[39] as Double?,
            )
            r.add(security)

        }
        return r
    }


    companion object {
        var gate: Gate? = null
        fun getInstance(): Gate {
            if (gate == null) {
                gate = Gate()
            }
            return gate!!
        }
    }


}
