package com.vauthenticator.server.password.domain

fun interface PasswordPolicy {

    fun accept(userName: String, password: String)
}

class PasswordPolicyViolation(message: String) : RuntimeException(message)

class CompositePasswordPolicy(private val passwordPolicies: Set<PasswordPolicy>) : PasswordPolicy {
    override fun accept(userName: String, password: String) {
        passwordPolicies.forEach { it.accept(userName, password) }
    }
}

class SpecialCharacterPasswordPolicy(private val minSpecialSymbol: Int) : PasswordPolicy {
    private val pattern = "^[A-Za-z0-9\\s]+".toRegex()


    override fun accept(userName: String, password: String) {
        val counter = password.count { c -> !pattern.matches(c.toString()) }
        if (counter < minSpecialSymbol) {
            throw PasswordPolicyViolation("the password should has at least $minSpecialSymbol special character like '!£\$%&/()=?. In your password you have $counter'")
        }
    }
}

class MinimumCharacterPasswordPolicy(private val size: Int) : PasswordPolicy {

    override fun accept(userName: String, password: String) {
        if (password.length < size) {
            throw PasswordPolicyViolation("the password size should be at least $size")
        }
    }
}

class ReusePreventionPasswordPolicy(
    private val passwordEncoder: VAuthenticatorPasswordEncoder,
    private val passwordHistoryRepository: PasswordHistoryRepository
) : PasswordPolicy {


    override fun accept(userName: String, password: String) {
        val passwordHistory = passwordHistoryRepository.load(userName)

        passwordHistory.forEach {
            if (passwordEncoder.matches(password, it.content)) {
                throw PasswordPolicyViolation("the password is already used in the past please consider to change password")

            }
        }
    }
}


