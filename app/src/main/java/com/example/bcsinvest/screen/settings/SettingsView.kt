package com.example.bcsinvest.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bcsinvest.data.BillType
import com.example.bcsinvest.data.InvestCurrency
import com.example.bcsinvest.format
import com.example.bcsinvest.formatByNumber
import com.example.bcsinvest.navigation.NavigationItem
import com.example.bcsinvest.screen.graph.GraphViewModel

@Composable
fun SettingsView(navController: NavController, viewModel: SettingsViewModel, graphViewModel: GraphViewModel) {
    val investPeriod = viewModel.investPeriod.observeAsState()
    val investSum = viewModel.investSum.observeAsState()
    val currency = viewModel.currency.observeAsState()
    val billType = viewModel.billType.observeAsState()
    val isRegularUp = viewModel.isRegularUp.observeAsState()
    val regularSum = viewModel.regularSum.observeAsState()
    val startLoading = remember {
        mutableStateOf(false)
    }
//    val yearPeriod = remember {
//        mutableStateOf(viewModel.investPeriod.value?.div(12).toString())
//    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        item {
            Text(
                text = "Срок инвестиций: ${(investPeriod.value?.div(12)!!.format(1))} ${
                    if (investPeriod.value?.div(12)!!.format(1).split(",").first().endsWith("1")) {
                        "год"
                    } else if (investPeriod.value?.div(12)!!.format(1).split(",").first()
                            .endsWith("2")
                        || investPeriod.value?.div(12)!!.format(1).split(",").first().endsWith("3")
                        || investPeriod.value?.div(12)!!.format(1).split(",").first().endsWith("4")
                    ) {
                        "года"
                    } else {
                        "лет"
                    }
                }",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 16.dp),
                textAlign = TextAlign.Start,
                fontSize = 18.sp
            )
            Slider(
                value = investPeriod.value!!,
                onValueChange = { viewModel.investPeriod.value = it },
                valueRange = if (billType.value!!.value == "ИИС") 36f..120f else 12f..120f,
            )
        }

        item {
            Spacer(modifier = Modifier.padding(16.dp))

            Text(
                text = "Валюта инвестиций: ",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 16.dp),
                textAlign = TextAlign.Start,
                fontSize = 18.sp
            )
            SwitchValueCurrency(
                list = listOf(InvestCurrency.RUR.name, InvestCurrency.USD.name),
                currency,
                viewModel
            )
        }

        item {
            Spacer(modifier = Modifier.padding(16.dp))
            Text(
                text = "Сумма инвестиций: ${
                    investSum.value!!.format(0).formatByNumber(" ")
                } ${if (currency.value == InvestCurrency.RUR) "Р" else "\$"}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 16.dp),
                textAlign = TextAlign.Start,
                fontSize = 18.sp
            )
            Slider(
                value = investSum.value!!,
                onValueChange = { viewModel.investSum.value = it },
                valueRange = 1f..5000000f
            )
        }

        item {
            Spacer(modifier = Modifier.padding(16.dp))

            Text(
                text = "Тип счета: ",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 16.dp),
                textAlign = TextAlign.Start,
                fontSize = 18.sp
            )
            SwitchValueBill(
                list = listOf(BillType.IIS().value, BillType.Simple().value),
                billType,
                viewModel
            )
        }


        item {
            Spacer(modifier = Modifier.padding(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isRegularUp.value!!,
                    onCheckedChange = {
                        viewModel.isRegularUp.value = !viewModel.isRegularUp.value!!
                    },
                    modifier = Modifier.padding(start = 8.dp)
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text(
                    text = "Регулярное пополнение: ",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp),
                    textAlign = TextAlign.Start,
                    fontSize = 18.sp
                )
            }
        }

        item {
            Text(
                text = "Сумма пополнения в месяц: ${
                    regularSum.value!!.format(0).formatByNumber(" ")
                } ${if (currency.value == InvestCurrency.RUR) "Р" else "\$"}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 16.dp),
                textAlign = TextAlign.Start,
                fontSize = 18.sp
            )
            Slider(
                value = regularSum.value!!,
                onValueChange = { viewModel.regularSum.value = it },
                valueRange = 0f..300000f,
                enabled = isRegularUp.value!!
            )

            Spacer(modifier = Modifier.padding(16.dp))

        }

        item {

            Button(onClick = { startLoading.value = true }) {
                Text(
                    text = "Собрать портфель",
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.padding(44.dp))
        }


    }

    LaunchedEffect(key1 = startLoading.value) {
        if (startLoading.value) {
            graphViewModel.billType.postValue(billType.value)
            graphViewModel.currency.postValue(currency.value)
            graphViewModel.investPeriod.postValue(investPeriod.value)
            graphViewModel.investSum.postValue(investSum.value)
            graphViewModel.isRegularUp.postValue(isRegularUp.value)
            graphViewModel.regularSum.postValue(regularSum.value)
            graphViewModel.needLoad.postValue(true)
            navController.navigate(NavigationItem.Graphic.route)
            startLoading.value = false
        }
    }


}

@Composable
fun SwitchValueCurrency(
    list: List<String>,
    choose: State<InvestCurrency?>,
    viewModel: SettingsViewModel
) {

    Row(
        modifier = Modifier
            .padding(horizontal = 50.dp, vertical = 16.dp)
            .fillMaxWidth(), horizontalArrangement = Arrangement.Center
    ) {
        list.forEachIndexed { i, name ->
            if (name == choose.value!!.name) {
                Button(
                    onClick = { }, colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFFB1CAF7),
                    )
                ) {
                    Text(text = name, fontSize = 18.sp, modifier = Modifier)
                }
            } else {
                Button(
                    onClick = { viewModel.currency.value = InvestCurrency.valueOf(name) },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Transparent,
                    )
                ) {
                    Text(text = name, fontSize = 18.sp, modifier = Modifier)
                }
            }
        }
    }
}

@Composable
fun SwitchValueBill(list: List<String>, choose: State<BillType?>, viewModel: SettingsViewModel) {

    Row(
        modifier = Modifier
            .padding(vertical = 16.dp)
            .fillMaxWidth(), horizontalArrangement = Arrangement.Center
    ) {
        list.forEachIndexed { i, name ->
            if (name == choose.value!!.value) {
                Button(
                    onClick = { }, colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFFB1CAF7),
                    )
                ) {
                    Text(text = name, fontSize = 18.sp, modifier = Modifier)
                }
            } else {
                Button(
                    onClick = {
                        if (name == "ИИС") {
                            viewModel.billType.value = BillType.IIS()
                        } else {
                            viewModel.billType.value = BillType.Simple()
                        }

                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Transparent,
                    )
                ) {
                    Text(text = name, fontSize = 18.sp, modifier = Modifier)
                }
            }
        }
    }
}

