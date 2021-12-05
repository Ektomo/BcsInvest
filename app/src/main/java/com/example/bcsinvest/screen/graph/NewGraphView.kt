package com.example.bcsinvest.screen.graph

import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavHostController
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bcsinvest.data.BagResult
import com.example.bcsinvest.data.InvestCurrency
import com.example.bcsinvest.formatByNumber

import com.example.bcsinvest.screen.LoadingView
import hu.ma.charts.bars.HorizontalBarsChart
import hu.ma.charts.bars.data.HorizontalBarsData
import hu.ma.charts.bars.data.StackedBarData
import hu.ma.charts.bars.data.StackedBarEntry
import hu.ma.charts.legend.data.LegendPosition
import hu.ma.charts.pie.PieChart
import hu.ma.charts.pie.data.PieChartData
import hu.ma.charts.pie.data.PieChartEntry
import java.lang.IllegalStateException
import java.time.LocalDate
import kotlin.math.roundToInt




val SimpleColors = listOf(
    Color.Black,
    Color.Blue,
    Color.Yellow,
    Color.Red,
    Color.LightGray,
    Color.Magenta,
    Color.Cyan,
    Color.Green,
    Color.Gray,
)



private fun createBars(
    map: Map<String, List<Int>>,
    categories: List<String>
): List<StackedBarData> {
    val mm = mutableListOf<StackedBarData>()
    map.forEach { (name, value) ->
        val temp = StackedBarData(
            title = AnnotatedString(name),
            entries = value.mapIndexed { index, i ->
                StackedBarEntry(
                    text = AnnotatedString(categories[index]),
                    value = i.toFloat(),
                    color = SimpleColors[index]
                )
            }
        )
        mm.add(temp)
    }
    return mm
}


@Composable
fun NewGraphView(navController: NavHostController, graphViewModel: GraphViewModel) {

    val date = LocalDate.now()
    val curState = graphViewModel.curState.observeAsState()
    val needLoad = graphViewModel.needLoad.observeAsState()


    Crossfade(targetState = curState.value) { state ->
        when (state) {
            is GraphViewModel.State.Loading -> {
                LoadingView()
            }
            is GraphViewModel.State.Data -> {
//                Column {
//                    Text(text = state.data.firstAfterSum.toString())
//                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(
                        top = 24.dp,
                        bottom = 24.dp,
                    ),
                ) {
                    item {


                        val list = mutableListOf<Int>()
                        val nameList = mutableListOf<String>()
                        val nameList1 = listOf("Тело", "Прибыль", "Остаток")
                        state.data.bag.forEach { (t, u) ->
                            list.add(u)
                            nameList.add(t)
                        }
                        val data = HorizontalBarsData(
                            bars = createBars(mapOf("Состав портфеля" to list), nameList)
                        )

                        val bars = mutableListOf<HorizontalBarsData>()
                        state.data.yearsAndResults.forEach { (t, u) ->
                            bars.add(
                                HorizontalBarsData(
                                    bars = createBars(
                                        mapOf(
//                                            "${date.year + t - 1}"
                                            "${t}-й год"
                                                    to listOf(
                                                u.sum,
                                                u.rate,
                                                u.afterSum
                                            )
                                        ), nameList1
                                    )
                                )
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .animateContentSize(),
                        ) {
                            investCard(
                                bagResult = state.data,
                                isRepay = graphViewModel.isRegularUp.value!!,
                                repaySum = graphViewModel.regularSum.value!!.roundToInt(),
                                currency = graphViewModel.currency.value!!,
                                months = graphViewModel.investPeriod.value!!.toInt()
                            )
                            Spacer(modifier = Modifier.padding(30.dp))
                            Text("Портфель", style = MaterialTheme.typography.h6)
                            Spacer(modifier = Modifier.requiredSize(12.dp))
                            HorizontalBarsChart(data = data)
                            Spacer(modifier = Modifier.padding(30.dp))
                            Text("Подробная информация о портфеле на каждый год", style = MaterialTheme.typography.h6)
                            Spacer(modifier = Modifier.requiredSize(12.dp))
                            bars.forEach {
                                HorizontalBarsChart(data = it)
                                Spacer(modifier = Modifier.requiredSize(8.dp))
                            }
                            Spacer(modifier  = Modifier.requiredSize(36.dp))




                        }
                    }

                }
            }
            is GraphViewModel.State.Error ->  {
                Text(text = state.e.localizedMessage ?: "Неизвестная ошибка")

            }
            is GraphViewModel.State.Default -> {

                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Заполните данные и нажмите собрать портфель на первом экране", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }

            }
            else -> {
                throw IllegalStateException("Невозможное состояние")
            }
        }

    }

    LaunchedEffect(key1 = needLoad.value) {
        if (needLoad.value!!) {
            graphViewModel.needLoad.value = false
            graphViewModel.getData()
        }
    }
}

@Composable
fun investCard(
    bagResult: BagResult,
    isRepay: Boolean,
    repaySum: Int,
    currency: InvestCurrency,
    months: Int
) {
    val rate =
        bagResult.yearsAndResults.values.sumOf { it.rateProc } / bagResult.yearsAndResults.count()
    val lastIdx = bagResult.yearsAndResults.count()
    Column() {
        val str1 = (bagResult.yearsAndResults[1]!!.sum).toString().formatByNumber(" ")
        Text(
            "Инвестиции сегодня: $str1",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.padding(8.dp))
        val str2 = repaySum.toString().formatByNumber(" ")
        Text(
            text = "Средняя доходность за все время ${if (isRepay) ",\n при пополнении счета на $str2 ежемесячно" else ""}:\n ${
                String.format(
                    "%2.2f",
                    rate
                )
            }% годовых",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.subtitle1
        )
        Spacer(modifier = Modifier.padding(8.dp))
        val str3 = (bagResult.yearsAndResults[lastIdx]!!.sum + bagResult.yearsAndResults[lastIdx]!!.rate + bagResult.yearsAndResults[lastIdx]!!.afterSum).toString().formatByNumber(" ")
        Text(
            text = "Через $months месяцев вы получите:\n $str3",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.subtitle1
        )
        Spacer(modifier = Modifier.padding(8.dp))
        val str4 = (bagResult.yearsAndResults[lastIdx]!!.rate).toString().formatByNumber(" ")
        Text(
            text = "Прибыль составит:\n $str4",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.subtitle1
        )


    }
}