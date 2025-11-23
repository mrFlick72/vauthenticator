const path = require('path');

const BUID_DIR = path.resolve(__dirname + "/dist");

module.exports = {
    mode: 'development',
    entry: {
        "default_error": path.resolve(__dirname, './app/errors/DefaultGenericErrorPage.tsx'),
        "404_error": path.resolve(__dirname, './app/errors/DefaultGenericErrorPage.tsx'),
        "400_error": path.resolve(__dirname, './app/errors/DefaultGenericErrorPage.tsx'),
        "500_error": path.resolve(__dirname, './app/errors/DefaultGenericErrorPage.tsx'),
        "admin": path.resolve(__dirname, './app/admin/index.tsx')
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
    output: {
        filename: 'asset/[name]_bundle.js',
        publicPath: "/",
        path: BUID_DIR
    }
};