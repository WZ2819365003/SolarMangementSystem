import AMapLoader from '@amap/amap-jsapi-loader'

const amapKey = process.env.VUE_APP_AMAP_KEY || ''
const amapSecurityCode = process.env.VUE_APP_AMAP_SECURITY_CODE || ''
const amapVersion = process.env.VUE_APP_AMAP_VERSION || '2.0'

export function applyAmapSecurityConfig() {
  if (!amapSecurityCode) {
    return
  }

  window._AMapSecurityConfig = {
    securityJsCode: amapSecurityCode
  }
}

export function createAmapLoader(options = {}) {
  applyAmapSecurityConfig()

  return AMapLoader.load({
    key: amapKey,
    version: amapVersion,
    plugins: options.plugins || [],
    AMapUI: options.AMapUI,
    Loca: options.Loca
  })
}

export function getAmapConfig() {
  return {
    key: amapKey,
    version: amapVersion,
    hasSecurityCode: Boolean(amapSecurityCode)
  }
}
