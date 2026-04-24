package com.yasashny.fortera.core.ui.currency

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The active [FiatDisplay] for the current Compose tree — selected currency + live rate.
 *
 * Provided once at the composition root (see `MainActivity`) from the combination of
 * [com.yasashny.fortera.core.network.currency.CurrencyRepository] (user's picked currency)
 * and [com.yasashny.fortera.core.network.currency.FiatRateRepository] (current USD→currency
 * rate). Every composable rendering a fiat value reads `LocalFiat.current` and passes it to
 * [com.yasashny.fortera.core.ui.format.formatFiat]. Changing either the selection or the rate
 * triggers one top-level recomposition that propagates through the whole tree.
 *
 * ViewModels should NOT pre-format fiat values into state strings — they don't observe rate or
 * currency changes reactively. Hold raw USD-canonical `Double`s and format at the leaf.
 */
val LocalFiat = staticCompositionLocalOf { FiatDisplay.Default }
