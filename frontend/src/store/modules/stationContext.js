/**
 * Station Context Module
 * ----------------------
 * Centralised filter context for the whole dashboard:
 *   company  → region → station → resourceUnit
 *
 * Pages read the focus from this store on mount so that a user's
 * "current station" selection persists across navigation, and write
 * back whenever they change focus (map click, top-bar select, list row).
 *
 * Secondary filters that are purely page-local (date range, metric type,
 * keyword search) stay inside the page component — see BaseFilterBar.
 */

const STORAGE_KEY = 'pvms:stationContext'

const defaultFocus = () => ({
  companyId: '',
  companyName: '',
  regionId: '',
  regionName: '',
  stationId: '',
  stationName: '',
  resourceUnitId: '',
  resourceUnitName: ''
})

function hydrate() {
  try {
    const raw = window.sessionStorage.getItem(STORAGE_KEY)
    if (!raw) return defaultFocus()
    return Object.assign(defaultFocus(), JSON.parse(raw))
  } catch (e) {
    return defaultFocus()
  }
}

function persist(state) {
  try {
    window.sessionStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        companyId: state.companyId,
        companyName: state.companyName,
        regionId: state.regionId,
        regionName: state.regionName,
        stationId: state.stationId,
        stationName: state.stationName,
        resourceUnitId: state.resourceUnitId,
        resourceUnitName: state.resourceUnitName
      })
    )
  } catch (e) {
    /* sessionStorage unavailable (SSR / private mode) — ignore */
  }
}

export default {
  namespaced: true,

  state: () => Object.assign(hydrate(), {
    // in-memory catalogues (not persisted)
    companies: [],
    regions: [],
    stations: [],
    resourceUnits: []
  }),

  getters: {
    focus: state => ({
      companyId: state.companyId,
      regionId: state.regionId,
      stationId: state.stationId,
      resourceUnitId: state.resourceUnitId
    }),
    breadcrumb: state => {
      const parts = []
      if (state.companyName) parts.push(state.companyName)
      if (state.regionName) parts.push(state.regionName)
      if (state.stationName) parts.push(state.stationName)
      if (state.resourceUnitName) parts.push(state.resourceUnitName)
      return parts
    },
    currentStation: state => state.stations.find(s => s.id === state.stationId) || null,
    hasFocus: state => Boolean(state.stationId || state.resourceUnitId)
  },

  mutations: {
    SET_COMPANIES(state, list) {
      state.companies = Array.isArray(list) ? list : []
    },
    SET_REGIONS(state, list) {
      state.regions = Array.isArray(list) ? list : []
    },
    SET_STATIONS(state, list) {
      state.stations = Array.isArray(list) ? list : []
    },
    SET_RESOURCE_UNITS(state, list) {
      state.resourceUnits = Array.isArray(list) ? list : []
    },
    SET_FOCUS(state, focus) {
      const next = Object.assign({}, defaultFocus(), focus || {})
      state.companyId = next.companyId
      state.companyName = next.companyName
      state.regionId = next.regionId
      state.regionName = next.regionName
      state.stationId = next.stationId
      state.stationName = next.stationName
      state.resourceUnitId = next.resourceUnitId
      state.resourceUnitName = next.resourceUnitName
      persist(state)
    },
    CLEAR_FOCUS(state) {
      Object.assign(state, defaultFocus())
      persist(state)
    }
  },

  actions: {
    focusStation({ commit, state }, payload) {
      // payload may be { id, name, regionId, regionName, ... } or just { stationId }
      const station = state.stations.find(s => s.id === (payload.stationId || payload.id))
      commit('SET_FOCUS', Object.assign({}, state, {
        stationId: payload.stationId || payload.id || '',
        stationName: payload.stationName || payload.name || (station && station.name) || '',
        regionId: payload.regionId || (station && station.regionId) || state.regionId,
        regionName: payload.regionName || (station && station.regionName) || state.regionName,
        companyId: payload.companyId || (station && station.companyId) || state.companyId,
        companyName: payload.companyName || (station && station.companyName) || state.companyName,
        resourceUnitId: payload.resourceUnitId || '',
        resourceUnitName: payload.resourceUnitName || ''
      }))
    },
    focusResourceUnit({ commit, state }, payload) {
      commit('SET_FOCUS', Object.assign({}, state, {
        resourceUnitId: payload.resourceUnitId || payload.id || '',
        resourceUnitName: payload.resourceUnitName || payload.name || ''
      }))
    },
    clearFocus({ commit }) {
      commit('CLEAR_FOCUS')
    },
    registerStations({ commit }, list) {
      commit('SET_STATIONS', list)
    },
    registerCompanies({ commit }, list) {
      commit('SET_COMPANIES', list)
    },
    registerRegions({ commit }, list) {
      commit('SET_REGIONS', list)
    },
    registerResourceUnits({ commit }, list) {
      commit('SET_RESOURCE_UNITS', list)
    }
  }
}
