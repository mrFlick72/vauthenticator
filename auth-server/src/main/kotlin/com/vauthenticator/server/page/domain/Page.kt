package com.vauthenticator.server.page.domain

/**
 * A screen the authorization server shows to an account holder in the browser, delivered as one compiled bundle.
 * The bundle names match the webpack entries in `src/main/frontend/webpack.config.js`.
 */
enum class Page(val bundle: String) {
    LOGIN("login_bundle.js"),
    MFA("mfa_bundle.js"),
    CHANGE_PASSWORD("changePassword_bundle.js"),
    SIGN_UP("signup_bundle.js"),
    SUCCESSFUL_SIGN_UP("successfulSignUp_bundle.js"),
    SUCCESSFUL_MAIL_VERIFY("successfulMailVerify_bundle.js"),
    RESET_PASSWORD_CHALLENGE_SENDER("resetPasswordChallengeSender_bundle.js"),
    SUCCESSFUL_RESET_PASSWORD_MAIL_CHALLENGE("successfulResetPasswordMailChallenge_bundle.js"),
    RESET_PASSWORD("resetPassword_bundle.js"),
    SUCCESSFUL_PASSWORD_RESET("successfulPasswordReset_bundle.js"),
    DEFAULT_ERROR("default_error_bundle.js"),
    BAD_REQUEST_ERROR("400_error_bundle.js"),
    NOT_FOUND_ERROR("404_error_bundle.js"),
    SERVER_ERROR("500_error_bundle.js");
}

/**
 * Where a page bundle (or any other static asset) lives in the document storage: under `content/asset`, prefixed by
 * the bundle version when one is configured. Both serving and tenant setup resolve the path here, so they never
 * disagree.
 */
class PageLocation(private val bundleVersion: String) {
    fun pathOf(assetName: String): String =
        if (bundleVersion.isBlank()) "content/asset/$assetName" else "content/asset/$bundleVersion/$assetName"
}
