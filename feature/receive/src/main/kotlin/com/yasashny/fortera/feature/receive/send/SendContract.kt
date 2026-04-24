package com.yasashny.fortera.feature.receive.send

import com.yasashny.fortera.core.mvi.UiEffect
import com.yasashny.fortera.core.mvi.UiIntent
import com.yasashny.fortera.core.mvi.UiState
import com.yasashny.fortera.core.ui.text.UiText
import java.math.BigDecimal

internal object SendContract {

    /**
     * [amount] is stored verbatim (so a user typing "1,5" keeps seeing "1,5") — normalisation
     * to `.` happens in the ViewModel when parsing to [BigDecimal] and when forwarding to the
     * confirm screen.
     *
     * [amountUsd] is the USD-canonical equivalent of [amount] (raw `Double`, null when the input
     * can't be parsed). The Layout formats it with the currently selected currency — ViewModels
     * don't see currency changes reactively, so storing a pre-formatted string here would go
     * stale the moment the user changes currency in settings.
     *
     * [insufficientFunds] — true when parsed amount exceeds [balance]; paints the amount field
     * red and surfaces a hint.
     * [addressError] — non-null only when the user has typed something invalid for the token's
     * network. Null when empty (to avoid error-shaming before input).
     */
    data class State(
        val tokenId: String = "",
        val tokenName: String = "",
        val tokenSymbol: String = "",
        val balance: BigDecimal = BigDecimal.ZERO,
        val priceUsd: Double = 0.0,
        val amount: String = "",
        val amountUsd: Double? = null,
        val address: String = "",
        val insufficientFunds: Boolean = false,
        /**
         * Set when the user tries to type something the amount field won't accept (letters,
         * a second decimal separator). Shown under the amount field, same styling as
         * [addressError]. Clears automatically on the next clean keystroke.
         */
        val amountError: UiText? = null,
        val addressError: UiText? = null,
        val isLoading: Boolean = true,
    ) : UiState {
        val isAmountValid: Boolean
            get() = amount.normalizeDecimal().toBigDecimalOrNull()?.let { it > BigDecimal.ZERO } == true

        val canContinue: Boolean
            get() = isAmountValid &&
                address.isNotBlank() &&
                !insufficientFunds &&
                amountError == null &&
                addressError == null
    }

    sealed interface Intent : UiIntent {
        data class UpdateAmount(val amount: String) : Intent
        data class UpdateAddress(val address: String) : Intent
        data object Continue : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
        data class NavigateToConfirm(
            val tokenId: String,
            val amount: String,
            val address: String,
        ) : Effect
    }
}

/** Replace locale-decimal ',' with the canonical '.' used by [BigDecimal]. */
internal fun String.normalizeDecimal(): String = replace(',', '.')
