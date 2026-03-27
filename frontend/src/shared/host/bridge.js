import { mockHandlers } from '@/shared/mock/data'

function getTsxRuntime() {
  return window._tsx || {}
}

function normalizeResponse(payload) {
  if (payload && typeof payload.code !== 'undefined') {
    return payload
  }
  return {
    code: 0,
    message: 'success',
    data: payload
  }
}

export function isHostMode() {
  return Boolean(window.__POWERED_BY_QIANKUN__)
}

export function initializeHostBridge() {
  if (!document.body.classList.contains('pvms-body')) {
    document.body.classList.add('pvms-body')
  }
}

export function buildRuntimeContext(props = {}) {
  const tsx = getTsxRuntime()
  const userInfo = tsx.userInfo || {}

  return {
    hostMode: isHostMode(),
    tenantName: props.tenantName || tsx.tenantName || '示范园区光伏集群',
    userName: props.userName || userInfo.name || '开发联调用户',
    themeName: props.themeName || tsx.themeName || 'light',
    hasHostRequest: typeof tsx.apirequest === 'function'
  }
}

export function syncDocumentTitle(title) {
  document.title = title
}

export async function request(url, options = {}) {
  const tsx = getTsxRuntime()
  const apirequest = tsx.apirequest

  if (typeof apirequest === 'function' && process.env.NODE_ENV === 'production') {
    const response = await apirequest({
      url,
      method: options.method || 'get',
      data: options.data,
      params: options.params
    })
    return normalizeResponse(response)
  }

  const handler = mockHandlers[url]
  if (!handler) {
    return normalizeResponse(null)
  }

  return Promise.resolve(normalizeResponse(handler(options)))
}
