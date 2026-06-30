package pl.tajchert.paczko.fast.core.model.auth

data class PhoneNumber(
    val prefix: String,
    val value: String,
) {
    init {
        require(prefix.isNotBlank())
        require(value.isNotBlank())
        require(prefix.none { it == '+' })
    }
}
