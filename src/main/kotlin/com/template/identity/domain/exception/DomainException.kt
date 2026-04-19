package com.template.identity.domain.exception

sealed class DomainException(
    message: String
) : RuntimeException(message) {
    class LastAuthMethodCannotBeRemoved :
        DomainException(
            "Cannot remove the last authentication method from an account"
        )
}