import './public-path'
import Vue from 'vue'
import VueRouter from 'vue-router'
import Vuex from 'vuex'
import App from './App.vue'
import createRouter from './router'
import createStore from './store'
import { initializeHostBridge } from './shared/host/bridge'
import { applyAmapSecurityConfig } from './shared/plugins/amap'
import './styles/index.less'
import './shared/plugins/echarts'
import './shared/plugins/element-ui'

Vue.use(VueRouter)
Vue.use(Vuex)
Vue.config.productionTip = false

applyAmapSecurityConfig()

let router = null
let store = null
let vueInst = null

function render(props = {}) {
  const { container, baseUrl = '/' } = props

  initializeHostBridge(props)
  router = createRouter(window.__POWERED_BY_QIANKUN__ ? baseUrl : '/')
  store = createStore()
  store.dispatch('initApp', props)

  vueInst = new Vue({
    router,
    store,
    mounted() {
      if (
        process.env.NODE_ENV === 'development' &&
        window.__POWERED_BY_QIANKUN__
      ) {
        window.__QIANKUN__PVMS_MICRO_APP_VM__ = this
      }
    },
    render: h => h(App)
  }).$mount(container ? container.querySelector('#app') : '#app')
}

if (!window.__POWERED_BY_QIANKUN__) {
  render()
}

export async function bootstrap() {}

export async function mount(props) {
  render(props)
}

export async function update() {}

export async function unmount() {
  if (vueInst) {
    vueInst.$destroy()
    vueInst.$el.innerHTML = ''
  }
  vueInst = null
  router = null
  store = null
}
