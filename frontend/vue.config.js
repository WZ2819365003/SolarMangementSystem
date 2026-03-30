const path = require('path')
const packageJson = require('./package.json')
const backendProxyTarget =
  process.env.VUE_APP_BACKEND_PROXY_TARGET || 'http://127.0.0.1:8091'

module.exports = {
  publicPath: process.env.NODE_ENV === 'production' ? '/pvms/' : '/',
  outputDir: 'dist',
  assetsDir: 'static',
  lintOnSave: true,
  devServer: {
    port: 6618,
    headers: {
      'Access-Control-Allow-Origin': '*'
    },
    historyApiFallback: true,
    proxy: {
      '/api': {
        target: backendProxyTarget,
        changeOrigin: true
      }
    }
  },
  configureWebpack: {
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src')
      }
    },
    output: {
      library: `${packageJson.name}-[name]`,
      libraryTarget: 'umd',
      jsonpFunction: `webpackJsonp_${packageJson.name.replace(/-/g, '_')}`
    }
  }
}
