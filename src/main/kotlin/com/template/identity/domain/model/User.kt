package com.template.identity.domain.model

import com.template.identity.domain.exception.DomainException
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false, unique = true)
    var email: String,

    /** Null for OAuth-only accounts that have never set a password. */
    @Column
    var password: String? = null,

    @Column(name = "first_name", nullable = false)
    var firstName: String,

    @Column(name = "last_name", nullable = false)
    var lastName: String,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,
) : AuditableEntity() {

    /** Returns true if the user can authenticate with a password (not OAuth-only). */
    fun hasPassword(): Boolean = password != null

    /**
     * Enforces the invariant that an account must always have at least one authentication method.
     * Call before removing an OAuth account or clearing the password.
     *
     * @param remainingOAuthAccountCount the count of OAuth accounts that will remain after removal.
     * @throws DomainException.LastAuthMethodCannotBeRemoved if removing would leave the account
     *   with no way to log in.
     */
    fun requireCanRemoveAuthMethod(remainingOAuthAccountCount: Int) {
        val willHaveNoAuthMethod = !hasPassword() && remainingOAuthAccountCount == 0
        if (willHaveNoAuthMethod) throw DomainException.LastAuthMethodCannotBeRemoved()
    }
}
