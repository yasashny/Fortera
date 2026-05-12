package com.yasashny.fortera.core.domaincrypto

import com.yasashny.fortera.core.domaincrypto.model.BlockchainNetwork
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TokenCatalogTest {

    @Test
    fun `tokens list contains the well-known catalog entries`() {
        val ids = TokenCatalog.tokens.map { it.id }.toSet()

        assertTrue("bitcoin" in ids)
        assertTrue("ethereum" in ids)
        assertTrue("tether" in ids)
        assertTrue("usd-coin" in ids)
        assertTrue("dai" in ids)
    }

    @Test
    fun `defaultTokenIds includes only entries marked isDefault`() {
        val expected = TokenCatalog.tokens.filter { it.isDefault }.map { it.id }.toSet()

        assertEquals(expected, TokenCatalog.defaultTokenIds)
        assertTrue("bitcoin" in TokenCatalog.defaultTokenIds)
        assertTrue("usd-coin" in TokenCatalog.defaultTokenIds)
        assertTrue("dai" !in TokenCatalog.defaultTokenIds)
    }

    @Test
    fun `nativeToken returns the token without a contract address for the network`() {
        val btc = TokenCatalog.nativeToken(BlockchainNetwork.BITCOIN)

        assertNotNull(btc)
        assertEquals("bitcoin", btc!!.id)
        assertNull(btc.contractAddress)
        assertEquals(BlockchainNetwork.BITCOIN, btc.network)
    }

    @Test
    fun `nativeToken for ETHEREUM returns ether without a contract address`() {
        val eth = TokenCatalog.nativeToken(BlockchainNetwork.ETHEREUM)

        assertNotNull(eth)
        assertEquals("ethereum", eth!!.id)
        assertNull(eth.contractAddress)
    }

    @Test
    fun `token symbols match expected display names`() {
        assertEquals("BTC", TokenCatalog.tokens.first { it.id == "bitcoin" }.symbol)
        assertEquals("ETH", TokenCatalog.tokens.first { it.id == "ethereum" }.symbol)
        assertEquals("USDT", TokenCatalog.tokens.first { it.id == "tether" }.symbol)
    }
}
