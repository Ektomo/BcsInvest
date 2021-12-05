package com.example.bcsinvest.navigation

sealed class NavigationItem(var route: String) {
    // объектры основных пакетов приложения
    object Graphic : NavigationItem("graphic")
    object Bag : NavigationItem("bag")
    object Settings : NavigationItem("settings")
    object CreateRecord : NavigationItem("createRecord")
    object Comments: NavigationItem("comments")
}