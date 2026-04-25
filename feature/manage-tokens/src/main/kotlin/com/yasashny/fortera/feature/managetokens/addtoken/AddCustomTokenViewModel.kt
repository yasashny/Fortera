package com.yasashny.fortera.feature.managetokens.addtoken

import com.yasashny.fortera.core.domain.wallet.WalletInteractor
import com.yasashny.fortera.core.domaincrypto.model.CustomTokenMetadata
import com.yasashny.fortera.core.domaincrypto.model.TokenDefinition
import com.yasashny.fortera.core.domaincrypto.repository.TokenMetadataFetcher
import com.yasashny.fortera.core.domaincrypto.repository.TokenRepository
import com.yasashny.fortera.core.mvi.MviViewModel
import com.yasashny.fortera.core.ui.text.UiText
import com.yasashny.fortera.feature.managetokens.R
import com.yasashny.fortera.feature.managetokens.addtoken.AddCustomTokenContract.Effect
import com.yasashny.fortera.feature.managetokens.addtoken.AddCustomTokenContract.Intent
import com.yasashny.fortera.feature.managetokens.addtoken.AddCustomTokenContract.State
import com.yasashny.fortera.feature.managetokens.addtoken.AddCustomTokenContract.Verification
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

class AddCustomTokenViewModel(
    private val tokenRepository: TokenRepository,
    private val tokenMetadataFetcher: TokenMetadataFetcher,
    private val walletInteractor: WalletInteractor,
) : MviViewModel<State, Intent, Effect>(State()) {

    private var verificationJob: Job? = null

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.InputChanged -> onInputChanged(intent.value)
            Intent.AddClicked -> onAddClicked()
        }
    }

    private fun onInputChanged(value: String) {
        verificationJob?.cancel()
        val trimmed = value.trim()
        when {
            trimmed.isEmpty() -> updateState { it.copy(input = value, verification = Verification.Idle) }
            !ETH_ADDRESS_REGEX.matches(trimmed) -> updateState {
                if (trimmed.length >= MIN_LENGTH_FOR_INVALID_HINT) {
                    it.copy(
                        input = value,
                        verification = Verification.Failed(UiText.of(R.string.add_token_error_invalid_address)),
                    )
                } else {
                    it.copy(input = value, verification = Verification.Idle)
                }
            }
            else -> {
                updateState { it.copy(input = value, verification = Verification.InProgress) }
                verificationJob = scheduleVerification(trimmed)
            }
        }
    }

    private fun scheduleVerification(input: String) = intent {
        launch {
            delay(VERIFICATION_DEBOUNCE_MS)
            if (currentState.input.trim() != input) return@launch

            val result = tokenMetadataFetcher.fetch(input)

            if (currentState.input.trim() != input) return@launch

            val verification = result.fold(
                onSuccess = { metadata ->
                    val existing = tokenRepository.findTokenByContract(metadata.contractAddress)
                    Verification.Verified(metadata = metadata, alreadyAdded = existing != null)
                },
                onFailure = { Verification.Failed(UiText.of(R.string.add_token_error_not_found)) },
            )
            updateState { it.copy(verification = verification) }
        }
    }

    private fun onAddClicked() {
        val verified = currentState.verification as? Verification.Verified ?: return
        if (currentState.isAdding) return
        intent {
            updateState { it.copy(isAdding = true) }
            launch {
                val wallet = walletInteractor.observeActiveWallet().first()
                if (wallet == null) {
                    updateState { it.copy(isAdding = false) }
                    sendEffect(Effect.ShowError(UiText.of(R.string.add_token_error_network)))
                    return@launch
                }

                val token = if (verified.alreadyAdded) {
                    tokenRepository.findTokenByContract(verified.metadata.contractAddress)!!
                } else {
                    val newToken = verified.metadata.toTokenDefinition()
                    tokenRepository.addCustomToken(newToken)
                    newToken
                }
                tokenRepository.setTokenEnabled(wallet.id, token.id, true)
                sendEffect(Effect.NavigateBack)
            }
        }
    }

    private fun CustomTokenMetadata.toTokenDefinition(): TokenDefinition = TokenDefinition(
        id = coingeckoId ?: "custom-${contractAddress.lowercase()}",
        name = name,
        symbol = symbol,
        network = network,
        decimals = decimals,
        contractAddress = contractAddress,
        coingeckoId = coingeckoId.orEmpty(),
        isDefault = false,
    )

    private companion object {
        val ETH_ADDRESS_REGEX = Regex("^0x[a-fA-F0-9]{40}$")
        const val VERIFICATION_DEBOUNCE_MS = 350L
        const val MIN_LENGTH_FOR_INVALID_HINT = 8
    }
}
