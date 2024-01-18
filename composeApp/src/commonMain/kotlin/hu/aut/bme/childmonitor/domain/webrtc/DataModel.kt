package hu.aut.bme.childmonitor.domain.webrtc

import kotlinx.serialization.Serializable

enum class DataModelType {
    Offer, Answer, IceCandidates, RequestOffer
}

@Serializable
data class DataModel(
    val type: DataModelType,
    val sender: String,
    val target: String,
    val data: String? = null,
)