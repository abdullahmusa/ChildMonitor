package hu.aut.bme.childmonitor.data.model

data class VideoStream(
    val url: String,
    val title: String = url,
)
