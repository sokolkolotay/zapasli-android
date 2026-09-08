package ru.zapasli.app.core.model

enum class ExpiryState {
    Fresh,
    Soon,
    Today,
    Expired,
    NoDate,
}

fun expiryState(daysUntilExpiry: Int?): ExpiryState = when {
    daysUntilExpiry == null -> ExpiryState.NoDate
    daysUntilExpiry < 0 -> ExpiryState.Expired
    daysUntilExpiry == 0 -> ExpiryState.Today
    daysUntilExpiry <= 3 -> ExpiryState.Soon
    else -> ExpiryState.Fresh
}
