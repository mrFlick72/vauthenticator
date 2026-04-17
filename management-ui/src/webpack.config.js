const path = require('path');
const BUID_DIR = path.resolve("../dist");
const Dotenv = require('dotenv-webpack');
const HtmlWebpackPlugin = require("html-webpack-plugin");

module.exports = {
    mode: 'development',
    entry: {
        callback: path.resolve(__dirname, './auth/Callback.tsx'),
        logout: path.resolve(__dirname, './auth/Logout.tsx'),
        "admin": path.resolve(__dirname, './admin/index.tsx')
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
                test: [/\.ts?$/, /\.tsx?$/],
                use: ['ts-loader'],
                exclude: /node_modules$/,
            },
            {
                test: /\.js?$/,
                exclude: path.resolve(__dirname, "node_modules"),
                use: {
                    loader: "babel-loader",
                    options: {
                        presets: ['@babel/env', '@babel/react']
                    }
                }

            }
        ]
    },
    plugins: [
        new Dotenv({
            path: `../environments/.env.${process.env.ENV}`
        }),

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
        path: BUID_DIR
    }
};