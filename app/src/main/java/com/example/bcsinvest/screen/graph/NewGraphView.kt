package com.example.bcsinvest.screen.graph

import android.util.Log
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
import hu.ma.charts.legend.data.LegendAlignment
import hu.ma.charts.legend.data.LegendPosition
import hu.ma.charts.line.LineChart
import hu.ma.charts.line.data.AxisLabel
import hu.ma.charts.line.data.DrawAxis
import hu.ma.charts.line.data.LineChartData
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
    map: Map<String, List<Long>>,
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


                        val list = mutableListOf<Long>()
                        val nameList = mutableListOf<String>()
                        val nameList1 = listOf("Тело", "Прибыль", "Остаток")
                        state.data.bag.forEach { (t, u) ->
                            if (u > 0){
                                list.add(u)
                                nameList.add(t)
                            }

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
                            Text(
                                "Подробная информация о портфеле на каждый год",
                                style = MaterialTheme.typography.h6
                            )
                            Spacer(modifier = Modifier.requiredSize(12.dp))
                            bars.forEach {
                                Spacer(modifier = Modifier.padding(8.dp))
                                HorizontalBarsChart(data = it)
                            }

//                            LinesSimpleScreen()
                            Spacer(modifier = Modifier.requiredSize(36.dp))


                        }
                    }

                }
            }
            is GraphViewModel.State.Error -> {
                Text(text = state.e.localizedMessage ?: "Неизвестная ошибка")

            }
            is GraphViewModel.State.Default -> {

                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Заполните данные и нажмите собрать портфель на первом экране",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
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
    val cur = if (currency == InvestCurrency.RUR) "Р" else "$"
    val rate =
        bagResult.yearsAndResults.values.sumOf { it.rateProc } / bagResult.yearsAndResults.count()
    val lastIdx = bagResult.yearsAndResults.count()
    Column {
        val str1 = (bagResult.yearsAndResults[1]!!.sum).toString().formatByNumber(" ")
        Text(
            "Инвестиции сегодня: $str1 $cur",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.padding(8.dp))
        val str2 = repaySum.toString().formatByNumber(" ")
        Text(
            text = "Средняя доходность каждого года ${if (isRepay) ",\n при пополнении счета на $str2 $cur ежемесячно" else ""}:\n ${
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
        val str3 =
            (bagResult.yearsAndResults[lastIdx]!!.sum + bagResult.yearsAndResults[lastIdx]!!.rate + bagResult.yearsAndResults[lastIdx]!!.afterSum).toString()
                .formatByNumber(" ")
        Text(
            text = "Через $months месяцев вы получите:\n $str3 $cur",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.subtitle1
        )
        Spacer(modifier = Modifier.padding(8.dp))
        val str4 = (bagResult.yearsAndResults.values.sumOf { it.rate } + bagResult.yearsAndResults[lastIdx]!!.afterSum).toString().formatByNumber(" ")
        Text(
            text = "Прибыль составит:\n $str4 $cur",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.subtitle1
        )


    }
}

//Дальше пример как рисовать графики по другому, рабочего кода там нет

//@Composable
//fun LinesSimpleScreen() {
//    Column(
//        modifier = Modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.spacedBy(24.dp),
////        contentPadding = PaddingValues(
////            top = 24.dp,
////            bottom = 24.dp,
////        ),
//    ) {
//        LinesSampleData.forEach { (title, data) ->
////            item {
//                Column {
//                    Text(title, style = MaterialTheme.typography.h6)
//                    Spacer(modifier = Modifier.requiredSize(12.dp))
//
//
//                    LineChart(
//                        chartHeight = 400.dp,
//                        data = data,
//                        onDrillDown = { xIndex, allSeries ->
//                            Log.d(
//                                "LineChart",
//                                "You are drilling down at xIndex=$xIndex, series values at this index: ${
//                                    allSeries.map { it.points.find { point -> point.x == xIndex } }
//                                        .map { it?.value }
//                                        .joinToString()
//                                }"
//                            )
//                        }
//                    )
//                }
////            }
//
////            item {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp, vertical = 8.dp),
//                ) {
//                    Text(title, style = MaterialTheme.typography.h6)
//                    Spacer(modifier = Modifier.requiredSize(12.dp))
//                    LineChart(
//                        chartHeight = 400.dp,
//                        data = LinesSampleData.first().second.copy(legendPosition = LegendPosition.Top),
//                        onDrillDown = { xIndex, allSeries ->
//                            Log.d(
//                                "LineChart",
//                                "You are drilling down at xIndex=$xIndex, series values at this index: ${
//                                    allSeries.map { it.points.find { point -> point.x == xIndex } }
//                                        .map { it?.value }
//                                        .joinToString()
//                                }"
//                            )
//                        },
//                        legend = { position, entries ->
//                            Text(text = "Showing series: ")
//                            entries.forEachIndexed { index, item ->
//                                Text(text = "${item.text}")
//                                if (index != entries.lastIndex) {
//                                    Text(text = ", ")
//                                }
//                            }
//                        }
//                    )
//                }
////            }
//        }
//
//    }
//
//
//}



//val Categories = listOf(
//    "Teams",
//    "Locations",
//    "Devices",
//    "People",
//    "Laptops",
//    "Titles",
//    "Flowers",
//    "Bugs",
//    "Windows",
//    "Screens",
//    "Colors",
//    "Bottles",
//    "Cars",
//    "Tricks",
//)
//
//internal val LinesSampleData = listOf(
//    "Lines" to LineChartData(
//        series = listOf(
//            LineChartData.SeriesData(
//                "Line A",
//                points = listOf(
//                    LineChartData.SeriesData.Point(0, 0f),
//                    LineChartData.SeriesData.Point(1, 10.0f),
//                    LineChartData.SeriesData.Point(2, 20.0f),
//                    LineChartData.SeriesData.Point(3, 30.0f),
//                    LineChartData.SeriesData.Point(4, 50.0f),
//                    LineChartData.SeriesData.Point(5, 35.0f),
//                ),
//                Color.Red
//            ),
//            LineChartData.SeriesData(
//                "Line B",
//                points = listOf(
//                    LineChartData.SeriesData.Point(0, 20f),
//                    LineChartData.SeriesData.Point(1, 10.0f),
//                    LineChartData.SeriesData.Point(2, 5.0f),
//                    LineChartData.SeriesData.Point(3, 15.0f),
//                    LineChartData.SeriesData.Point(4, 30.0f),
//                    LineChartData.SeriesData.Point(5, 35.0f),
//                ),
//                Color.Blue
//            ),
//        ),
//        xLabels = listOf("Year 1", "2", "3", "4", "5", "6")
//    ),
//    "Gradient fill" to LineChartData(
//        series = listOf(
//            LineChartData.SeriesData(
//                title = "Line A",
//                points = listOf(
//                    LineChartData.SeriesData.Point(0, 0f),
//                    LineChartData.SeriesData.Point(1, 10.0f),
//                    LineChartData.SeriesData.Point(2, 20.0f),
//                    LineChartData.SeriesData.Point(3, 30.0f),
//                    LineChartData.SeriesData.Point(4, 50.0f),
//                    LineChartData.SeriesData.Point(5, 35.0f),
//                ),
//                Color.Red, gradientFill = true
//            ),
//            LineChartData.SeriesData(
//                title = "Line B",
//                points = listOf(
//                    LineChartData.SeriesData.Point(0, 20f),
//                    LineChartData.SeriesData.Point(1, 10.0f),
//                    LineChartData.SeriesData.Point(2, 5.0f),
//                    LineChartData.SeriesData.Point(3, 15.0f),
//                    LineChartData.SeriesData.Point(4, 30.0f),
//                    LineChartData.SeriesData.Point(5, 35.0f),
//                ),
//                Color.Blue, gradientFill = true
//            ),
//        ),
//    ),
//    "Y-axis labels" to LineChartData(
//        series = listOf(
//            LineChartData.SeriesData(
//                title = "Line A",
//                points = listOf(
//                    LineChartData.SeriesData.Point(0, 0f),
//                    LineChartData.SeriesData.Point(1, 10.0f),
//                    LineChartData.SeriesData.Point(2, 20.0f),
//                    LineChartData.SeriesData.Point(3, 30.0f),
//                    LineChartData.SeriesData.Point(4, 50.0f),
//                    LineChartData.SeriesData.Point(5, 35.0f),
//                ),
//                Color.Red, gradientFill = true
//            ),
//            LineChartData.SeriesData(
//                title = "Line B",
//                points = listOf(
//                    LineChartData.SeriesData.Point(0, 20f),
//                    LineChartData.SeriesData.Point(1, 10.0f),
//                    LineChartData.SeriesData.Point(2, 5.0f),
//                    LineChartData.SeriesData.Point(3, 15.0f),
//                    LineChartData.SeriesData.Point(4, 30.0f),
//                    LineChartData.SeriesData.Point(5, 35.0f),
//                ),
//                Color.Blue, gradientFill = true
//            ),
//        ),
//        yLabels = listOf(
//            AxisLabel(0f, "0K"),
//            AxisLabel(20f, "20K"),
//            AxisLabel(40f, "40K"),
//        ),
//        drawAxis = DrawAxis.X,
//    ),
//    "Y-axis labels w/lines" to LineChartData(
//        series = listOf(
//            LineChartData.SeriesData(
//                title = "Line A",
//                points = listOf(
//                    LineChartData.SeriesData.Point(0, 0f),
//                    LineChartData.SeriesData.Point(1, 10.0f),
//                    LineChartData.SeriesData.Point(2, 20.0f),
//                    LineChartData.SeriesData.Point(3, 30.0f),
//                    LineChartData.SeriesData.Point(4, 50.0f),
//                    LineChartData.SeriesData.Point(5, 35.0f),
//                ),
//                Color.Red, gradientFill = true
//            ),
//            LineChartData.SeriesData(
//                title = "Line B",
//                points = listOf(
//                    LineChartData.SeriesData.Point(0, 20f),
//                    LineChartData.SeriesData.Point(1, 10.0f),
//                    LineChartData.SeriesData.Point(2, 5.0f),
//                    LineChartData.SeriesData.Point(3, 15.0f),
//                    LineChartData.SeriesData.Point(4, 30.0f),
//                    LineChartData.SeriesData.Point(5, 35.0f),
//                ),
//                Color.Blue, gradientFill = true
//            ),
//        ),
//        yLabels = listOf(
//            AxisLabel(0f, "0K"),
//            AxisLabel(20f, "20K"),
//            AxisLabel(40f, "40K"),
//        ),
//        drawAxis = DrawAxis.X,
//        horizontalLines = true,
//    ),
//    "Legend top" to LineChartData(
//        series = listOf(
//            LineChartData.SeriesData(
//                title = "Line A",
//                points = listOf(
//                    LineChartData.SeriesData.Point(0, 0f),
//                    LineChartData.SeriesData.Point(1, 10.0f),
//                    LineChartData.SeriesData.Point(2, 20.0f),
//                    LineChartData.SeriesData.Point(3, 30.0f),
//                    LineChartData.SeriesData.Point(4, 50.0f),
//                    LineChartData.SeriesData.Point(5, 35.0f),
//                ),
//                Color.Red, gradientFill = true
//            ),
//            LineChartData.SeriesData(
//                title = "Line B",
//                points = listOf(
//                    LineChartData.SeriesData.Point(0, 20f),
//                    LineChartData.SeriesData.Point(1, 10.0f),
//                    LineChartData.SeriesData.Point(2, 5.0f),
//                    LineChartData.SeriesData.Point(3, 15.0f),
//                    LineChartData.SeriesData.Point(4, 30.0f),
//                    LineChartData.SeriesData.Point(5, 35.0f),
//                ),
//                Color.Blue, gradientFill = true
//            ),
//        ),
//        yLabels = listOf(
//            AxisLabel(0f, "0K"),
//            AxisLabel(20f, "20K"),
//            AxisLabel(40f, "40K"),
//        ),
//        drawAxis = DrawAxis.X,
//        horizontalLines = true,
//        legendPosition = LegendPosition.Top,
//    ),
//    "Legend bottom" to LineChartData(
//        series = listOf(
//            LineChartData.SeriesData(
//                title = "Line A",
//                points = listOf(
//                    LineChartData.SeriesData.Point(0, 0f),
//                    LineChartData.SeriesData.Point(1, 10.0f),
//                    LineChartData.SeriesData.Point(2, 20.0f),
//                    LineChartData.SeriesData.Point(3, 30.0f),
//                    LineChartData.SeriesData.Point(4, 50.0f),
//                    LineChartData.SeriesData.Point(5, 35.0f),
//                ),
//                Color.Red, gradientFill = true
//            ),
//            LineChartData.SeriesData(
//                title = "Line B",
//                points = listOf(
//                    LineChartData.SeriesData.Point(0, 20f),
//                    LineChartData.SeriesData.Point(1, 10.0f),
//                    LineChartData.SeriesData.Point(2, 5.0f),
//                    LineChartData.SeriesData.Point(3, 15.0f),
//                    LineChartData.SeriesData.Point(4, 30.0f),
//                    LineChartData.SeriesData.Point(5, 35.0f),
//                ),
//                Color.Blue, gradientFill = true
//            ),
//        ),
//        yLabels = listOf(
//            AxisLabel(0f, "0K"),
//            AxisLabel(20f, "20K"),
//            AxisLabel(40f, "40K"),
//        ),
//        drawAxis = DrawAxis.X,
//        horizontalLines = true,
//        legendPosition = LegendPosition.Bottom,
//    ),
//    "Legend start" to LineChartData(
//        series = listOf(
//            LineChartData.SeriesData(
//                title = "Line A",
//                points = listOf(
//                    LineChartData.SeriesData.Point(0, 0f),
//                    LineChartData.SeriesData.Point(1, 10.0f),
//                    LineChartData.SeriesData.Point(2, 20.0f),
//                    LineChartData.SeriesData.Point(3, 30.0f),
//                    LineChartData.SeriesData.Point(4, 50.0f),
//                    LineChartData.SeriesData.Point(5, 35.0f),
//                ),
//                Color.Red, gradientFill = true
//            ),
//            LineChartData.SeriesData(
//                title = "Line B",
//                points = listOf(
//                    LineChartData.SeriesData.Point(0, 20f),
//                    LineChartData.SeriesData.Point(1, 10.0f),
//                    LineChartData.SeriesData.Point(2, 5.0f),
//                    LineChartData.SeriesData.Point(3, 15.0f),
//                    LineChartData.SeriesData.Point(4, 30.0f),
//                    LineChartData.SeriesData.Point(5, 35.0f),
//                ),
//                Color.Blue, gradientFill = true
//            ),
//        ),
//        yLabels = listOf(
//            AxisLabel(0f, "0K"),
//            AxisLabel(20f, "20K"),
//            AxisLabel(40f, "40K"),
//        ),
//        drawAxis = DrawAxis.X,
//        horizontalLines = true,
//        legendPosition = LegendPosition.Start,
//    ),
//    "Legend end" to LineChartData(
//        series = listOf(
//            LineChartData.SeriesData(
//                title = "Line A",
//                points = listOf(
//                    LineChartData.SeriesData.Point(0, 0f),
//                    LineChartData.SeriesData.Point(1, 10.0f),
//                    LineChartData.SeriesData.Point(2, 20.0f),
//                    LineChartData.SeriesData.Point(3, 30.0f),
//                    LineChartData.SeriesData.Point(4, 50.0f),
//                    LineChartData.SeriesData.Point(5, 35.0f),
//                ),
//                Color.Red, gradientFill = true
//            ),
//            LineChartData.SeriesData(
//                title = "Line B",
//                points = listOf(
//                    LineChartData.SeriesData.Point(0, 20f),
//                    LineChartData.SeriesData.Point(1, 10.0f),
//                    LineChartData.SeriesData.Point(2, 5.0f),
//                    LineChartData.SeriesData.Point(3, 15.0f),
//                    LineChartData.SeriesData.Point(4, 30.0f),
//                    LineChartData.SeriesData.Point(5, 35.0f),
//                ),
//                Color.Blue, gradientFill = true
//            ),
//        ),
//        yLabels = listOf(
//            AxisLabel(0f, "0K"),
//            AxisLabel(20f, "20K"),
//            AxisLabel(40f, "40K"),
//        ),
//        drawAxis = DrawAxis.X,
//        horizontalLines = true,
//        legendPosition = LegendPosition.End,
//    ),
//    "Legend end, vertical alignment" to LineChartData(
//        series = listOf(
//            LineChartData.SeriesData(
//                title = "Line A",
//                points = listOf(
//                    LineChartData.SeriesData.Point(0, 0f),
//                    LineChartData.SeriesData.Point(1, 10.0f),
//                    LineChartData.SeriesData.Point(2, 20.0f),
//                    LineChartData.SeriesData.Point(3, 30.0f),
//                    LineChartData.SeriesData.Point(4, 50.0f),
//                    LineChartData.SeriesData.Point(5, 35.0f),
//                ),
//                Color.Red, gradientFill = true
//            ),
//            LineChartData.SeriesData(
//                title = "Line B",
//                points = listOf(
//                    LineChartData.SeriesData.Point(0, 20f),
//                    LineChartData.SeriesData.Point(1, 10.0f),
//                    LineChartData.SeriesData.Point(2, 5.0f),
//                    LineChartData.SeriesData.Point(3, 15.0f),
//                    LineChartData.SeriesData.Point(4, 30.0f),
//                    LineChartData.SeriesData.Point(5, 35.0f),
//                ),
//                Color.Blue, gradientFill = true
//            ),
//        ),
//        yLabels = listOf(
//            AxisLabel(0f, "0K"),
//            AxisLabel(20f, "20K"),
//            AxisLabel(40f, "40K"),
//        ),
//        drawAxis = DrawAxis.X,
//        horizontalLines = true,
//        legendPosition = LegendPosition.End,
//        legendAlignment = LegendAlignment.Center,
//    ),
//    "Legend bottom, center alignment" to LineChartData(
//        series = listOf(
//            LineChartData.SeriesData(
//                title = "Line A",
//                points = listOf(
//                    LineChartData.SeriesData.Point(0, 0f),
//                    LineChartData.SeriesData.Point(1, 10.0f),
//                    LineChartData.SeriesData.Point(2, 20.0f),
//                    LineChartData.SeriesData.Point(3, 30.0f),
//                    LineChartData.SeriesData.Point(4, 50.0f),
//                    LineChartData.SeriesData.Point(5, 35.0f),
//                ),
//                Color.Red, gradientFill = true
//            ),
//            LineChartData.SeriesData(
//                title = "Line B",
//                points = listOf(
//                    LineChartData.SeriesData.Point(0, 20f),
//                    LineChartData.SeriesData.Point(1, 10.0f),
//                    LineChartData.SeriesData.Point(2, 5.0f),
//                    LineChartData.SeriesData.Point(3, 15.0f),
//                    LineChartData.SeriesData.Point(4, 30.0f),
//                    LineChartData.SeriesData.Point(5, 35.0f),
//                ),
//                Color.Blue, gradientFill = true
//            ),
//        ),
//        xLabels = listOf("A", "B", "C", "D", "E", "F"),
//        yLabels = listOf(
//            AxisLabel(0f, "0K"),
//            AxisLabel(20f, "20K"),
//            AxisLabel(40f, "40K"),
//        ),
//        drawAxis = DrawAxis.X,
//        horizontalLines = true,
//        legendPosition = LegendPosition.Bottom,
//        legendAlignment = LegendAlignment.Center,
//    ),
//    "Autogenerated Y-labels" to LineChartData(
//        series = listOf(
//            LineChartData.SeriesData(
//                title = "Line A",
//                points = listOf(
//                    LineChartData.SeriesData.Point(0, 0f),
//                    LineChartData.SeriesData.Point(1, 10.0f),
//                    LineChartData.SeriesData.Point(2, 20.0f),
//                    LineChartData.SeriesData.Point(3, 30.0f),
//                    LineChartData.SeriesData.Point(4, 50.0f),
//                    LineChartData.SeriesData.Point(5, 35.0f),
//                ),
//                Color.Red, gradientFill = true
//            ),
//            LineChartData.SeriesData(
//                title = "Line B",
//                points = listOf(
//                    LineChartData.SeriesData.Point(0, 20f),
//                    LineChartData.SeriesData.Point(1, 10.0f),
//                    LineChartData.SeriesData.Point(2, 5.0f),
//                    LineChartData.SeriesData.Point(3, 15.0f),
//                    LineChartData.SeriesData.Point(4, 30.0f),
//                    LineChartData.SeriesData.Point(5, 35.0f),
//                ),
//                Color.Blue, gradientFill = true
//            ),
//        ),
//        xLabels = listOf("A", "B", "C", "D", "E", "F"),
//        autoYLabels = true,
//        maxYLabels = 4,
//        drawAxis = DrawAxis.X,
//        horizontalLines = true,
//        legendPosition = LegendPosition.Bottom,
//        legendAlignment = LegendAlignment.Center,
//    ),
//    "Floating y-value" to LineChartData(
//        series = listOf(
//            LineChartData.SeriesData(
//                title = "Line A",
//                points = listOf(
//                    LineChartData.SeriesData.Point(0, 15_000f),
//                    LineChartData.SeriesData.Point(1, 10_000f),
//                    LineChartData.SeriesData.Point(2, 20_000f),
//                    LineChartData.SeriesData.Point(3, 30_000f),
//                    LineChartData.SeriesData.Point(4, 50_000f),
//                    LineChartData.SeriesData.Point(5, 35_000f),
//                ),
//                Color.Red, gradientFill = true
//            ),
//            LineChartData.SeriesData(
//                title = "Line B",
//                points = listOf(
//                    LineChartData.SeriesData.Point(0, 20_000f),
//                    LineChartData.SeriesData.Point(1, 10_000f),
//                    LineChartData.SeriesData.Point(2, 18_000f),
//                    LineChartData.SeriesData.Point(3, 15_000f),
//                    LineChartData.SeriesData.Point(4, 30_000f),
//                    LineChartData.SeriesData.Point(5, 35_000f),
//                ),
//                Color.Blue, gradientFill = true
//            ),
//        ),
//        xLabels = listOf("A", "B", "C", "D", "E", "F"),
//        autoYLabels = true,
//        maxYLabels = 4,
//        floatingYValue = true,
//        drawAxis = DrawAxis.X,
//        horizontalLines = true,
//        legendPosition = LegendPosition.Bottom,
//        legendAlignment = LegendAlignment.Center,
//    ),
//)
//
//private fun createBars(withColor: Boolean) = listOf(
//    listOf(12f, 2f, 3f, 2f),
//    listOf(3f, 2f, 4f, 5f),
//    listOf(1f, 4f, 12f, 5f),
//    listOf(1f, 20f, 2f, 1f),
//).mapIndexed { idx, values ->
//    StackedBarData(
//        title = AnnotatedString("Bars $idx"),
//        entries = values.mapIndexed { index, value ->
//            StackedBarEntry(
//                text = AnnotatedString(Categories[index]),
//                value = value,
//                color = SimpleColors[index].takeIf { withColor }
//            )
//        }
//    )
//}
