package com.example.bcsinvest.screen

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel: ViewModel() {

    val investPeriod = MutableLiveData(36f)
    val investSum = MutableLiveData(100000f)
    val currency = MutableLiveData(InvestCurrency.RUR)
    val billType = MutableLiveData<BillType>(BillType.IIS())
    val isRegularUp = MutableLiveData(false)
    val regularSum = MutableLiveData(1000f)

    val curState = MutableLiveData<State>()



    sealed class State(){
        object Loading: State()
        class Error(e: Exception): State()
        class Data()
    }



}

enum class InvestCurrency{
    RUR, USD
}

sealed class BillType(val value: String){
    class IIS(): BillType("ИИС")
    class Simple(): BillType("Обычный счет")
}