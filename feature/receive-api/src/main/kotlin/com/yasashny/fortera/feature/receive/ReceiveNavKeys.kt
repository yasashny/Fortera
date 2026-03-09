package com.yasashny.fortera.feature.receive

import kotlinx.serialization.Serializable

@Serializable
data object SelectTokenForReceive

@Serializable
data class ReceiveToken(val tokenId: String)

@Serializable
data object SelectTokenForSend

@Serializable
data class SendToken(val tokenId: String)

@Serializable
data class ConfirmSend(
    val tokenId: String,
    val amount: String,
    val address: String,
)

@Serializable
data object SendSuccess
