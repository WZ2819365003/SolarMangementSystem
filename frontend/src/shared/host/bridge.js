import axios from 'axios'
import { mockHandlers } from '@/shared/mock/data'

const REQUEST_MODE_BACKEND = 'backend'
const REQUEST_MODE_MOCK = 'mock'
const backendBaseUrl = process.env.VUE_APP_BACKEND_BASE_URL || '/api'
const requestMode = process.env.VUE_APP_REQUEST_MODE || REQUEST_MODE_BACKEND

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

function shouldUseHostRequest(apirequest) {
  return typeof apirequest === 'function'
}

function shouldUseMockRequest() {
  return requestMode === REQUEST_MODE_MOCK
}

async function requestViaBackend(url, options = {}) {
  const response = await axios({
    baseURL: backendBaseUrl,
    url,
    method: options.method || 'get',
    data: options.data,
    params: options.params
  })

  return normalizeResponse(response.data)
}

export async function request(url, options = {}) {
  const tsx = getTsxRuntime()
  const apirequest = tsx.apirequest

  if (shouldUseHostRequest(apirequest)) {
    const response = await apirequest({
      url,
      method: options.method || 'get',
      data: options.data,
      params: options.params
    })
    return normalizeResponse(response)
  }

  if (!shouldUseMockRequest()) {
    return requestViaBackend(url, options)
  }

  const handler = mockHandlers[url]
  if (!handler) {
    return normalizeResponse(null)
  }

  return Promise.resolve(normalizeResponse(handler(options)))
}
