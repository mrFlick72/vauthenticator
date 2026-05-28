const path = require('path');
const BUILD_DIR = path.resolve(__dirname, "../dist");
const HtmlWebpackPlugin = require("html-webpack-plugin");

module.exports = {
    entry: {
        callback: path.resolve(__dirname, './auth/Callback.tsx'),
        logout: path.resolve(__dirname, './auth/Logout.tsx'),
        "admin": path.resolve(__dirname, './admin/index.tsx')
    },
    resolve: {
        extensions: ['.tsx', '.ts', ".js", ".jsx"]
    },
    module: {
        rules: [
            {
                test: /\.css$/,
                use: ['style-loader', 'css-loader']
            },
            {
                test: [/\.ts?$/, /\.tsx?$/],
                use: ['ts-loader'],
                exclude: /node_modules$/,
            },
        ]
    },
    plugins: [
        new HtmlWebpackPlugin({
            title: 'VAuthenticator',
            filename: 'logout.html',
            chunks: ["logout"],
            template: "template.html"
        }),
        new HtmlWebpackPlugin({
            title: 'VAuthenticator',
            filename: 'callback.html',
            chunks: ["callback"],
            template: "template.html"
        }),
        new HtmlWebpackPlugin({
            title: 'VAuthenticator',
            filename: 'secure/admin/index.html',
            chunks: ["admin"],
            template: "template.html"
        }),
    ],
    output: {
        filename: '[name].[fullhash].js',
        path: BUILD_DIR
    }
};
