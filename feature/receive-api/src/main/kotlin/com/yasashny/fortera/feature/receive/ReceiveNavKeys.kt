package com.yasashny.fortera.feature.receive

import kotlinx.serialization.Serializable

@Serializable
data object SelectTokenForReceive

@Serializable
data class ReceiveToken(val tokenId: String)
