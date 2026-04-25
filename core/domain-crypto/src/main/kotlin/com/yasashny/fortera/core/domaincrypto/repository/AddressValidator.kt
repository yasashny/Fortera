package com.yasashny.fortera.core.domaincrypto.repository

import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork

interface AddressValidator {
    suspend fun isValid(address: String, network: BlockchainNetwork): Boolean
}
