package com.yasashny.fortera.feature.receive.send.ui

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit-тесты для парсера адреса, извлечённого из QR-кода на экране отправки.
 *
 * Парсер должен корректно обрабатывать:
 * - голый адрес (Ethereum / Bitcoin);
 * - URI-схемы BIP-21 / EIP-681 ("bitcoin:...", "ethereum:...");
 * - параметры запроса после "?" (amount, value, gas и т.п.);
 * - пробелы по краям строки.
 */
class ParseScannedAddressTest {

    @Test
    fun `plain Ethereum address is returned unchanged`() {
        val raw = "0x1234567890abcdefABCDEF1234567890abcdef12"

        assertEquals(raw, parseScannedAddress(raw))
    }

    @Test
    fun `plain Bitcoin address is returned unchanged`() {
        val raw = "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq"

        assertEquals(raw, parseScannedAddress(raw))
    }

    @Test
    fun `ethereum scheme prefix is stripped`() {
        val raw = "ethereum:0x1234567890abcdefABCDEF1234567890abcdef12"

        assertEquals(
            "0x1234567890abcdefABCDEF1234567890abcdef12",
            parseScannedAddress(raw),
        )
    }

    @Test
    fun `bitcoin scheme prefix is stripped`() {
        val raw = "bitcoin:bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq"

        assertEquals(
            "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq",
            parseScannedAddress(raw),
        )
    }

    @Test
    fun `EIP-681 query parameters are dropped`() {
        val raw =
            "ethereum:0x1234567890abcdefABCDEF1234567890abcdef12?value=1e18&gas=21000"

        assertEquals(
            "0x1234567890abcdefABCDEF1234567890abcdef12",
            parseScannedAddress(raw),
        )
    }

    @Test
    fun `BIP-21 amount parameter is dropped`() {
        val raw =
            "bitcoin:bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq?amount=0.001&label=tip"

        assertEquals(
            "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq",
            parseScannedAddress(raw),
        )
    }

    @Test
    fun `surrounding whitespace is trimmed`() {
        val raw = "  0x1234567890abcdefABCDEF1234567890abcdef12  "

        assertEquals(
            "0x1234567890abcdefABCDEF1234567890abcdef12",
            parseScannedAddress(raw),
        )
    }

    @Test
    fun `whitespace inside scheme and query is trimmed at edges only`() {
        val raw = "  ethereum:0xABCDef00112233445566778899aabbccddeeff00?value=10  "

        assertEquals(
            "0xABCDef00112233445566778899aabbccddeeff00",
            parseScannedAddress(raw),
        )
    }

    @Test
    fun `EIP-681 with target function preserves the recipient address only`() {
        // ethereum:<contract>/transfer?address=0x..&uint256=1 — для нашего MVP мы
        // ожидаем, что в поле адреса попадёт contract, а параметры будут отброшены.
        val raw =
            "ethereum:0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48/transfer?address=" +
                "0xBEef00000000000000000000000000000000c0DE&uint256=1000000"

        assertEquals(
            "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48/transfer",
            parseScannedAddress(raw),
        )
    }

    @Test
    fun `empty string yields empty string`() {
        assertEquals("", parseScannedAddress(""))
    }
}
