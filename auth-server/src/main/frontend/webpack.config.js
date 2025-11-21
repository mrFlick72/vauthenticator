var path = require('path');

const BUID_DIR = path.resolve(__dirname + "/dist");

module.exports = {
    entry: {
        "default_error": path.resolve(__dirname, './app/errors/DefaultGenericErrorPage.tsx'),
        "404_error": path.resolve(__dirname, './app/errors/DefaultGenericErrorPage.tsx'),
        "400_error": path.resolve(__dirname, './app/errors/DefaultGenericErrorPage.tsx'),
        "500_error": path.resolve(__dirname, './app/errors/DefaultGenericErrorPage.tsx'),
        login: path.resolve(__dirname, './app/login/LoginPage.tsx'),
        mfa: path.resolve(__dirname, './app/mfa/index.tsx'),
        changePassword: path.resolve(__dirname, './app/change-password/index.tsx'),
        signup: path.resolve(__dirname, './app/signup/SignUpPage.tsx'),
        successfulSignUp: path.resolve(__dirname, './app/signup/SuccessfulSignUpPage.tsx'),
        successfulMailVerify: path.resolve(__dirname, './app/email-verify/SuccessfulEMailVerifyPage.tsx'),
        resetPasswordChallengeSender: path.resolve(__dirname, './app/reset-password/ResetPasswordChallengeSenderPage.tsx'),
        successfulResetPasswordMailChallenge: path.resolve(__dirname, './app/reset-password/SuccessfulResetPasswordEMailChallengePage.tsx'),
        resetPassword: path.resolve(__dirname, './app/reset-password/ResetPasswordPage.tsx'),
        successfulPasswordReset: path.resolve(__dirname, './app/reset-password/SuccessfulPasswordReset.tsx')
    },
    resolve: {
        extensions: ['.tsx', '.ts', ".js", ".jsx"]
    },
    plugins: [],
    module: {
        rules: [
            {
                test: /\.css$/,
                use: ['style-loader', 'css-loader']
            },
            {
                test: /\.tsx?$/,
                use: ['ts-loader'],
                exclude: /node_modules$/,
            }
        ]
    },
    output: {
        filename: 'asset/[name]_bundle.js',
        path: BUID_DIR
    }
};