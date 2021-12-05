package com.example.bcsinvest.screen.graph

import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavHostController
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

import com.example.bcsinvest.screen.LoadingView
import java.lang.IllegalStateException


@Composable
fun NewGraphView(navController: NavHostController, graphViewModel: GraphViewModel) {


    val curState = graphViewModel.curState.observeAsState()
    val needLoad = graphViewModel.needLoad.observeAsState()


    Crossfade(targetState = curState.value) {state ->
        when(state){
            is GraphViewModel.State.Loading ->{
                LoadingView()
            }
            is GraphViewModel.State.Data -> {
                Column {
                    Text(text = state.data.firstAfterSum.toString())
                }
            }
            is GraphViewModel.State.Error -> {
                Text(text = state.e.localizedMessage ?: state.e.message!!)
            }
            else ->{
                throw IllegalStateException("Невозможное состояние")
            }
        }

    }

    LaunchedEffect(key1 = needLoad.value){
        if (needLoad.value!!){
            graphViewModel.needLoad.value = false
            graphViewModel.getData()
        }
    }
}