module.exports = {
    presets: [
        ['@babel/preset-env', {targets: {esmodules: false}}],
        ['@babel/preset-react', {runtime: 'automatic'}],
        '@babel/preset-typescript'
    ]
};
