package com.example.bcsinvest.screen.graph

import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavHostController
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

import com.example.bcsinvest.screen.LoadingView
import hu.ma.charts.bars.HorizontalBarsChart
import hu.ma.charts.bars.data.HorizontalBarsData
import hu.ma.charts.bars.data.StackedBarData
import hu.ma.charts.bars.data.StackedBarEntry
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
                        state.data.yearsAndResults.forEach { t, u ->
                            bars.add(
                                HorizontalBarsData(
                                    bars = createBars(
                                        mapOf(
                                            "${date.year + t}" to listOf(
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

                            Text("Состав", style = MaterialTheme.typography.h6)
                            Spacer(modifier = Modifier.requiredSize(12.dp))
                            HorizontalBarsChart(data = data)
                            Spacer(modifier = Modifier.requiredSize(30.dp))
                            Text("Подробный График по годам", style = MaterialTheme.typography.h6)
                            Spacer(modifier = Modifier.requiredSize(12.dp))
                            bars.forEach {
                                HorizontalBarsChart(data = it)
                                Spacer(modifier = Modifier.requiredSize(8.dp))
                            }
                            Spacer(modifier = Modifier.requiredSize(36.dp))
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
        Text(
            "Инвестиции сегодня: ${bagResult.yearsAndResults[1]!!.sum}",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.padding(8.dp))
        Text(
            text = "Средняя доходность за все время ${if (isRepay) ",\n при пополнении счета на ${repaySum} ежемесячно" else ""}:\n ${
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

        Text(
            text = "Через $months месяцев вы получите:\n ${bagResult.yearsAndResults[lastIdx]!!.sum + bagResult.yearsAndResults[lastIdx]!!.rate}",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.subtitle1
        )
        Spacer(modifier = Modifier.padding(8.dp))

        Text(
            text = "Прибыль составит:\n ${bagResult.yearsAndResults[lastIdx]!!.rate}",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.subtitle1
        )


    }
}