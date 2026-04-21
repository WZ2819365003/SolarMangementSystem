import AMapLoader from '@amap/amap-jsapi-loader'

const amapKey = '735d57cb68fc13197c26bc4068bbf558'
const amapSecurityCode = 'ee65df02b76c0f4d4a14924b661db3df'
const amapVersion = '2.0'

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
