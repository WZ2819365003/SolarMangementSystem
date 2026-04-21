import Vue from 'vue'
import Vuex from 'vuex'
import appSettings from '@/settings'
import { buildRuntimeContext, syncDocumentTitle } from '@/shared/host/bridge'
import stationContext from './modules/stationContext'

Vue.use(Vuex)

export default function createStore() {
  return new Vuex.Store({
    modules: {
      stationContext
    },
    state: {
      appName: appSettings.appName,
      appVersion: appSettings.version,
      appDescription: appSettings.description,
      menus: appSettings.menus,
      runtime: buildRuntimeContext()
    },
    getters: {
      menus: state => state.menus,
      runtime: state => state.runtime
    },
    mutations: {
      setRuntime(state, runtime) {
        state.runtime = runtime
      }
    },
    actions: {
      initApp({ commit, state }, props) {
        const runtime = buildRuntimeContext(props)
        commit('setRuntime', runtime)
        syncDocumentTitle(state.appName)
      }
    }
  })
}
