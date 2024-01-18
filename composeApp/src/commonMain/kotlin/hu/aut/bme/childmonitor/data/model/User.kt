package hu.aut.bme.childmonitor.data.model

data class User(
    val uid: String,
    val email: String,
    val token: String = "",
)