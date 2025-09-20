package com.example.mdpremotecontroller.nav

sealed class Screen(val route: String) {
    object Connect : Screen("connect")
    object Control : Screen("control")
    object Map : Screen("map")
}
