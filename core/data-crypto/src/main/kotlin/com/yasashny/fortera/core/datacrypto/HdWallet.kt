package com.yasashny.fortera.core.datacrypto

import org.bitcoinj.core.ECKey
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.core.SegwitAddress
import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.crypto.HDKeyDerivation
import org.bitcoinj.params.MainNetParams
import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.Credentials
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * BIP-39 → BIP-32 derivation using web3j (ETH, m/44'/60'/0'/0/0) and
 * bitcoinj (BTC, m/84'/0'/0'/0/0 → P2WPKH). Bitcoin address is network-dependent
 * — pass [MainNetParams] for mainnet (bc1…) or [TestNet3Params] for testnet (tb1…).
 */
internal object HdWallet {

    private val ETH_PATH = intArrayOf(
        44 or HARDENED,
        60 or HARDENED,
        0 or HARDENED,
        0,
        0,
    )

    private fun btcPath(params: NetworkParameters): List<ChildNumber> {
        val coinType = if (params.id == NetworkParameters.ID_MAINNET) 0 else 1
        return listOf(
            ChildNumber(84, true),
            ChildNumber(coinType, true),
            ChildNumber(0, true),
            ChildNumber(0, false),
            ChildNumber(0, false),
        )
    }

    fun deriveEthAddress(mnemonic: String): String = ethKeys(mnemonic).address

    fun deriveBtcAddress(
        mnemonic: String,
        params: NetworkParameters = MainNetParams.get(),
    ): String = btcKeys(mnemonic, params).address

    fun ethKeys(mnemonic: String): EthKeys {
        val seed = mnemonicToSeed(mnemonic)
        val master = Bip32ECKeyPair.generateKeyPair(seed)
        val derived = Bip32ECKeyPair.deriveKeyPair(master, ETH_PATH)
        val credentials = Credentials.create(derived)
        return EthKeys(credentials = credentials, address = credentials.address)
    }

    fun btcKeys(
        mnemonic: String,
        params: NetworkParameters = MainNetParams.get(),
    ): BtcKeys {
        val seed = mnemonicToSeed(mnemonic)
        var key: DeterministicKey = HDKeyDerivation.createMasterPrivateKey(seed)
        for (step in btcPath(params)) {
            key = HDKeyDerivation.deriveChildKey(key, step)
        }
        val ecKey = ECKey.fromPrivate(key.privKey)
        val address = SegwitAddress.fromKey(params, ecKey).toBech32()
        return BtcKeys(ecKey = ecKey, address = address)
    }

    private fun mnemonicToSeed(mnemonic: String): ByteArray {
        val spec = PBEKeySpec(
            mnemonic.toCharArray(),
            "mnemonic".toByteArray(Charsets.UTF_8),
            BIP39_PBKDF2_ROUNDS,
            BIP39_SEED_BITS,
        )
        return SecretKeyFactory
            .getInstance("PBKDF2WithHmacSHA512")
            .generateSecret(spec)
            .encoded
    }
}

internal data class EthKeys(
    val credentials: Credentials,
    val address: String,
)

internal data class BtcKeys(
    val ecKey: ECKey,
    val address: String,
)

private const val HARDENED = 0x80000000.toInt()
private const val BIP39_PBKDF2_ROUNDS = 2048
private const val BIP39_SEED_BITS = 512
