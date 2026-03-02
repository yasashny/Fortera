package com.yasashny.fortera.core.cryptoapi

import org.bouncycastle.crypto.digests.KeccakDigest
import org.bouncycastle.crypto.digests.RIPEMD160Digest
import org.bouncycastle.math.ec.ECPoint
import org.bouncycastle.asn1.x9.X9ECParameters
import org.bouncycastle.crypto.ec.CustomNamedCurves
import java.math.BigInteger
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object HdWallet {

    private val CURVE_PARAMS: X9ECParameters = CustomNamedCurves.getByName("secp256k1")
    private val CURVE_N: BigInteger = CURVE_PARAMS.n
    private val CURVE_G: ECPoint = CURVE_PARAMS.g

    // BIP44 path m/44'/60'/0'/0/0 for Ethereum
    fun deriveEthAddress(mnemonic: String): String {
        val seed = generateSeed(mnemonic)
        var (key, chain) = masterKey(seed)
        // m/44' (hardened)
        val path = listOf(44 or 0x80000000.toInt(), 60 or 0x80000000.toInt(), 0 or 0x80000000.toInt(), 0, 0)
        for (index in path) {
            val result = deriveChild(key, chain, index)
            key = result.first
            chain = result.second
        }
        val pubKey = publicKeyUncompressed(key)
        // Drop the 0x04 prefix, keccak256 the remaining 64 bytes, take last 20
        val pubKeyBody = pubKey.drop(1).toByteArray()
        val hash = keccak256(pubKeyBody)
        return "0x" + hash.drop(12).joinToString("") { "%02x".format(it) }
    }

    // BIP44 path m/44'/0'/0'/0/0 for Bitcoin (P2PKH mainnet)
    fun deriveBtcAddress(mnemonic: String): String {
        val seed = generateSeed(mnemonic)
        var (key, chain) = masterKey(seed)
        val path = listOf(44 or 0x80000000.toInt(), 0 or 0x80000000.toInt(), 0 or 0x80000000.toInt(), 0, 0)
        for (index in path) {
            val result = deriveChild(key, chain, index)
            key = result.first
            chain = result.second
        }
        val pubKey = publicKeyCompressed(key)
        val pubKeyHash = ripemd160(sha256(pubKey))
        return base58CheckEncode(byteArrayOf(0x00.toByte()) + pubKeyHash)
    }

    private fun generateSeed(mnemonic: String): ByteArray {
        val pass = mnemonic.toCharArray()
        val salt = "mnemonic".toByteArray(Charsets.UTF_8)
        val spec = PBEKeySpec(pass, salt, 2048, 512)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
        return factory.generateSecret(spec).encoded
    }

    private fun masterKey(seed: ByteArray): Pair<ByteArray, ByteArray> {
        val mac = Mac.getInstance("HmacSHA512")
        mac.init(SecretKeySpec("Bitcoin seed".toByteArray(Charsets.UTF_8), "HmacSHA512"))
        val result = mac.doFinal(seed)
        return result.copyOfRange(0, 32) to result.copyOfRange(32, 64)
    }

    private fun deriveChild(parentKey: ByteArray, parentChain: ByteArray, index: Int): Pair<ByteArray, ByteArray> {
        val mac = Mac.getInstance("HmacSHA512")
        mac.init(SecretKeySpec(parentChain, "HmacSHA512"))

        val data = if (index < 0) {
            // hardened: 0x00 || parent_key || index
            byteArrayOf(0x00) + parentKey + index.toByteArrayBE()
        } else {
            // normal: compressed_pubkey || index
            publicKeyCompressed(parentKey) + index.toByteArrayBE()
        }

        val result = mac.doFinal(data)
        val il = result.copyOfRange(0, 32)
        val ir = result.copyOfRange(32, 64)

        val ilInt = BigInteger(1, il)
        val parentKeyInt = BigInteger(1, parentKey)
        val childKey = ilInt.add(parentKeyInt).mod(CURVE_N)

        return childKey.toByteArrayPadded(32) to ir
    }

    private fun publicKeyCompressed(privateKey: ByteArray): ByteArray {
        val point = CURVE_G.multiply(BigInteger(1, privateKey)).normalize()
        return point.getEncoded(true)
    }

    private fun publicKeyUncompressed(privateKey: ByteArray): List<Byte> {
        val point = CURVE_G.multiply(BigInteger(1, privateKey)).normalize()
        return point.getEncoded(false).toList()
    }

    private fun keccak256(input: ByteArray): List<Byte> {
        val digest = KeccakDigest(256)
        digest.update(input, 0, input.size)
        val result = ByteArray(32)
        digest.doFinal(result, 0)
        return result.toList()
    }

    private fun sha256(input: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(input)

    private fun ripemd160(input: ByteArray): ByteArray {
        val digest = RIPEMD160Digest()
        digest.update(input, 0, input.size)
        val result = ByteArray(20)
        digest.doFinal(result, 0)
        return result
    }

    private fun base58CheckEncode(payload: ByteArray): String {
        val checksum = sha256(sha256(payload)).copyOfRange(0, 4)
        val full = payload + checksum
        return base58Encode(full)
    }

    private fun base58Encode(input: ByteArray): String {
        val alphabet = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
        var num = BigInteger(1, input)
        val sb = StringBuilder()
        val base = BigInteger.valueOf(58)
        while (num > BigInteger.ZERO) {
            val (quotient, remainder) = num.divideAndRemainder(base)
            sb.append(alphabet[remainder.toInt()])
            num = quotient
        }
        // Leading zeros
        for (byte in input) {
            if (byte == 0.toByte()) sb.append(alphabet[0]) else break
        }
        return sb.reverse().toString()
    }

    private fun Int.toByteArrayBE(): ByteArray = byteArrayOf(
        (this shr 24).toByte(),
        (this shr 16).toByte(),
        (this shr 8).toByte(),
        this.toByte(),
    )

    private fun BigInteger.toByteArrayPadded(size: Int): ByteArray {
        val bytes = toByteArray()
        return when {
            bytes.size == size -> bytes
            bytes.size > size -> bytes.copyOfRange(bytes.size - size, bytes.size)
            else -> ByteArray(size - bytes.size) + bytes
        }
    }
}
